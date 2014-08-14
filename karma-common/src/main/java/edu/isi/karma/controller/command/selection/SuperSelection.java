package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.controller.command.selection.Selection.SelectionStatus;
import edu.isi.karma.rep.Row;

public class SuperSelection {
	private List<Selection> selections;
	private String name;
	public SuperSelection(String name) {
		selections = new ArrayList<Selection>();
		this.name = name;
	}
	
	public void addSelection(Selection sel) {
		selections.add(sel);
	}
	
	public boolean isSelected(Row row) {
		boolean ret = false;
		for (Selection sel : selections) {
			ret |= sel.isSelected(row);
		}
		return ret;
	}
	
	public String getName() {
		return name;
	}
	
	public SelectionStatus refreshStatus() {
		for (Selection sel : selections) {
			if (sel.getStatus() == SelectionStatus.OUT_OF_DATE)
				return SelectionStatus.OUT_OF_DATE;
		}
		return SelectionStatus.UP_TO_DATE;
	}
	
	public void updateSelection() {
		for (Selection sel : selections) {
			sel.updateSelection();
		}
	}
}
