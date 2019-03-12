package edu.isi.karma.controller.command.alignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.common.OSUtils;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommand.PreferencesKeys;
import edu.isi.karma.controller.history.IHistorySaver;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMappingWriter;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingGenerator;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.IAlignmentSaver;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class R2RMLAlignmentFileSaver implements IAlignmentSaver, IHistorySaver {

	private static Logger logger = LoggerFactory
			.getLogger(R2RMLAlignmentFileSaver.class);
	private KR2RMLMappingGenerator alignmentMappingGenerator = null;
	private String namespace;
	private String prefix;
	private Workspace workspace;
	
	public R2RMLAlignmentFileSaver(Workspace workspace) {
		this.workspace = workspace;
		JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject(
				"PublishRDFCommand"+"Preferences");
		if (prefObject != null) {
			if (prefObject.has(PreferencesKeys.rdfNamespace.name()))
				namespace = prefObject.getString(PreferencesKeys.rdfNamespace.name());
			if (prefObject.has(PreferencesKeys.rdfPrefix.name()))
				prefix = prefObject.getString(PreferencesKeys.rdfPrefix.name());
			namespace = ((namespace == null) || (namespace.equals(""))) ? 
					Namespaces.KARMA_DEV : namespace;
			prefix = ((prefix == null) || (prefix.equals(""))) ? 
					Prefixes.KARMA_DEV : prefix;
		} else {
			namespace = Namespaces.KARMA_DEV;
			prefix = Prefixes.KARMA_DEV;
		}
	}
	
	@Override
	public void saveAlignment(Alignment alignment) throws Exception {
		saveAlignment(alignment, null, null, false, null);
	}
	
	public void saveAlignment(Alignment alignment, JSONArray history)  throws Exception {
		saveAlignment(alignment, history, null, false, null);
	}
	
	public void saveAlignment(Alignment alignment, String modelFilename)  throws Exception {
		saveAlignment(alignment, null, modelFilename, false, null);
	}
	
	public void saveAlignment(Alignment alignment, JSONArray history, String modelFilename, boolean onlyHistory) throws Exception {
		saveAlignment(alignment, history, modelFilename, onlyHistory, null);
		
	}

	public void saveAlignment(Alignment alignment, JSONArray history, String modelFilename, boolean onlyHistory, String optionalMappingUri) throws Exception {
		long start = System.currentTimeMillis();
		
		String workspaceId = AlignmentManager.Instance().getWorkspaceId(alignment);
		String worksheetId = AlignmentManager.Instance().getWorksheetId(alignment);
		
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		
		if(modelFilename == null && worksheet != null) {
			modelFilename = getHistoryFilepath(worksheetId);
		} 
		
		long end1 = System.currentTimeMillis();
		logger.info("Time to get alignment info for saving: " + (end1-start) + "msec");
		
		// Generate the KR2RML data structures for the RDF generation
		if (worksheet != null)
			alignmentMappingGenerator = new KR2RMLMappingGenerator(workspace, worksheet, alignment, 
					worksheet.getSemanticTypes(), prefix, namespace, false, history, onlyHistory);
		
		long end2 = System.currentTimeMillis();
		logger.info("Time to generate mappings:" + (end2-end1) + "msec");
		
		// Write the model
		if (modelFilename != null && !modelFilename.trim().isEmpty())
			writeModel(workspace, workspace.getOntologyManager(), alignmentMappingGenerator, worksheet, modelFilename, optionalMappingUri);
		logger.info("Alignment for " + workspaceId + ":" + worksheetId + " saved to file: " + modelFilename + " with optional mapping uri: " + optionalMappingUri);
		long end3 = System.currentTimeMillis();
		logger.info("Time to write to file:" + (end3-end2) + "msec");
	}

	public KR2RMLMapping getMappings() {
		return alignmentMappingGenerator.getKR2RMLMapping();
	}
	
	private void writeModel(Workspace workspace, OntologyManager ontMgr, 
			KR2RMLMappingGenerator mappingGen, Worksheet worksheet, String modelFileLocalPath, String optionalMappingUri) 
					throws RepositoryException, FileNotFoundException,
							UnsupportedEncodingException, JSONException {
		File f = new File(modelFileLocalPath);
		File parentDir = f.getParentFile();
		parentDir.mkdirs();
		PrintWriter writer = new PrintWriter(f, "UTF-8");

		KR2RMLMappingWriter mappingWriter = new KR2RMLMappingWriter();
		mappingWriter.addR2RMLMapping(mappingGen.getKR2RMLMapping(), worksheet, workspace, optionalMappingUri);
		mappingWriter.writeR2RMLMapping(writer);
		mappingWriter.close();
		writer.flush();
		writer.close();
		if(OSUtils.isWindows())
			System.gc(); //Invoke gc for windows, else it gives error: The requested operation cannot be performed on a file with a user-mapped section open
					//when the model is republished, and the original model is earlier open
	}

	@Override
	public String getHistoryFilepath(String worksheetId) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		String modelFilename = workspace.getCommandPreferencesId() + worksheetId + "-" + 
				worksheet.getTitle() +  "-auto-model.ttl"; 
		String modelFileLocalPath = contextParameters.getParameterValue(
				ContextParameter.R2RML_USER_DIR) +  modelFilename;
		return modelFileLocalPath;
	}
	
	@Override
	public void saveHistory(String workspaceId, String worksheetId,
			JSONArray history) throws Exception {
		Alignment alignment = AlignmentManager.Instance().getAlignment(workspaceId, worksheetId);
		this.saveAlignment(alignment, history);
		
	}

	@Override
	public JSONArray loadHistory(String filename) throws Exception {
		File file = new File(filename);
//		String encoding = EncodingDetector.detect(file);
//		String contents = EncodingDetector.getString(file, encoding);
		
		SailRepository myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		SailRepositoryConnection con = myRepository.getConnection();
		con.add(file, "", RDFFormat.TURTLE);
		
		RepositoryResult<Statement> result = con.getStatements(null, new URIImpl("http://isi.edu/integration/karma/dev#hasWorksheetHistory"), null, false);
		if(result.hasNext()) {
			Statement stmt = result.next();
			String history = stmt.getObject().stringValue();
			return new JSONArray(history);
		}
		return new JSONArray();
	}
	
	

}
