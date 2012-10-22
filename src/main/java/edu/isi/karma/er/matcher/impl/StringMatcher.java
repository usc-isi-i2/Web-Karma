package edu.isi.karma.er.matcher.impl;

import java.util.List;

import org.json.JSONObject;

import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.helper.entity.SaamPerson;
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

	public Score match(String pred, SaamPerson v, SaamPerson w) {
		Score s = new Score();
		s.setScoreType(ScoreType.INVALID);
		
		if (v == null || w == null) {
			return s;
		}
		
		List<String> listV = v.getProperty(pred).getValue();
		List<String> listW = w.getProperty(pred).getValue();
		
		if (listV == null || listV.size() <= 0 || listW == null || listW.size() <= 0) {
			return s;
		}
		
		String strV = listV.get(0);
		String strW = listW.get(0);
		
		
		
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
