/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;

/**
 * Store strokes with a none border that define the separators for a cell in
 * VDTableCells. They are defined based on the headers and reused for each cell
 * in the corresponding column in VDTableCells.
 * 
 * @author szekely
 * 
 */
public class VDVerticalSeparator {

	private final List<Stroke> leftSeparators = new LinkedList<Stroke>();
	private final List<Stroke> rightSeparators = new LinkedList<Stroke>();

	public VDVerticalSeparator() {
		super();
	}

	public void add(int depth, String hTableId) {
		Stroke s = new Stroke(StrokeStyle.none, hTableId, depth);
		leftSeparators.add(s);
		rightSeparators.add(s);
	}

	public void addLeft(int depth, String hTableId) {
		Stroke s = new Stroke(StrokeStyle.none, hTableId, depth);
		leftSeparators.add(s);
	}

	public void addRight(int depth, String hTableId) {
		Stroke s = new Stroke(StrokeStyle.none, hTableId, depth);
		rightSeparators.add(s);
	}

	public List<Stroke> getLeftSeparators() {
		return leftSeparators;
	}

	public List<Stroke> getRightSeparators() {
		return rightSeparators;
	}

	public void addLeft(List<Stroke> list) {
		leftSeparators.addAll(list);
	}

	public void addRight(List<Stroke> list) {
		rightSeparators.addAll(list);
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("left").value(Stroke.toString(leftSeparators))//
				.key("right").value(Stroke.toString(rightSeparators))//
				.endObject();
	}
}
