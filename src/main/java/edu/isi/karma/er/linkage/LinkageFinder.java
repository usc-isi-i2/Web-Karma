package edu.isi.karma.er.linkage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.aggregator.impl.AverageAggregator;
import edu.isi.karma.er.aggregator.impl.RatioMultiplyAggregator;
import edu.isi.karma.er.aggregator.impl.RatioWeightAggregator;
import edu.isi.karma.er.helper.ConfigUtil;
import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.RatioFileUtil;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.PersonProperty;
import edu.isi.karma.er.helper.entity.ResultRecord;
import edu.isi.karma.er.helper.entity.SaamPerson;

public class LinkageFinder {

	private JSONArray confArr = null;		// configuration data, will be loaded from /config/configuration.json during constructor.
	private Logger log = Logger.getLogger(LinkageFinder.class);
	
	
	public LinkageFinder() {
		ConfigUtil util = new ConfigUtil();
		util.loadConstants();
		confArr = util.loadConfig();
		
	}
	
	public List<ResultRecord> findLinkage(Model srcModel, Model dstModel) {
		
		int THREAD_NUM = 3;
		
		List<SaamPerson> list1 = loadOntologies(srcModel);
		List<SaamPerson> list2 = loadOntologies(dstModel);
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
			aver = new RatioMultiplyAggregator(confArr2, dependArr);
		} else {
			System.err.println("aggregator error");
		}
		
		//long startTime = System.currentTimeMillis();
		
		int i = 0;
		List<ResultRecord> resultList = new ArrayList<ResultRecord>();
		
		// long lines = list1.size() * list2.size();			// total times of cross comparing
		List<List<SaamPerson>> dstListArr = new ArrayList<List<SaamPerson>>(THREAD_NUM);
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
		for (SaamPerson res1 : list1) {
			if (++i % 100 == 0) {
				log.info(" processed " + i + " rows in " + (System.currentTimeMillis() - startTime) + "ms.");
				startTime = System.currentTimeMillis();
				
			}
			ResultRecord rec = new ResultRecord();
			rec.setRes(res1);
			for (SaamPerson res2 : list2) {
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
	
	private List<SaamPerson> loadOntologies(Model model) {
		Property RDF = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		ResIterator iter = model.listResourcesWithProperty(RDF);
		
		List<SaamPerson> list = new Vector<SaamPerson>();
		while (iter.hasNext()) {
			Resource res = iter.next();
			StmtIterator siter = res.listProperties();
			SaamPerson per = new SaamPerson();
			per.setSubject(res.getURI());
			
			while (siter.hasNext()) {
				Statement st = siter.next();
				String pred = st.getPredicate().getURI();
				RDFNode node = st.getObject();
				PersonProperty p = per.getProperty(pred);
				if (p == null) {
					p = new PersonProperty();
					p.setPredicate(pred);
				}
				
				if (node != null) {
					if (pred.indexOf("fullName") > -1) {
						p.setValue(node.asLiteral().getString());
						per.setFullName(p);
					} else if (pred.indexOf("birthYear") > -1) {
						p.setValue(node.asLiteral().getString());
						per.setBirthYear(p);
					} else if (pred.indexOf("deathYear") > -1) {
						p.setValue(node.asLiteral().getString());
						per.setDeathYear(p);
					}
				}
			}
			list.add(per);
		}
		return list;
	}
}
