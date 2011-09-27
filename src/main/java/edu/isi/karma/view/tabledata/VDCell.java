/**
 * 
 */
package edu.isi.karma.view.tabledata;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.CellValue;

/**
 * @author szekely
 * 
 */
public class VDCell {

	private String fillHTableId;

	private int depth = -1;

	private CellValue value = null;

	VDCell() {
		super();
	}

	void setFillHTableId(String fillHTableId) {
		this.fillHTableId = fillHTableId;
	}

	void setDepth(int depth) {
		this.depth = depth;
	}

	void setValue(CellValue value) {
		this.value = value;
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw//
		.key("fillTableId").value(fillHTableId)//
				.key("depth").value(depth)//
				.key("value").value(value == null ? "null" : value.asString())//
		//
		;
	}
}
