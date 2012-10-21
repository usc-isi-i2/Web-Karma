/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
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

		public boolean hasNext() {
			return increment == 1 ? index != limit : index >= limit;
		}

		public Stroke next() {
			Stroke result = strokes[position.ordinal()][index];
			index += increment;
			return result;
		}

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

	private final int cellDepth;

	public static VDCellStrokes create(int cellDepth, VDVerticalSeparator vdVS,
			int numTop, int minTop, int numBottom, int minBottom) {

		int numLeft = vdVS.getLeftStrokes().size();
		int minLeft = vdVS.getMinDepth(Position.left);

		int numRight = vdVS.getRightStrokes().size();
		int minRight = vdVS.getMinDepth(Position.right);

		return new VDCellStrokes(cellDepth, numLeft, numRight, numTop,
				numBottom, minLeft, minRight, minTop, minBottom);
	}

	VDCellStrokes(int cellDepth, int numLeft, int numRight, int numTop,
			int numBottom, int minLeft, int minRight, int minTop, int minBottom) {
		this.cellDepth = cellDepth;
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
				if (j + minDepth[i] + cellDepth >= stroke.getDepth()) {
					if (s == null || defaultStrokes.contains(s)) {
						strokes[i][j] = new Stroke(StrokeStyle.none,
								stroke.getHTableId(), j + minDepth[i]
										+ cellDepth);
						defaultStrokes.add(strokes[i][j]);
					}
				}
			}
		}
	}

	public String getHTableId(int depth) {
		for (int i = 0; i < 4; i++) {
			int index = depth - minDepth[i];
			if (index >= 0 && index < strokes[i].length) {
				return strokes[i][index].getHTableId();
			}
		}
		return null;
	}

	Stroke getStroke(Position position, int depth) {
		int index = depth - minDepth[position.ordinal()];
		if (index >= 0 && index < strokes[position.ordinal()].length) {
			return strokes[position.ordinal()][index];
		} else {
			if (depth > 0) {
				return getStroke(position, depth - 1);
			}
			return null;
		}
	}

	Stroke getStrokeByIndex(Position position, int index) {
		if (index >= 0 && index < strokes[position.ordinal()].length) {
			return strokes[position.ordinal()][index];
		} else {
			return new Stroke(StrokeStyle.none, "" + position.name() + "/"
					+ index, index + minDepth[position.ordinal()]);
		}
	}

	int ordinalToIndex(Position position, int ordinal) {
		int index = ordinal - 1;
		if (position == Position.right || position == Position.bottom) {
			index = strokes[position.ordinal()].length - ordinal - 1;
		}
		return index;
	}

//	int getDistanceFromCell(Position position, int ordinal) {
//		if (position == Position.left || position == Position.top) {
//			return strokes[position.ordinal()].length - ordinal;
//		} else {
//			return ordinal;
//		}
//	}

//	Stroke getMaxStroke(Position position) {
//		Stroke[] x = strokes[position.ordinal()];
//		return x[x.length - 1];
//	}

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

	JSONWriter prettyPrintJson(JSONWriter jw, Set<Stroke> defaultStrokes)
			throws JSONException {
		jw.object()
				//
				.key("_D")
				.value(cellDepth)
				//
				.key("_minT")
				.value(minDepth[Position.top.ordinal()])
				//
				.key("_minB")
				.value(minDepth[Position.bottom.ordinal()])
				//
				.key("_minL")
				.value(minDepth[Position.left.ordinal()])
				//
				.key("_minR")
				.value(minDepth[Position.right.ordinal()])
				//
				.key("left_S")
				.value(Stroke.toString(getList(Position.left), defaultStrokes))
				//
				.key("right_S")
				.value(Stroke.toString(getList(Position.right), defaultStrokes))
				//
				.key("top_S")
				.value(Stroke.toString(getList(Position.top), defaultStrokes))
				//
				.key("bottom_S")
				.value(Stroke
						.toString(getList(Position.bottom), defaultStrokes))//
				.endObject();
		return jw;
	}
}
