package edu.isi.karma.er.matcher;


import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.helper.entity.Score;

public interface Matcher {

	public Score match(String p, Ontology v, Ontology w);
}
