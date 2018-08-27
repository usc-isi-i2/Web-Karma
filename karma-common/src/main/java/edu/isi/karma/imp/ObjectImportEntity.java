package edu.isi.karma.imp;

import java.util.List;

public class ObjectImportEntity {

	protected List<String> columns;
	protected List<String> literalValues;
	protected List<String> nestedValues;
	public ObjectImportEntity(List<String> columns, List<String> literalValues, List<String> nestedValues) {
		super();
		this.columns = columns;
		this.literalValues = literalValues;
		this.nestedValues = nestedValues;
	}
	public List<String> getColumns() {
		return columns;
	}
	public List<String> getLiteralValues() {
		return literalValues;
	}
	public List<String> getNestedValues() {
		return nestedValues;
	}
	
}
