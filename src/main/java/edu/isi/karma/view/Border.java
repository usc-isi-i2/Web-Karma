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
package edu.isi.karma.view;

import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.border;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.cellType;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.fillId;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.leftBorder;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.rightBorder;
import static edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate.JsonKeys.topBorder;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.Stroke.StrokeStyle;

/**
 * @author szekely
 * 
 */
public class Border {

	enum Position {
		left, right
	}

	private final Position position;

	private boolean hasTopStroke = false;

	private final Stroke stroke;

	private final Margin margin;

	// private static final Border rootBorder = new
	// Border(Stroke.getRootStroke(),
	// Margin.getRootMargin());

	private static final List<Border> rootBorderList = new LinkedList<Border>();

	public static List<Border> getRootBorderList() {
		return rootBorderList;
	}

	Border(Position position, Stroke stroke, Margin margin) {
		super();
		this.position = position;
		this.stroke = stroke;
		this.margin = margin;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public Margin getMargin() {
		return margin;
	}

	public boolean isHasTopStroke() {
		return hasTopStroke;
	}

	public void setHasTopStroke(boolean hasTopStroke) {
		this.hasTopStroke = hasTopStroke;
	}

	Position getPosition() {
		return position;
	}

	public String toString() {
		String top = hasTopStroke ? "top" : "";
		String s = "null stroke";
		if (stroke != null) {
			s = stroke.toString();
		}
		String m = "null margin";
		if (margin != null) {
			m = margin.toString();
		}
		return position + ":" + top + ":" + s + m;
	}

	private static Stroke getStroke(List<Stroke> strokes, int depth) {
		for (Stroke s : strokes) {
			if (s.getDepth() == depth) {
				return s;
			}
		}
		return new Stroke(depth);
	}

	private static Margin getMargin(List<Margin> margins, int depth) {
		for (Margin m : margins) {
			if (m.getDepth() == depth) {
				return m;
			}
		}
		return new Margin("dummy", depth);
	}

	public boolean isOuter() {
		return stroke.getStyle() == Stroke.StrokeStyle.outer;
	}

	private static List<Margin> reverseMargins(List<Margin> margins) {
		List<Margin> result = new LinkedList<Margin>();
		result.addAll(margins);
		Collections.reverse(result);
		return result;
	}

	public static List<Border> constructBorderList(List<Margin> margins,
			List<Stroke> strokes, int nodeDepth, boolean isIncreasing) {
		List<Margin> list = isIncreasing ? margins : reverseMargins(margins);
		List<Border> result = new LinkedList<Border>();
		Position pos = isIncreasing ? Position.left : Position.right;
		for (Margin m : list) {
			if (m.getDepth() >= nodeDepth) {
				// Our margin is the wrong color when it's depth is too deep,
				// ie, created by a nested table.
				Margin lowerMargin = getMargin(margins, nodeDepth - 1);
				m = new Margin(lowerMargin.getHTableId(), m.getDepth());
			}
			Stroke s = getStroke(strokes, m.getDepth());
			result.add(new Border(pos, s, m));
		}
		return result;
	}

	public static String getBordersString(List<Border> borders) {
		StringBuffer b = new StringBuffer();
		Iterator<Border> it = borders.iterator();
		while (it.hasNext()) {
			b.append(it.next().toString());
			if (it.hasNext()) {
				b.append(",");
			}
		}
		return b.toString();
	}

	public void generateJson(JSONWriter jw, boolean asLeft,
			VWorksheet vWorksheet, VWorkspace vWorkspace) throws JSONException {
		StrokeStyle topStroke = hasTopStroke ? Stroke.StrokeStyle.outer
				: Stroke.StrokeStyle.none;
		StrokeStyle leftStroke = StrokeStyle.none, rightStroke = StrokeStyle.none;
		switch (position) {
		case left:
			leftStroke = stroke.getStyle();
			rightStroke = StrokeStyle.none;
			break;
		case right:
			leftStroke = StrokeStyle.none;
			rightStroke = stroke.getStyle();

		}

		VTableCssTags css = vWorkspace.getViewFactory().getTableCssTags();

		jw.object()
				//
				.key(cellType.name())
				.value(border.name())
				//
				.key(fillId.name())
				.value(css.getCssTag(margin.getHTableId(), margin.getDepth()))
				//
				.key(topBorder.name())
				.value(encodeBorder(topStroke, css.getCssTag(margin.getHTableId(), margin.getDepth())))
				//
				.key(leftBorder.name())
				.value(encodeBorder(leftStroke, css.getCssTag(stroke.getHTableId(), margin.getDepth())))
				//
				.key(rightBorder.name())
				.value(encodeBorder(rightStroke, css.getCssTag(stroke.getHTableId(), margin.getDepth())))//
		;
		jw.endObject();
	}

	public static String encodeBorder(StrokeStyle style, String cssStyle) {
		return style.name() + ":" + cssStyle;
	}
}
