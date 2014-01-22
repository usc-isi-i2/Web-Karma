package edu.isi.karma.kr2rml;

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.*;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Class KR2RMLWorksheetRDFGenerator
 *
 * @since 01/22/2014
 */
public class KR2RMLWorksheetRDFGenerator {

	private RepFactory factory;
	private Worksheet worksheet;
	private String outputFileName;
	private OntologyManager ontMgr;
	private ErrorReport errorReport;
	private boolean addColumnContextInformation;
	private KR2RMLMapping kr2rmlMapping;
	private Map<String, String> prefixToNamespaceMap;
	private Map<String, String> hNodeToContextUriMap;
	private Map<String, String> columnNameToHNodeId;
	private Map<String, String> hNodeIdToColumnName;
	private PrintWriter outWriter;

	private Logger logger = LoggerFactory.getLogger(KR2RMLWorksheetRDFGenerator.class);
	public static String BLANK_NODE_PREFIX = "_:";

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, RepFactory factory,
			OntologyManager ontMgr, String outputFileName, boolean addColumnContextInformation,
			KR2RMLMapping kr2rmlMapping, ErrorReport errorReport) {
		super();
		this.ontMgr = ontMgr;
		this.kr2rmlMapping = kr2rmlMapping;
		this.factory = factory;
		this.worksheet = worksheet;
		this.outputFileName = outputFileName;
		this.errorReport = errorReport;
		this.prefixToNamespaceMap = new HashMap<String, String>();
		this.hNodeToContextUriMap = new HashMap<String, String>();
		this.addColumnContextInformation = addColumnContextInformation;
		this.columnNameToHNodeId = new HashMap<String,String>();
		this.hNodeIdToColumnName = new HashMap<String,String>();
		populatePrefixToNamespaceMap();
		populateHNodeIdAndColumnNameMaps();
	}

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, RepFactory factory,
			OntologyManager ontMgr, PrintWriter writer, KR2RMLMapping kr2rmlMapping,
			ErrorReport errorReport, boolean addColumnContextInformation) {
		super();
		this.ontMgr = ontMgr;
		this.kr2rmlMapping = kr2rmlMapping;
		this.factory = factory;
		this.worksheet = worksheet;
		this.outWriter = writer;;
		this.errorReport = errorReport;
		this.prefixToNamespaceMap = new HashMap<String, String>();
		this.hNodeToContextUriMap = new HashMap<String, String>();
		this.addColumnContextInformation = addColumnContextInformation;
		this.columnNameToHNodeId = new HashMap<String,String>();
		this.hNodeIdToColumnName = new HashMap<String,String>();
		populatePrefixToNamespaceMap();
		populateHNodeIdAndColumnNameMaps();

	}



	public void generateRDF(boolean closeWriterAfterGeneration) throws IOException
	{
		// Prepare the output writer
		BufferedWriter bw = null;
		try {
			if(this.outWriter == null && this.outputFileName != null){
				File f = new File(this.outputFileName);
				File parentDir = f.getParentFile();
				parentDir.mkdirs();
				bw = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
				outWriter = new PrintWriter (bw);
			} else if (this.outWriter == null && this.outputFileName == null) {
				outWriter = new PrintWriter (System.out);
			}

			// RDF Generation starts at the top level rows
			ArrayList<Row> rows = this.worksheet.getDataTable().getRows(0,
					this.worksheet.getDataTable().getNumRows());
			int i=1;
			for (Row row:rows) {
				Set<String> rowTriplesSet = new HashSet<String>();
				Set<String> rowPredicatesCovered = new HashSet<String>();
				Set<String> predicatesSuccessful = new HashSet<String>();
				Map<String, ReportMessage> predicatesFailed = new HashMap<String,ReportMessage>();
				generateTriplesForRow(row, rowTriplesSet, rowPredicatesCovered, predicatesFailed, predicatesSuccessful);

				outWriter.println();
				if (i++%2000 == 0)
					logger.info("Done processing " + i + " rows");
				for (ReportMessage errMsg:predicatesFailed.values()){
					this.errorReport.addReportMessage(errMsg);
				}
			}

			// Generate column provenance information if required
			if (addColumnContextInformation) {
				generateColumnProvenanceInformation();
			}

		} catch (Exception e)
		{
			logger.error("Unable to generate RDF: ", e);
		}
		finally {
			if (closeWriterAfterGeneration) {
				if(outWriter != null) {
				outWriter.flush();
				outWriter.close();
				}

				if(bw != null)
					bw.close();
			}
		}
	}

	public void generateTriplesForRow(Row row, Set<String> existingTopRowTriples,
			Set<String> predicatesCovered, Map<String, ReportMessage> predicatesFailed,
			Set<String> predicatesSuccessful) {
		Map<String, Node> rowNodes = row.getNodesMap();
		for (String hNodeId:rowNodes.keySet()) {
			Node rowNode = rowNodes.get(hNodeId);
			if (rowNode.hasNestedTable()) {
				Table rowNodeTable = rowNode.getNestedTable();
				if (rowNodeTable != null) {
					for (Row nestedTableRow:rowNodeTable.getRows(0, rowNodeTable.getNumRows())) {
						Set<String> rowPredicatesCovered = new HashSet<String>();
						generateTriplesForRow(nestedTableRow, existingTopRowTriples,
								rowPredicatesCovered, predicatesFailed, predicatesSuccessful);
					}
				}
			} else {
				generateTriplesForCell(rowNode, existingTopRowTriples, hNodeId,
						predicatesCovered, predicatesFailed, predicatesSuccessful);
			}
		}
	}

	public void generateTriplesForCell(Node node, Set<String> existingTopRowTriples,
			String hNodeId, Set<String> predicatesCovered,
			Map<String, ReportMessage> predicatesFailed, Set<String> predicatesSuccessful) {
		List<PredicateObjectMap> pomList = this.kr2rmlMapping.getAuxInfo().getColumnNameToPredObjLinks().get(hNodeIdToColumnName.get(hNodeId));
		if (pomList == null || pomList.isEmpty())
			return;

		List<TriplesMap> toBeProcessedTriplesMap = new LinkedList<TriplesMap>();
		for (PredicateObjectMap pom:pomList) {
			toBeProcessedTriplesMap.add(pom.getTriplesMap());
		}

		Set<String> alreadyProcessedTriplesMapIds = new HashSet<String>();
		while (!toBeProcessedTriplesMap.isEmpty()) {
			TriplesMap trMap = toBeProcessedTriplesMap.remove(0);
			boolean dontAddNeighboringMaps = false;

			// Generate properties for the triple maps
			for (PredicateObjectMap pom:trMap.getPredicateObjectMaps()) {
				if (!predicatesCovered.contains(pom.getPredicate().getId())) {
					generatePropertyForPredObjMap(pom, predicatesCovered,
							existingTopRowTriples, node, predicatesFailed, predicatesSuccessful);
				}
			}

			// Need to stop at the root
			if (trMap.getSubject().isSteinerTreeRootNode()) {
				dontAddNeighboringMaps = true;
			}

			List<TriplesMapLink> neighboringLinks = this.kr2rmlMapping.getAuxInfo().getTriplesMapGraph()
					.getAllNeighboringTriplesMap(trMap.getId());

			for (TriplesMapLink trMapLink:neighboringLinks) {
				if (predicatesCovered.contains(trMapLink.getPredicateObjectMapLink().getPredicate().getId()))
					continue;

				// Add the other triplesMap in queue to be processed later
				if (!alreadyProcessedTriplesMapIds.contains(trMapLink.getSourceMap().getId())
						&& !dontAddNeighboringMaps) {
					toBeProcessedTriplesMap.add(trMapLink.getSourceMap());
				}

				if (!alreadyProcessedTriplesMapIds.contains(trMapLink.getTargetMap().getId())
						&& !dontAddNeighboringMaps) {
					toBeProcessedTriplesMap.add(trMapLink.getTargetMap());
				}
			}
			alreadyProcessedTriplesMapIds.add(trMap.getId());
		}
	}


	private void generatePropertyForPredObjMap(PredicateObjectMap pom, Set<String> predicatesCovered,
			Set<String> existingTopRowTriples, Node node,
			Map<String, ReportMessage> predicatesFailed, Set<String> predicatesSuccessful) {
		SubjectMap subjMap = pom.getTriplesMap().getSubject();

		// Generate subject RDF
		String subjUri = "";
		try {
			subjUri = generateSubjectMapRDF(subjMap, existingTopRowTriples, node);
		} catch (ValueNotFoundKarmaException ve) {
			ReportMessage msg = createReportMessage("Could not generate subject's RDF and URI for <i>predicate:" +
					 pom.getPredicate().getTemplate().toString().replaceAll("<", "{").replaceAll(">", "}") +
					 ", subject node: " + subjMap.getId()+"</i>", ve,
					 this.factory.getHNode(node.getHNodeId()).getColumnName());
			if (!predicatesSuccessful.contains(pom.getPredicate().getId()))
				predicatesFailed.put(pom.getPredicate().getId(), msg);
			return;
		} catch (NoValueFoundInNodeException e) {
			logger.debug("No value found in a node required to generate subject's RDF or URI.");
			return;
		} catch (HNodeNotFoundKarmaException e) {
			logger.debug("No hnode found for a node required to generate subject's RDF or URI.");
			return;
		}

		// Generate the predicate RDF
		String predicateUri = "";
		try {
			predicateUri = normalizeUri(getTemplateTermSetPopulatedWithValues(node,
					pom.getPredicate().getTemplate()));
			if (predicateUri.equals(Uris.CLASS_INSTANCE_LINK_URI)
					|| predicateUri.equals(Uris.COLUMN_SUBCLASS_LINK_URI)) {
				return;
			}

		} catch (ValueNotFoundKarmaException ve) {
			ReportMessage msg = createReportMessage("Could not generate predicate's URI for <i>predicate:" +
					pom.getPredicate().getTemplate().toString().replaceAll("<", "{").replaceAll(">", "}") +
					", subject node: " + subjMap.getId() + "</i>",  ve,
					this.factory.getHNode(node.getHNodeId()).getColumnName());
			if (!predicatesSuccessful.contains(pom.getPredicate().getId()))
				predicatesFailed.put(pom.getPredicate().getId(), msg);
			return;
		} catch (NoValueFoundInNodeException e) {
			logger.debug("No value found in a node required to generate predicate's URI.");
			return;
		} catch (HNodeNotFoundKarmaException e) {
			logger.debug("No hnode found fir a node required to generate predicate's URI.");
			return;
		}


		// Object property
		if (pom.getObject().hasRefObjectMap()) {
			// Generate the object URI
			TriplesMap objPropertyObjectTriplesMap = pom.getObject().getRefObjectMap().
					getParentTriplesMap();
			String objUri = "";
			try {
				objUri = generateSubjectMapRDF(objPropertyObjectTriplesMap.getSubject(),
						existingTopRowTriples, node);
			} catch (ValueNotFoundKarmaException ve) {
				ReportMessage msg = createReportMessage("Could not generate object's URI for <i>predicate:" +
						pom.getPredicate().getTemplate().toString()
						.replaceAll("<", "{").replaceAll(">", "}") +
						", subject node: " + pom.getTriplesMap().getSubject().getId()+"</i>", ve
						, this.factory.getHNode(node.getHNodeId()).getColumnName());
				if (!predicatesSuccessful.contains(pom.getPredicate().getId()))
					predicatesFailed.put(pom.getPredicate().getId(), msg);
				return;
			} catch (NoValueFoundInNodeException e) {
				logger.debug("No value found in a node required to generate object's URI for a predicate.");
				return;
			} catch (HNodeNotFoundKarmaException e) {
				logger.debug("No hnode found for a node required to generate value for a predicate.");
				return;
			}

			String triple = constructTripleWithURIObject(subjUri, predicateUri, objUri);
			if (!existingTopRowTriples.contains(triple)) {
				outWriter.println(triple);
				existingTopRowTriples.add(triple);
			}
		}
		// Data Property
		else {
			// Get the value
			String value = "";
			String rdfLiteralType = "";
			try {
				value = getTemplateTermSetPopulatedWithValues(node, pom.getObject().getTemplate());
				if (value == null || value.trim().equals(""))
					return;
				TemplateTermSet rdfLiteralTypeTermSet = pom.getObject().getRdfLiteralType();
				if (rdfLiteralTypeTermSet != null) {
					rdfLiteralType = rdfLiteralTypeTermSet.getR2rmlTemplateString(factory);
				}
			} catch (ValueNotFoundKarmaException ve) {
				ReportMessage msg = createReportMessage("Could not retrieve value for the <i>predicate:" +
						pom.getPredicate().getTemplate().toString().replaceAll("<", "{").replaceAll(">", "}") +
						", subject node: " + subjMap.getId()+"</i>", ve,
						this.factory.getHNode(node.getHNodeId()).getColumnName());
				if (!predicatesSuccessful.contains(pom.getPredicate().getId()))
					predicatesFailed.put(pom.getPredicate().getId(), msg);
				return;
			} catch (NoValueFoundInNodeException e) {
				logger.debug("No value found in a node required to generate value for a predicate.");
				return;
			} catch (HNodeNotFoundKarmaException e) {
				logger.debug("No hnode found for a node required to generate value for a predicate.");
				return;
			}
			if (addColumnContextInformation) {
				TemplateTermSet templ = pom.getObject().getTemplate();
				if (templ.isSingleColumnTerm()) {
					try
					{
						String hNodeId_val = getHNodeIdForColumnName(templ.getAllTerms().get(0).getTemplateTermValue());
						String quad = constructQuadWithLiteralObject(subjUri, predicateUri, value,
								rdfLiteralType, hNodeId_val);
						if (!existingTopRowTriples.contains(quad)) {
							existingTopRowTriples.add(quad);
							outWriter.println(quad);
						}
					}
					catch(HNodeNotFoundKarmaException he)
					{
						logger.error("No hnode id found to generate quad for");
						return;
					}
				}
			} else {
				String triple = constructTripleWithLiteralObject(subjUri, predicateUri, value, rdfLiteralType);
				if (!existingTopRowTriples.contains(triple)) {
					existingTopRowTriples.add(triple);
					outWriter.println(triple);
				}
			}
		}
		predicatesCovered.add(pom.getPredicate().getId());
		predicatesSuccessful.add(pom.getPredicate().getId());
		if (predicatesFailed.containsKey(pom.getPredicate().getId()))
			predicatesFailed.remove(pom.getPredicate().getId());
	}

	private void populateHNodeIdAndColumnNameMaps()
	{
		for(String columnName : kr2rmlMapping.getAuxInfo().getColumnNameToPredObjLinks().keySet())
		{
			try
			{
				getHNodeIdForColumnName(columnName);
			}
			catch (HNodeNotFoundKarmaException ex)
			{
				logger.debug("Unable to find HNodeId for column name: " + columnName, ex);
			}
		}
	}
	private String getHNodeIdForColumnName(String templateTermValue) throws HNodeNotFoundKarmaException {
		if(!this.columnNameToHNodeId.containsKey(templateTermValue))
		{
			try {
				String hNodeId = translateColumnNameToHNodeId(templateTermValue);
				columnNameToHNodeId.put(templateTermValue, hNodeId);
				hNodeIdToColumnName.put(hNodeId, templateTermValue);

			} catch (JSONException e) {
				throw new HNodeNotFoundKarmaException("Unable to find HNodeId for column name", templateTermValue);
			}
		}
		return this.columnNameToHNodeId.get(templateTermValue);
	}

	private String translateColumnNameToHNodeId(String colTermVal) throws JSONException
	{
		HTable hTable = worksheet.getHeaders();
    	// If hierarchical columns
    	if (colTermVal.startsWith("[") && colTermVal.endsWith("]") && colTermVal.contains(",")) {
    		JSONArray strArr = new JSONArray(colTermVal);
    		for (int i=0; i<strArr.length(); i++) {
				String cName = (String) strArr.get(i);

				logger.debug("Column being normalized: "+ cName);
				HNode hNode = hTable.getHNodeFromColumnName(cName);
				if(hNode == null || hTable == null) {
					logger.error("Error retrieving column: " + cName);
					return null;
				}

				if (i == strArr.length()-1) {		// Found!
					return hNode.getId();
				} else {
					hTable = hNode.getNestedTable();
				}
    		}
    	} else {
    		HNode hNode = hTable.getHNodeFromColumnName(colTermVal);
    		logger.debug("Column" +colTermVal);
    		return hNode.getId();
    	}
    	return null;
	}

	private ReportMessage createReportMessage(String title, ValueNotFoundKarmaException ve,
			String cellColumnName) {
		ReportMessage msg = new ReportMessage(title, ve.getMessage()
				+ " from column: <i>" +  cellColumnName + "</i>", ErrorReport.Priority.high);
		return msg;
	}

	private String generateSubjectMapRDF(SubjectMap subjMap, Set<String> existingTopRowTriples, Node node) throws ValueNotFoundKarmaException, NoValueFoundInNodeException, HNodeNotFoundKarmaException {
		// Generate URI for subject
		String uri = "";
		if (subjMap.isBlankNode()) {
			uri = getExpandedAndNormalizedUri(getBlankNodeUri(subjMap.getId(), node));
		} else {
			uri = getExpandedAndNormalizedUri(getTemplateTermSetPopulatedWithValues(node,
					subjMap.getTemplate()));
		}

		// Generate triples for specifying the types
		for (TemplateTermSet typeTerm:subjMap.getRdfsType()) {
			String typeUri = getExpandedAndNormalizedUri(getTemplateTermSetPopulatedWithValues(
					node, typeTerm));
			String triple = constructTripleWithURIObject(uri, Uris.RDF_TYPE_URI, typeUri);
			if (!existingTopRowTriples.contains(triple)) {
				existingTopRowTriples.add(triple);
				outWriter.println(triple);
			}
		}
		return uri;
	}

	private String constructTripleWithURIObject(String subjUri, String predicateUri, String objectUri) {
		return subjUri + " "
				+ getExpandedAndNormalizedUri(predicateUri) + " "
				+ objectUri + " .";
	}

	private String constructTripleWithLiteralObject(String subjUri, String predicateUri, String value,
			String literalType) {
		// Use Apache Commons to escape the value
		value = StringEscapeUtils.escapeJava(value);

		// Add the RDF literal type to the literal if present
		if (literalType != null && !literalType.equals("")) {
			return subjUri + " " + getExpandedAndNormalizedUri(predicateUri) + " \"" + value +
					"\"" + "^^<" + literalType + "> .";
		}
		return subjUri + " " + getExpandedAndNormalizedUri(predicateUri) + " \"" + value + "\" .";
	}

	private String constructQuadWithLiteralObject(String subjUri, String predicateUri,
			String value, String literalType, String valueHNodeId) {
		String triple = constructTripleWithLiteralObject(subjUri, predicateUri, value, literalType);
		String columnContextUri = getColumnContextUri(valueHNodeId);
		if (triple.length() > 2)
			return triple.substring(0, triple.length()-1) + "<" + columnContextUri + "> ." ;
		else
			return "";
	}



	private String getBlankNodeUri(String subjMapid, Node node)
			throws ValueNotFoundKarmaException, HNodeNotFoundKarmaException {

		StringBuilder output = new StringBuilder();
		// Add the blank namespace
		output.append(BLANK_NODE_PREFIX);

		// Add the class node prefix
		output.append(this.kr2rmlMapping.getAuxInfo().getBlankNodesUriPrefixMap().get(subjMapid).replaceAll(":", "_"));

		// Add the node ids for tha columns covered
		List<String> columnsCovered = this.kr2rmlMapping.getAuxInfo().getBlankNodesColumnCoverage().get(subjMapid);

		if (columnsCovered != null && !columnsCovered.isEmpty()) {
			for (int i=0; i<columnsCovered.size(); i++) {
				String hNodeId = this.getHNodeIdForColumnName(columnsCovered.get(i));
				if (node.canReachNeighbor(hNodeId)) {
					output.append("_" + node.getNeighbor(hNodeId).getId());
				} else {
					String columnName = this.factory.getHNode(hNodeId).getColumnName();
					throw new ValueNotFoundKarmaException("Could not retrieve value while constructing " +
							"blank URI of column:" + columnName + ". ", hNodeId);
				}
			}
		}
		return output.toString();
	}

	public String getTemplateTermSetPopulatedWithValues(Node node,
			TemplateTermSet termSet) throws ValueNotFoundKarmaException, NoValueFoundInNodeException, HNodeNotFoundKarmaException {
		StringBuilder output = new StringBuilder();
		for (TemplateTerm term:termSet.getAllTerms()) {
			// String template term
			if (term instanceof StringTemplateTerm) {
				output.append(term.getTemplateTermValue());
			}
			// Column template term
			else if (term instanceof ColumnTemplateTerm) {
				String hNodeId = this.getHNodeIdForColumnName(term.getTemplateTermValue());
				if (node.canReachNeighbor(hNodeId)) {
					Node neighborNode = node.getNeighbor(hNodeId);
					if (neighborNode != null) {
						if (neighborNode.getValue().asString() == null
								|| neighborNode.getValue().asString().equals("")) {
							throw new NoValueFoundInNodeException();
						}
						output.append(neighborNode.getValue().asString());
					}
				} else {
					String columnName = this.factory.getHNode(hNodeId).getColumnName();
					throw new ValueNotFoundKarmaException("Could not retrieve value of column: " +
							columnName + ".", hNodeId);
				}
			}
		}
		return output.toString();
	}

	private String getExpandedAndNormalizedUri(String uri) {
		// Check if the predicate contains a predicate.
		if (!uri.startsWith("http:") && uri.contains(":")) {
			// Replace the prefix with proper namespace by looking into the ontology manager
			String prefix = uri.substring(0, uri.indexOf(":"));

			String namespace = this.prefixToNamespaceMap.get(prefix);
			if (namespace == null || namespace.isEmpty()) {
				this.errorReport.createReportMessage("Error creating predicate's URI: " + uri,
						"No namespace found for the prefix: " + prefix, ErrorReport.Priority.high);
//				logger.error("No namespace found for the predicate prefix: " + prefix);
			} else {
				uri = namespace + uri.substring(uri.indexOf(":")+1);
			}
		}

		// Remove all unwanted characters
		uri = normalizeUri(uri);

		// Put angled brackets if required
		if (!uri.startsWith(BLANK_NODE_PREFIX)) {
			uri = "<" + uri + ">";
		}

		return uri;
	}

	public String normalizeUri(String inputUri) {

		boolean foundIssue = false;
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < inputUri.length(); i++)
		{
			char value = inputUri.charAt(i);
			if(value == ' ')
			{
				if(!foundIssue)
				{
					foundIssue = true;
					sb.append(inputUri.substring(0, i));
				}

				continue;
			}
			else if(value == ',' || value == '`' || value == '\'' )
			{
				if(!foundIssue)
				{
					foundIssue = true;
					sb.append(inputUri.substring(0, i));
				}
				else
				{
					sb.append('_');
				}
			}
			else
			{
				if(foundIssue)
				{
					sb.append(value);
				}
			}
		}
		if(foundIssue)
		{
			return sb.toString();
		}
		else
		{
			return inputUri;
		}
	}



	private void populatePrefixToNamespaceMap() {
		Map<String, String> prefixMapOntMgr = this.ontMgr.getPrefixMap();
		for (String ns:prefixMapOntMgr.keySet()) {
			String prefix = prefixMapOntMgr.get(ns);
			this.prefixToNamespaceMap.put(prefix, ns);
		}
	}

	private String getColumnContextUri (String hNodeId) {
		if (hNodeToContextUriMap.containsKey(hNodeId))
			return hNodeToContextUriMap.get(hNodeId);
		else {
			String randomId = RandomStringUtils.randomAlphanumeric(10);
			String uri = Namespaces.KARMA_DEV + randomId + "_" + hNodeId;
			hNodeToContextUriMap.put(hNodeId, uri);
			return uri;
		}
	}

	private void generateColumnProvenanceInformation() {
		for (String hNodeId:hNodeToContextUriMap.keySet()) {
			List<String> columnTriples = getColumnContextTriples(hNodeId);
			for (String triple:columnTriples) {
				outWriter.println(triple);
			}

			// Generate wasDerivedFrom property if required
			HNode hNode = factory.getHNode(hNodeId);
			if (hNode.isDerivedFromAnotherColumn()) {
				HNode originalHNode = factory.getHNode(hNode.getOriginalColumnHNodeId());
				if (originalHNode != null) {
					columnTriples = getColumnContextTriples(originalHNode.getId());
					for (String triple:columnTriples) {
						outWriter.println(triple);
					}

					String derivedFromTriple = constructTripleWithURIObject(
							hNodeToContextUriMap.get(hNodeId), Uris.PROV_WAS_DERIVED_FROM_URI,
							getColumnContextUri(originalHNode.getId()));
					outWriter.println(derivedFromTriple);
				}
			}
		}
	}

	private List<String> getColumnContextTriples(String hNodeId) {
		List<String> colContextTriples = new ArrayList<String>();
		String colUri = getColumnContextUri(hNodeId);

		// Generate the type
		String typeTriple = constructTripleWithURIObject(colUri, Uris.RDF_TYPE_URI,
				Uris.PROV_ENTITY_URI);
		colContextTriples.add(typeTriple);

		// Generate the label
		HNode hNode = factory.getHNode(hNodeId);
		String labelTriple = constructTripleWithLiteralObject(colUri, Uris.RDFS_LABEL_URI,
				hNode.getColumnName(), "");
		colContextTriples.add(labelTriple);

		return colContextTriples;
	}
}
