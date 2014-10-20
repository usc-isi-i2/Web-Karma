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
	private Map<String, String>			columnPythonTransform;
	private Map<String, String>			columnPreviousCommandId;
	private Map<String, String>			columnDerivedFrom;
	private Map<String, DataStructure>  columnDataStructure;
	private Map<String, Boolean>	    columnOnError;
	public ColumnMetadata() {
		super();
		this.columnPreferredLengths = new HashMap<String, Integer>();
//		this.columnTypes 			= new HashMap<String, ColumnMetadata.COLUMN_TYPE>();
//		this.invalidNodeIds 		= new HashMap<String, List<String>>();
		this.columnHistogramData	= new HashMap<String, JSONObject>();
		this.columnPythonTransform  = new HashMap<String, String>();
		this.columnPreviousCommandId = new HashMap<String, String>();
		this.columnDerivedFrom = new HashMap<String, String>();
		this.columnDataStructure = new HashMap<String, DataStructure>();
		this.columnOnError = new HashMap<String, Boolean>();
	}
	
	public enum DataStructure {
		PRIMITIVE, COLLECTION, OBJECT
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
	
	public void addColumnOnError(String hNodeId, Boolean onError) {
		columnOnError.put(hNodeId, onError);
	}
	
	public Boolean getColumnOnError(String hNodeId) {
		return columnOnError.get(hNodeId);
	}
	
	public String getColumnPython(String hNodeId)
	{
		return columnPythonTransform.get(hNodeId);
	}
	
	public String getColumnPreviousCommandId(String hNodeId)
	{
		return columnPreviousCommandId.get(hNodeId);
	}
	
	public String getColumnDerivedFrom(String hNodeId)
	{
		return columnDerivedFrom.get(hNodeId);
	}
	
	public DataStructure getColumnDataStructure(String hNodeId)
	{
		return columnDataStructure.get(hNodeId);
	}
	
	public void addColumnPythonTransformation(String hNodeId, String pythonTransform)
	{
		columnPythonTransform.put(hNodeId, pythonTransform);
	}

	public void addPreviousCommandId(String hNodeId, String commandId) {
		columnPreviousCommandId.put(hNodeId, commandId);		
	}
	
	public void addColumnDerivedFrom(String hNodeId, String sourceHNodeId) {
		columnDerivedFrom.put(hNodeId, sourceHNodeId);		
	}
	
	public void addColumnDataStructure(String hNodeId, DataStructure dataStructure)
	{
		columnDataStructure.put(hNodeId, dataStructure);
	}
}
