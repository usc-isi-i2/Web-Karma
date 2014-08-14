package edu.isi.karma.controller.command.selection;

import java.util.HashSet;
import java.util.Set;

import edu.isi.karma.controller.command.selection.Selection.RowStatus;
import edu.isi.karma.controller.command.selection.Selection.SelectionStatus;
import edu.isi.karma.rep.Row;

public class SuperSelection {
	private Set<String> selections;
	private String name;
	private SelectionManager selMgr;
	public SuperSelection(String name, SelectionManager selMgr) {
		selections = new HashSet<String>();
		this.name = name;
		this.selMgr = selMgr;
	}
	
	public SuperSelection(String name) {
		selections = new HashSet<String>();
		this.name = name;
	}

	public void addSelection(String hTableId) {
		selections.add(hTableId);
	}

	public RowStatus getSelectedStatus(Row row) {
		for (String s : selections) {
			Selection sel = selMgr.getSelection(s);
			if (sel != null) {
				RowStatus status = sel.getSelectedStatus(row);
				if (status == RowStatus.OUT_OF_DATE || status == RowStatus.SELECTED)
					return status;
			}
		}
		return RowStatus.NOT_SELECTED;
	}

	public boolean isSelected(Row row) {
		boolean ret = false;
		for (String s : selections) {
			Selection sel = selMgr.getSelection(s);
			if (sel != null)
				ret |= sel.isSelected(row);
		}
		return ret;
	}

	public String getName() {
		return name;
	}

	public SelectionStatus refreshStatus() {
		for (String s : selections) {
			Selection sel = selMgr.getSelection(s);
			if (sel != null)
				if (sel.getStatus() == SelectionStatus.OUT_OF_DATE)
					return SelectionStatus.OUT_OF_DATE;
		}
		return SelectionStatus.UP_TO_DATE;
	}

	public void updateSelection() {
		for (String s : selections) {
			Selection sel = selMgr.getSelection(s);
			if (sel != null)
				sel.updateSelection();
		}
	}

	public void removeSelection(String hTableId) {
		selections.remove(hTableId);
	}
}
