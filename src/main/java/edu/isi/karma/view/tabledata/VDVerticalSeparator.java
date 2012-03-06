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

	public Stroke getStroke(Position position, int depth) {
		for (Stroke s : getStrokes(position)) {
			if (s.getDepth() == depth) {
				return s;
			}
		}
		return null;
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
