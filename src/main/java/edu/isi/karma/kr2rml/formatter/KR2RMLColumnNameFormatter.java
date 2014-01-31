package edu.isi.karma.kr2rml.formatter;

import java.util.HashMap;
import java.util.Map;

public abstract class KR2RMLColumnNameFormatter {

	protected Map<String, String> formattedColumnNames = new HashMap<String, String>();
	protected Map<String, String> formattingRemovedColumnNames = new HashMap<String, String>();
	
	public String getFormattedColumnName(String columnName) {
		if(!isFormattedColumnNameMemoized(columnName))
		{
			memoizeColumnName(columnName, format(columnName));
		}
		return getMemoizedFormattedColumnName(columnName);
	}

	protected abstract String format(String columnName);

	private void memoizeColumnName(String columnName, String formattedColumnName) {
		
		if(null != columnName)
		{
			formattedColumnNames.put(columnName, formattedColumnName);
		}
		if(null != formattedColumnName)
		{
			formattingRemovedColumnNames.put(formattedColumnName, columnName);
		}		
	}

	private String getMemoizedFormattedColumnName(String columnName) {
		return formattedColumnNames.get(columnName);
	}

	private boolean isFormattedColumnNameMemoized(String columnName) {
		return formattedColumnNames.containsKey(columnName);
	}

	public String getColumnNameWithoutFormatting(String formattedColumnName) {
		if(!isColumnNameWithoutFormattingMemoized(formattedColumnName))
		{
			memoizeColumnName(removeFormatting(formattedColumnName), formattedColumnName);
		}
		return getMemoizedColumnNameWithoutFormatting(formattedColumnName);
	}

	private String getMemoizedColumnNameWithoutFormatting(
			String formattedColumnName) {
		return formattingRemovedColumnNames.get(formattedColumnName);
	}

	private boolean isColumnNameWithoutFormattingMemoized(
			String formattedColumnName) {
		return formattingRemovedColumnNames.containsKey(formattedColumnName);
	}

	protected abstract String removeFormatting(String formattedColumnName);


}
