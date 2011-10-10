/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.tabledata.VDCell.Position;

/**
 * @author szekely
 * 
 */
public class VDCellStrokes {

	private int[] minDepth = new int[4];

	/**
	 * First dimension is Position.ordinal(), the second is the stroke depth
	 * minus minDepth.
	 */
	private Stroke[][] strokes = new Stroke[4][];

	public static VDCellStrokes create(VDVerticalSeparator vdVS, int numTop,
			int minTop, int numBottom, int minBottom) {

		int numLeft = vdVS.getLeftStrokes().size();
		int minLeft = vdVS.getMinDepth(Position.left);

		int numRight = vdVS.getRightStrokes().size();
		int minRight = vdVS.getMinDepth(Position.right);

		return new VDCellStrokes(numLeft, numRight, numTop, numBottom, minLeft,
				minRight, minTop, minBottom);
	}

	VDCellStrokes(int numLeft, int numRight, int numTop, int numBottom,
			int minLeft, int minRight, int minTop, int minBottom) {
		minDepth[Position.left.ordinal()] = minLeft;
		minDepth[Position.right.ordinal()] = minRight;
		minDepth[Position.top.ordinal()] = minTop;
		minDepth[Position.bottom.ordinal()] = minBottom;

		int[] numStrokes = new int[4];
		numStrokes[Position.left.ordinal()] = numLeft;
		numStrokes[Position.right.ordinal()] = numRight;
		numStrokes[Position.top.ordinal()] = numTop;
		numStrokes[Position.bottom.ordinal()] = numBottom;
		for (Position pos : Position.values()) {
			strokes[pos.ordinal()] = new Stroke[numStrokes[pos.ordinal()]];
		}
	}

	private void addStroke(Stroke stroke, Position position) {
		strokes[position.ordinal()][stroke.getDepth()
				- minDepth[position.ordinal()]] = stroke;
	}

	private void addStrokes(List<Stroke> strokes, Position position) {
		for (Stroke s : strokes) {
			addStroke(s, position);
		}
	}

	public void populateFromVDCell(VDVerticalSeparator vdVerticalSeparator,
			VDCell c) {
		// Add the vertical strokes as defaults, so we get the correct colors
		// and no lines.
		addStrokes(vdVerticalSeparator.getLeftStrokes(), Position.left);
		addStrokes(vdVerticalSeparator.getRightStrokes(), Position.right);

		// Now copy all the strokes from the cell.
		addStrokes(c.getLeftStrokes(), Position.left);
		addStrokes(c.getRightStrokes(), Position.right);
		addStrokes(c.getTopStrokes(), Position.top);
		addStrokes(c.getBottomStrokes(), Position.bottom);
	}

	public Stroke getStroke(Position position, int depth) {
		return strokes[position.ordinal()][depth - minDepth[position.ordinal()]];
	}

	public List<Stroke> getList(Position position) {
		List<Stroke> result = new LinkedList<Stroke>();
		for (int i = 0; i < strokes[position.ordinal()].length; i++) {
			Stroke s = strokes[position.ordinal()][i];
			if (s == null) {
				s = new Stroke(-1);
			}
			result.add(s);
		}
		return result;
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()
				//
				.key("minTop").value(minDepth[Position.top.ordinal()])
				//
				.key("minBottom").value(minDepth[Position.bottom.ordinal()])
				//
				.key("left_S").value(Stroke.toString(getList(Position.left)))
				//
				.key("right_S").value(Stroke.toString(getList(Position.right)))
				//
				.key("top_S").value(Stroke.toString(getList(Position.top)))
				//
				.key("bottom_S")
				.value(Stroke.toString(getList(Position.bottom)))//
				.endObject();
	}

}
