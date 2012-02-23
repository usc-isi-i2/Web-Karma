package edu.isi.karma.rep.semantictypes;

import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;

public class SynonymSemanticTypes implements Jsonizable {
	
	private List<SemanticType> synonyms;

	public SynonymSemanticTypes(List<SemanticType> semTypes) {
		synonyms = semTypes;
	}
	
	public List<SemanticType> getSynonyms(){
		return synonyms;
	}
	
	@Override
	public void write(JSONWriter writer) throws JSONException {
		writer.array();
		for (SemanticType type : synonyms) {
			type.write(writer);
		}
		writer.endArray();
	}
}