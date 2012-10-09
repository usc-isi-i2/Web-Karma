package edu.isi.karma.er.matcher;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.er.helper.entity.Score;

public interface Matcher {

	public Score match(Property p, Resource v, Resource w);
}
