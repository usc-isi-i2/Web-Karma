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
package edu.isi.karma.geospatial;

import java.util.HashMap;
import java.util.Map;

public class Point {
	private final double latitude;
	private final double longitude;

	private Map<String, String> popupData = new HashMap<>();

	public Point(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void addColumnToDescription(String columnName, String data) {
		popupData.put(columnName, data);
	}

	public String getHTMLDescription() {
		StringBuilder str = new StringBuilder();
		for (Map.Entry<String, String> stringStringEntry : popupData.entrySet()) {
			str.append("<b>" + stringStringEntry.getKey() + "</b>: " + stringStringEntry.getValue()
					+ " <br />");
		}
		return str.toString();
	}

	@Override
	public String toString() {
		return "Point [latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}
