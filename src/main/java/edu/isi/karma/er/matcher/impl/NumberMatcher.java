package edu.isi.karma.er.matcher.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.json.JSONObject;

import edu.isi.karma.er.compare.NumberComparator;
import edu.isi.karma.er.helper.entity.SaamPerson;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreType;
import edu.isi.karma.er.matcher.Matcher;

public class NumberMatcher implements Matcher {

	private double min = -1;
	private double max = -1;
	private double delta = 3;
	private NumberComparator comp = null;
	


	public NumberMatcher(double min, double max, double delta, NumberComparator comp) {
		this.min = min;
		this.max = max;
		this.delta = delta;
		this.comp = comp;
		// System.out.println("min:" + min + "\tmax:" + max + "\tdelta:" + delta);
	}
	
	@SuppressWarnings("unchecked")
	public NumberMatcher(JSONObject config) {
		double min = config.optDouble("min");
		double max = config.optDouble("max");
		double delta = config.optDouble("delta");
		
		try {
			String className = config.getString("class");
		
			Class<NumberComparator> clazz = (Class<NumberComparator>) Class.forName(className);
			Constructor<NumberComparator> constructor = clazz.getConstructor(double.class, double.class, double.class);
			NumberComparator comp = constructor.newInstance(min, max, delta);
			
			this.min = min;
			this.max = max;
			this.delta = delta;
			this.comp = comp;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public Score match(String pred, SaamPerson v, SaamPerson w) {
		Score s = new Score();
		s.setPredicate(pred);
		String nodeV = null, nodeW = null;
		
		if (v == null || w == null || v.getProperty(pred) == null || w.getProperty(pred) == null ) {
			s.setScoreType(ScoreType.IGNORE);
			return s;
		}
		
		List<String> listV = v.getProperty(pred).getValue();
		List<String> listW = w.getProperty(pred).getValue();
		if (listV == null || listV.size() <= 0 || listW == null || listW.size() <= 0) {
			s.setScoreType(ScoreType.IGNORE);
			return s;
		}
		
		nodeV = listV.get(0);
		s.setSrcObj(nodeV);
		nodeW = listW.get(0);
		s.setDstObj(nodeW);
	
		double numV = 0, numW = 0;
		if (!isParsable(nodeV) || !isParsable(nodeW)) {
			s.setScoreType(ScoreType.INVALID);
			return s;
		}
			
		numV = Integer.parseInt(nodeV);
		numW = Integer.parseInt(nodeW);
		
		
		if (numV > max || numV < min || numW > max || numW < min) {
			s.setScoreType(ScoreType.INVALID);
			
		}else { 
			s.setScoreType(ScoreType.NORMAL);
			s.setSimilarity(comp.getSimilarity(numV, numW));	
		}	
		
		return s;
	}

	private boolean isParsable(String str) {
		if (str == null || "" == str) {
			return false;
		}
		char[] ch = str.toCharArray();
		int i;
		for (i = 0; i < ch.length; i++) {
			if (ch[i] > '9' || ch[i] < '0') {
				break;
			}
		}

		if (i >= ch.length)
			return true;
		else
			return false;
	}


	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}
}
