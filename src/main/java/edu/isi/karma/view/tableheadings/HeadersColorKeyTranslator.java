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
package edu.isi.karma.view.tableheadings;

import java.util.HashMap;

import edu.isi.karma.rep.hierarchicalheadings.ColorKeyTranslator;

public class HeadersColorKeyTranslator implements ColorKeyTranslator {

	private static final HashMap<Integer, String> depthCssMap = new HashMap<Integer, String>();
	static {
		depthCssMap.put(0, "topLevelTableCell");
		depthCssMap.put(1, "table01cell");
		depthCssMap.put(2, "table02cell");
		depthCssMap.put(3, "table03cell");
		depthCssMap.put(4, "table04cell");
		depthCssMap.put(5, "table05cell");
		depthCssMap.put(6, "table06cell");
	}
	
	@Override
	public String getCssTag(String colorKey, int depth) {
		return depthCssMap.get(depth%7);
	}

}
