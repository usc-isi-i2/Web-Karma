package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SelectionManager {
	private Map<String, List<Selection> > selectionMapping = new HashMap<String, List<Selection> >();
	
	public void addSelection(Selection sel) {
		String hTableId = sel.getHTableId();
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections == null)
			selections = new ArrayList<Selection>();
		selections.add(sel);
		selectionMapping.put(hTableId, selections);
	}
	
	public void removeSelection(Selection sel) {
		String hTableId = sel.getHTableId();
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections == null)
			return;
		if (selections.remove(sel))
			selectionMapping.put(hTableId, selections);
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

}
