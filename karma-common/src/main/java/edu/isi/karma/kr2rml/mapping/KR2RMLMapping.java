/*******************************************************************************
 * Copyright 2014 University of Southern California
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
package edu.isi.karma.kr2rml.mapping;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import edu.isi.karma.kr2rml.KR2RMLVersion;
import edu.isi.karma.kr2rml.SubjectMap;
import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatter;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;

public class KR2RMLMapping extends R2RMLMapping{
	
	private KR2RMLVersion version;
	private KR2RMLMappingAuxillaryInformation auxInfo;
	private Map<String, SubjectMap> subjectMapIndex;
	private Map<String, TriplesMap> triplesMapIndex;
	private JSONArray worksheetHistory;
	private String worksheetHistoryString;
	private KR2RMLColumnNameFormatter formatter;
	private boolean isR2RMLCompatible;
	private boolean isRMLCompatible;
	private SourceTypes sourceType; 
	
	public KR2RMLMapping(R2RMLMappingIdentifier id, KR2RMLVersion version)
	{
		super(id);
		this.version = version;
		this.auxInfo = new KR2RMLMappingAuxillaryInformation();	
		this.subjectMapIndex = new HashMap<>();
		this.triplesMapIndex = new HashMap<>();
	}

	public KR2RMLVersion getVersion()
	{
		return version;
	}
	
	public KR2RMLMappingAuxillaryInformation getAuxInfo() {
		return auxInfo;
	}
	
	public Map<String, SubjectMap> getSubjectMapIndex() {
		return subjectMapIndex;
	}

	public Map<String, TriplesMap> getTriplesMapIndex() {
		return triplesMapIndex;
	}

	public JSONArray getWorksheetHistory() {
		return worksheetHistory;
	}

	public void setWorksheetHistory(JSONArray worksheetHistory) {
		this.worksheetHistory = worksheetHistory;
		worksheetHistoryString = worksheetHistory.toString();
	}

	public void setColumnNameFormatter(KR2RMLColumnNameFormatter formatter) {
		this.formatter = formatter;
	}
	public KR2RMLColumnNameFormatter getColumnNameFormatter()
	{
		return formatter;
	}

	public boolean isR2RMLCompatible() {
		return isR2RMLCompatible;
	}

	public boolean isRMLCompatible()
	{
		return isRMLCompatible;
	}
	
	public void setR2RMLCompatible(boolean isR2RMLCompatible) {
		this.isR2RMLCompatible = isR2RMLCompatible;
	}

	public void setRMLCompatible(boolean isRMLCompatible)
	{
		this.isRMLCompatible = isRMLCompatible;
	}

	public SourceTypes getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceTypes sourceType) {
		this.sourceType = sourceType;
	}
	
	public String getWorksheetHistoryString() {
		return worksheetHistoryString;
	}
	
	public String translateGraphNodeIdToTriplesMapId(String nodeId){
		String triplesMapId = null;
		
		triplesMapId = auxInfo.getGraphNodeIdToTriplesMapIdMap().get(nodeId);
		if(triplesMapId != null){
			return triplesMapId;
		}
		
		//check if the nodeId provided is actually a triplesMapId
		if(triplesMapIndex.containsKey(nodeId)){
			return nodeId;
		}
		
		return triplesMapId;
		
	}
}
