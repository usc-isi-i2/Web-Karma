package edu.isi.karma.controller.command.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.hp.hpl.jena.sparql.function.library.e;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.CleaningResultUpdate;
import edu.isi.karma.controller.update.FetchResultUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorkspace;

public class FetchTransformingDataCommand extends WorksheetCommand {
	private final String id;
	private final String worksheetId;
	private final String hNodeId;
	public FetchTransformingDataCommand(String id, String worksheetId, String hNodeId)
	{
		super(id,worksheetId);
		this.hNodeId = hNodeId;
		this.id = id;
		this.worksheetId = worksheetId;
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
	
	public HashSet<Integer> obtainIndexs(int size)
	{
		HashSet<Integer> inds = new HashSet<Integer>();
		//select 30% or 50
		int sample_size = (int)(size*0.3);
		if(sample_size >=50)
		{
			sample_size = 50;
		}
		else {
			sample_size = size;
		}
		Random rad = new Random();
		while( inds.size() <sample_size)
		{
			int cand = rad.nextInt(size);
			if(!inds.contains(cand))
			{
				inds.add(cand);
			}
		}
		return inds;
	}
	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet wk = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		// Get the HNode
		HashMap<String, String> rows = new HashMap<String,String>();
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}	
		//random nodes 
		Collection<Node> nodes = new ArrayList<Node>();
		wk.getDataTable().collectNodes(selectedPath, nodes);	
		HashSet<Integer> indSet = this.obtainIndexs(nodes.size());
		int index = 0;
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			Node node = iterator.next();
			if(indSet.contains(index))
			{
				String id = node.getId();
				String originalVal = node.getValue().asString();
				rows.put(id, originalVal);
			}
			index ++;
		}
		return new UpdateContainer(new FetchResultUpdate(hNodeId,rows));
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}
}
