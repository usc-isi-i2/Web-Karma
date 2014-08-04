package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.rep.Row;

public class SuperSelection {
	private List<Selection> selections;
	private String name;
	public SuperSelection(String name) {
		selections = new ArrayList<Selection>();
		this.name = name;
	}
	
	public SuperSelection(String name, List<Selection> selections) {
		this.name = name;
		this.selections = selections;
	}
	
	public void addSelection(Selection sel) {
		selections.add(sel);
	}
	
	public boolean isSelected(Row row) {
		if (selections == null)
			return false;
		boolean ret = false;
		for (Selection sel : selections) {
			ret |= sel.isSelected(row);
		}
		return ret;
	}
	
	public String getName() {
		return name;
	}
}
