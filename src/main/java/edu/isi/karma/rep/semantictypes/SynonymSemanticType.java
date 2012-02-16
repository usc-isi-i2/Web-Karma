package edu.isi.karma.rep.semantictypes;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.util.Jsonizable;

public class SynonymSemanticType implements Jsonizable {
	
	private List<SemanticType> synonyms;

	public SynonymSemanticType() {
		synonyms = new ArrayList<SemanticType>();
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