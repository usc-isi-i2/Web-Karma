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

package edu.isi.karma.imp.rdf;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.er.helper.ConfigUtil;
import edu.isi.karma.imp.rdf.testRepository;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;

public class RDFImportMain {

	/**
	 * @param args
	 */
	static PrintWriter pw = null;
	static Map<String, String> map = new HashMap<String, String>();

	public static void main(String[] args) {

		// Import the json file
		JSONArray json = new JSONArray();
		File file = new File("config/c.json");
		if (!file.exists()) {
			throw new IllegalArgumentException("file name "
					+ file.getAbsolutePath() + " does not exist.");
		}
		ConfigUtil util = new ConfigUtil();
		json = util.loadConfig(file);

		// Create a worksheet, HTable, and input data into it.
		ArrayList<String> headersList = null;
		RepFactory factory;
		Worksheet worksheet = null;
		Workspace workspace = WorkspaceManager.getInstance().getFactory()
				.createWorkspace();
		factory = workspace.getFactory();
		worksheet = factory.createWorksheet("WorksheetName", workspace);

		RDFImport rdfImport = new RDFImport(json, workspace, worksheet);
		// create a HTable;
		try {
			headersList = rdfImport.generateWorksheetFromGeneralJSON(map);
		} catch (JSONException e) {
			e.printStackTrace();
		}


		String source_path[] = {
				"/Users/yingzhang/Documents/USC/Building_ontology/n3/Union-n3/OSM_buildings.n3",
				"/Users/yingzhang/Documents/USC/Building_ontology/n3/Union-n3/Wikimapia_buildings.n3",
				"/Users/yingzhang/Documents/USC/Building_ontology/n3/Union-n3/sameAs.n3" };
		String dataSet_path = "/Users/yingzhang/Documents/USC/Building_ontology/Repository";
		String query = null;
		testRepository tR = new testRepository();

		/*
		 * execute only once. Before execute it, please make sure that the
		 * repository folder is empty
		 */
		tR.setupDirectoryModelfrom(dataSet_path, source_path);

		/* SPARQL QUERY. */
		RDFQuery rdfQuery = new RDFQuery();

		/* general sparql query which is used to extract URI */
		String generalQuery = rdfQuery.createGeneralQuery();
		List<String> listURI = tR.sparqlQuery(dataSet_path, generalQuery, "a");

		String[] properties = { "buildingName", "countyName", "stateName",
				"yInDecimalLatitude", "xInDecimalLongitude", "sridValue",
				"wellKnownText" };
		Table dataTable = worksheet.getDataTable();
		Map<String, String> mapRows = new HashMap<String, String>();
		Map<String, HNode> mapForNewNode = new HashMap<String, HNode>();
		HNode newHNode = null;

		for (String pro : properties) {
			String HNode_idnum = map.get(pro);
			HNode hNode = factory.getHNode(HNode_idnum);
			HTable Ht = hNode.addNestedTable(pro + "ValuesHTable", worksheet,
					factory);
			newHNode = Ht.addHNode(pro + "ValuesHNode", worksheet, factory);
			mapForNewNode.put(pro, newHNode);

		}

		for (String str : listURI) { // URIs
			Row row = dataTable.addRow(factory);
			mapRows.put(dataTable.getId(), row.getId());
			for (String pro : properties) {// in one row: name, lat, long;
				List<String> ls_sparql_result = new ArrayList<String>();
				List<String> ls_sparql_result_S2 = new ArrayList<String>();
				List<String> literal = new ArrayList<String>();
				literal.add("c");
				literal.add("d");
				if (pro.equalsIgnoreCase("xInDecimalLongitude")) {
					query = rdfQuery.createQueryForLongitudeS1(str,
							"BuildingOntology", pro);
					ls_sparql_result = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					query = rdfQuery.createQueryForLongitudeS2(str,
							"BuildingOntology", pro);
					ls_sparql_result_S2 = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					ls_sparql_result.addAll(ls_sparql_result_S2);
				} else if (pro.equalsIgnoreCase("yInDecimalLatitude")) {
					query = rdfQuery.createQueryForLatitudeS1(str,
							"BuildingOntology", pro);
					ls_sparql_result = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					query = rdfQuery.createQueryForLatitudeS2(str,
							"BuildingOntology", pro);
					ls_sparql_result_S2 = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					ls_sparql_result.addAll(ls_sparql_result_S2);
				} else if (pro.equalsIgnoreCase("buildingName")) {
					query = rdfQuery.createQueryForBuildingNameS1(str,
							"BuildingOntology", pro);
					ls_sparql_result = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					query = rdfQuery.createQueryForBuildingNameS2(str,
							"BuildingOntology", pro);
					ls_sparql_result_S2 = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					ls_sparql_result.addAll(ls_sparql_result_S2);
				} else if (pro.equalsIgnoreCase("countyName")) {

					query = rdfQuery.createQueryForCountyNameS1(str,
							"BuildingOntology", pro);
					ls_sparql_result = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					query = rdfQuery.createQueryForCountyNameS2(str,
							"BuildingOntology", pro);
					ls_sparql_result_S2 = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					ls_sparql_result.addAll(ls_sparql_result_S2);
				} else if (pro.equalsIgnoreCase("stateName")) {
					query = rdfQuery.createQueryForStateNameS1(str,
							"BuildingOntology", pro);
					ls_sparql_result = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					query = rdfQuery.createQueryForStateNameS2(str,
							"BuildingOntology", pro);
					ls_sparql_result_S2 = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					ls_sparql_result.addAll(ls_sparql_result_S2);

				} else if (pro.equalsIgnoreCase("sridValue")) {
					query = rdfQuery.createQueryForSridValueS1(str,
							"BuildingOntology", pro);
					ls_sparql_result = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					query = rdfQuery.createQueryForSridValueS2(str,
							"BuildingOntology", pro);
					ls_sparql_result_S2 = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					ls_sparql_result.addAll(ls_sparql_result_S2);
				} else if (pro.equalsIgnoreCase("wellKnownText")) {
					query = rdfQuery.createQueryForWellKnownTextS1(str,
							"BuildingOntology", pro);
					ls_sparql_result = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					query = rdfQuery.createQueryForWellKnownTextS2(str,
							"BuildingOntology", pro);
					ls_sparql_result_S2 = tR.sparqlQueryLiteral(dataSet_path,
							query, literal);
					ls_sparql_result.addAll(ls_sparql_result_S2);
				}

				/* fill in the HTable with sparql result */
				String HNode_id = map.get(pro);
				String hTable_id1 = factory.getHNode(HNode_id).getHTableId();
				HTable ht = factory.getHTable(hTable_id1);

				if (ht == worksheet.getHeaders()) {// has no nested table
					for (String ss : ls_sparql_result) {
						if (ss.equalsIgnoreCase("")) {
							ss = "  ";
						}
						Row subRow1 = row.getNode(HNode_id).getNestedTable()
								.addRow(factory);
						subRow1.setValue(mapForNewNode.get(pro).getId(), ss,
								factory);
					}
				} else {// this Hnode has been nested in a Htable
					Row subRow = null;
					HNode parentHNode = ht.getParentHNode();
					if (mapRows.get(parentHNode.getNestedTable().getId()) != null) {
						// in one row, and this row has been created;
						String nestRow_id = mapRows.get(parentHNode
								.getNestedTable().getId());
						Row nestedRow = factory.getRow(nestRow_id);
						for (String ss : ls_sparql_result) {
							Row subRow1 = nestedRow.getNode(HNode_id)
									.getNestedTable().addRow(factory);
							subRow1.setValue(mapForNewNode.get(pro).getId(),
									ss, factory);
						}
					} else {// in one row, but this row has not been created;
						subRow = row.addNestedRow(parentHNode.getId(), factory);
						mapRows.put(parentHNode.getNestedTable().getId(),
								subRow.getId());
						for (String ss : ls_sparql_result) {
							Row subRow1 = subRow.getNode(HNode_id)
									.getNestedTable().addRow(factory);
							subRow1.setValue(mapForNewNode.get(pro).getId(),
									ss, factory);
						}
					}
				}
			}
			mapRows.clear();

		}
		/* show the result */
		pw = new PrintWriter(System.out, true);
		System.out.println("PRINTING THE DATA IN TABLE:)))))))");
		dataTable.prettyPrint("", pw, factory); // show the data in HTable;
	}
}
