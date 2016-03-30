package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.WorkspaceKarmaHomeRegistry;

public class FetchR2RMLModelsListCommand extends WorksheetCommand{

	private String TripleStoreUrl;
	private String context;
	private static Logger logger = LoggerFactory.getLogger(FetchR2RMLModelsListCommand.class);

	public FetchR2RMLModelsListCommand(String id, String model, String TripleStoreUrl, String context, String worksheetId) {
		super(id, model, worksheetId);
		this.TripleStoreUrl = TripleStoreUrl;
		this.context = context;
	}

	@Override
	public String getCommandName() {
		return FetchR2RMLModelsListCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Fetch R2RML from Triple Store";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(WorkspaceKarmaHomeRegistry.getInstance().getKarmaHome(workspace.getId()));
		TripleStoreUtil utilObj = new TripleStoreUtil();
		boolean showModelsWithoutMatching = modelingConfiguration.isShowModelsWithoutMatching();
		try {
			HashMap<String, List<String>> metadata = utilObj.getMappingsWithMetadata(TripleStoreUrl, context);
			RepFactory factory = workspace.getFactory();
			List<String> model_Names = metadata.get("model_names");
			List<String> model_Urls = metadata.get("model_urls");
			List<String> model_Times = metadata.get("model_publishtimes");
			List<String> model_Contexts = metadata.get("model_contexts");
			List<String> model_inputColumns = metadata.get("model_inputcolumns");
			final List<JSONObject> list = new ArrayList<>();
			Set<String> worksheetcolumns = new HashSet<>();
			if (worksheetId != null && !worksheetId.trim().isEmpty()) {
				HTable htable = factory.getWorksheet(worksheetId).getHeaders();
				getHNodesForWorksheet(htable, worksheetcolumns, factory);
			}
			Iterator<String> nameitr = model_Names.iterator();
			Iterator<String> urlitr = model_Urls.iterator();
			Iterator<String> timeitr = model_Times.iterator();
			Iterator<String> contextitr = model_Contexts.iterator();
			Iterator<String> inputitr = model_inputColumns.iterator();
			while(nameitr.hasNext() && urlitr.hasNext() && timeitr.hasNext() && contextitr.hasNext() && inputitr.hasNext()) {
				JSONObject obj = new JSONObject();
				Set<String> inputs = new HashSet<>();
				obj.put("name", nameitr.next());
				obj.put("url",  urlitr.next());
				obj.put("publishTime", timeitr.next());
				obj.put("context", contextitr.next());
				String columns = inputitr.next();
				if (columns != null && !columns.isEmpty()) {
					JSONArray array = new JSONArray(columns);
					for (int i = 0; i < array.length(); i++)
						inputs.add(array.get(i).toString());
				}
				else if (showModelsWithoutMatching){
					list.add(obj);
				}
				if (worksheetId != null && !worksheetId.isEmpty()) {		
					inputs.retainAll(worksheetcolumns);
					obj.put("inputColumns", inputs.size());
				}
				else
					obj.put("inputColumns", 0);
				if (!inputs.isEmpty() || (worksheetId == null || worksheetId.trim().isEmpty()))
					list.add(obj);

			}

			Collections.sort(list, new Comparator<JSONObject>() {

				@Override
				public int compare(JSONObject a, JSONObject b) {
					return b.getInt("inputColumns") - a.getInt("inputColumns");
				}
			});


			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					try
					{
						JSONArray array = new JSONArray();
						for (JSONObject obj : list) {
							array.put(obj);
						}
						pw.print(array.toString());
					} catch (Exception e) {
						logger.error("Error generating JSON!", e);
					}
				}
			});
		}
		catch(Exception e) {
			return new UpdateContainer(new ErrorUpdate("Unable to get mappings with metadata: "+ e.getMessage()));
		}
	}

	private void getHNodesForWorksheet(HTable htable, Set<String> hnodes, RepFactory factory) {
		for (HNode hnode : htable.getHNodes()) {
			hnodes.add(hnode.getJSONArrayRepresentation(factory).toString());
			if (hnode.hasNestedTable()) {
				getHNodesForWorksheet(hnode.getNestedTable(), hnodes, factory);
			}
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
