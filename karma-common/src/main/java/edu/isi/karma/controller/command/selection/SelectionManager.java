package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public List<String> getAllDefinedSelection(String hTableId) {
		List<String> list = new ArrayList<String>();
		List<Selection> selections = selectionMapping.get(hTableId);
		if (selections != null) {
			for (Selection sel : selections) {
				list.add(sel.getId());
			}
		}
		return list;
	}

}
