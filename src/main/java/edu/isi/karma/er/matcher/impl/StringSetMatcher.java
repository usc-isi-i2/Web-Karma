package edu.isi.karma.er.matcher.impl;

import java.util.List;

import org.json.JSONObject;

import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.helper.entity.SaamPerson;
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

	public Score match(String pred, Ontology o1, Ontology o2) {
		SaamPerson v = (SaamPerson) o1;
		SaamPerson w = (SaamPerson) o2;
		
		Score s = new Score();
		s.setPredicate(pred);
		
		s.setScoreType(ScoreType.INVALID);
		if (v == null || w == null || v.getProperty(pred) == null || w.getProperty(pred) == null) {
			return s;
		}
		
		List<String> listV = v.getProperty(pred).getValue();
		List<String> listW = w.getProperty(pred).getValue();
		
		if (listV == null || listV.size() <= 0 || listW == null || listW.size() <= 0) {
			return s;
		}
		
		double similarity, maxSimilarity = -1;
		
		
		
		s.setScoreType(ScoreType.IGNORE);
		
		
		// get all elements of the result set of querying property value from specified subject.
		for (String strV : listV) {
			
			s.setSrcObj(strV);
			// find the most appropriate matched pair in target set
			for (String strW : listW) {
				
				if (strV == null || "".equals(strV) || strW == null || "".equals(strW)) {
					continue;
				}
				
				s.setScoreType(ScoreType.NORMAL);
				similarity = comp.getSimilarity(strV, strW);
				if (similarity > maxSimilarity) {
					s.setDstObj(strW);
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
