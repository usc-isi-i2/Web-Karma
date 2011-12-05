package edu.isi.karma.view.tableheadings;

import java.util.HashMap;

import edu.isi.karma.rep.hierarchicalheadings.ColorKeyTranslator;

public class HeadersColorKeyTranslator implements ColorKeyTranslator {

	private static final HashMap<Integer, String> depthCssMap = new HashMap<Integer, String>();
	static {
		depthCssMap.put(0, "topLevelTableCell");
		depthCssMap.put(1, "table01cell");
		depthCssMap.put(2, "table02cell");
		depthCssMap.put(3, "table03cell");
		depthCssMap.put(4, "table04cell");
		depthCssMap.put(5, "table05cell");
		depthCssMap.put(6, "table06cell");
	}
	
	@Override
	public String getCssTag(String colorKey, int depth) {
		return depthCssMap.get(depth%7);
	}

}
