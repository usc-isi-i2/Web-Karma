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

package edu.isi.karma.rep;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class ColumnMetadata {
	
	private Map<String, Integer>		columnPreferredLengths;
//	private Map<String, COLUMN_TYPE>	columnTypes;
//	private Map<String, List<String>>	invalidNodeIds;
	private Map<String, JSONObject>		columnHistogramData;
	
	public ColumnMetadata() {
		super();
		this.columnPreferredLengths = new HashMap<String, Integer>();
//		this.columnTypes 			= new HashMap<String, ColumnMetadata.COLUMN_TYPE>();
//		this.invalidNodeIds 		= new HashMap<String, List<String>>();
		this.columnHistogramData	= new HashMap<String, JSONObject>();
	}
	
	
	public enum COLUMN_TYPE {
		String, Long, Double, Date, URI, HTML
	}
	
	public void addColumnPreferredLength(String hNodeId, int preferredLength) {
		columnPreferredLengths.put(hNodeId, preferredLength);
	}
	
	public Integer getColumnPreferredLength(String hNodeId) {
		return columnPreferredLengths.get(hNodeId);
	}
	
	public JSONObject getColumnHistogramData(String hNodeId) {
		return columnHistogramData.get(hNodeId);
	}
	
	public void addColumnHistogramData(String hNodeId, JSONObject data) {
		columnHistogramData.put(hNodeId, data);
	}
}
