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
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.ViewPreferences.ViewPreference;
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
		if (columnNameFull.length() > preferences.getIntViewPreferenceValue(ViewPreference.maxCharactersInHeader)) {
			columnNameShort = JSONUtil.truncateForHeader(columnNameFull,
					preferences.getIntViewPreferenceValue(ViewPreference.maxCharactersInHeader));
		}
		tableCssTags.registerTablesInPath(path);
		return new VColumnHeader(path.toString(), columnNameFull,
				columnNameShort);
	}

	public VWorksheet createVWorksheet(Worksheet worksheet,
			List<HNodePath> columns, List<Row> rows, VWorkspace vWorkspace) {
		String id = getId(ID_TYPE_VW);
		VWorksheet vw = new VWorksheet(id, worksheet, columns, vWorkspace);
		vWorksheets.put(id, vw);
		return vw;
	}

	public VWorksheet getVWorksheet(String vWorksheetId) {
		return vWorksheets.get(vWorksheetId);
	}
}
