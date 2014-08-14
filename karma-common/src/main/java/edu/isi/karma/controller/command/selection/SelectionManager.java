package edu.isi.karma.controller.command.selection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import edu.isi.karma.controller.command.selection.LargeSelection.Operation;
import edu.isi.karma.rep.Workspace;

public class SelectionManager {
	private Map<String, List<Selection> > selectionMapping = new ConcurrentHashMap<String, List<Selection> >();
	private Map<String, Selection> currentSelectionMapping = new ConcurrentHashMap<String, Selection>();
	public static String defaultCode = "return False";
	public Selection createMiniSelection(Workspace workspace, String worksheetId, 
			String hTableId, String pythonCode) {
		try {
			Selection sel = new MiniSelection(workspace, worksheetId, hTableId, workspace.getFactory().getNewId("SEL"), pythonCode);
			addSelection(sel);
			return sel;
		} catch (IOException e) {
			return null;
		}
		
	}
	
	public Selection createLargeSelection(Selection selectionA, Selection selectionB, Operation op){	
		if (!selectionA.hTableId.equals(selectionB.hTableId) || 
				!selectionA.worksheetId.equals(selectionB.worksheetId) ||
				!selectionA.workspace.equals(selectionB.workspace))
			return null;
		Workspace workspace = selectionA.workspace;		
		try {
			Selection sel = new LargeSelection(workspace, 
					selectionA.worksheetId, selectionA.hTableId, 
					 workspace.getFactory().getNewId("SEL"), selectionA, selectionB, op);
			addSelection(sel);
			return sel;
		} catch (IOException e) {
			return null;
		}
	}
	
	public Selection getSelection(String hTableId) {
		return currentSelectionMapping.get(hTableId);
	}
	
	public void removeSelection(String hTableId) {
		List<Selection> selections = selectionMapping.get(hTableId);
		Selection cur = currentSelectionMapping.get(hTableId);
		if (cur != null && selections != null)
			selections.remove(cur);
	}
	
	public List<Selection> getDefinedSelection(String hTableId) {
		List<Selection> list = new ArrayList<Selection>();
		Selection cur = currentSelectionMapping.get(hTableId);
		if (cur != null)
			list.add(cur);
		return list;
	}
	
	public List<Selection> getDefinedSelection() {
		List<Selection> selections = new ArrayList<Selection>();
		for (Entry<String, Selection> entry : currentSelectionMapping.entrySet()) {
			Selection sel = entry.getValue();
			if (sel != null)
				selections.add(sel);
		}
		return selections;
	}
	
	public Selection updateCurrentSelection(String hTableId, Selection sel) {
		if (sel == null) {
			currentSelectionMapping.remove(hTableId);
			return null;
		}
		return currentSelectionMapping.put(hTableId, sel);
	}
	
	private void addSelection(Selection sel) {
		String hTableId = sel.getHTableId();
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections == null)
			selections = new ArrayList<Selection>();
		selections.add(sel);
		selectionMapping.put(hTableId, selections);
	}

}
