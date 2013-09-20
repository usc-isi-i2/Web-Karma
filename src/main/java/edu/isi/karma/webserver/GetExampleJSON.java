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
package edu.isi.karma.webserver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.rep.metadata.Tag;
import edu.isi.karma.rep.metadata.TagsContainer.Color;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GetExampleJSON extends HttpServlet {
	private enum Arguments {
		hasPreferenceId, workspacePreferencesId
	}

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(GetExampleJSON.class);

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		logger.info("Creating new workspace ...");

		Workspace workspace = WorkspaceManager.getInstance().createWorkspace();

		logger.info("done");
		/* Check if any workspace id is set in cookies. */
		boolean hasWorkspaceCookieId = request.getParameter(Arguments.hasPreferenceId.name()).equals("true");
		VWorkspace vwsp = null;
		File crfModelFile = null;
		
		/* If set, pick the right preferences and CRF Model file */
		if(hasWorkspaceCookieId) {
			String cachedWorkspaceId = request.getParameter(Arguments.workspacePreferencesId.name());
			vwsp = new VWorkspace(workspace, cachedWorkspaceId);
			crfModelFile = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + 
					"CRF_Models/"+cachedWorkspaceId+"_CRFModel.txt");
		} else {
			vwsp = new VWorkspace(workspace);
			crfModelFile = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + 
					"CRF_Models/"+workspace.getId()+"_CRFModel.txt");
		}
		/* Read and populate CRF Model from a file */
		if(!crfModelFile.exists())
			crfModelFile.createNewFile();
		boolean result = workspace.getCrfModelHandler().readModelFromFile(crfModelFile.getAbsolutePath());
		if (!result)
			logger.error("Error occured while reading CRF Model!");
		
		WorkspaceRegistry.getInstance().register(new ExecutionController(vwsp));
		
		/** Check if any ontology needs to be preloaded **/
		String preloadedOntDir = ServletContextParameterMap.getParameterValue(ServletContextParameterMap.ContextParameter.PRELOADED_ONTOLOGY_DIRECTORY);
		File ontDir = new File(preloadedOntDir);
		if (ontDir.exists()) {
			File[] ontologies = ontDir.listFiles();
			OntologyManager mgr = workspace.getOntologyManager();
			for (File ontology: ontologies) {
				if (ontology.getName().endsWith(".owl") || ontology.getName().endsWith(".rdf") || ontology.getName().endsWith(".xml")) {
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try {
						mgr.doImport(ontology);
					} catch (Exception t) {
						logger.error ("Error loading ontology: " + ontology.getAbsolutePath(), t);
					}
				}
			}
			// update the cache at the end when all files are added to the model
			mgr.updateCache();
		} else {
			logger.info("No directory for preloading ontologies exists.");
		}

		//mariam
		//File file = new File("../demofiles/usc_faculty.csv");
		
//		File file = new File("SampleData/CSV/PharmGKBPathways");
//		CSVFileImport imp = new CSVFileImport(1, 2, ',', '"', file, workspace.getFactory(), workspace);
//		try {
//			imp.generateWorksheet();
//		} catch (KarmaException e) {
//			e.printStackTrace();
//		}
		
//		File file = new File("/Users/mohsen/Desktop/temp/karma issues/Shubham/artworks-list.xml");
//		String fileContents = FileUtil.readFileContentsToString(file);
//		JSONObject json;
//		try {
//			json = XML.toJSONObject(fileContents);
//			JsonImport imp = new JsonImport(json, file.getName(), workspace);
//			imp.generateWorksheet();
//		} catch (JSONException e1) {
//			e1.printStackTrace();
//		}

//		SampleDataFactory.createFromJsonTextFile(workspace,"./SampleData/JSON/Events.json");

		// Initialize the Outlier tag
		Tag outlierTag = new Tag(TagName.Outlier, Color.Red);
		workspace.getTagsContainer().addTag(outlierTag);

		// SampleDataFactory.createSample1small(workspace);
//		SampleDataFactory.createSample1(workspace);
		// SampleDataFactory.createSampleJsonWithNestedTable2(false/* true: 2
		// rows */,
		// vwsp.getWorkspace());
		// //SampleDataFactory.createFlatWorksheet(workspace, 10000, 6);
		// SampleDataFactory.createFlatWorksheet(workspace, 2, 2);
		// //SampleDataFactory.createFromJsonTextFile(workspace,
		// "samplejson-1.txt");
		// SampleDataFactory.createJsonWithFunnyCharacters(workspace);
		// SampleDataFactory.createSampleJson(workspace, 3);
		// SampleDataFactory.createSampleJsonWithEmptyNestedTable1(workspace);
		// SampleDataFactory.createSampleJsonWithEmptyNestedTable2(workspace);
		// SampleDataFactory.createSampleJsonWithEmptyNestedTable3(workspace);
		// SampleDataFactory.createSampleJsonWithEmptyNestedTable4(workspace);
		// SampleDataFactory.createUnitTest1(workspace);
		// SampleDataFactory.createUnitTest2(workspace);
		// SampleDataFactory.createUnitTest3(workspace);
		// SampleDataFactory.createUnitTest4(workspace);
		// SampleDataFactory.createUnitTest5(workspace);
		// SampleDataFactory.createUnitTest6(workspace);
		// // SampleDataFactory.createFromJsonTextFile(workspace,
		// "unit-test-json.json");
		// // SampleDataFactory.createFromJsonTextFile(workspace,
		// "testUnitTest1.json");
		// SampleDataFactory.createFromJsonTextFile(workspace,
		// "testUnitTest2.json");
		// SampleDataFactory.createFromJsonTextFile(workspace,
		// "testUnitTest4.json");
		// SampleDataFactory.createFromJsonTextFile(workspace,
		// "testUnitTest5.json");
		// SampleDataFactory.createFromJsonTextFile(workspace,
		// "testUnitTest6.json");
		// SampleDataFactory.createFromJsonTextFile(workspace,
		// "testSampleJsonWithEmptyNestedTable1.json");
		// SampleDataFactory.createFromJsonTextFile(workspace,
		// "createSampleJsonWithNestedTable2.json");
		// SampleDataFactory.createFromJsonTextFile(workspace, "f6.json");
		// SampleDataFactory.createFromJsonTextFile(workspace,
		// "createSampleJsonWithNestedTable2_VD.json");
		// Put all created worksheet models in the view.

		UpdateContainer c = new UpdateContainer();
		c.add(new WorksheetListUpdate());
		
		for (Worksheet w : vwsp.getWorkspace().getWorksheets()) {
			c.append(WorksheetUpdateFactory.createWorksheetHierarchicalUpdates(w.getId())); 
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		c.applyUpdates(vwsp);
		c.generateJson("", pw, vwsp);
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(sw.toString());
	}
}
