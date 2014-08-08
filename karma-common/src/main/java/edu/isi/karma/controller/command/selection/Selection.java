package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Workspace;

public abstract class Selection {
	
	public enum SelectionStatus {
		UP_TO_DATE, OUT_OF_DATE
	}
	
	public enum Tag {
		IGNORE_IN_PUBLISH_RDF, IGNORE_IN_JSON_EXPORT, 
		IGNORE_IN_SERVICE_INVOCATION, IGNORE_IN_WORKSHEET_TRANSFORMATION
	}
	
	protected SelectionStatus status;
	protected Workspace workspace;
	protected String worksheetId;
	protected List<Tag> tags = new ArrayList<Tag>();
	protected String hTableId;
	protected String Id;
	protected Map<Row, Boolean> selectedRowsCache;
	protected Set<String> evalColumns;

	Selection(Workspace workspace, String worksheetId, 
			String hTableId, String name) {
		this.worksheetId = worksheetId;
		this.workspace = workspace;
		this.hTableId = hTableId;
		this.Id = name;
		this.status = SelectionStatus.UP_TO_DATE;
		selectedRowsCache = new HashMap<Row, Boolean>();
		evalColumns = new HashSet<String>();
	}
		
	public void setTags(List<Tag> tags) {
		this.tags.clear();
		this.tags.addAll(tags);
	}
		
	public String getHTableId() {
		return hTableId;
	}
	
	public String getId() {
		return Id;
	}
	
	public void invalidateSelection() {
		this.status = SelectionStatus.OUT_OF_DATE;
	}
	
	public SelectionStatus getStatus() {
		return status;
	}
	
	public List<Tag> getTags() {
		return tags;
	}
	
	public Map<Row, Boolean> getCache() {
		return selectedRowsCache;
	}
	
	public boolean isSelected(Row row) {
		Boolean prop = selectedRowsCache.get(row);
		if (prop == null)
			return false;
		return prop;
	}
	
	public abstract void updateSelection();
	
	public Set<String> getInputColumns() {
		return evalColumns;
	}
}
