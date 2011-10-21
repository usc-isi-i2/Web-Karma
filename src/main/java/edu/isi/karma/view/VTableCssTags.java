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

	public static boolean useDepthBasedCSSTags = true;
	private static final HashMap<Integer, String> depthCssMap = new HashMap<Integer, String>();
	static {
		depthCssMap.put(0, "topLevelTableCell");
		depthCssMap.put(1, "table01cell");
		depthCssMap.put(2, "table02cell");
		depthCssMap.put(3, "table03cell");
		depthCssMap.put(4, "table04cell");
	}
	
	/**
	 * Map HTable Ids to CSS tags.
	 */
	private Map<String, String> tagMap = new HashMap<String, String>();

	public void registerHTable(String hTableId, int depth) {
		if (!tagMap.containsKey(hTableId)) {
			int tagIndex = tagMap.size() % cssTags.length;
			tagMap.put(hTableId, cssTags[tagIndex]);
		}
	}
	
	public String getCssTag(String hTableId, int depth) {
		if(useDepthBasedCSSTags)
			return depthCssMap.get(depth%5);
		String result = tagMap.get(hTableId);
		if (result == null){
			result = CSS_TOP_LEVEL_TABLE_CELL;
			//result = "BOGUS-"+hTableId;
		}
		return result;
	}

	public void registerTablesInPath(HNodePath path) {
		HNodePath p = path;
		int i = 0;
		while (!p.isEmpty()) {
			HNode hn = p.getFirst();
			if (hn.hasNestedTable()) {
				registerHTable(hn.getNestedTable().getId(), i);
			}
			p = p.getRest();
			i++;
		}
	}
}
