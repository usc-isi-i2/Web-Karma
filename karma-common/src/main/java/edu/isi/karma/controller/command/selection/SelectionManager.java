package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import edu.isi.karma.controller.command.selection.LargeSelection.Operation;
import edu.isi.karma.rep.Workspace;

public class SelectionManager {
	private Map<String, List<Selection> > selectionMapping = new ConcurrentHashMap<String, List<Selection> >();
	public static String defaultCode = "return False";
	public Selection createMiniSelection(Workspace workspace, String worksheetId, 
			String hTableId, String pythonCode, boolean onError) {
		Selection sel = new MiniSelection(workspace, worksheetId, hTableId, workspace.getFactory().getNewId("SEL"), pythonCode, onError);
		addSelection(sel);
		return sel;		
	}
	
	public Selection createLargeSelection(Selection selectionA, Selection selectionB, Operation op){
		if (selectionB != null)
			if (!selectionA.hTableId.equals(selectionB.hTableId) || 
				!selectionA.worksheetId.equals(selectionB.worksheetId) ||
				!selectionA.workspace.equals(selectionB.workspace))
			return null;
		if (selectionB == null && op != Operation.Invert)
			return null;
		Workspace workspace = selectionA.workspace;		
		Selection sel = new LargeSelection(workspace, 
				selectionA.worksheetId, selectionA.hTableId, 
				 workspace.getFactory().getNewId("SEL"), selectionA, selectionB, op);
		addSelection(sel);
		return sel;
	}
	
	public void removeSelection(Selection sel) {
		List<Selection> selections = selectionMapping.get(sel.hTableId);
		if (sel != null && selections != null)
			selections.remove(sel);
	}
	
	public List<Selection> getAllDefinedSelection() {
		List<Selection> selections = new ArrayList<Selection>();
		for (Entry<String, List<Selection> > entry : selectionMapping.entrySet()) {
			List<Selection> sels = entry.getValue();
			if (sels != null)
				selections.addAll(sels);
		}
		return selections;
	}
	
	public void addSelection(Selection sel) {
		String hTableId = sel.getHTableId();
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections == null)
			selections = new ArrayList<Selection>();
		selections.add(sel);
		selectionMapping.put(hTableId, selections);
	}

}
