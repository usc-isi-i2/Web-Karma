package edu.isi.karma.er.matcher.impl;

import java.util.List;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreType;
import edu.isi.karma.er.matcher.Matcher;

public class StringSetMatcher implements Matcher {

	private StringComparator comp = null;
	// private Logger log = null;

	public StringSetMatcher(StringComparator comp) {
		this.comp = comp;
		// this.log = Logger.getLogger(this.getClass());
	}
	
	@SuppressWarnings("unchecked")
	public StringSetMatcher(JSONObject paramConfig) {
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
		s.setPredicate(p);
		s.setScoreType(ScoreType.INVALID);
		if (v == null || w == null) {
			return s;
		}
		// get all property value to the spcified subject with given property.
		List<Statement> listV = v.listProperties(p).toList();
		List<Statement> listW = w.listProperties(p).toList();
		
		RDFNode nodeV = null;
		RDFNode nodeW = null;
		double similarity, maxSimilarity = -1;
		
		s.setScoreType(ScoreType.IGNORE);
		
		
		// get all elements of the result set of querying property value from specified subject.
		for (Statement sv : listV) {
			nodeV = sv.getObject();
			String strV = nodeV.asLiteral().getString();
			if (nodeV == null || nodeV == null) {
				continue;
			}
			
			// find the most appropriate matched pair in target set
			for (Statement sw : listW) {
				nodeW = sw.getObject();
				
				String strW = nodeW.asLiteral().getString();
				if (strV == null || strV.trim().length() <= 0 || strW == null || strW.trim().length() <= 0) {
					continue;
				}
				
				s.setScoreType(ScoreType.NORMAL);
				similarity = comp.getSimilarity(strV, strW);
				if (similarity > maxSimilarity) {
					s.setSrcObj(sv);
					s.setDstObj(sw);
					if (1 - similarity< 1e-5) {
						s.setSimilarity(similarity);
						return s;
					} else {
						maxSimilarity = similarity;
					}
				}
				
			}
			
		}
		s.setSimilarity(maxSimilarity);
		
		return s;
	}
	
	
	public StringComparator getComp() {
		return comp;
	}

	public void setComp(StringComparator comp) {
		this.comp = comp;
	}

	

}
