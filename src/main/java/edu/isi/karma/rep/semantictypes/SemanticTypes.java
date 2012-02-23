package edu.isi.karma.rep.semantictypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;

public class SemanticTypes implements Jsonizable {
	// Map from the HNodeIds (for each column) to the semantic type
	private Map<String, SemanticType> types = new HashMap<String, SemanticType>();
	private Map<String, SynonymSemanticTypes> synonymTypes = new HashMap<String, SynonymSemanticTypes>();

	public Map<String, SemanticType> getTypes() {
		return types;
	}
	
	public Collection<SemanticType> getListOfTypes() {
		return types.values();
	}

	public SemanticType getSemanticTypeForHNodeId(String hNodeId) {
		return types.get(hNodeId);
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


	@Override
	public void write(JSONWriter writer) throws JSONException {
		writer.array();
		for (SemanticType type : types.values()) {
			type.write(writer);
		}
		writer.endArray();
	}

	@SuppressWarnings("unused")
	private void initializeFromJSON() {

	}

	public void addType(SemanticType type) {
		types.put(type.getHNodeId(), type);
	}
}
