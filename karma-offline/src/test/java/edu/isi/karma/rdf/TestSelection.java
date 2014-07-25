package edu.isi.karma.rdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.Selection.SelectionProperty;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.WorkspaceRegistry;


public class TestSelection {
	private static Logger logger = LoggerFactory.getLogger(TestSelection.class);
	Workspace workspace;
	Worksheet worksheet;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

        KarmaMetadataManager userMetadataManager = new KarmaMetadataManager();
        UpdateContainer uc = new UpdateContainer();
        userMetadataManager.register(new UserPreferencesMetadata(), uc);
        userMetadataManager.register(new UserConfigMetadata(), uc);
        userMetadataManager.register(new PythonTransformationMetadata(), uc);
	}
	
	@Before
	public void setUp() throws Exception {
		workspace = WorkspaceManager.getInstance().createWorkspace();
        WorkspaceRegistry.getInstance().register(new ExecutionController(workspace));
        ModelingConfiguration.load();
        ModelingConfiguration.setManualAlignment(true);
        File file = new File(getClass().getClassLoader().getResource("people.json").toURI());
        InputStream is = new FileInputStream(file);
        Reader reader = EncodingDetector.getInputStreamReader(is, EncodingDetector.detect(file));
		Object json = JSONUtil.createJson(reader);
		JsonImport imp = new JsonImport(json, "people.json", workspace, EncodingDetector.detect(file), 1000);
		worksheet = imp.generateWorksheet();
	}
	@Test
	public void testSelection1() throws IOException {
		Selection sel = new Selection(workspace, worksheet.getId());
		StringBuilder pythonCode = new StringBuilder();
		pythonCode.append("if getValue(\"title\") == \"Prof\": \n");
		pythonCode.append("	 return True \n");
		sel.addSelections(worksheet.getHeaders(), pythonCode.toString());
		for (Entry<Row, SelectionProperty> entry : sel.getSelectedRows().entrySet()) {
			SelectionProperty prop = entry.getValue();
			Row r = entry.getKey();
			assertEquals(r.getNeighborByColumnName("title", workspace.getFactory()).getValue().asString().equals("Prof"), prop.selected);
			logger.debug(r.getNeighborByColumnName("title", workspace.getFactory()).getValue().asString() + " " + prop.selected);
		}
	}
}
