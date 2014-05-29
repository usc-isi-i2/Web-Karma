package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
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

public class FetchR2RMLModelsListCommand extends Command{
	
	private String TripleStoreUrl;
	private String context;
	private static Logger logger = LoggerFactory.getLogger(FetchR2RMLModelsListCommand.class);

	public FetchR2RMLModelsListCommand(String id, String TripleStoreUrl, String context) {
		super(id);
		this.TripleStoreUrl = TripleStoreUrl;
		this.context = context;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return FetchR2RMLModelsListCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Fetch R2RML from Triple Store";
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
		TripleStoreUtil utilObj = new TripleStoreUtil();
		try
		{
		final HashMap<String, List<String>> list = utilObj.getMappingsWithMetadata(TripleStoreUrl, context);
		return new UpdateContainer(new AbstractUpdate() {
			
			@Override
			public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
				List<String> model_Names = list.get("model_names");
				List<String> model_Urls = list.get("model_urls");
				List<String> model_Times = list.get("model_publishtimes");
				JSONArray list = new JSONArray();
				try {
					int count = 0;
					while(count < model_Names.size()) {
						JSONObject obj = new JSONObject();
						obj.put("name", model_Names.get(count));
						obj.put("url",  model_Urls.get(count));
						obj.put("publishTime", model_Times.get(count));
						count++;
						list.put(obj);
						
					}
					pw.print(list.toString());
				} catch (Exception e) {
					logger.error("Error generating JSON!", e);
				}
			}
		});
		}
		catch(Exception e)
		{
			return new UpdateContainer(new ErrorUpdate("Unable to get mappings with metadata: "+ e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
