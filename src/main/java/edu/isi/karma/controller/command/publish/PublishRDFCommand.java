package edu.isi.karma.controller.command.publish;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.jgrapht.graph.DirectedWeightedMultigraph;
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
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rdf.SourceDescription;
import edu.isi.karma.rdf.WorksheetRDFGenerator;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences;

public class PublishRDFCommand extends Command {
	private final String vWorksheetId;
	private String publicRDFAddress;
	private String rdfSourcePrefix;
	private String addInverseProperties;
	private boolean saveToStore;
	private String hostName;
	private String dbName;
	private String userName;
	private String password;
	private String modelName;
	
	public enum JsonKeys {
		updateType, fileUrl, vWorksheetId
	}

	private static Logger logger = LoggerFactory
			.getLogger(PublishRDFCommand.class);

	public enum PreferencesKeys {
		rdfPrefix, addInverseProperties, saveToStore, dbName, hostName, userName, modelName
	}

	protected PublishRDFCommand(String id, String vWorksheetId,
			String publicRDFAddress, String rdfSourcePrefix, String addInverseProperties,
			String saveToStore,String hostName,String dbName,String userName,String password, String modelName) {
		super(id);
		this.vWorksheetId = vWorksheetId;
		this.publicRDFAddress = publicRDFAddress;
		this.rdfSourcePrefix = rdfSourcePrefix;
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
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		
		//save the preferences 
		savePreferences(vWorkspace);

		System.out.println("do it..." + vWorkspace);
		System.out.println("id=..." + vWorksheetId);

		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		final String rdfFileName = "./src/main/webapp/RDF/" + vWorksheetId
				+ ".n3";

		// get alignment for this worksheet
		logger.info("Get alignment for " + vWorksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				vWorkspace.getWorkspace().getId() + ":" + vWorksheetId + "AL");
		if (alignment == null) {
			logger.info("Alignment is NULL for " + vWorksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Please align the worksheet before generating RDF!"));
		}

		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = alignment
				.getSteinerTree();
		Vertex root = alignment.GetTreeRoot();

		try {
			if (root != null) {
				// Write the source description
				// use true to generate a SD with column names (for use
				// "outside" of Karma)
				// use false for internal use
				SourceDescription desc = new SourceDescription(
						vWorkspace.getRepFactory(), tree, root,
						rdfSourcePrefix, Boolean.valueOf(addInverseProperties),false);
				String descString = desc.generateSourceDescription();
				logger.info("SD=" + descString);
				WorksheetRDFGenerator wrg = new WorksheetRDFGenerator(
						vWorkspace.getRepFactory(), descString, rdfFileName);
				if (worksheet.getHeaders().hasNestedTables()) {
					wrg.generateTriplesCell(worksheet);
				} else {
					wrg.generateTriplesRow(worksheet);
				}
				String fileName = "./publish/Source Description/"
						+ worksheet.getTitle() + ".txt";
				FileUtil.writeStringToFile(descString, fileName);
				logger.info("Source description written to file: " + fileName);
				logger.info("RDF written to file: " + rdfFileName);
				if(saveToStore){
					//take the contents of the RDF file and save them to the store
					logger.info("Using Jena DB:" + hostName + "/"+dbName + " user="+userName);
					saveToStore(rdfFileName);
				}
				// //////////////////

			} else {
				return new UpdateContainer(new ErrorUpdate(
						"Alignment returned null root!!"));
			}

			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(),
								"PublishRDFUpdate");
						outputObject.put(JsonKeys.fileUrl.name(),
								publicRDFAddress + vWorksheetId + ".n3");
						outputObject.put(JsonKeys.vWorksheetId.name(),
								vWorksheetId);
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

	private void savePreferences(VWorkspace vWorkspace){
		try{
			JSONObject prefObject = new JSONObject();
			prefObject.put(PreferencesKeys.addInverseProperties.name(), addInverseProperties);
			prefObject.put(PreferencesKeys.rdfPrefix.name(), rdfSourcePrefix);
			prefObject.put(PreferencesKeys.saveToStore.name(), saveToStore);
			prefObject.put(PreferencesKeys.dbName.name(), dbName);
			prefObject.put(PreferencesKeys.hostName.name(), hostName);
			prefObject.put(PreferencesKeys.modelName.name(), modelName);
			prefObject.put(PreferencesKeys.userName.name(), userName);
			vWorkspace.getPreferences().setCommandPreferences(
					"PublishRDFCommandPreferences", prefObject);
			
			System.out.println("I Saved .....");
			ViewPreferences prefs = vWorkspace.getPreferences();
			JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
			System.out.println("I Saved ....."+prefObject1);

			
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
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
