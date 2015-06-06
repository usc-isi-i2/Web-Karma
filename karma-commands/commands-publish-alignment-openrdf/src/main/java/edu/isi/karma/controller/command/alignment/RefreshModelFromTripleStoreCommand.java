package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

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
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class RefreshModelFromTripleStoreCommand extends Command{
	private String tripleStoreURL;
	private String context;
	private String mappingURI;
	private static Logger logger = LoggerFactory.getLogger(RefreshModelFromTripleStoreCommand.class);

	public RefreshModelFromTripleStoreCommand(String id, String model, String tripleStoreURL, String context, String mappingURI) {
		super(id, model);
		this.tripleStoreURL = tripleStoreURL;
		this.context = context;
		this.mappingURI = mappingURI;
	}
	
	private enum JsonKeys {
		updateType, messages
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Delete Model";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		boolean urlExists = true;
		UpdateContainer uc = new UpdateContainer();
		try {
			URL url = new URL(mappingURI);
			Scanner in = new Scanner(url.openStream());
			if (!in.hasNext())
				urlExists = false;
			in.close();
		} catch (Exception e) {
			urlExists = false;
		}
		if (!urlExists) {
			uc.add(new AbstractUpdate() {
				public void generateJson(String prefix, PrintWriter pw,	
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(), "RefreshModel");
						outputObject.put(JsonKeys.messages.name(), "URL broken");
						//.put(JsonKeys.fileUrl.name(), graphUrl);
						pw.println(outputObject.toString());
					} catch (JSONException e) {
						//e.printStackTrace();
						logger.error("Error occured while generating JSON!");
					}
				}
			});
			return uc;
		}
		TripleStoreUtil util = new TripleStoreUtil();
		try {
			util.deleteMappingFromTripleStore(tripleStoreURL, context, mappingURI);
			SaveR2RMLModelCommandFactory scf = new SaveR2RMLModelCommandFactory();
			SaveR2RMLModelCommand command = scf.createCommand(model, workspace, mappingURI, tripleStoreURL, context, "URL");
			command.doIt(workspace);
		} catch (KarmaException e) {
			return new UpdateContainer(new ErrorUpdate("Error occured while deleting R2RML model!"));
		}
		
		uc.add(new AbstractUpdate() {
			public void generateJson(String prefix, PrintWriter pw,	
					VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(), "RefreshModel");
					outputObject.put(JsonKeys.messages.name(), "Successful");
					pw.println(outputObject.toString());
				} catch (JSONException e) {
					//e.printStackTrace();
					logger.error("Error occured while generating JSON!");
				}
			}
		});
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
