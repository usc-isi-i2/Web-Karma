package edu.isi.karma.er.matcher.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.isi.karma.er.compare.NumberComparator;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreType;
import edu.isi.karma.er.matcher.Matcher;

public class NumberSetMatcher implements Matcher {

	private double min = -1;
	private double max = -1;
	private double delta = 3;
	private NumberComparator comp = null;
	


	public NumberSetMatcher(double min, double max, double delta, NumberComparator comp) {
		this.min = min;
		this.max = max;
		this.delta = delta;
		this.comp = comp;
		// System.out.println("min:" + min + "\tmax:" + max + "\tdelta:" + delta);
	}
	
	@SuppressWarnings("unchecked")
	public NumberSetMatcher(JSONObject config) {
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
		s.setScoreType(ScoreType.IGNORE);
		RDFNode nodeV = null, nodeW = null;
		
		if (v == null || w == null) {
			return s;
		}
		
		List<Statement> listV = v.listProperties(p).toList();
		List<Statement> listW = w.listProperties(p).toList();
		
		if (listV == null || listV.size() <= 0 || listW == null || listW.size() <= 0)
			return s;
		
		int numV = -10000, numW = -10000;
		double sim = 0, maxSim = 0;
		
		for (Statement sv : listV) {
			if (sv == null || sv.getObject() == null)
				continue;
			nodeV = sv.getObject();
			try {
				numV = Integer.parseInt(nodeV.asLiteral().getString());
			} catch (Exception e) { continue; }
			s.setSrcObj(sv);
			for (Statement sw : listW) {
				if (sw == null || sw.getObject() == null)
					continue;
				nodeW = sw.getObject();
				try {
					numW = Integer.parseInt(nodeW.asLiteral().getString());
				} catch (Exception e) { continue; }
				s.setDstObj(sw);
				
				if (numV > max || numV < min || numW > max || numW < min) {
					if (s.getScoreType() == ScoreType.IGNORE) {
						s.setScoreType(ScoreType.INVALID);
					}
					
				}else { 
					s.setScoreType(ScoreType.NORMAL);
					sim = comp.getSimilarity(numV, numW);	
					if (sim > maxSim) {
						if (1 - sim < 1e-5) {
							s.setSimilarity(1);
							return s;
						} else {
							maxSim = sim;
						}
					}
				}	
			}
		}
		
		if (s.getScoreType() == ScoreType.NORMAL) {
			s.setSimilarity(maxSim);
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
