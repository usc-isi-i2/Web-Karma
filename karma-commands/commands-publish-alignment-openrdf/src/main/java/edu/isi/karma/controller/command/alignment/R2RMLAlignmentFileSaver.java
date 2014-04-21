package edu.isi.karma.controller.command.alignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommand.PreferencesKeys;
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
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class R2RMLAlignmentFileSaver implements IAlignmentSaver {

	private static Logger logger = LoggerFactory
			.getLogger(R2RMLAlignmentFileSaver.class);
	private KR2RMLMappingGenerator alignmentMappingGenerator = null;
	private String namespace;
	private String prefix;
	
	public R2RMLAlignmentFileSaver(Workspace workspace) {
		JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject(
				"PublishRDFCommand"+"Preferences");
		if (prefObject != null) {
			namespace = prefObject.getString(PreferencesKeys.rdfNamespace.name());
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
		saveAlignment(alignment, null);
	}
	
	public void saveAlignment(Alignment alignment, String modelFilename) throws Exception {
		long start = System.currentTimeMillis();
		
		String workspaceId = AlignmentManager.Instance().getWorkspaceId(alignment);
		String worksheetId = AlignmentManager.Instance().getWorksheetId(alignment);
		
		Workspace workspace = WorkspaceManager.getInstance().getWorkspace(workspaceId);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		
		if(modelFilename == null)
			modelFilename = workspace.getCommandPreferencesId() + worksheetId + "-" + 
				worksheet.getTitle() +  "-auto-model.ttl"; 
		final String modelFileLocalPath = ServletContextParameterMap.getParameterValue(
				ContextParameter.R2RML_PUBLISH_DIR) +  modelFilename;
		
		long end1 = System.currentTimeMillis();
		logger.info("Time to get alignment info for saving: " + (end1-start) + "msec");
		
		// Generate the KR2RML data structures for the RDF generation
		final ErrorReport errorReport = new ErrorReport();
		alignmentMappingGenerator = new KR2RMLMappingGenerator(workspace, worksheet, alignment, 
				worksheet.getSemanticTypes(), prefix, namespace, true, errorReport);
		long end2 = System.currentTimeMillis();
		logger.info("Time to generate mappings:" + (end2-end1) + "msec");
		
		// Write the model
		writeModel(workspace, workspace.getOntologyManager(), alignmentMappingGenerator, worksheet, modelFileLocalPath);
		logger.info("Alignment for " + workspaceId + ":" + worksheetId + " saved to file: " + modelFileLocalPath);
		long end3 = System.currentTimeMillis();
		logger.info("Time to write to file:" + (end3-end2) + "msec");
	}

	public KR2RMLMapping getMappings() {
		return alignmentMappingGenerator.getKR2RMLMapping();
	}
	
	private void writeModel(Workspace workspace, OntologyManager ontMgr, 
			KR2RMLMappingGenerator mappingGen, Worksheet worksheet, String modelFileLocalPath) 
					throws RepositoryException, FileNotFoundException,
							UnsupportedEncodingException, JSONException {
		File f = new File(modelFileLocalPath);
		File parentDir = f.getParentFile();
		parentDir.mkdirs();
		PrintWriter writer = new PrintWriter(f, "UTF-8");

		KR2RMLMappingWriter mappingWriter = new KR2RMLMappingWriter();
		mappingWriter.addR2RMLMapping(mappingGen.getKR2RMLMapping(), worksheet, workspace);
		mappingWriter.writeR2RMLMapping(writer);
		mappingWriter.close();
		writer.flush();
		writer.close();
	}
}
