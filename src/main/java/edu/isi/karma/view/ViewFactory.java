/**
 * 
 */
package edu.isi.karma.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.tableheadings.VColumnHeader;

/**
 * @author szekely
 * 
 */
public class ViewFactory {

	private static final String ID_TYPE_VW = "VW";

	public ViewFactory() {
	}

	private int nextId = 1;

	private Map<String, VWorksheet> vWorksheets = new HashMap<String, VWorksheet>();

	/**
	 * Maps table Ids to CSS tags. By putting it here the same table type will
	 * have the same color in all worksheets.
	 */
	private final VTableCssTags tableCssTags = new VTableCssTags();

	private String getId(String prefix) {
		return prefix + (nextId++);
	}

	public VTableCssTags getTableCssTags() {
		return tableCssTags;
	}

	VColumnHeader createVColumnHeader(HNodePath path,
			ViewPreferences preferences) {
		HNode hn = path.getLeaf();
		String columnNameFull = hn.getColumnName();
		String columnNameShort = columnNameFull;
		if (columnNameFull.length() > preferences.getMaxCharactersInHeader()) {
			columnNameShort = Util.truncateForHeader(columnNameFull,
					preferences.getMaxCharactersInHeader());
		}
		tableCssTags.registerTablesInPath(path);
		return new VColumnHeader(path.toString(), columnNameFull,
				columnNameShort);
	}

	public VWorksheet createVWorksheet(Worksheet worksheet,
			List<HNodePath> columns, List<Row> rows, ViewPreferences preferences) {
		String id = getId(ID_TYPE_VW);
		VWorksheet vw = new VWorksheet(id, worksheet, columns, preferences,
				this);
		vWorksheets.put(id, vw);
		return vw;
	}

	public VWorksheet getVWorksheet(String vWorksheetId) {
		return vWorksheets.get(vWorksheetId);
	}
}
