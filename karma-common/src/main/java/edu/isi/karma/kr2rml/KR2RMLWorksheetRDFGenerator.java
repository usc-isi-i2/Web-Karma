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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.kr2rml.ErrorReport.Priority;
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
import edu.isi.karma.kr2rml.writer.AvroKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.N3KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.SFKR2RMLRDFWriter;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class KR2RMLWorksheetRDFGenerator {

	protected Workspace workspace;
	protected RepFactory factory;
	protected Worksheet worksheet;
	protected String outputFileName;
	protected ErrorReport errorReport;
	protected boolean addColumnContextInformation;
	protected KR2RMLMapping kr2rmlMapping;
	protected KR2RMLMappingColumnNameHNodeTranslator translator;
	protected ConcurrentHashMap<String, String> hNodeToContextUriMap;
	protected List<KR2RMLRDFWriter> outWriters;
	protected List<String> tripleMapToKill = new ArrayList<>();
	protected List<String> tripleMapToStop = new ArrayList<>();
	protected List<String> POMToKill = new ArrayList<>();
	private Logger logger = LoggerFactory.getLogger(KR2RMLWorksheetRDFGenerator.class);
	private URIFormatter uriFormatter;
	private RootStrategy strategy;
	private SuperSelection selection;
	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, Workspace workspace, 
			String outputFileName, boolean addColumnContextInformation, 
			KR2RMLMapping kr2rmlMapping, ErrorReport errorReport, SuperSelection sel) throws UnsupportedEncodingException, FileNotFoundException {
		initializeMemberVariables(worksheet, workspace, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		File f = new File(this.outputFileName);
		File parentDir = f.getParentFile();
		parentDir.mkdirs();
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
		outWriters.add(new N3KR2RMLRDFWriter(uriFormatter, new PrintWriter (bw)));
		this.selection = sel;

	}

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, Workspace workspace, KR2RMLRDFWriter writer, boolean addColumnContextInformation,RootStrategy strategy, 
			KR2RMLMapping kr2rmlMapping, ErrorReport errorReport, SuperSelection sel) {
		initializeMemberVariables(worksheet, workspace, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		this.outWriters.add(writer);
		this.strategy = strategy;
		this.selection = sel;
	}

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, Workspace workspace, 
			List<KR2RMLRDFWriter> writers, boolean addColumnContextInformation,  
			KR2RMLMapping kr2rmlMapping, ErrorReport errorReport, SuperSelection sel) {
		initializeMemberVariables(worksheet, workspace, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		this.outWriters.addAll(writers);
		this.selection = sel;
	}

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, Workspace workspace, List<KR2RMLRDFWriter> writers, boolean addColumnContextInformation, 
			RootStrategy strategy,  List<String> tripleMapToKill, List<String> tripleMapToStop, 
			List<String> POMToKill, 
			KR2RMLMapping kr2rmlMapping, ErrorReport errorReport, SuperSelection sel) {
		initializeMemberVariables(worksheet, workspace, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		this.strategy = strategy;
		this.tripleMapToKill = tripleMapToKill;
		this.tripleMapToStop = tripleMapToStop;
		this.POMToKill = POMToKill;
		this.outWriters.addAll(writers);
		this.selection = sel;
	}

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet, Workspace workspace, 
			 PrintWriter writer, KR2RMLMapping kr2rmlMapping,   
			ErrorReport errorReport, boolean addColumnContextInformation, SuperSelection sel) {
		super();
		initializeMemberVariables(worksheet, workspace, outputFileName,
				addColumnContextInformation, kr2rmlMapping, errorReport);
		this.outWriters.add(new N3KR2RMLRDFWriter(uriFormatter, writer));
		this.selection = sel;
	}


	private void initializeMemberVariables(Worksheet worksheet,
			Workspace workspace, String outputFileName,
			boolean addColumnContextInformation, KR2RMLMapping kr2rmlMapping,
			ErrorReport errorReport) {
	//	
		this.kr2rmlMapping = kr2rmlMapping;
		this.workspace = workspace;
		this.factory = workspace.getFactory();
		this.worksheet = worksheet;
		this.outputFileName = outputFileName;
		this.errorReport = errorReport;
		this.uriFormatter = new URIFormatter(kr2rmlMapping.getPrefixes(), errorReport);
		this.hNodeToContextUriMap = new ConcurrentHashMap<>();
		this.addColumnContextInformation = addColumnContextInformation;
		this.translator = new KR2RMLMappingColumnNameHNodeTranslator(factory, worksheet);
		this.outWriters = new LinkedList<>();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void generateRDF(boolean closeWriterAfterGeneration) throws IOException {

		try {

			// RDF Generation starts at the top level rows
			ArrayList<Row> rows = this.worksheet.getDataTable().getRows(0, 
					this.worksheet.getDataTable().getNumRows(), selection);



			Map<TriplesMapGraph, List<String>> graphTriplesMapsProcessingOrder = new HashMap<>();
			for(TriplesMapGraph graph : kr2rmlMapping.getAuxInfo().getTriplesMapGraph().getGraphs())
			{
				TriplesMapGraph copyGraph = graph.copyGraph();
				if(null == strategy) {
					strategy = new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy());
				}
				copyGraph.killTriplesMap(tripleMapToKill, strategy);
				copyGraph.stopTriplesMap(tripleMapToStop, strategy);
				copyGraph.killPredicateObjectMap(POMToKill, strategy);
				try{
					DFSTriplesMapGraphDAGifier dagifier = new DFSTriplesMapGraphDAGifier();
					
					List<String> triplesMapsProcessingOrder = new LinkedList<>();
					triplesMapsProcessingOrder = dagifier.dagify(copyGraph, strategy);
					graphTriplesMapsProcessingOrder.put(copyGraph, triplesMapsProcessingOrder);
				}catch (Exception e)
				{
					logger.error("Unable to find DAG for RDF Generation!", e);
					throw new Exception("Unable to find DAG for RDF Generation!", e);

				}
			}
			for (KR2RMLRDFWriter writer : outWriters) {
				if (writer instanceof SFKR2RMLRDFWriter) {
					SFKR2RMLRDFWriter jsonWriter = (SFKR2RMLRDFWriter)writer;
					jsonWriter.addPrefixes(kr2rmlMapping.getPrefixes());
					for(Entry<TriplesMapGraph, List<String>> entry : graphTriplesMapsProcessingOrder.entrySet())
					{
						List<String> triplesMapIds = entry.getValue();
						jsonWriter.addRootTriplesMapId(triplesMapIds.get(triplesMapIds.size()-1));	
					}
					if(jsonWriter instanceof AvroKR2RMLRDFWriter)
					{
						AvroKR2RMLRDFWriter avroWriter = (AvroKR2RMLRDFWriter) jsonWriter;
						avroWriter.setProcessingOrder(graphTriplesMapsProcessingOrder);
					}
				}
			}
			int i=1;
			TriplesMapPlanExecutor e = new TriplesMapPlanExecutor(true);
			Map<TriplesMap, TriplesMapWorkerPlan> triplesMapToWorkerPlan = new HashMap<>() ;
			for(TriplesMap triplesMap : kr2rmlMapping.getTriplesMapList())
			{
				try{
					TriplesMapWorkerPlan workerPlan = new TriplesMapWorkerPlan(factory, triplesMap, kr2rmlMapping, uriFormatter, translator,  addColumnContextInformation, hNodeToContextUriMap, selection, graphTriplesMapsProcessingOrder);
					triplesMapToWorkerPlan.put(triplesMap, workerPlan);
				}
				catch (Exception ex)
				{
					logger.error("unable to generate working plan for " + triplesMap.getId(), ex.getMessage());
				}
			}
			for (Row row:rows) {
				for(Entry<TriplesMapGraph, List<String>> entry : graphTriplesMapsProcessingOrder.entrySet())
				{
					TriplesMapPlanGenerator g = new TriplesMapPlanGenerator(triplesMapToWorkerPlan, row, outWriters);
					TriplesMapPlan plan = g.generatePlan(entry.getKey(), entry.getValue(), strategy);
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
			throw new IOException("Unable to generate RDF: " +e.getMessage());
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
		//System.gc();
	}

	private void generateColumnProvenanceInformation() {
		for (Entry<String, String> stringStringEntry : hNodeToContextUriMap.entrySet()) {
			getColumnContextTriples(stringStringEntry.getKey());


			// Generate wasDerivedFrom property if required
			HNode hNode = factory.getHNode(stringStringEntry.getKey());
			if (hNode.isDerivedFromAnotherColumn()) {
				HNode originalHNode = factory.getHNode(hNode.getOriginalColumnHNodeId());
				if (originalHNode != null) {
					getColumnContextTriples(originalHNode.getId());

					for(KR2RMLRDFWriter outWriter : outWriters)
					{
						outWriter.outputTripleWithURIObject(
								stringStringEntry.getValue(), Uris.PROV_WAS_DERIVED_FROM_URI, 
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
			String randomId = UUID.randomUUID().toString();
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
					hNode.getColumnName(), "", "");
		}

	}

}

