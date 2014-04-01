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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.ErrorReport.Priority;
import edu.isi.karma.kr2rml.exception.HNodeNotFoundKarmaException;
import edu.isi.karma.kr2rml.exception.NoValueFoundInNodeException;
import edu.isi.karma.kr2rml.exception.ValueNotFoundKarmaException;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingColumnNameHNodeTranslator;
import edu.isi.karma.kr2rml.planning.DFSTriplesMapGraphDAGifier;
import edu.isi.karma.kr2rml.planning.SteinerTreeRootStrategy;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.TriplesMapLink;
import edu.isi.karma.kr2rml.planning.TriplesMapPlan;
import edu.isi.karma.kr2rml.planning.TriplesMapPlanExecutor;
import edu.isi.karma.kr2rml.planning.TriplesMapPlanGenerator;
import edu.isi.karma.kr2rml.planning.TriplesMapWorkerPlan;
import edu.isi.karma.kr2rml.planning.WorksheetDepthRootStrategy;
import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;
import edu.isi.karma.kr2rml.template.StringTemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTerm;
import edu.isi.karma.kr2rml.template.TemplateTermSet;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNode;
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
	protected ConcurrentHashMap<String, String> hNodeToContextUriMap;
	protected KR2RMLRDFWriter outWriter;

	private Logger logger = LoggerFactory.getLogger(KR2RMLWorksheetRDFGenerator.class);
	private URIFormatter uriFormatter;

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
		this.uriFormatter = new URIFormatter(ontMgr, errorReport);
		this.hNodeToContextUriMap = new ConcurrentHashMap<String, String>();
		this.addColumnContextInformation = addColumnContextInformation;
		this.translator = new KR2RMLMappingColumnNameHNodeTranslator(factory, worksheet);

	}

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, RepFactory factory, 
			OntologyManager ontMgr, PrintWriter writer, KR2RMLMapping kr2rmlMapping,  
			ErrorReport errorReport, boolean addColumnContextInformation) {
		super();
		this.ontMgr = ontMgr;
		this.kr2rmlMapping = kr2rmlMapping;
		this.factory = factory;
		this.worksheet = worksheet;
		this.errorReport = errorReport;
		this.uriFormatter = new URIFormatter(ontMgr, errorReport);
		this.outWriter = new N3KR2RMLRDFWriter(uriFormatter, writer);
		this.hNodeToContextUriMap = new ConcurrentHashMap<String, String>();
		this.addColumnContextInformation = addColumnContextInformation;
		this.translator = new KR2RMLMappingColumnNameHNodeTranslator(factory, worksheet);


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
				outWriter = new N3KR2RMLRDFWriter(uriFormatter, new PrintWriter (bw));
			} else if (this.outWriter == null && this.outputFileName == null) {
				outWriter = new N3KR2RMLRDFWriter(uriFormatter, new PrintWriter (System.out));			
			}

			// RDF Generation starts at the top level rows
			ArrayList<Row> rows = this.worksheet.getDataTable().getRows(0, 
					this.worksheet.getDataTable().getNumRows());



			try{
				DFSTriplesMapGraphDAGifier dagifier = new DFSTriplesMapGraphDAGifier();
				dagifier.dagify(kr2rmlMapping.getAuxInfo().getTriplesMapGraph(), new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy()));

			}catch (Exception e)
			{
				logger.error("Unable to find DAG for RDF Generation!", e);
				throw new Exception("Unable to find DAG for RDF Generation!", e);

			}
			int i=1;
			TriplesMapPlanExecutor e = new TriplesMapPlanExecutor();
			Map<TriplesMap, TriplesMapWorkerPlan> triplesMapToWorkerPlan = new HashMap<TriplesMap, TriplesMapWorkerPlan>() ;
			for(TriplesMap triplesMap : kr2rmlMapping.getTriplesMapList())
			{
				TriplesMapWorkerPlan workerPlan = new TriplesMapWorkerPlan(factory, triplesMap, kr2rmlMapping, uriFormatter, translator,  addColumnContextInformation, hNodeToContextUriMap);
				triplesMapToWorkerPlan.put(triplesMap, workerPlan);
			}
			for (Row row:rows) {

				TriplesMapPlanGenerator g = new TriplesMapPlanGenerator(triplesMapToWorkerPlan, row, outWriter);
				TriplesMapPlan plan = g.generatePlan(kr2rmlMapping.getAuxInfo().getTriplesMapGraph());
				errorReport.combine(e.execute(plan));
				outWriter.finishRow();
				if (i++%2000 == 0)
					logger.info("Done processing " + i + " rows");

			}
			e.shutdown(errorReport);
			// Generate column provenance information if required
			if (addColumnContextInformation) {
				generateColumnProvenanceInformation();
			}

		} catch (Exception e)
		{
			logger.error("Unable to generate RDF: ", e);
			errorReport.addReportMessage(new ReportMessage("General RDF Generation Error", e.getMessage(), Priority.high));
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
			predicateUri = URIFormatter.normalizeUri(getTemplateTermSetPopulatedWithValues(node,  
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

			outWriter.outputTripleWithURIObject(subjUri, predicateUri, objUri);

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
						outWriter.outputQuadWithLiteralObject(subjUri, predicateUri, value, rdfLiteralType, getColumnContextUri(hNodeId_val));
					}
					catch(HNodeNotFoundKarmaException he)
					{
						logger.error("No hnode id found to generate quad for");
						return;
					}
				}
			} else {
				outWriter.outputTripleWithLiteralObject(subjUri, predicateUri, value, rdfLiteralType);
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
			uri = uriFormatter.getExpandedAndNormalizedUri(getBlankNodeUri(subjMap.getId(), node));
		} else {
			uri = uriFormatter.getExpandedAndNormalizedUri(getTemplateTermSetPopulatedWithValues(node,
					subjMap.getTemplate()));
		}

		// Generate triples for specifying the types
		for (TemplateTermSet typeTerm:subjMap.getRdfsType()) {
			String typeUri = uriFormatter.getExpandedAndNormalizedUri(getTemplateTermSetPopulatedWithValues(
					node, typeTerm));
			outWriter.outputTripleWithURIObject(uri, Uris.RDF_TYPE_URI, typeUri);

		}
		return uri;
	}



	private String getBlankNodeUri(String subjMapid, Node node) 
			throws ValueNotFoundKarmaException, HNodeNotFoundKarmaException {

		StringBuilder output = new StringBuilder();
		// Add the blank namespace
		output.append(Uris.BLANK_NODE_PREFIX);

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

	private void generateColumnProvenanceInformation() {
		for (String hNodeId:hNodeToContextUriMap.keySet()) {
			getColumnContextTriples(hNodeId);


			// Generate wasDerivedFrom property if required
			HNode hNode = factory.getHNode(hNodeId);
			if (hNode.isDerivedFromAnotherColumn()) {
				HNode originalHNode = factory.getHNode(hNode.getOriginalColumnHNodeId());
				if (originalHNode != null) {
					getColumnContextTriples(originalHNode.getId());

					outWriter.outputTripleWithURIObject(
							hNodeToContextUriMap.get(hNodeId), Uris.PROV_WAS_DERIVED_FROM_URI, 
							getColumnContextUri(originalHNode.getId()));

				}
			}
		}
	}
	protected String getColumnContextUri (String hNodeId) {

		if (hNodeToContextUriMap.containsKey(hNodeId))
			return hNodeToContextUriMap.get(hNodeId);
		else {
			String randomId = RandomStringUtils.randomAlphanumeric(10);
			String uri = Namespaces.KARMA_DEV + randomId + "_" + hNodeId;
			hNodeToContextUriMap.put(hNodeId, uri);
			return uri;
		}
	}

	private void getColumnContextTriples(String hNodeId) {
		String colUri = getColumnContextUri(hNodeId);

		// Generate the type
		outWriter.outputTripleWithURIObject("<" + colUri + ">", Uris.RDF_TYPE_URI, 
				"<" + Uris.PROV_ENTITY_URI + ">");


		// Generate the label
		HNode hNode = factory.getHNode(hNodeId);
		outWriter.outputTripleWithLiteralObject("<" + colUri + ">", Uris.RDFS_LABEL_URI, 
				hNode.getColumnName(), "");


	}


}

