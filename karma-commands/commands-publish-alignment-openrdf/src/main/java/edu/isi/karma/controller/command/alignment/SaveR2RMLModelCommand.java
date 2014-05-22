package edu.isi.karma.controller.command.alignment;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;


public class SaveR2RMLModelCommand extends Command{

	private String modelUrl;
	private String tripleStoreUrl;
	private String graphContext;
	private String worksheetId;
	private String collection;
	private final String graphBaseUrl = "http://localhost/worksheets/";
	private static Logger logger = LoggerFactory.getLogger(SaveR2RMLModelCommand.class);

	protected SaveR2RMLModelCommand(String id, String worksheetId, String modelUrl, String url, String context, String collection) {
		super(id);
		this.modelUrl = modelUrl;
		this.tripleStoreUrl = url;
		this.graphContext = context;
		this.worksheetId = worksheetId;
		this.collection = collection;
	}


	public enum JsonKeys {
		updateType, fileUrl, worksheetId
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Save R2RML Model";
	}

	@Override
	public String getDescription() {
		return "Save " + modelUrl;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		UpdateContainer uc = new UpdateContainer();
		if (collection.compareTo("Collection") == 0) {
			try {
				URL url = new URL(modelUrl);
				Scanner in = new Scanner(url.openStream());
				JSONArray array = new JSONArray(in.nextLine());
				in.close();
				boolean result = true;
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					String modelUrl = obj.getString("url");
					String filename = modelUrl.substring(modelUrl.indexOf('-') + 1);
					int location = filename.indexOf("-auto-model.ttl");
					if (location == -1)
						location = filename.indexOf("-model.ttl");
					filename = filename.substring(0, location);
					result &= saveMapping(worksheet, modelUrl, graphBaseUrl + filename);
					//System.out.println("here: " + graphBaseUrl + filename);
				}
				if (result) {
					logger.info("Saved collection to triple store");
					uc.add(new AbstractUpdate() {
						public void generateJson(String prefix, PrintWriter pw,	
								VWorkspace vWorkspace) {
							JSONObject outputObject = new JSONObject();
							try {
								outputObject.put(JsonKeys.updateType.name(), "SaveCollection");								
								pw.println(outputObject.toString());
							} catch (JSONException e) {
								e.printStackTrace();
								logger.error("Error occured while generating JSON!");
							}
						}
					});
					return uc;
				}
			}catch(Exception e) {
				System.out.println("here");
				e.printStackTrace();
				logger.error("Error occured while generating R2RML Model!");
				return new UpdateContainer(new ErrorUpdate("Error occured while generating R2RML model!"));
			}
			
		}
		else {
			boolean result = saveMapping(worksheet, modelUrl, graphContext);
			if (result) {
				logger.info("Saved model to triple store");
				uc.add(new AbstractUpdate() {
					public void generateJson(String prefix, PrintWriter pw,	
							VWorkspace vWorkspace) {
						JSONObject outputObject = new JSONObject();
						try {
							outputObject.put(JsonKeys.updateType.name(), "PublishR2RMLUpdate");

							outputObject.put(JsonKeys.fileUrl.name(), modelUrl);
							outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
							pw.println(outputObject.toString());
						} catch (JSONException e) {
							e.printStackTrace();
							logger.error("Error occured while generating JSON!");
						}
					}
				});
				return uc;
			}
		}
		logger.error("Error occured while generating R2RML Model!");
		return new UpdateContainer(new ErrorUpdate("Error occured while generating R2RML model!"));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	private boolean saveMapping(Worksheet worksheet, String modelUrl, String graphContext) {
		try {
			TripleStoreUtil utilObj = new TripleStoreUtil();
			String graphName = (graphContext.compareTo("") == 0) ? worksheet.getMetadataContainer().getWorksheetProperties()
					.getPropertyValue(Property.graphName) : graphContext;
					if (graphName == null || graphName.isEmpty()) {
						// Set to default
						worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
								Property.graphName, WorksheetProperties.createDefaultGraphName(worksheet.getTitle()));
						graphName = WorksheetProperties.createDefaultGraphName(worksheet.getTitle());
					}
					URL url = new URL(modelUrl);
					File file = new File("tmp.ttl");	
					FileUtils.copyURLToFile(url, file);
					boolean result = utilObj.saveToStore(file, tripleStoreUrl, graphName, true, null);
					return result;
		}catch (Exception e) {
			return false;
		}
	}
}
