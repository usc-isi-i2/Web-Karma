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
package edu.isi.karma.controller.command.publish;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishRDFCommand extends Command {
	private final String worksheetId;
	private String rdfSourcePrefix;
	private String rdfSourceNamespace;
	private String addInverseProperties;
	private boolean saveToStore;
	private String hostName;
	private String dbName;
	private String userName;
	private String password;
	private String modelName;
	private String worksheetName;
	private String tripleStoreUrl;
	private String graphUri;
	private boolean replaceContext;
	
	public enum JsonKeys {
		updateType, fileUrl, worksheetId, errorReport
	}

	private static Logger logger = LoggerFactory
			.getLogger(PublishRDFCommand.class);

	public enum PreferencesKeys {
		rdfPrefix, rdfNamespace, addInverseProperties, saveToStore, dbName, hostName, userName, modelName, rdfSparqlEndPoint
	}

	protected PublishRDFCommand(String id, String worksheetId,
			String publicRDFAddress, String rdfSourcePrefix, String rdfSourceNamespace, String addInverseProperties,
			String saveToStore,String hostName,String dbName,String userName,String password, String modelName, String tripleStoreUrl,
			String graphUri, boolean replace) {
		super(id);
		this.worksheetId = worksheetId;
		this.rdfSourcePrefix = rdfSourcePrefix;
		this.rdfSourceNamespace = rdfSourceNamespace;
		this.addInverseProperties = addInverseProperties;
		this.saveToStore=Boolean.valueOf(saveToStore);
		this.hostName=hostName;
		this.dbName=dbName;
		this.userName=userName;
		this.password=password;
		if(modelName==null || modelName.trim().isEmpty())
			this.modelName="karma";
		else
			this.modelName=modelName;
		this.tripleStoreUrl = tripleStoreUrl;
		this.graphUri = graphUri;
		this.replaceContext = replace;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish RDF";
	}

	@Override
	public String getDescription() {
		return this.worksheetName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
		//save the preferences 
		savePreferences(workspace);

		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		this.worksheetName = worksheet.getTitle();
		
		// Prepare the file path and names
		final String rdfFileName = workspace.getCommandPreferencesId() + worksheetId + ".ttl"; 
		final String rdfFileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +  
				"publish/RDF/" + rdfFileName;

		// Get the alignment for this worksheet
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId));
		
		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Please align the worksheet before generating RDF!"));
		}
		
		// Generate the KR2RML data structures for the RDF generation
		final ErrorReport errorReport = new ErrorReport();
		KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(workspace.getOntologyManager(), 
				alignment, worksheet.getSemanticTypes(), rdfSourcePrefix, rdfSourceNamespace, 
				Boolean.valueOf(addInverseProperties), errorReport);
		
		logger.debug(mappingGen.getR2RMLMapping().toString());
		
		KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet, 
				workspace.getFactory(), workspace.getOntologyManager(),
				rdfFileLocalPath, false, mappingGen.getMappingAuxillaryInformation(), errorReport);
		
		// Generate the RDF using KR2RML data structures
		try {
			rdfGen.generateRDF(true);
			logger.info("RDF written to file: " + rdfFileLocalPath);
			if(saveToStore){
				//take the contents of the RDF file and save them to the store
				logger.info("Using Jena DB:" + hostName + "/"+dbName + " user="+userName);
				saveToStore(rdfFileLocalPath);
			}
		} catch (Exception e1) {
			logger.error("Error occured while generating RDF!", e1);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating RDF!"));
		}
		try {
			
			// Get the graph name from properties if empty graph uri 
//			String graphName = worksheet.getMetadataContainer().getWorksheetProperties()
//					.getPropertyValue(Property.graphName);
//			if (this.graphUri == null || this.graphUri.isEmpty()) {
//				// Set to default
//				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
//						Property.graphName, WorksheetProperties.createDefaultGraphName(worksheet.getTitle()));
//				this.graphUri = WorksheetProperties.createDefaultGraphName(worksheet.getTitle());
//			}
			
			if (tripleStoreUrl == null || tripleStoreUrl.isEmpty()) {
				tripleStoreUrl = TripleStoreUtil.defaultDataRepoUrl;
			}
			logger.info("tripleStoreURl : " + tripleStoreUrl);
			TripleStoreUtil utilObj = new TripleStoreUtil();
			boolean result = utilObj.saveToStore(rdfFileLocalPath, tripleStoreUrl, this.graphUri, this.replaceContext);
			if(result) {
				logger.info("Saved rdf to store");
			} else {
				logger.error("Falied to store rdf to karma_data store");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		try {
			return new UpdateContainer(new AbstractUpdate() {
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(), "PublishRDFUpdate");
						outputObject.put(JsonKeys.fileUrl.name(), "publish/RDF/" + rdfFileName);
						outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
						outputObject.put(JsonKeys.errorReport.name(), errorReport.toJSONString());
						pw.println(outputObject.toString(4));
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
		} catch (Exception e) {
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	private void savePreferences(Workspace workspace){
		try{
			JSONObject prefObject = new JSONObject();
			prefObject.put(PreferencesKeys.addInverseProperties.name(), addInverseProperties);
			prefObject.put(PreferencesKeys.rdfPrefix.name(), rdfSourcePrefix);
			prefObject.put(PreferencesKeys.rdfNamespace.name(), rdfSourceNamespace);
			prefObject.put(PreferencesKeys.saveToStore.name(), saveToStore);
			prefObject.put(PreferencesKeys.dbName.name(), dbName);
			prefObject.put(PreferencesKeys.hostName.name(), hostName);
			prefObject.put(PreferencesKeys.modelName.name(), modelName);
			prefObject.put(PreferencesKeys.userName.name(), userName);
			prefObject.put(PreferencesKeys.rdfSparqlEndPoint.name(), tripleStoreUrl);
			workspace.getCommandPreferences().setCommandPreferences(
					"PublishRDFCommandPreferences", prefObject);
			
			/*
			logger.debug("I Saved .....");
			ViewPreferences prefs = vWorkspace.getPreferences();
			JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
			logger.debug("I Saved ....."+prefObject1);
			 */
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void saveToStore(String rdfFileName) throws ClassNotFoundException, IOException {
		String M_DBDRIVER_CLASS = "com.mysql.jdbc.Driver";
		// load the the driver class
		Class.forName(M_DBDRIVER_CLASS);
		
		String dbUrl = "jdbc:mysql://" + hostName + "/" + dbName;
		// create a database connection
		IDBConnection conn = new DBConnection(dbUrl, userName, password, "MySQL");
		
		// create a model maker with the given connection parameters
		ModelMaker maker = ModelFactory.createModelRDBMaker(conn);
		
		ModelRDB model = (ModelRDB) maker.openModel(modelName);
		InputStream file = new FileInputStream(rdfFileName);
		model.read(file,null,"N3");
		file.close();
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
