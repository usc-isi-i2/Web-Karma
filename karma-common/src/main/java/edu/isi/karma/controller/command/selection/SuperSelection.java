package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.rep.Row;

public class SuperSelection {
	private List<Selection> selections;
	public SuperSelection() {
		selections = new ArrayList<Selection>();
	}
	
	public SuperSelection(List<Selection> selections) {
		this.selections = selections;
	}
	
	public void addSelection(Selection sel) {
		selections.add(sel);
	}
	
	public boolean isSelected(Row row) {
		if (selections == null)
			return true;
		boolean ret = false;
		for (Selection sel : selections) {
			ret |= sel.isSelected(row);
		}
		return ret;
	}
}
