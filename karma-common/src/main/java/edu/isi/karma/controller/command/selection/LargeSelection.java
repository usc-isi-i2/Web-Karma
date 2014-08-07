package edu.isi.karma.controller.command.selection;

import java.io.IOException;
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
	LargeSelection(Workspace workspace, String worksheetId,
			String hTableId, String name, 
			Selection sourceA, Selection sourceB, Operation operation) throws IOException {
		super(workspace, worksheetId, hTableId, name);
		this.sourceA = sourceA;
		this.sourceB = sourceB;
		this.operation = operation;
		updateSelection();
	}
	
	private void union() throws IOException {
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
	}
	
	private void intersect() throws IOException {
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
	}
	
	private void subtract() throws IOException {
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
	}
	
	private void invert() throws IOException {
		if (sourceA.getStatus() == SelectionStatus.OUT_OF_DATE)
			sourceA.updateSelection();
		for (Entry<Row, Boolean> entry : sourceA.getCache().entrySet()) {
			Boolean valueA = entry.getValue();
			selectedRowsCache.put(entry.getKey(), !valueA);
		}
		evalColumns.addAll(sourceA.evalColumns);
	}


	@Override
	public void updateSelection() throws IOException {
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

}
