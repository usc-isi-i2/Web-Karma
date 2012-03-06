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

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * @author szekely
 * 
 */
public class VDTriangle {

	enum TriangleLocation {
		topLeft, bottomLeft, topRight, bottomRight
	}

	private final String hTableId;
	private final String rowId;
	private final int depth;

	private final TriangleLocation location;

	VDTriangle(String hTableId, String rowId, int depth,
			TriangleLocation location) {
		super();
		this.hTableId = hTableId;
		this.rowId = rowId;
		this.depth = depth;
		this.location = location;
	}

	/*****************************************************************
	 * 
	 * Debugging Support
	 * 
	 *****************************************************************/
	
	void prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("location").value(location)//
				.key("depth").value(depth)//
				.key("hTableId").value(hTableId)//
				.key("rowId").value(rowId)//
				.endObject();
	}

}
