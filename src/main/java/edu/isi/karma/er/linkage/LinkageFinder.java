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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.aggregator.impl.AverageAggregator;
import edu.isi.karma.er.aggregator.impl.RatioMultiplyAggregator;
import edu.isi.karma.er.aggregator.impl.RatioPossibilityAggregator;
import edu.isi.karma.er.aggregator.impl.RatioWeightAggregator;
import edu.isi.karma.er.helper.ConfigUtil;
import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.RatioFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.ResultRecord;
import edu.isi.karma.er.test.calPosibility;

public class LinkageFinder {

	private JSONArray confArr = null;		// configuration data, will be loaded from /config/configuration.json during constructor.
	private Logger log = Logger.getLogger(LinkageFinder.class);
	
	
	public LinkageFinder() {
		ConfigUtil util = new ConfigUtil();
		util.loadConstants();
		confArr = util.loadConfig();
		
	}
	
	public List<ResultRecord> findLinkage(Model srcModel, Model dstModel) {
		
		String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";	// property to retrieve all the subjects from models.
		Property RDF = ResourceFactory.createProperty(RDF_TYPE);
		int THREAD_NUM = 10;
		
		List<Resource> list1 = srcModel.listSubjectsWithProperty(RDF).toList();
		List<Resource> list2 = dstModel.listSubjectsWithProperty(RDF).toList();
		System.out.println("list size:" + list1.size() + "|" + list2.size());
		
		
		// load aggregator type from configuration, which generally locates in the end of configuration json file. 
		JSONObject aggr = null;
		String aggrStr = "";
		JSONArray confArr2 = new JSONArray();
		
		try {
			aggr = confArr.getJSONObject(confArr.length() - 1);
			aggrStr = aggr.optString("aggregator");
			
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
			aver = new AverageAggregator();
		} else if ("ratio_weight".equalsIgnoreCase(aggr.optString("aggregator"))) {
			aver = new RatioWeightAggregator(null);
		} else if ("possibility".equalsIgnoreCase(aggr.optString("aggregator"))) {
			aver = new RatioPossibilityAggregator(new calPosibility());
		} else if ("ratio_multiply".equalsIgnoreCase(aggr.optString("aggregator"))) {
			aver = new RatioMultiplyAggregator();
		} else {
			System.err.println("aggregator error");
		}
		
		//long startTime = System.currentTimeMillis();
		
		int i = 0;
		//DecimalFormat df = new DecimalFormat("0.000");
		List<ResultRecord> resultList = new ArrayList<ResultRecord>();
		// long lines = list1.size() * list2.size();			// total times of cross comparing
		List<List<Resource>> dstListArr = new ArrayList<List<Resource>>(THREAD_NUM);
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
		
		return resultList;
	}

	private List<ResultRecord> mergeResultList(List<ResultRecord> resultList,
			List<ResultRecord> resultList2) {
		for (ResultRecord subRec : resultList2) {
			int size = resultList.size();
			int i;
			for (i = 0; i < size; i++) {
				ResultRecord rec = resultList.get(i);
				if (rec.getRes().getURI().equals(subRec.getRes().getURI())) {
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
}
