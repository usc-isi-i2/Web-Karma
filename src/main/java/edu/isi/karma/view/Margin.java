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
import java.util.Iterator;

/**
 * @author szekely
 * 
 */
public class Margin {

	private final String hTableId;

	private final int depth;

	private static Margin rootMargin = new Margin("root", 0);
	private static Margin leafMargin = new Margin("leaf", -1);

	public boolean isRootMargin(Margin margin){
		return hTableId.equals("root");
	}
	
	public static Margin getRootMargin() {
		return rootMargin;
	}

	public static Margin getleafMargin() {
		return leafMargin;
	}

	public Margin(String hTableId, int depth) {
		super();
		this.hTableId = hTableId;
		this.depth = depth;
	}

	public String getHTableId() {
		return hTableId;
	}

	public int getDepth() {
		return depth;
	}

	public String toString() {
		return "m(" + depth + ":" + hTableId + ")";
	}
	
	public static String toString(Collection<Margin> marginList) {
		StringBuffer b = new StringBuffer();
		Iterator<Margin> it = marginList.iterator();
		while (it.hasNext()) {
			b.append(it.next().toString());
			if (it.hasNext()) {
				b.append("/");
			}
		}
		return b.toString();
	}

	public static String getMarginsString(Margin margin) {
		if (margin == null) {
			return "none";
		} else {
			return margin.toString();
		}
	}
}
