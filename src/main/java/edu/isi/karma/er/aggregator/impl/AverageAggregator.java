package edu.isi.karma.er.aggregator.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.matcher.Matcher;
import edu.isi.karma.er.matcher.impl.NumberMatcher;
import edu.isi.karma.er.matcher.impl.StringMatcher;
import edu.isi.karma.er.matcher.impl.StringSetMatcher;

public class AverageAggregator implements Aggregator {

	@SuppressWarnings("unchecked")
	public MultiScore match(JSONArray confArr, Resource res1, Resource res2) {
		MultiScore ms = new MultiScore();
		ms.setSrcSubj(res1);
		ms.setDstSubj(res2);
		List<Score> sList = new ArrayList<Score>();
		double sum = 0;
		
		for (int i = 0; i < confArr.length(); i++) {
			try {
				JSONObject config = confArr.getJSONObject(i);
				String className = "";
				Matcher m = null;
				
				if ("string".equalsIgnoreCase(config.optString("type"))) {
					
					className = ((JSONObject)config.get("comparator")).getString("class");
					Class<StringComparator> clazz = (Class<StringComparator>) Class.forName(className);
					StringComparator comp = clazz.newInstance();
					
					if (true == ((JSONObject)config.get("comparator")).getBoolean("is_set")) {
						m = new StringSetMatcher(comp);
						
					} else {
						m = new StringMatcher(comp);
					}
					
				} else if ("number".equalsIgnoreCase(config.optString("type"))) {
					className = ((JSONObject)config.get("comparator")).getString("class");
					// Class<NumberComparator> clazz = (Class<NumberComparator>) Class.forName(className);
					
					m = new NumberMatcher((JSONObject)config.get("comparator"));
				}
				
				String predicate = config.getString("property");
				Property p = ResourceFactory.createProperty(predicate);
				Score s = m.match(p, res1, res2);
				sList.add(s);
				sum += s.getSimilarity();
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		if (confArr.length() > 0) {
			ms.setFinalScore(sum / confArr.length());
		}
		ms.setScoreList(sList);
		return ms;
	}

}
