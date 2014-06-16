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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.BloomFilterKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLBloomFilter;
import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.N3KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;
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
	private boolean generateBloomFilters;
	private static Logger logger = LoggerFactory
			.getLogger(PublishRDFCommand.class);

	protected PublishRDFCommand(String id, String worksheetId,
			String publicRDFAddress, String rdfSourcePrefix, String rdfSourceNamespace, String addInverseProperties,
			String saveToStore,String hostName,String dbName,String userName,String password, String modelName, String tripleStoreUrl,
			String graphUri, boolean replace, boolean generateBloomFilters) {
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
		this.generateBloomFilters = generateBloomFilters;
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
		final String rdfFileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.RDF_PUBLISH_DIR) +  
				rdfFileName;

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
		KR2RMLMappingGenerator mappingGen = null;
		String url = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.modelUrl);
		String modelContext = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.modelContext);
		TripleStoreUtil utilObj = new TripleStoreUtil();
		String modelRepoUrl = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.modelRepository);
		modelRepoUrl = modelRepoUrl == null || modelRepoUrl.isEmpty()? TripleStoreUtil.defaultModelsRepoUrl : modelRepoUrl;
		Map<String, String> bloomfilterMapping = new HashMap<String, String>();
		try{
			mappingGen = new KR2RMLMappingGenerator(workspace, worksheet,

					alignment, worksheet.getSemanticTypes(), rdfSourcePrefix, rdfSourceNamespace, 
					Boolean.valueOf(addInverseProperties), errorReport);
		}
		catch (KarmaException e)
		{
			logger.error("Error occured while generating RDF!", e);
			return new UpdateContainer(new ErrorUpdate("Error occured while generating RDF: " + e.getMessage()));
		}

		KR2RMLMapping mapping = mappingGen.getKR2RMLMapping();
		if (url != null && !url.trim().isEmpty() && modelContext != null && !modelContext.trim().isEmpty()) {
			try {
				File tmp = new File("tmp");
				PrintWriter pw = new PrintWriter(tmp);
				pw.println(utilObj.getMappingFromTripleStore(modelRepoUrl, modelContext, url));
				pw.close();
				Model model = WorksheetR2RMLJenaModelParser.loadSourceModelIntoJenaModel(tmp.toURI().toURL());
				tmp.delete();
				R2RMLMappingIdentifier identifier = new R2RMLMappingIdentifier(mapping.getId().getName(), new URL(url));
				WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(model, identifier);
				mapping = parser.parse();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.debug(mapping.toString());

		StringWriter sw = new StringWriter();
		// Generate the RDF using KR2RML data structures
		long start = 0;
		try {
			List<KR2RMLRDFWriter> writers = new ArrayList<KR2RMLRDFWriter>();
			File f = new File(rdfFileLocalPath);
			File parentDir = f.getParentFile();
			parentDir.mkdirs();
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
			writers.add(new N3KR2RMLRDFWriter(new URIFormatter(workspace.getOntologyManager(), errorReport), new PrintWriter (bw)));
			if (generateBloomFilters)
				writers.add(new BloomFilterKR2RMLRDFWriter(new PrintWriter(sw), mapping.getId()));
			KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet, 
					workspace.getFactory(), workspace.getOntologyManager(),
					writers, false, mapping, errorReport);

			rdfGen.generateRDF(true);
			logger.info("RDF written to file: " + rdfFileLocalPath);
			if(saveToStore){
				//take the contents of the RDF file and save them to the store
				logger.info("Using Jena DB:" + hostName + "/"+dbName + " user="+userName);
				saveToStore(rdfFileLocalPath);
			}
			start = System.currentTimeMillis();
			if (generateBloomFilters) {
				JSONObject obj = new JSONObject(sw.toString());
				Set<String> triplemaps = new HashSet<String>(Arrays.asList(obj.getString("ids").split(",")));
				bloomfilterMapping.putAll(utilObj.getBloomFiltersForMaps(modelRepoUrl, modelContext, triplemaps));
				for (String tripleUri : triplemaps) {
					String serializedBloomFilter = obj.getString(tripleUri);
					KR2RMLBloomFilter bf = new KR2RMLBloomFilter();
					bf.populateFromCompressedAndBase64EncodedString(serializedBloomFilter);
					String oldserializedBloomFilter = bloomfilterMapping.get(tripleUri);
					if (oldserializedBloomFilter != null) {
						KR2RMLBloomFilter bf2 = new KR2RMLBloomFilter();
						bf2.populateFromCompressedAndBase64EncodedString(oldserializedBloomFilter);
						bf.or(bf2);
					}
					bloomfilterMapping.put(tripleUri, bf.compressAndBase64Encode());
				}
			}
		} catch (Exception e1) {
			logger.error("Error occured while generating RDF!", e1);
			return new UpdateContainer(new ErrorUpdate("Error occured while generating RDF: " + e1.getMessage()));
		}
		String rdf = null;
		if (generateBloomFilters)
			rdf = toRDF(bloomfilterMapping);
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


			boolean result = utilObj.saveToStore(rdfFileLocalPath, tripleStoreUrl, this.graphUri, this.replaceContext, this.rdfSourceNamespace);
			if (url != null && !url.isEmpty() && url.compareTo("") != 0 && utilObj.testURIExists(TripleStoreUtil.defaultModelsRepoUrl, "", url)) {
				TripleStoreUtil util = new TripleStoreUtil();
				StringBuilder sb = new StringBuilder();
				url = url.trim();
				if(!url.startsWith("<"))
				{
					sb.append("<");
				}
				sb.append(url);
				if(!url.endsWith(">"))
				{
					sb.append(">");
				}
				sb.append(" <");
				sb.append( Uris.MODEL_HAS_DATA_URI);
				sb.append("> \"true\" .\n");
				String input = sb.toString();


				result &= util.saveToStore(input, modelRepoUrl, modelContext, new Boolean(false), this.rdfSourceNamespace);
				if (rdf != null && rdf.trim().compareTo("") != 0)
					result &= util.saveToStore(rdf, modelRepoUrl, modelContext, new Boolean(false), this.rdfSourceNamespace);
				long end = System.currentTimeMillis();
				System.out.println("execution time: " + (end - start) + "node total: " + bloomfilterMapping.size());
			}
			if(result) {
				logger.info("Saved rdf to store");
			} else {
				logger.error("Falied to store rdf to karma_data store");
				return new UpdateContainer(new ErrorUpdate("Error: Failed to store RDF to the triple store"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate("Error occured while generating RDF: " + e.getMessage()));
		}

		try {
			return new UpdateContainer(new AbstractUpdate() {
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(PublishRDFCommandJsonKeys.updateType.name(), "PublishRDFUpdate");
						outputObject.put(PublishRDFCommandJsonKeys.fileUrl.name(), 
								ServletContextParameterMap.getParameterValue(ContextParameter.RDF_PUBLISH_RELATIVE_DIR) + rdfFileName);
						outputObject.put(PublishRDFCommandJsonKeys.worksheetId.name(), worksheetId);
						outputObject.put(PublishRDFCommandJsonKeys.errorReport.name(), errorReport.toJSONString());
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
			prefObject.put(PublishRDFCommandPreferencesKeys.addInverseProperties.name(), addInverseProperties);
			prefObject.put(PublishRDFCommandPreferencesKeys.rdfPrefix.name(), rdfSourcePrefix);
			prefObject.put(PublishRDFCommandPreferencesKeys.rdfNamespace.name(), rdfSourceNamespace);
			prefObject.put(PublishRDFCommandPreferencesKeys.saveToStore.name(), saveToStore);
			prefObject.put(PublishRDFCommandPreferencesKeys.dbName.name(), dbName);
			prefObject.put(PublishRDFCommandPreferencesKeys.hostName.name(), hostName);
			prefObject.put(PublishRDFCommandPreferencesKeys.modelName.name(), modelName);
			prefObject.put(PublishRDFCommandPreferencesKeys.userName.name(), userName);
			prefObject.put(PublishRDFCommandPreferencesKeys.rdfSparqlEndPoint.name(), tripleStoreUrl);
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

	private void saveToStore(String rdfFileName) throws ClassNotFoundException, IOException
	{
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

	private String toRDF(Map<String, String> bloomfilters)
	{
		StringBuilder builder = new StringBuilder();
		for(Entry<String, String> entry : bloomfilters.entrySet())
		{
			String bf = entry.getValue();
			String key = entry.getKey();
			builder.append("<");
			builder.append(key);
			builder.append("> <");
			builder.append(Uris.KM_HAS_BLOOMFILTER);
			builder.append("> \"");
			builder.append(bf);
			builder.append("\" . \n");
		}
		return builder.toString();
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
