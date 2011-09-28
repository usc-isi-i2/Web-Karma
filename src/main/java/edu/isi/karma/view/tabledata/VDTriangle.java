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
public class VDTriangle {

	enum TriangleLocation {
		topLeft, bottomLeft, topRight, bottomRight
	}

	private final String hTableId;
	private final String rowId;
	private final int depth;

	private final TriangleLocation location;

	VDTriangle(String hTableId, String rowId, int depth,
			TriangleLocation location) {
		super();
		this.hTableId = hTableId;
		this.rowId = rowId;
		this.depth = depth;
		this.location = location;
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/
	
	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("location").value(location)//
				.key("depth").value(depth)//
				.key("hTableId").value(hTableId)//
				.key("rowId").value(rowId)//
				.endObject();
	}

}
