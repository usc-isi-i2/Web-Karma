package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class SearchForDataToAugmentCommand extends Command{
	private String tripleStoreUrl;
	private String context;
	private String nodeUri;
	public SearchForDataToAugmentCommand(String id, String url, String context, String nodeUri) {
		super(id);
		this.tripleStoreUrl = url;
		this.context = context;
		this.nodeUri = nodeUri;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Search For Data To Augment";
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
		// TODO Auto-generated method stub
		
		TripleStoreUtil util = new TripleStoreUtil();
		HashMap<String, List<String>> result = null;
		nodeUri = nodeUri.trim();
		nodeUri = "<" + nodeUri + ">";
		try {
			result = util.getPredicatesForTriplesMapsWithSameClass(tripleStoreUrl, context, nodeUri);
		} catch (KarmaException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		final JSONArray array = new JSONArray();
		List<String> triplesMaps = result.get("triplesMaps");
		List<String> predicate = result.get("predicate");
		for (int i = 0; i < predicate.size(); i++) {
			JSONObject obj = new JSONObject();
			obj.put("predicate", predicate.get(i));
			obj.put("tripleMap", triplesMaps.get(i));
			array.put(obj);
		}
		return new UpdateContainer(new AbstractUpdate() {
			
			@Override
			public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
				pw.print(array.toString());
			}
		});
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
