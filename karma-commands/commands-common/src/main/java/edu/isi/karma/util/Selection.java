package edu.isi.karma.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;

public class Selection {
	public enum SelectionStatus {
		UP_TO_DATE, OUT_OF_DATE
	}
	
	public enum Tag {
		IGNORE_IN_PUBLISH_RDF, IGNORE_IN_JSON_EXPORT, 
		IGNORE_IN_SERVICE_INVOCATION, IGNORE_IN_WORKSHEET_TRANSFORMATION
	}
	
	private class SelectionProperty {
		public Boolean selected;
		public String pythonCode;
		public SelectionProperty(boolean selected, String pythonCode) {
			this.selected = selected;
			this.pythonCode = pythonCode;
		}
	}
	
	private SelectionStatus status;
	private List<Tag> tags = new ArrayList<Tag>();
	private Map<Row, SelectionProperty> selectedRows = new HashMap<Row, SelectionProperty>();
	
	public void addSelections(Worksheet worksheet, HTable htable, String pythonCode) {
		List<Table> tables = new ArrayList<Table>();
		CloneTableUtils.getDatatable(worksheet.getDataTable(), htable, tables);
		for (Table t : tables) {
			for (Row r : t.getRows(0, t.getNumRows())) {
				selectedRows.put(r, new SelectionProperty(evaluatePythonExpression(r, pythonCode), pythonCode));
			}
		}
	}
	
	public Map<Row, SelectionProperty> getSelectedRows() {
		return selectedRows;
	}
	
	public void Intersect(Selection source) {
		for (Entry<Row, SelectionProperty> entry : this.selectedRows.entrySet()) {
			Row key = entry.getKey();
			SelectionProperty value = entry.getValue();
			if (source.getSelectedRows().containsKey(key)) {
				value.selected = value.selected & source.selectedRows.get(key).selected;
			}
			else
				value.selected = false;
		}
	}
	
	public void Subtract(Selection source) {
		for (Entry<Row, SelectionProperty> entry : this.selectedRows.entrySet()) {
			Row key = entry.getKey();
			SelectionProperty value = entry.getValue();
			if (source.getSelectedRows().containsKey(key) && value.selected) {
				value.selected = value.selected ^ source.selectedRows.get(key).selected;
			}
		}
	}
	
	public void Invert() {
		for (Entry<Row, SelectionProperty> entry : this.selectedRows.entrySet()) {
			SelectionProperty value = entry.getValue();
			value.selected = !value.selected;
		}
	}
	
	public void setTags(List<Tag> tags) {
		this.tags.clear();
		this.tags.addAll(tags);
	}
	
	public void updateSelection() {
		for (Entry<Row, SelectionProperty> entry : this.selectedRows.entrySet()) {
			Row key = entry.getKey();
			SelectionProperty value = entry.getValue();
			value.selected = evaluatePythonExpression(key, value.pythonCode);
		}
	}
	
	
	private boolean evaluatePythonExpression(Row r, String pythonCode) {
		return false;
	}
	
	
}
