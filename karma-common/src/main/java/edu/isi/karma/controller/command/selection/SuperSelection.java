package edu.isi.karma.controller.command.selection;

import java.util.HashSet;
import java.util.Set;

import edu.isi.karma.controller.command.selection.Selection.RowStatus;
import edu.isi.karma.controller.command.selection.Selection.SelectionStatus;
import edu.isi.karma.rep.Row;

public class SuperSelection {
	private Set<Selection> selections;
	private String name;
	
	public SuperSelection(String name) {
		selections = new HashSet<>();
		this.name = name;
	}

	public void addSelection(Selection sel) {
		selections.add(sel);
	}

	public RowStatus getSelectedStatus(Row row) {
		for (Selection sel : selections) {
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
		for (Selection sel : selections) {
			if (sel != null)
				ret |= sel.isSelected(row);
		}
		return ret;
	}

	public String getName() {
		return name;
	}

	public SelectionStatus refreshStatus() {
		for (Selection sel : selections) {
			if (sel != null && sel.getStatus() == SelectionStatus.OUT_OF_DATE)
				return SelectionStatus.OUT_OF_DATE;
		}
		return SelectionStatus.UP_TO_DATE;
	}

	public void updateSelection() {
		for (Selection sel : selections) {
			if (sel != null)
				sel.updateSelection();
		}
	}

	public void removeSelection(Selection sel) {
		selections.remove(sel);
	}
	
	public Selection getSelection(String hTableId) {
		for (Selection sel : selections) {
			if (sel.hTableId.equals(hTableId))
				return sel;
		}
		return null;
	}
	
	public Set<Selection> getAllSelection() {
		return new HashSet<>(selections);
	}
}
