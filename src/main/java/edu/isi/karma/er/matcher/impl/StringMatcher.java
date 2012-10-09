package edu.isi.karma.er.matcher.impl;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreType;
import edu.isi.karma.er.matcher.Matcher;

public class StringMatcher implements Matcher {

	private StringComparator comp = null;

	public StringMatcher(StringComparator comp) {
		this.comp = comp;
	}

	@SuppressWarnings("unchecked")
	public StringMatcher(JSONObject paramConfig) {
		String className;
		try {
			className = paramConfig.getString("class");
			Class<StringComparator> clazz = (Class<StringComparator>) Class.forName(className);
			StringComparator comp = clazz.newInstance();
			this.comp = comp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public Score match(Property p, Resource v, Resource w) {
		Score s = new Score();
		s.setScoreType(ScoreType.INVALID);
		
		if (v == null || w == null) {
			return s;
		}
		
		// get all property value to the spcified subject with given property.
		StmtIterator iterV = v.listProperties(p);
		StmtIterator iterW = w.listProperties(p);
		
		RDFNode nodeV = null;
		RDFNode nodeW = null;
		
		// get the first element of the result set of querying property value from specified subject.
		if (iterV.hasNext()) 
			nodeV = iterV.next().getObject();
		if (iterW.hasNext()) 
			nodeW = iterW.next().getObject();
		
		if (nodeV == null || nodeV == null) {
			return s;
		}
		
		String strV = nodeV.asLiteral().getString(), strW = nodeW.asLiteral().getString();
		
		if (strV == null || strV.trim().length() <= 0 || strW == null || strW.trim().length() <= 0) {
			s.setScoreType(ScoreType.IGNORE);
			return s;
		}
		
		if (strV.equals(strW)) {
			s.setSimilarity(1);
			s.setScoreType(ScoreType.NORMAL);
			return s;
		}
		
		s.setSimilarity(comp.getSimilarity(strV, strW));
		s.setScoreType(ScoreType.NORMAL);
		
		return s;
	}
	
	public StringComparator getComp() {
		return comp;
	}

	public void setComp(StringComparator comp) {
		this.comp = comp;
	}
	
	
}
