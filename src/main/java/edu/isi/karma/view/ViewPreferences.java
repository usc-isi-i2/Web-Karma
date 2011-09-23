/**
 * 
 */
package edu.isi.karma.view;

/**
 * @author szekely
 * 
 */
public class ViewPreferences {

	/**
	 * Column headings longer than this will be truncated.
	 */
	private int maxCharactersInHeader = 15;
	
	/**
	 * The maximum number of rows to show in the nested tables.
	 */
	private int maxRowsToShowInNestedTables = 5;

	/**
	 * The number of rows to show in top tables when they are first displayed.
	 */
	private int defaultRowsToShowInTopTables = 10;
	
	
	public int getMaxCharactersInHeader() {
		return maxCharactersInHeader;
	}

	public void setMaxCharactersInHeader(int maxCharactersInHeader) {
		this.maxCharactersInHeader = maxCharactersInHeader;
	}

	public int getMaxRowsToShowInNestedTables() {
		return maxRowsToShowInNestedTables;
	}

	public void setMaxRowsToShowInNestedTables(int maxRowsToShowInNestedTables) {
		this.maxRowsToShowInNestedTables = maxRowsToShowInNestedTables;
	}

	public int getDefaultRowsToShowInTopTables() {
		return defaultRowsToShowInTopTables;
	}

	public void setDefaultRowsToShowInTopTables(int defaultRowsToShowInTopTables) {
		this.defaultRowsToShowInTopTables = defaultRowsToShowInTopTables;
	}
	
}
