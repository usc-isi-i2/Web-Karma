package edu.isi.karma.er.linkage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.aggregator.impl.AverageAggregator;
import edu.isi.karma.er.aggregator.impl.RatioMultiplyAggregator;
import edu.isi.karma.er.aggregator.impl.RatioWeightAggregator;
import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.OntologyUtil;
import edu.isi.karma.er.helper.RatioFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.helper.entity.ResultRecord;

public class LinkageFinder {

	private JSONArray confArr = null;		// configuration data, will be loaded from /config/configuration.json during constructor.
	private Logger log = Logger.getLogger(LinkageFinder.class);
	private double threshold;
	
	public LinkageFinder(JSONArray confArr) {
		this.confArr = confArr;
		
	}
	
	public List<ResultRecord> findLinkage(Model srcModel, Model dstModel) {
		
		int THREAD_NUM = 3;
		OntologyUtil outil = new OntologyUtil();
		List<Ontology> list1 = outil.loadSaamPersonOntologies(srcModel);
		List<Ontology> list2 = outil.loadSaamPersonOntologies(dstModel);
		System.out.println("list size:" + list1.size() + "|" + list2.size());
		
		
		// load aggregator type from configuration, which generally locates in the end of configuration json file. 
		JSONObject aggr = null;
		String aggrStr = "";
		JSONArray confArr2 = new JSONArray();
		List<String> dependArr = new ArrayList<String>();
		
		try {
			aggr = confArr.getJSONObject(confArr.length() - 1);
			aggrStr = aggr.optString("aggregator");
			
			// parsing pair properties from configuration file
			JSONArray dpArr = aggr.optJSONArray("dependencies");
			for (int i = 0; i < dpArr.length(); i++) {
				dependArr.add(dpArr.getJSONObject(i).getString("source"));	// put source property into odd row
				dependArr.add(dpArr.getJSONObject(i).getString("target"));	// put target property into even row
			}
			
			//confArr.remove(confArr.length() - 1);		// remove the aggregator info from configuration file, in order to not affect the parse process for properties
			
			for (int i = 0; i < confArr.length() - 1; i++) {  // can not use method 'remove' in old version of json, so just copy to a new array discard the last row.
				confArr2.put(confArr.get(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		// Map<String, Map<String, Double>> ratioMapList = loadRatioFilesFromConfig(confArr);
		log.info("[[[ Aggregator: " + aggrStr + "]]]");
		// initialize aggregator in term of loaded aggregator type.
		Aggregator aver = null;
		
		if ("average".equals(aggr.optString("aggregator"))) {
			aver = new AverageAggregator(confArr2);
		} else if ("ratio_weight".equalsIgnoreCase(aggr.optString("aggregator"))) {
			aver = new RatioWeightAggregator(confArr2);
		} else if ("possibility".equalsIgnoreCase(aggr.optString("aggregator"))) {
			//aver = new RatioPossibilityAggregator(new calPosibility());
		} else if ("ratio_multiply".equalsIgnoreCase(aggr.optString("aggregator"))) {
			aver = new RatioMultiplyAggregator(confArr2, dependArr, threshold);
		} else {
			System.err.println("aggregator error");
		}
		
		//long startTime = System.currentTimeMillis();
		
		int i = 0;
		List<ResultRecord> resultList = new ArrayList<ResultRecord>();
		
		
		// long lines = list1.size() * list2.size();			// total times of cross comparing
		List<List<Ontology>> dstListArr = new ArrayList<List<Ontology>>(THREAD_NUM);
		int total = list2.size();
		int pageSize = total / THREAD_NUM;
		for (i = 0; i < THREAD_NUM - 1; i++) {
			dstListArr.add(i, list2.subList(i * pageSize, (i + 1) * pageSize));
		}
		dstListArr.add(THREAD_NUM - 1, list2.subList((THREAD_NUM - 1) * pageSize, total));
		
		Thread[] threads = new Thread[THREAD_NUM];
		for (i = 0; i < THREAD_NUM; i++) {
			threads[i] = new LinkageFinderThread(list1, dstListArr.get(i), aver, confArr2);
			threads[i].start();
		}
		
		try {
			for (i = 0; i < THREAD_NUM; i++) {
				threads[i].join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (i = 0; i < THREAD_NUM; i++) {
			resultList = mergeResultList(resultList, ((LinkageFinderThread)threads[i]).getResultList());
		}
		/*
		long startTime = System.currentTimeMillis();
		for (Ontology res1 : list1) {
			if (++i % 100 == 0) {
				log.info(" processed " + i + " rows in " + (System.currentTimeMillis() - startTime) + "ms.");
				startTime = System.currentTimeMillis();
				
			}
			ResultRecord rec = new ResultRecord();
			rec.setRes(res1);
			int j = 0;
			for (Ontology res2 : list2) {
				//if (++i % 1000000 == 0) {
				//	log.info(" processed " + i + " rows in " + (System.currentTimeMillis() - startTime) + "ms.");
				//	startTime = System.currentTimeMillis();
				//}
				MultiScore ms = aver.match(res1, res2); 	// compare 2 resource to return a result of match with match details
				if ( ms.getFinalScore() > rec.getCurrentMinScore()) {	// to decide whether current pair can rank to top 5 
					rec.addMultiScore(ms);
				}
			}
			
			//log.info("[" + df.format(rec.getCurrentMinScore()) + " | " + df.format(rec.getCurrentMaxScore()) + "] " + rec.getRes() + " has " + rec.getRankList().size() + " results");
			resultList.add(rec);
		}
		*/
		return resultList;
	}

	private List<ResultRecord> mergeResultList(List<ResultRecord> resultList,
			List<ResultRecord> resultList2) {
		for (ResultRecord subRec : resultList2) {
			int size = resultList.size();
			int i;
			for (i = 0; i < size; i++) {
				ResultRecord rec = resultList.get(i);
				if (rec.getRes().getSubject().equals(subRec.getRes().getSubject())) {
					for (MultiScore ms : subRec.getRankList()) {
						if (ms.getFinalScore() > rec.getCurrentMinScore()) {
							rec.addMultiScore(ms);
						}
					}
					break;
				}
			}
			if (i >= size) {
				resultList.add(subRec);
			}
		}
		return resultList;
		
	}

	/**
	 * Load required ratio files from configurations.
	 * @param confArr the configuration array containing properties involved in comparison.
	 * @return the map of pair(string,map<string, double>) which string denotes the full uri of property, the map denotes the ratio of each unique value of that property
	 */
	@SuppressWarnings("unused")
	private Map<String, Map<String, Double>> loadRatioFilesFromConfig(
			JSONArray confArr) {
		Map<String, Map<String, Double>> ratioMap = new HashMap<String, Map<String, Double>>();
		RatioFileUtil util = new RatioFileUtil();
		try {
			for (int i = 0; i < confArr.length() - 1; i++) {
				JSONObject json = confArr.getJSONObject(i);
				String propertyName = json.getString("property");
				String ratioFile = ((JSONObject) json.get("comparator")).getString("ratio_file");
				Map<String, Double> map = util.getRatioMap(Constants.PATH_RATIO_FILE + ratioFile);
				ratioMap.put(propertyName, map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ratioMap;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	
}
