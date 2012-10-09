package edu.isi.karma.er.matcher.impl;

import java.lang.reflect.Constructor;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.er.compare.NumberComparator;
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
	

	public Score match(Property p, Resource v, Resource w) {
		Score s = new Score();
		s.setPredicate(p);
		RDFNode nodeV = null, nodeW = null;
		
		if (v == null || w == null) {
			s.setScoreType(ScoreType.IGNORE);
			return s;
		}
		
		StmtIterator iterV = v.listProperties(p);
		StmtIterator iterW = w.listProperties(p);
		
		if (iterV.hasNext()) {
			s.setSrcObj(iterV.next());
			nodeV = s.getSrcObj().getObject();
		}
		if (iterW.hasNext()) {
			s.setDstObj(iterW.next());
			nodeW = s.getDstObj().getObject();
		}
		
		
		if (nodeV == null || nodeW == null) {
			s.setScoreType(ScoreType.IGNORE);
			return s;
		}
		
		double numV = 0, numW = 0;
		try {
			numV = Double.parseDouble(nodeV.asLiteral().getString());
			numW = Double.parseDouble(nodeW.asLiteral().getString());
		} catch (Exception e) { 
			s.setScoreType(ScoreType.INVALID);
			return s;
		}
		
		if (numV > max || numV < min || numW > max || numW < min) {
			s.setScoreType(ScoreType.INVALID);
			
		}else { 
			s.setScoreType(ScoreType.NORMAL);
			s.setSimilarity(comp.getSimilarity(numV, numW));	
		}	
		
		return s;
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
