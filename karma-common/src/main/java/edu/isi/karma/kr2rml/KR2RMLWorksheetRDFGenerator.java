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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import edu.isi.karma.kr2rml.planning.RootStrategy;
import edu.isi.karma.kr2rml.planning.SteinerTreeRootStrategy;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.TriplesMapGraph;
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
	protected List<KR2RMLRDFWriter> outWriters;

	private Logger logger = LoggerFactory.getLogger(KR2RMLWorksheetRDFGenerator.class);
	private URIFormatter uriFormatter;
	private RootStrategy strategy;

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, RepFactory factory, 
			OntologyManager ontMgr, String outputFileName, boolean addColumnContextInformation, 
			KR2RMLMapping kr2rmlMapping, ErrorReport errorReport) throws UnsupportedEncodingException, FileNotFoundException {
		initializeMemberVariables(worksheet, factory, ontMgr, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		File f = new File(this.outputFileName);
		File parentDir = f.getParentFile();
		parentDir.mkdirs();
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
		outWriters.add(new N3KR2RMLRDFWriter(uriFormatter, new PrintWriter (bw)));


	}

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, RepFactory factory, 
			OntologyManager ontMgr, KR2RMLRDFWriter writer, boolean addColumnContextInformation,RootStrategy strategy, 
			KR2RMLMapping kr2rmlMapping, ErrorReport errorReport) {
		initializeMemberVariables(worksheet, factory, ontMgr, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		this.outWriters.add(writer);

	}

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, RepFactory factory, 
			OntologyManager ontMgr, List<KR2RMLRDFWriter> writers, boolean addColumnContextInformation,RootStrategy strategy, 
			KR2RMLMapping kr2rmlMapping, ErrorReport errorReport) {
		initializeMemberVariables(worksheet, factory, ontMgr, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		this.outWriters.addAll(writers);

	}
	
	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, RepFactory factory, 
			OntologyManager ontMgr, PrintWriter writer, KR2RMLMapping kr2rmlMapping,   
			ErrorReport errorReport, boolean addColumnContextInformation) {
		super();
		initializeMemberVariables(worksheet, factory, ontMgr, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		this.outWriters.add(new N3KR2RMLRDFWriter(uriFormatter, writer));

	}


	private void initializeMemberVariables(Worksheet worksheet,
			RepFactory factory, OntologyManager ontMgr, String outputFileName,
			boolean addColumnContextInformation, KR2RMLMapping kr2rmlMapping,
			ErrorReport errorReport) {
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
		this.outWriters = new LinkedList<KR2RMLRDFWriter>();
	}


	public void generateRDF(boolean closeWriterAfterGeneration) throws IOException {

		try {

			// RDF Generation starts at the top level rows
			ArrayList<Row> rows = this.worksheet.getDataTable().getRows(0, 
					this.worksheet.getDataTable().getNumRows());


			
			Map<TriplesMapGraph, List<String>> graphTriplesMapsProcessingOrder = new HashMap<TriplesMapGraph, List<String>>();
			for(TriplesMapGraph graph : kr2rmlMapping.getAuxInfo().getTriplesMapGraph().getGraphs())
			{
				try{
					DFSTriplesMapGraphDAGifier dagifier = new DFSTriplesMapGraphDAGifier();
					if(null == strategy)
					{
						strategy =new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy());
					
					}
					List<String> triplesMapsProcessingOrder = new LinkedList<String>();
					triplesMapsProcessingOrder = dagifier.dagify(graph, strategy);
					graphTriplesMapsProcessingOrder.put(graph, triplesMapsProcessingOrder);
				}catch (Exception e)
				{
					logger.error("Unable to find DAG for RDF Generation!", e);
					throw new Exception("Unable to find DAG for RDF Generation!", e);
	
				}
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
				for(Entry<TriplesMapGraph, List<String>> entry : graphTriplesMapsProcessingOrder.entrySet())
				{
					TriplesMapPlanGenerator g = new TriplesMapPlanGenerator(triplesMapToWorkerPlan, row, outWriters);
					TriplesMapPlan plan = g.generatePlan(entry.getKey(), entry.getValue());
					errorReport.combine(e.execute(plan));
				}
				for(KR2RMLRDFWriter outWriter : outWriters)
				{
					outWriter.finishRow();
				}
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
				for(KR2RMLRDFWriter outWriter : outWriters)
				{
					outWriter.flush();
					outWriter.close();
				}
			}
		}
		// An attempt to prevent an occasional error that occurs on Windows platform
		// The requested operation cannot be performed on a file with a user-mapped section open
		System.gc();
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

					for(KR2RMLRDFWriter outWriter : outWriters)
					{
						outWriter.outputTripleWithURIObject(
								hNodeToContextUriMap.get(hNodeId), Uris.PROV_WAS_DERIVED_FROM_URI, 
								getColumnContextUri(originalHNode.getId()));
					}

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

		for(KR2RMLRDFWriter outWriter : outWriters)
		{
			// Generate the type
			outWriter.outputTripleWithURIObject("<" + colUri + ">", Uris.RDF_TYPE_URI, 
					"<" + Uris.PROV_ENTITY_URI + ">");
	
	
			// Generate the label
			HNode hNode = factory.getHNode(hNodeId);
			outWriter.outputTripleWithLiteralObject("<" + colUri + ">", Uris.RDFS_LABEL_URI, 
					hNode.getColumnName(), "");
		}

	}


}

