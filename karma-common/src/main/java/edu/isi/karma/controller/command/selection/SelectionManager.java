package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.rep.Workspace;

public class SelectionManager {
	private Map<String, List<Selection> > selectionMapping = new HashMap<String, List<Selection> >();
	
	public boolean createSelection(Workspace workspace, String worksheetId, 
			String hTableId, String name) {
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections != null) {
			for (Selection s : selections) {
				if (s.getId().equals(name))
					return false;
			}
		}		
		Selection sel = new Selection(workspace, worksheetId, hTableId, name);
		addSelection(sel);
		return true;
	}
	
	public Selection createSelection(Selection selection) {	
		Selection sel = selection.clone();
		addSelection(sel);
		return sel;
	}
	
	public Selection getSelection(String hTableId, String name) {
		if (name == null || name.trim().isEmpty())
			return null;
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections != null) {
			for (Selection s : selections) {
				if (s.getId().equals(name))
					return s;
			}
		}	
		return null;
	}
	
	public boolean removeSelection(String hTableId, String name) {
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections != null) {
			Iterator<Selection> selItr = selections.iterator();
			while (selItr.hasNext()) {
				Selection sel = selItr.next();
				if (sel.getId().equals(name)) {
					selItr.remove();
					return true;
				}
			}
		}
		return false;
	}
	
	public List<Selection> getAllDefinedSelection(String hTableId) {
		List<Selection> list = new ArrayList<Selection>();
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections != null) {
			list.addAll(selections);
		}
		return list;
	}
	
	public List<Selection> getAllDefinedSelection() {
		List<Selection> selections = new ArrayList<Selection>();
		for (Entry<String, List<Selection>> entry : selectionMapping.entrySet()) {
			List<Selection> sels = entry.getValue();
			if (sels != null)
				selections.addAll(sels);
		}
		return selections;
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
