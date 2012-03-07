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

import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.tabledata.VDCell.Position;

/**
 * @author szekely
 * 
 */
public class StrokeStyles {
	private StrokeStyle left = StrokeStyle.none;
	private StrokeStyle right = StrokeStyle.none;
	private StrokeStyle top = StrokeStyle.none;
	private StrokeStyle bottom = StrokeStyle.none;

	public StrokeStyles() {
		super();
	}

	public void setStrokeStyle(Position position, StrokeStyle strokeStyle) {
		switch (position) {
		case left:
			this.left = strokeStyle;
			return;
		case right:
			this.right = strokeStyle;
			return;
		case top:
			this.top = strokeStyle;
			return;
		case bottom:
			this.bottom = strokeStyle;
			return;
		}
	}

	public String getJsonEncoding() {
		return left.code() + ":" + right.code() + ":" + top.code() + ":"
				+ bottom.code();
	}
}
