package edu.isi.karma.er.aggregator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.PairPropertyUtil;
import edu.isi.karma.er.helper.RatioFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.SaamPerson;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreType;
import edu.isi.karma.er.matcher.Matcher;
import edu.isi.karma.er.matcher.impl.NumberMatcher;
import edu.isi.karma.er.matcher.impl.NumberSetMatcher;
import edu.isi.karma.er.matcher.impl.StringMatcher;
import edu.isi.karma.er.matcher.impl.StringSetMatcher;

public class RatioMultiplyAggregator implements Aggregator {

	private double threshold = 0.85;
	
	private Map<String, Map<String, Double>> ratioMaps;
	
	private Map<String, Map<String, Double>> pairFreqMaps;
	
	private Map<String, Matcher> compMap;
	
	private List<String> dependArr;		// source property uri in odd row, target property uri in even row
	
	public RatioMultiplyAggregator(JSONArray confArr, List<String> dependArr) {
		compMap = parseConfig(confArr);
		ratioMaps = loadRatioMaps(confArr);
		pairFreqMaps = loadPairFreqMaps(dependArr);
	}
	
	private Map<String, Map<String, Double>> loadPairFreqMaps(List<String> dependArr) {
		this.dependArr = dependArr;
		PairPropertyUtil util = new PairPropertyUtil();
		return util.loadPairFreqMap();
	}

	private Map<String, Map<String, Double>> loadRatioMaps(JSONArray confArr) {
		Map<String, Map<String, Double>> ratioMaps = new HashMap<String, Map<String, Double>>();
		RatioFileUtil util = new RatioFileUtil();
			try {
				for(int i = 0; i < confArr.length(); i ++) {
						JSONObject conf = confArr.getJSONObject(i);
						Map<String, Double> map = util.getRatioMap(Constants.PATH_RATIO_FILE + "dbpedia/" + (conf.getJSONObject("comparator")).optString("ratio_file"));
						ratioMaps.put(conf.optString("property"), map);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		
		return ratioMaps;
		
	}

	private Map<String, Matcher> parseConfig(JSONArray confArr) {
		Map<String, Matcher> map = new HashMap<String, Matcher>();
		for (int i = 0; i < confArr.length(); i++) {
			try {
				JSONObject config = confArr.getJSONObject(i);
				Matcher m = null;

				if ("string".equalsIgnoreCase(config.optString("type"))) { // if the property is string-type
					JSONObject paramConfig = config.getJSONObject("comparator");

					if (true == config.getBoolean("is_set")) { // if this property possibly has several values.
						m = new StringSetMatcher(paramConfig);

					} else { // else has only single value
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

				String predicate = config.getString("property"); // create the property object to be compared
				map.put(predicate, m);

			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return map;
	}

	public MultiScore match(SaamPerson res1, SaamPerson res2) {
		double freq, sim, totalFreq = 1, ratio;
		int pairFlag = 0;
		// boolean canHalt = false;
		MultiScore ms = new MultiScore();
		ms.setSrcSubj(res1);
		ms.setDstSubj(res2);
		List<Score> sList = new ArrayList<Score>();
		
		// for each property to be compared in configuration array, load its configurations and initialize the detailed comparator to be invoked.
		for (String pred : compMap.keySet()) {
			Matcher m = compMap.get(pred);
			
			Score s = m.match(pred, res1, res2);		
			
			if (s.getScoreType() == ScoreType.NORMAL) {
				sim = s.getSimilarity();
				if (sim >= threshold) {
					if (1 - sim < 1e-6) {  // sim == 1
						ratio = (getRatio(pred, s.getSrcObj()) + getRatio(pred, s.getDstObj())) / 2;
						freq = ratio;
					} else {
						freq = getFrequency(s.getPredicate(), sim) ;
					}
					totalFreq *= (1-freq);
					pairFlag ++;
				} else {
					freq = 0;
					totalFreq = 0;
				}
				
			} else {			// if value of this property is missing, then punish it.
				freq = getFrequency(s.getPredicate(), threshold);
				totalFreq *= 1-freq;
				//ratio = getRatio(pred, "");
			}
			//totalRatio *= (1- ratio);
			s.setFreq(freq);
			sList.add(s);
			
			//if (totalFreq < 0.0026) {
			//	break;
			//}
		}
		if (pairFlag >= 3) { 
			totalFreq = dealWithDependent(sList);
		}
			ms.setScoreList(sList);				// add the detailed compare result of each property into a score list.
			ms.setFinalScore(totalFreq );	// aggregate score of properties
		
		return ms;
	}

	

	private double dealWithDependent(List<Score> sList) {
		double totalFreq = 1;
		for (int i = 0; i < dependArr.size() / 2; i+=2) {
			String sourcePred = dependArr.get(i + 0);
			String targetPred = dependArr.get(i + 1);
			String vs1 = null, vs2 = null, vt1 = null, vt2 = null;
			
			for (int j = 0; j < sList.size(); j++) {
				Score s = sList.get(j);
				if (s.getPredicate().equals(sourcePred)) {
					vs1 = s.getSrcObj();
					vs2 = s.getDstObj();
				} else if (s.getPredicate().equals(targetPred)) {
					vt1 = s.getSrcObj();
					vt2 = s.getDstObj();
				} else {
					totalFreq *= 1- s.getFreq();
				}
			}
			
			double freq = 1 - getRangeFrequency(vs1, vt1, vs2, vt2) ;
			totalFreq *= freq;
		}
		return totalFreq;
	}

	private double getRangeFrequency(String vs1, String vs2, String ws1,
			String ws2) {
		PairPropertyUtil util = new PairPropertyUtil();
		double sum = util.getRangePairCount(pairFreqMaps, vs1, ws1, vs2, ws2);
		return sum ;
	}


	private double getRatio(String uri, String value) {
		
		Map<String, Double> map = ratioMaps.get(uri);
		double total = map.get("__total__");
		Double ratio = map.get(value);
		if (ratio != null) {
			return ratio.doubleValue() ;
		} else {
			return 1 / total;
		}
	}
	
	private double getFrequency(String uri, double similarity) {
		RatioFileUtil util = new RatioFileUtil();
		return util.queryFrequency(uri, similarity);
	}

}
