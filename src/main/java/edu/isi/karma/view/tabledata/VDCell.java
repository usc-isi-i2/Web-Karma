/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.rep.CellValue;
import edu.isi.karma.view.Stroke;

/**
 * @author szekely
 * 
 */
public class VDCell {

	private String fillHTableId;

	private int depth = -1;

	private CellValue value = null;

	private List<Stroke> topStrokes = new LinkedList<Stroke>();

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
	
	void addTopStroke(Stroke stroke){
		topStrokes.add(stroke);
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
				.key("strokes (top)").value(Stroke.toString(topStrokes))//
		//
		;
	}
}
