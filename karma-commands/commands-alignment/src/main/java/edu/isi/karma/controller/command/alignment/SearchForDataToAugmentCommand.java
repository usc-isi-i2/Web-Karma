package edu.isi.karma.controller.command.alignment;

import java.util.HashMap;
import java.util.List;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class SearchForDataToAugmentCommand extends Command{
	private String tripleStoreUrl;
	private String context;
	private String nodeType;
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
		OntologyManager ontMgr = workspace.getOntologyManager();
		Label label = ontMgr.getUriLabel(nodeUri);
		nodeType = label.getPrefix() + ":";
		nodeType += nodeUri.substring(nodeUri.lastIndexOf("/") + 1);
		UpdateContainer uc = new UpdateContainer();
		TripleStoreUtil util = new TripleStoreUtil();
		HashMap<String, List<String>> result = null;
		try {
			result = util.getPredicatesForTriplesMapsWithSameClass(tripleStoreUrl, context, nodeType);
		} catch (KarmaException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
