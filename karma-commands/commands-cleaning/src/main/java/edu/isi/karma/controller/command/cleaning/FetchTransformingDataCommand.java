package edu.isi.karma.controller.command.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.FetchResultUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class FetchTransformingDataCommand extends WorksheetSelectionCommand {

	private static Logger logger = LoggerFactory
			.getLogger(FetchTransformingDataCommand.class);
	private final String hNodeId;

	public FetchTransformingDataCommand(String id, String worksheetId,
			String hNodeId, String selectionId) {
		super(id, worksheetId, selectionId);
		this.hNodeId = hNodeId;

	}

	@Override
	public String getCommandName() {
		return FetchTransformingDataCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Fetching transforming data";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	public HashSet<Integer> obtainIndexs(int size) {
		HashSet<Integer> inds = new HashSet<Integer>();
		// select 30% or 50
		int sample_size = size;
		if (sample_size >= 500) {
			sample_size = 500;
		} else {
			sample_size = size;
		}
		// Random rad = new Random();
		int cand = 0;
		while (inds.size() < sample_size) {
			// int cand = rad.nextInt(size);
			if (!inds.contains(cand)) {
				inds.add(cand);
			}
			cand++;
		}
		return inds;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet wk = workspace.getWorksheet(worksheetId);
		wk.clearSessionData();
		SuperSelection selection = getSuperSelection(wk);
		String Msg = String.format("begin, Time,%d, Worksheet,%s",
				System.currentTimeMillis(), worksheetId);
		logger.info(Msg);
		// Get the HNode
		HashMap<String, HashMap<String, String>> rows = new HashMap<String, HashMap<String, String>>();
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}
		// random nodes
		Collection<Node> nodes = new ArrayList<Node>();
		wk.getDataTable().collectNodes(selectedPath, nodes, selection);
		HashSet<Integer> indSet = this.obtainIndexs(nodes.size());
		int index = 0;
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			Node node = iterator.next();
			if (indSet.contains(index)) {
				String id = node.getId();
				String originalVal = node.getValue().asString();
				HashMap<String, String> x = new HashMap<String, String>();
				x.put("Org", originalVal);
				x.put("Tar", originalVal);
				x.put("Orgdis", originalVal);
				x.put("Tardis", originalVal);
				rows.put(id, x);
			}
			index++;
		}
		Msg = String.format("end, Time,%d, Worksheet,%s",
				System.currentTimeMillis(), worksheetId);
		logger.info(Msg);
		return new UpdateContainer(new FetchResultUpdate(hNodeId, rows));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}
}
