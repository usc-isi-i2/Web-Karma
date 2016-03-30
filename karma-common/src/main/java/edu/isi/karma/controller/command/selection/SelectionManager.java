package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionManager {
	private Map<String, List<Selection> > selectionMapping = new ConcurrentHashMap<>();
	public static String defaultCode = "return False";
	
	public void removeSelection(Selection sel) {
		List<Selection> selections = selectionMapping.get(sel.hTableId);
		if (sel != null && selections != null)
			selections.remove(sel);
	}
	
	public List<Selection> getAllDefinedSelection() {
		List<Selection> selections = new ArrayList<>();
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
			selections = new ArrayList<>();
		selections.add(sel);
		selectionMapping.put(hTableId, selections);
	}

}
