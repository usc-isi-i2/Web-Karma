package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.List;

public class SuperSelection {
	private List<Selection> selections;
	public SuperSelection() {
		selections = new ArrayList<Selection>();
	}
	
	public void addSelection(Selection sel) {
		selections.add(sel);
	}
}
