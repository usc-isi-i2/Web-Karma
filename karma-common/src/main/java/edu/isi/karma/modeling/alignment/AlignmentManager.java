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
package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.WorkspaceManager;

public class AlignmentManager {
	private static Map<String, Alignment> alignmentMap = null;
	private static AlignmentManager _InternalInstance = null;
	public static AlignmentManager Instance()
	{
		if (_InternalInstance == null)
		{
			_InternalInstance = new AlignmentManager();
			alignmentMap = new ConcurrentHashMap<>();
		}
		return _InternalInstance;
	}
	
	public void addAlignmentToMap(String key, Alignment alignment) {
		alignmentMap.put(key, alignment);
	}

	public Alignment getAlignment(String alignmentId) {
		return alignmentMap.get(alignmentId);
	}
	
	public String getAlignmentId(Alignment alignment) {
		for(Map.Entry<String, Alignment> stringAlignmentEntry : alignmentMap.entrySet()) {
			if(stringAlignmentEntry.getValue() == alignment)
				return stringAlignmentEntry.getKey();
		}
		return null;
	}
	
	public Alignment getAlignment(String workspaceId, String worksheetId) {
		String alignmentId = constructAlignmentId(workspaceId, worksheetId);
		return getAlignment(alignmentId);
	}

	
	public Alignment createAlignment(String workspaceId, String worksheetId, OntologyManager ontologyManager){
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspaceId, worksheetId);
		
		Worksheet worksheet = WorkspaceManager.getInstance().getWorkspace(workspaceId).getWorksheet(worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		
		if (alignment == null) {
			alignment = new Alignment(ontologyManager);
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
			alignment.updateColumnNodesInAlignment(worksheet);
		}
	
		return alignment;
	}

	public void removeWorkspaceAlignments(String workspaceId) {
		List<String> keysToBeRemoved = new ArrayList<>();
		for(String key:alignmentMap.keySet()) {
			if(key.startsWith(workspaceId+":")) {
				keysToBeRemoved.add(key);
			}
		}
		// Remove the keys
		for(String key:keysToBeRemoved) {
			Alignment a = alignmentMap.remove(key);
			a.cleanup();
		}
	}
	
	public boolean removeAlignment(String alignmentId) {
		Alignment a =  alignmentMap.remove(alignmentId);
		if (a != null)
			a.cleanup();
		return a != null;
	}
	
	public String constructAlignmentId(String workspaceId, String worksheetId) {
		return workspaceId + ":" + worksheetId + "AL";
	}
	
	public String getWorkspaceId(Alignment align) {
		String id = getAlignmentId(align);
		if(id != null) {
			int idx = id.indexOf(":");
			id = id.substring(0, idx);
		}
		return id;
	}
	
	public String getWorksheetId(Alignment align) {
		String id = getAlignmentId(align);
		if(id != null) {
			int idx = id.indexOf(":");
			int endIdx = id.indexOf("AL", idx);
			id = id.substring(idx+1, endIdx);
		}
		return id;
	}
}
