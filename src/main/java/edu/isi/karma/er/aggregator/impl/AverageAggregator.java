package edu.isi.karma.er.aggregator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.matcher.Matcher;
import edu.isi.karma.er.matcher.impl.NumberMatcher;
import edu.isi.karma.er.matcher.impl.NumberSetMatcher;
import edu.isi.karma.er.matcher.impl.StringMatcher;
import edu.isi.karma.er.matcher.impl.StringSetMatcher;

public class AverageAggregator implements Aggregator {

	private Map<String, Matcher> compMap = null;
	
	public AverageAggregator(JSONArray confArr) {
		compMap = parseConfig(confArr);
	}
	
	private Map<String, Matcher> parseConfig(JSONArray confArr) {
		Map<String, Matcher> map = new HashMap<String, Matcher>();
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
				map.put(predicate, m);

				
				
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return map;
	}
	
	public MultiScore match(Ontology o1, Ontology o2) {
		
		MultiScore ms = new MultiScore();
		ms.setSrcSubj(o1);
		ms.setDstSubj(o2);
		List<Score> sList = new ArrayList<Score>();
		double sum = 0;
		
		for (String pred : compMap.keySet()) {
			Matcher m = compMap.get(pred);
			Score s = m.match(pred, o1, o2);
			sList.add(s);
			sum += s.getSimilarity();
		}
		
		if (compMap.size() > 0) {
			ms.setFinalScore(sum / compMap.size());
		}
		ms.setScoreList(sList);
		return ms;
	
		
	}

}
