/**
 * 
 */
package edu.isi.karma.view;

import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;

/**
 * @author szekely
 * 
 */
public class VTableCssTags {

	private static String cssTags[] = { "table01cell", "table02cell",
			"table03cell", "table04cell", "table05cell", "table06cell",
			"table07cell", "table08cell", "table09cell" };
	static final String CSS_TOP_LEVEL_TABLE_CELL = "topLevelTableCell";

	/**
	 * Map HTable Ids to CSS tags.
	 */
	private Map<String, String> tagMap = new HashMap<String, String>();

	public void registerHTable(String hTableId) {
		if (!tagMap.containsKey(hTableId)) {
			int tagIndex = tagMap.size() % cssTags.length;
			tagMap.put(hTableId, cssTags[tagIndex]);
		}
	}

	public String getCssTag(String hTableId) {
		String result = tagMap.get(hTableId);
		if (result == null){
			result = CSS_TOP_LEVEL_TABLE_CELL;
			//result = "BOGUS-"+hTableId;
		}
		return result;
	}

	public void registerTablesInPath(HNodePath path) {
		HNodePath p = path;
		while (!p.isEmpty()) {
			HNode hn = p.getFirst();
			if (hn.hasNestedTable()) {
				registerHTable(hn.getNestedTable().getId());
			}
			p = p.getRest();
		}
	}
}
