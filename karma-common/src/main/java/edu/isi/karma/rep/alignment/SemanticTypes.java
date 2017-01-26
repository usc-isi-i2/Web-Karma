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
package edu.isi.karma.rep.alignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;

public class SemanticTypes implements Jsonizable {
	// Map from the HNodeIds (for each column) to the semantic type
	private Map<String, ArrayList<SemanticType>> types = new HashMap<>();
	private Map<String, SynonymSemanticTypes> synonymTypes = new HashMap<>();

	public Map<String, ArrayList<SemanticType>> getTypes() {
		return types;
	}
	
	public Collection<SemanticType> getListOfTypes() {
		Collection<ArrayList<SemanticType>> semTypes = types.values();
		Collection<SemanticType> result = new ArrayList<SemanticType>();
		for(ArrayList<SemanticType> item : semTypes)
			result.addAll(item);
		return result;
	}

	public ArrayList<SemanticType> getSemanticTypeForHNodeId(String hNodeId) {
		ArrayList<SemanticType> type = types.get(hNodeId);
		if(type == null)
			type = new ArrayList<>();
		return type;
	}
	
	public SynonymSemanticTypes getSynonymTypesForHNodeId(String hNodeId) {
		return synonymTypes.get(hNodeId);
	}
	
	public void addSynonymTypesForHNodeId(String hNodeId, SynonymSemanticTypes synTypes) {
		synonymTypes.put(hNodeId, synTypes);
	}

	public void unassignColumnSemanticType(String hNodeId) {
		types.remove(hNodeId);		
		synonymTypes.remove(hNodeId);
	}

	public void write(JSONWriter writer) throws JSONException {
		writer.array();
		for (ArrayList<SemanticType> typeArr : types.values()) {
			for(SemanticType type: typeArr)
				type.write(writer);
		}
		writer.endArray();
	}

	@SuppressWarnings("unused")
	private void initializeFromJSON() {

	}

	public void addType(SemanticType type) {
		ArrayList<SemanticType> typeArr = getSemanticTypeForHNodeId(type.getHNodeId());
		typeArr.add(type);
		types.put(type.getHNodeId(), typeArr);
	}
	
	public void removeType(SemanticType type) {
		ArrayList<SemanticType> typeArr = getSemanticTypeForHNodeId(type.getHNodeId());
		typeArr.remove(type);
		types.put(type.getHNodeId(), typeArr);
	}
	
	public void setType(ArrayList<SemanticType>  type) {
		if(type.size() > 0) {
			types.put(type.get(0).getHNodeId(), type);
		} 
	}
}
