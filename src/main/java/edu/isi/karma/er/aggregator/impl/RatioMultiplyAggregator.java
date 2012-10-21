package edu.isi.karma.er.aggregator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.helper.RatioFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreType;
import edu.isi.karma.er.matcher.Matcher;
import edu.isi.karma.er.matcher.impl.NumberMatcher;
import edu.isi.karma.er.matcher.impl.NumberSetMatcher;
import edu.isi.karma.er.matcher.impl.StringMatcher;
import edu.isi.karma.er.matcher.impl.StringSetMatcher;

public class RatioMultiplyAggregator implements Aggregator {

	private double threshold = 0.8;
	
	public MultiScore match(JSONArray confArr, Resource res1, Resource res2) {
		double ratio, sim, totalRatio = 1, totalSim = 1;
		boolean canHalt = false;
		MultiScore ms = new MultiScore();
		ms.setSrcSubj(res1);
		ms.setDstSubj(res2);
		List<Score> sList = new ArrayList<Score>();
		
		// for each property to be compared in configuration array, load its configurations and initialize the detailed comparator to be invoked.
		for (int i = 0; i < confArr.length(); i++) {	
			try {
				JSONObject config = confArr.getJSONObject(i);
				Matcher m = null;
				
				if ("string".equalsIgnoreCase(config.optString("type"))) {		// if the property is string-type
					JSONObject paramConfig = config.getJSONObject("comparator");
					
					if (true == config.getBoolean("is_set")) {					// if this property possibly has several values.
						m = new StringSetMatcher(paramConfig);
						
					} else {													// else has only single value
						m = new StringMatcher(paramConfig);
					}
					
				} else if ("number".equalsIgnoreCase(config.optString("type"))) { // if the property is numberic-type
					JSONObject paramConfig = config.getJSONObject("comparator");
					
					if (true == config.getBoolean("is_set")) {
						m = new NumberSetMatcher(paramConfig);
					} else {
						m = new NumberMatcher(paramConfig);
					}
				}
				
				String predicate = config.getString("property");				// create the property object to be compared
				Property p = ResourceFactory.createProperty(predicate);
				
				Score s = m.match(p, res1, res2);	
				ratio = getRatio(s.getPredicate().getURI());
				if (s.getScoreType() == ScoreType.NORMAL) {
					sim = s.getSimilarity();
					if (sim < threshold) {
						canHalt = true;
						break;
					}
					totalSim *= sim;
					sList.add(s);
				} else {			// if value of this property is missing, then punish it.
					totalSim *= (1-Math.sqrt(ratio));
				}
				totalRatio *= ratio;
				
				
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		if (canHalt) {
			ms.setFinalScore(0);
		} else {
			ms.setScoreList(sList);				// add the detailed compare result of each property into a score list.
			ms.setFinalScore((1 - totalRatio) * totalSim);	// aggregate score of properties
		}
		return ms;
	}

	private double calcRatio(List<Score> sList) {
		double ratio, sim, totalRatio = 1, totalSim = 1;
		
		for (Score s : sList) {
			ratio = getRatio(s.getPredicate().getURI());
			if (s.getScoreType() == ScoreType.NORMAL) {
				sim = s.getSimilarity();
				if (sim < threshold) {
					sim = 0.01;
				}
				totalSim *= sim;
			} else {			// if value of this property is missing, then punish it.
				totalSim *= (1-Math.sqrt(ratio));
			}
			totalRatio *= ratio;
		}
		return (1 - totalRatio) * totalSim;
	}

	private double getRatio(String uri) {
		RatioFileUtil util = new RatioFileUtil();
		Map<String, Double> map = util.getDefaultRatio();
		return map.get(uri);
	}

}
