/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.tabledata.VDCell.Position;

/**
 * Store strokes with a none border that define the separators for a cell in
 * VDTableCells. They are defined based on the headers and reused for each cell
 * in the corresponding column in VDTableCells.
 * 
 * @author szekely
 * 
 */
public class VDVerticalSeparator {

	private final ArrayList<Stroke> leftSeparators = new ArrayList<Stroke>();
	private final ArrayList<Stroke> rightSeparators = new ArrayList<Stroke>();

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

	public ArrayList<Stroke> getLeftStrokes() {
		return leftSeparators;
	}

	public ArrayList<Stroke> getRightStrokes() {
		return rightSeparators;
	}

	public ArrayList<Stroke> getStrokes(Position position) {
		switch (position) {
		case left:
			return leftSeparators;
		case right:
			return rightSeparators;
		default:
			return null; // cause caller to crash.
		}
	}

	public int getMinDepth(Position position) {
		int min = Integer.MAX_VALUE;
		for (Stroke l : getStrokes(position)) {
			min = Math.min(min, l.getDepth());
		}
		return min;
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
