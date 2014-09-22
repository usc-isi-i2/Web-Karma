package edu.isi.karma.controller.command.selection;

import java.util.Map.Entry;

import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Workspace;

public class LargeSelection extends Selection {

	public enum Operation {
		Union, Intersect, Subtract, Invert
	}
	
	private Selection sourceA;
	private Selection sourceB;
	private Operation operation;
	public LargeSelection(Workspace workspace, String worksheetId,
			String hTableId, String name, String superSelectionName, 
			Selection sourceA, Selection sourceB, Operation operation) {
		super(workspace, worksheetId, hTableId, name, superSelectionName);
		this.sourceA = sourceA;
		this.sourceB = sourceB;
		this.operation = operation;
		if (sourceA != null && sourceA.hasSelectedRowsMethod)
			hasSelectedRowsMethod = true;
		if (sourceB != null && sourceB.hasSelectedRowsMethod)
			hasSelectedRowsMethod = true;
		populateSelection();
	}
	
	private void union() {
		if (sourceA.getStatus() == SelectionStatus.OUT_OF_DATE)
			sourceA.updateSelection();
		if (sourceB.getStatus() == SelectionStatus.OUT_OF_DATE)
			sourceB.updateSelection();
		for (Entry<Row, Boolean> entry : sourceA.getCache().entrySet()) {
			Boolean valueB = sourceB.getCache().get(entry.getKey());
			Boolean valueA = entry.getValue();
			if (valueB == null)
				valueB = false;
			selectedRowsCache.put(entry.getKey(), valueB | valueA);
		}
		evalColumns.addAll(sourceA.evalColumns);
		evalColumns.addAll(sourceB.evalColumns);
		selectedRowsColumns.addAll(sourceA.evalColumns);
		selectedRowsColumns.addAll(sourceB.evalColumns);
	}
	
	private void intersect(){
		if (sourceA.getStatus() == SelectionStatus.OUT_OF_DATE)
			sourceA.updateSelection();
		if (sourceB.getStatus() == SelectionStatus.OUT_OF_DATE)
			sourceB.updateSelection();
		for (Entry<Row, Boolean> entry : sourceA.getCache().entrySet()) {
			Boolean valueB = sourceB.getCache().get(entry.getKey());
			Boolean valueA = entry.getValue();
			if (valueB == null)
				selectedRowsCache.put(entry.getKey(), false);
			else
				selectedRowsCache.put(entry.getKey(), valueB & valueA);
		}
		evalColumns.addAll(sourceA.evalColumns);
		evalColumns.addAll(sourceB.evalColumns);
		selectedRowsColumns.addAll(sourceA.evalColumns);
		selectedRowsColumns.addAll(sourceB.evalColumns);
	}
	
	private void subtract(){
		if (sourceA.getStatus() == SelectionStatus.OUT_OF_DATE)
			sourceA.updateSelection();
		if (sourceB.getStatus() == SelectionStatus.OUT_OF_DATE)
			sourceB.updateSelection();
		for (Entry<Row, Boolean> entry : sourceA.getCache().entrySet()) {
			Boolean valueB = sourceB.getCache().get(entry.getKey());
			Boolean valueA = entry.getValue();
			if (valueB == null)
				valueB = false;
			if (valueB == true && valueA == true)
				selectedRowsCache.put(entry.getKey(), false);
			else
				selectedRowsCache.put(entry.getKey(), valueA);
		}
		evalColumns.addAll(sourceA.evalColumns);
		evalColumns.addAll(sourceB.evalColumns);
		selectedRowsColumns.addAll(sourceA.evalColumns);
		selectedRowsColumns.addAll(sourceB.evalColumns);
	}
	
	private void invert(){
		if (sourceA.getStatus() == SelectionStatus.OUT_OF_DATE)
			sourceA.updateSelection();
		for (Entry<Row, Boolean> entry : sourceA.getCache().entrySet()) {
			Boolean valueA = entry.getValue();
			selectedRowsCache.put(entry.getKey(), !valueA);
		}
		evalColumns.addAll(sourceA.evalColumns);
		selectedRowsColumns.addAll(sourceA.evalColumns);
	}

	private void populateSelection(){
		switch(operation) {
		case Intersect:
			intersect();
			break;
		case Invert:
			invert();
			break;
		case Subtract:
			subtract();
			break;
		case Union:
			union();
			break;
		}
	}

	@Override
	public void updateSelection(){
		if (this.status == SelectionStatus.UP_TO_DATE)
			return;
		evalColumns.clear();
		selectedRowsColumns.clear();
		populateSelection();
		this.status = SelectionStatus.UP_TO_DATE;
		
	}

}
