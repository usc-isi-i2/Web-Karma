/**
 * 
 */
package edu.isi.karma.view.tabledata;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * @author szekely
 * 
 */
public class VDCell {

	private String fillHTableId;

	VDCell() {
		super();
	}

	void setFillHTableId(String fillHTableId) {
		this.fillHTableId = fillHTableId;
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw//
				.key("fillTableId").value(fillHTableId)//
		//
		;
	}
}
