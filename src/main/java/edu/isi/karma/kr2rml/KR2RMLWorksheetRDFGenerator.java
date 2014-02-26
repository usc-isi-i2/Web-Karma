/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.kr2rml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.ErrorReport.Priority;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;

public class KR2RMLWorksheetRDFGenerator {
	
	protected RepFactory factory;
	protected Worksheet worksheet;
	protected String outputFileName;
	protected OntologyManager ontMgr;
	protected ErrorReport errorReport;
	protected boolean addColumnContextInformation;
	protected KR2RMLMapping kr2rmlMapping;
	protected KR2RMLMappingColumnNameHNodeTranslator translator;
	protected Map<String, String> prefixToNamespaceMap;
	protected Map<String, String> hNodeToContextUriMap;
	protected PrintWriter outWriter;
	
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
		this.translator = new KR2RMLMappingColumnNameHNodeTranslator(factory, worksheet);
		populatePrefixToNamespaceMap();
		
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
		this.translator = new KR2RMLMappingColumnNameHNodeTranslator(factory, worksheet);
		populatePrefixToNamespaceMap();
	
	}
	
	
	
	public void generateRDF(boolean closeWriterAfterGeneration) throws IOException {
		
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
			

			
			try{
				//TODO move this out!
				DFSTriplesMapGraphTreeifier treeifier = new DFSTriplesMapGraphTreeifier();
				treeifier.treeify(kr2rmlMapping.getAuxInfo().getTriplesMapGraph(), new SteinerTreeRootStrategy(new WorksheetDepthTreeRootStrategy()));
				
			}catch (Exception e)
			{
				logger.error("unable to treeify!");
			}
			int i=1;
			
			for (Row row:rows) {
				TriplesMapPlanExecutor e = new TriplesMapPlanExecutor();
				TriplesMapPlanGenerator g = new TriplesMapPlanGenerator(this, row);
				TriplesMapPlan plan = g.generatePlan(kr2rmlMapping.getAuxInfo().getTriplesMapGraph());
				e.execute(plan);
				outWriter.println();
			
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
				outWriter.flush();
				outWriter.close();
				if(bw != null)
					bw.close();
			}
		}
		// An attempt to prevent an occasional error that occurs on Windows platform
		// The requested operation cannot be performed on a file with a user-mapped section open
		System.gc();
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
		
		String columnName = translator.getColumnNameForHNodeId(hNodeId);
		List<PredicateObjectMap> pomList = this.kr2rmlMapping.getAuxInfo().getColumnNameToPredObjLinks().get(columnName);
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
						String hNodeId_val = translator.getHNodeIdForColumnName(templ.getAllTerms().get(0).getTemplateTermValue());
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

	private ReportMessage createReportMessage(String title, ValueNotFoundKarmaException ve, 
			String cellColumnName) {
		ReportMessage msg = new ReportMessage(title, ve.getMessage() 
				+ " from column: <i>" +  cellColumnName + "</i>", Priority.high);
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
				String hNodeId = translator.getHNodeIdForColumnName(columnsCovered.get(i));
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
				String hNodeId = translator.getHNodeIdForColumnName(term.getTemplateTermValue());
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
						"No namespace found for the prefix: " + prefix, Priority.high);
//				logger.error("No namespace found for the predicate prefix: " + prefix);
			} else {
				uri = namespace + uri.substring(uri.indexOf(":")+1);
			}
		}
		
		// Remove all unwanted characters
		uri = normalizeUri(uri);
		
		// Put angled brackets if required
		if (!uri.startsWith(BLANK_NODE_PREFIX) && !uri.startsWith("<") && !uri.endsWith(">")) {
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
		String typeTriple = constructTripleWithURIObject("<" + colUri + ">", Uris.RDF_TYPE_URI, 
				"<" + Uris.PROV_ENTITY_URI + ">");
		colContextTriples.add(typeTriple);
		
		// Generate the label
		HNode hNode = factory.getHNode(hNodeId);
		String labelTriple = constructTripleWithLiteralObject("<" + colUri + ">", Uris.RDFS_LABEL_URI, 
				hNode.getColumnName(), "");
		colContextTriples.add(labelTriple);
		
		return colContextTriples;
	}
	

	public class TriplesMapWorker implements Callable<Boolean> {
	
		private Logger LOG = LoggerFactory.getLogger(TriplesMapWorker.class);
		protected List<CountDownLatch> dependentTriplesMapLatches;
		protected CountDownLatch latch;
		protected TriplesMap triplesMap;
		protected Row r;
		protected Map<String, List<Subject>> triplesMapSubjects;
		public TriplesMapWorker(TriplesMap triplesMap, CountDownLatch latch, Row r, Map<String, List<Subject>> triplesMapSubjects)
		{
			this.latch = latch;
			this.triplesMap = triplesMap;
			this.dependentTriplesMapLatches = new LinkedList<CountDownLatch>();
			this.r = r;
			this.triplesMapSubjects = triplesMapSubjects;
		}
		public void addDependentTriplesMapLatch(CountDownLatch latch)
		{
			dependentTriplesMapLatches.add(latch);
		}
		
		public void notifyDependentTriplesMapWorkers()
		{
			for(CountDownLatch latch : dependentTriplesMapLatches)
			{
				latch.countDown();
			}
		}
		@Override
		public Boolean call() throws HNodeNotFoundKarmaException, ValueNotFoundKarmaException, NoValueFoundInNodeException {
			
			List<Subject> subjects = new LinkedList<Subject>();
			triplesMapSubjects.put(triplesMap.getId(), subjects);
			Map<Node, Set<Subject>> stuffs = new HashMap<Node, Set<Subject>>();
			try{
				latch.await();
			}
			catch (Exception e )
			{
				LOG.error("Error while waiting for dependent triple maps to process", e);
				notifyDependentTriplesMapWorkers();
				return false;
			}
			
			String templateAnchor = kr2rmlMapping.getAuxInfo().getSubjectMapIdToTemplateAnchor().get(triplesMap.getSubject().getId());
			String templateHNodeId = translator.getHNodeIdForColumnName(templateAnchor);
			HNode templateHNode = factory.getHNode(templateHNodeId);
			
			
			Collection<Node> nodes = new LinkedList<Node>();
			HNodePath templateHNodePath = templateHNode.getHNodePath(factory);
			r.collectNodes(templateHNodePath, nodes);
			List<String> columnsCovered;
			List<HNodePath> columnsCoveredPaths = new LinkedList<HNodePath>();
			SubjectMap subjMap = triplesMap.getSubject();
			HNodePath commonTemplatePath = templateHNodePath;
			if (subjMap.isBlankNode()) {
				columnsCovered = kr2rmlMapping.getAuxInfo().getBlankNodesColumnCoverage().get(subjMap.getId());
				Map<ColumnTemplateTerm, Collection<Node>> columnsToNodes = new HashMap<ColumnTemplateTerm, Collection<Node>>();

				StringBuilder output = new StringBuilder();
				
				TemplateTermSet terms = new TemplateTermSet();
				terms.addTemplateTermToSet(new StringTemplateTerm(BLANK_NODE_PREFIX));
				terms.addTemplateTermToSet(new StringTemplateTerm(kr2rmlMapping.getAuxInfo().getBlankNodesUriPrefixMap().get(triplesMap.getSubject().getId()).replaceAll(":", "_")));
				LinkedList<Collection<Node>> allNodesCovered = gatherNodesForURI(columnsCovered);
				Iterator<String> columnsCoveredIterator = columnsCovered.iterator();
				Iterator<Collection<Node>> nodesIterator = allNodesCovered.iterator();
				while(columnsCoveredIterator.hasNext() && nodesIterator.hasNext())
				{
					ColumnTemplateTerm term = new ColumnTemplateTerm(columnsCoveredIterator.next());
					terms.addTemplateTermToSet(term);
					columnsToNodes.put(term, nodesIterator.next());
				}
				LinkedList<TemplateTerm> allTerms = new LinkedList<TemplateTerm>();
				allTerms.addAll(terms.getAllTerms());
				subjects.addAll(generateSubjectsForTemplates(output, allTerms,columnsToNodes, new LinkedList<Node>(), true, false ));
					
			} else {
				LinkedList<TemplateTerm> allTerms = new LinkedList<TemplateTerm>();
				allTerms.addAll(subjMap.getTemplate().getAllTerms());
				List<ColumnTemplateTerm> terms =  subjMap.getTemplate().getAllColumnNameTermElements();
				columnsCovered = new LinkedList<String>();
				for(ColumnTemplateTerm term : terms)
				{
					columnsCovered.add(term.getTemplateTermValue());
				}
				LinkedList<Collection<Node>> allNodesCovered = gatherNodesForURI(columnsCovered);
				Map<ColumnTemplateTerm, Collection<Node>> columnsToNodes = new HashMap<ColumnTemplateTerm, Collection<Node>>();
				Iterator<ColumnTemplateTerm> termIterator = terms.iterator();
				Iterator<Collection<Node>> nodesIterator = allNodesCovered.iterator();
				while(termIterator.hasNext() && nodesIterator.hasNext())
				{
					columnsToNodes.put(termIterator.next(), nodesIterator.next());
				}
				StringBuilder output = new StringBuilder();
				
				subjects.addAll(generateSubjectsForTemplates(output, allTerms, columnsToNodes, new LinkedList<Node>(), true, true));
			}
			
			for(String columnCovered : columnsCovered)
			{
				HNodePath columnCoveredPath = factory.getHNode(translator.getHNodeIdForColumnName(columnCovered)).getHNodePath(factory);
				columnsCoveredPaths.add(columnCoveredPath);
				commonTemplatePath = HNodePath.findCommon(commonTemplatePath, columnCoveredPath);
			}
				
			for(Subject subject : subjects)
			{
				for(Node n : subject.references)
				{
					if(!stuffs.containsKey(n))
					{
						stuffs.put(n, new HashSet<Subject>());
					}
					Set<Subject> s = stuffs.get(n);
					s.add(subject);
				}
			}
			
			
			
			// Generate triples for specifying the types
			for (TemplateTermSet typeTerm:subjMap.getRdfsType()) {
				if(typeTerm.getAllColumnNameTermElements().isEmpty())
				{
					for(Subject subject : subjects)
					{
						
						//TODO dothis
						StringBuffer sb = new StringBuffer();
						for(TemplateTerm term : typeTerm.getAllTerms())
						{
							sb.append(term.getTemplateTermValue());
						}
						outWriter.println(constructTripleWithURIObject(subject.uri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", getExpandedAndNormalizedUri(sb.toString())));
					}
				}
				else
				{
					//dynamic types
				}
			
			}
			List<TriplesMapLink> links = kr2rmlMapping.getAuxInfo().getTriplesMapGraph().getAllNeighboringTriplesMap(triplesMap.getId());
			for(TriplesMapLink link : links) {
				if(link.getSourceMap().getId().compareTo(triplesMap.getId()) ==0  && !link.isFlipped() ||
						link.getTargetMap().getId().compareTo(triplesMap.getId()) == 0 && link.isFlipped())
				{
					PredicateObjectMap pom = link.getPredicateObjectMapLink();
					List<Subject> predicates = generatePredicatesForPom(pom);
					TriplesMap subjectTriplesMap = link.getSourceMap();
					TriplesMap objectTriplesMap = link.getTargetMap();
					
					for(Subject subject : triplesMapSubjects.get(subjectTriplesMap.getId()))
					{
						for(Subject objectSubject : triplesMapSubjects.get(objectTriplesMap.getId()))
						{
							for(Subject predicate : predicates)
							{
								outWriter.println(constructTripleWithURIObject(subject.getURI(), predicate.getURI(), objectSubject.getURI()));
							}
						}
						
					}
				}	
			}
			
			for(PredicateObjectMap pom : triplesMap.getPredicateObjectMaps())
				{
					if(pom.getObject().hasRefObjectMap())
					{
						continue;
					}
					if(pom.getPredicate().toString().contains("classLink"))
						continue;
					
					List<Subject> predicates = generatePredicatesForPom(pom);
					ObjectMap objMap = pom.getObject();
					LinkedList<TemplateTerm> allTerms = new LinkedList<TemplateTerm>();
					allTerms.addAll(objMap.getTemplate().getAllTerms());
					List<ColumnTemplateTerm> objectTemplateTerms =  objMap.getTemplate().getAllColumnNameTermElements();
					// we got a literal
					if(objectTemplateTerms == null || objectTemplateTerms.isEmpty())
					{
						List<Subject> values = generateSubjectsForTemplates(new StringBuilder(""), allTerms, null, null, true, true);
						for(Subject value: values)
						{
							for(Subject subject : subjects)
							{
								for(Subject predicate : predicates)
								{
									outWriter.println(constructTripleWithLiteralObject(subject.uri, predicate.getURI(),  value.uri, pom.getObject().getRdfLiteralType().toString()));
								}
							}
						}	
					}
					Map<ColumnTemplateTerm, Collection<Node>> columnsToNodes = new HashMap<ColumnTemplateTerm, Collection<Node>>();
					HNodePath objectTemplateCommonPath = null;
					HNodePath currentObjectTemplatePath = null;
					LinkedList<String> objectColumnsCovered = new LinkedList<String>();
					LinkedList<HNodePath> objectTemplatePaths = new LinkedList<HNodePath>();
					Integer maxDepth = null;
					Map<ColumnTemplateTerm, HNodePath> termToReferenceSubjectPath = new HashMap<ColumnTemplateTerm, HNodePath>(); 
					Map<ColumnTemplateTerm, HNodePath> termToReferenceObjectPath = new HashMap<ColumnTemplateTerm, HNodePath>();
					for(ColumnTemplateTerm term : objectTemplateTerms)
					{
						String objectTemplateValue = term.getTemplateTermValue();
						if(objectTemplateCommonPath == null)
						{
							objectTemplateCommonPath = currentObjectTemplatePath = factory.getHNode(translator.getHNodeIdForColumnName(objectTemplateValue)).getHNodePath(factory);
							maxDepth = objectTemplateCommonPath.length();
						}
						else
						{
							currentObjectTemplatePath = factory.getHNode(translator.getHNodeIdForColumnName(objectTemplateValue)).getHNodePath(factory);
							objectTemplatePaths.add(currentObjectTemplatePath);
							objectTemplateCommonPath = HNodePath.findCommon(objectTemplateCommonPath, currentObjectTemplatePath);
							
							maxDepth = Math.max(maxDepth, currentObjectTemplatePath.length());
						}
						objectColumnsCovered.add(objectTemplateValue);
						//find the most in common path
						int mostInCommonLength = -1;
						HNodePath referencePath = null;
						for(HNodePath columnCoveredPath : columnsCoveredPaths)
						{
								HNodePath commonPath = HNodePath.findCommon(columnCoveredPath, currentObjectTemplatePath);
								if(commonPath.length() > mostInCommonLength)
								{
									referencePath = columnCoveredPath;
									termToReferenceSubjectPath.put(term, referencePath);
									termToReferenceObjectPath.put(term, currentObjectTemplatePath);
								}
							
						}
						
					}
					for(ColumnTemplateTerm term : objectTemplateTerms)
					{
						if(termToReferenceSubjectPath.get(term).length() > termToReferenceObjectPath.get(term).length())
						{
							LinkedList<Node> objectNodes = new LinkedList<Node>();
							r.collectNodes(termToReferenceObjectPath.get(term), objectNodes);
							columnsToNodes.put(term, objectNodes);
						}
					}
					
						for(Subject subject : subjects)
						{
							for(Node node : subject.references)
							{
								for(ColumnTemplateTerm term : objectTemplateTerms)
								{
									HNodePath referencePath = termToReferenceSubjectPath.get(term);
									currentObjectTemplatePath = termToReferenceObjectPath.get(term);
									HNodePath objectToLookUpPath = null;
									if(referencePath.length() <= currentObjectTemplatePath.length())
									{
										objectToLookUpPath = HNodePath.removeCommon(currentObjectTemplatePath, referencePath);
									}
									else
									{
										continue;
									}
									if(node.getHNodeId() == referencePath.getLeaf().getId())
									{
										LinkedList<Node> objectNodes = new LinkedList<Node>();
										if(referencePath.length() < currentObjectTemplatePath.length())
										{
											node.getNestedTable().collectNodes(objectToLookUpPath.getRest(), objectNodes);
										}
										else if(referencePath.length() == currentObjectTemplatePath.length())
										{
											node.getBelongsToRow().collectNodes(objectToLookUpPath, objectNodes);
										}
										columnsToNodes.put(term, objectNodes);
									}
								}
							}
							List<Subject> values = generateSubjectsForTemplates(new StringBuilder(""), allTerms, columnsToNodes, new LinkedList<Node>(), false, true);
							for(Subject value: values)
							{
								for(Subject predicate : predicates)
								{
									outWriter.println(constructTripleWithLiteralObject(subject.uri, predicate.getURI(),  value.uri, pom.getObject().getRdfLiteralType().toString()));
								}
							}
						}
				}
			
				
			LOG.info("Processing " + triplesMap.getId() + " " +triplesMap.getSubject().getId());
			notifyDependentTriplesMapWorkers();
			return true;
		}
		private List<Subject> generatePredicatesForPom(PredicateObjectMap pom) {
			List<ColumnTemplateTerm> predicateTemplateTerms = pom.getPredicate().getTemplate().getAllColumnNameTermElements();
			LinkedList<TemplateTerm> allPredicateTemplateTerms = new LinkedList<TemplateTerm>();
			allPredicateTemplateTerms.addAll(pom.getPredicate().getTemplate().getAllTerms());
			List<Subject> predicates = new LinkedList<Subject>();
			if(predicateTemplateTerms == null || predicateTemplateTerms.isEmpty())
			{
				predicates = generateSubjectsForTemplates(new StringBuilder(""), allPredicateTemplateTerms, null, null, true, true);
				
			}
			else
			{
				//dynamic predicates;
			}
			return predicates; 
		}
		private List<Subject> generateSubjectsForTemplates(StringBuilder output,
				List<TemplateTerm> terms,
				Map<ColumnTemplateTerm, Collection<Node>> columnsToNodes, List<Node> references, boolean URIify, boolean useNodeValue) {
			List<Subject> subjects = new LinkedList<Subject>();
			
			if(!terms.isEmpty())
			{
				List<TemplateTerm> tempTerms = new LinkedList<TemplateTerm>();
				tempTerms.addAll(terms);
				TemplateTerm term = tempTerms.remove(0);
				boolean recurse = false;
				if(!tempTerms.isEmpty())
				{
					recurse = true;
				}
					
					if(term instanceof ColumnTemplateTerm)
					{
						for(Node node : columnsToNodes.get(term))
						{
							if(node.getValue().isEmptyValue() || node.getValue().asString().trim().isEmpty())
							{
								continue;
							}
							StringBuilder newPrefix = new StringBuilder(output);
							if(useNodeValue)
							{
								newPrefix.append(node.getValue().asString());
							}
							else
							{
								newPrefix.append(node.getId());
							}

							List<Node> newReferences = new LinkedList<Node>();
							newReferences.addAll(references);
							newReferences.add(node);
							if(recurse)
							{
								subjects.addAll(generateSubjectsForTemplates(newPrefix, tempTerms, columnsToNodes, references, URIify, useNodeValue));
							}
							else
							{
								String value = newPrefix.toString();
								if(URIify)
								{
									value = getExpandedAndNormalizedUri(value);
								}
								subjects.add(new Subject(newReferences, value));
							}
						}
					}
					else
					{
						StringBuilder newPrefix = new StringBuilder(output);
						newPrefix.append(term.getTemplateTermValue());
						if(recurse)
						{
							subjects.addAll(generateSubjectsForTemplates(newPrefix, tempTerms, columnsToNodes, references, URIify, useNodeValue));
						}
						else
						{
							String value = newPrefix.toString();
							if(URIify)
							{
								value = getExpandedAndNormalizedUri(value);
							}
							subjects.add(new Subject(references, value));
						}
					}
				
			}
		
			return subjects;
		}
		
		private LinkedList<Collection<Node>> gatherNodesForURI(
				List<String> columnsCovered) throws HNodeNotFoundKarmaException {
			LinkedList<Collection<Node>> allNodesCovered = new LinkedList<Collection<Node>>();
			for(String columnCovered: columnsCovered)
			{
				Collection<Node> nodesCovered = new LinkedList<Node>();
				HNodePath columnCoveredPath = factory.getHNode(translator.getHNodeIdForColumnName(columnCovered)).getHNodePath(factory);
				r.collectNodes(columnCoveredPath, nodesCovered);
				allNodesCovered.add(nodesCovered);
				//TODO what if there are no nodes.
			}
			return allNodesCovered;
		}
		public CountDownLatch getLatch() {
			return latch;
		}
		
		
		public List<String> generateURIsForBlankNodes(StringBuilder prefix, LinkedList<Collection<Node>> allNodesCovered)
		{
			List<String> uris = new LinkedList<String>();
			if(!allNodesCovered.isEmpty())
			{
				Collection<Node> nodesCovered = allNodesCovered.pop();
				if(!allNodesCovered.isEmpty())
				{
					for(Node node : nodesCovered)
					{
						StringBuilder longerPrefix = new StringBuilder(prefix.toString());
						longerPrefix.append("_");
						longerPrefix.append(node.getId());
						LinkedList<Collection<Node>> temp = new LinkedList<Collection<Node>>();
						temp.addAll(allNodesCovered);
						uris.addAll(generateURIsForBlankNodes(longerPrefix, temp));
					}
				}
				else
				{
					String tempPrefix = prefix.toString();
					for(Node node : nodesCovered)
					{
						//TODO this could probably be done earlier.
						uris.add(getExpandedAndNormalizedUri(tempPrefix + "_" + node.getId()));
					} 
					return uris;
				}
			}
			return uris;
		}
		
		
	}

}

class HNodeNotFoundKarmaException extends Exception {
	private static final long serialVersionUID = 1L;
	private String offendingColumnName;
	
	//constructor without parameters
	public HNodeNotFoundKarmaException() {}

	//constructor for exception description
	public HNodeNotFoundKarmaException(String description, String offendingColumnName) {
	    super(description);
	    this.offendingColumnName = offendingColumnName;
	}
	
	public String getOffendingColumn() {
		return this.offendingColumnName;
	}	
}
class ValueNotFoundKarmaException extends Exception{

	private static final long serialVersionUID = 1L;
	private String offendingColumnHNodeId;
	
	//constructor without parameters
	public ValueNotFoundKarmaException() {}

	//constructor for exception description
	public ValueNotFoundKarmaException(String description, String offendingColumnHNodeId) {
	    super(description);
	    this.offendingColumnHNodeId = offendingColumnHNodeId;
	}
	
	public String getOffendingColumnHNodeId() {
		return this.offendingColumnHNodeId;
	}
}

class NoValueFoundInNodeException extends Exception{

	private static final long serialVersionUID = 1L;
	
	//constructor without parameters
	public NoValueFoundInNodeException() {}

	//constructor for exception description
	public NoValueFoundInNodeException(String description) {
	    super(description);
	}
}


