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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * @author szekely
 * 
 */
public class Stroke {

	private static Stroke rootStroke = new Stroke(StrokeStyle.none, "root", 0);

	public static Stroke getRootStroke() {
		return rootStroke;
	}

	public enum StrokeStyle {
		outer("o"), inner("i"), none("_");

		private String code;

		private StrokeStyle(String code) {
			this.code = code;
		}

		public String code() {
			return code;
		}
	}

	private final StrokeStyle style;

	private final String hTableId;

	private final int depth;

	public Stroke(StrokeStyle style, String hTableId, int depth) {
		super();
		this.style = style;
		this.hTableId = hTableId;
		this.depth = depth;
	}

	public Stroke(int depth) {
		this(StrokeStyle.none, "dummy", depth);
	}

	public StrokeStyle getStyle() {
		return style;
	}

	public String getHTableId() {
		return hTableId;
	}

	public int getDepth() {
		return depth;
	}

	public String toString() {
		return "s(" + depth + ":" + style.name() + ":" + hTableId + ")";
	}

	public static String toString(Collection<Stroke> strokeList) {
		Set<Stroke> empty = Collections.emptySet();
		return toString(strokeList, empty);
	}

	public static String toString(Collection<Stroke> strokeList,
			Set<Stroke> defaultStrokes) {
		StringBuffer b = new StringBuffer();
		Iterator<Stroke> it = strokeList.iterator();
		while (it.hasNext()) {
			Stroke s = it.next();
			if (defaultStrokes.contains(s)) {
				b.append("**");
			}
			b.append(s.toString());
			if (it.hasNext()) {
				b.append("/ ");
			}
		}
		return b.toString();
	}
}
