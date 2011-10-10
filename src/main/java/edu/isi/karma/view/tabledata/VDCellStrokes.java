/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.Stroke;
import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.tabledata.VDCell.Position;

/**
 * @author szekely
 * 
 */
public class VDCellStrokes {

	public class StrokeIterator implements Iterator<Stroke> {

		private final Position position;
		private int index = 0;
		private int increment = 1;
		private int limit;

		StrokeIterator(Position position) {
			this.position = position;
			this.limit = strokes[position.ordinal()].length - 1;
			if (position == Position.right || position == Position.bottom) {
				this.index = strokes[position.ordinal()].length - 2;
				this.increment = -1;
				limit = 0;
			}
		}

		@Override
		public boolean hasNext() {
			return increment == 1 ? index != limit : index >= limit;
		}

		@Override
		public Stroke next() {
			Stroke result = strokes[position.ordinal()][index];
			index += increment;
			return result;
		}

		@Override
		public void remove() {
			throw new Error("Cannot remove!");
		}

	}

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

	int getNumStrokes(Position position) {
		return strokes[position.ordinal()].length - 1;
	}

	StrokeIterator iterator(Position position) {
		return new StrokeIterator(position);
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

	void populateFromVDCell(VDVerticalSeparator vdVerticalSeparator, VDCell c) {
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

	void setDefault(Stroke stroke, Set<Stroke> defaultStrokes) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < strokes[i].length; j++) {
				Stroke s = strokes[i][j];
				if (s == null || defaultStrokes.contains(s)) {
					strokes[i][j] = new Stroke(StrokeStyle.none,
							stroke.getHTableId(), j + minDepth[i]);
					defaultStrokes.add(strokes[i][j]);
				}
			}
		}
	}

	Stroke getStroke(Position position, int depth) {
		return strokes[position.ordinal()][depth - minDepth[position.ordinal()]];
	}

	List<Stroke> getList(Position position) {
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

	void populateStrokeStyles(StrokeStyles strokeStyles) {
		for (Position p : Position.values()) {
			strokeStyles.setStrokeStyle(p, strokes[p.ordinal()][strokes[p
					.ordinal()].length - 1].getStyle());
		}
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/

	JSONWriter prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()
				//
				.key("_minT").value(minDepth[Position.top.ordinal()])
				//
				.key("_minB").value(minDepth[Position.bottom.ordinal()])
				//
				.key("_minL").value(minDepth[Position.left.ordinal()])
				//
				.key("_minR").value(minDepth[Position.right.ordinal()])
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
		return jw;
	}

}
