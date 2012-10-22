package edu.isi.karma.er.test.old;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

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
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.PairPropertyUtil;
import edu.isi.karma.er.helper.entity.PersonProperty;
import edu.isi.karma.er.helper.entity.SaamPerson;

public class ConstructPairPropertyMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Model srcModel = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "dbpedia/").getDefaultModel();
		// Model dstModel = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "dbpedia_a/").getDefaultModel();
		String p1 = "http://americanart.si.edu/saam/birthYear";
		String p2 = "http://americanart.si.edu/saam/deathYear";
		
		PairPropertyUtil util = new PairPropertyUtil();
		//Map<String, Map<String, Double>> map = configPairMap(srcModel, p1, p2);
		Map<String, Map<String, Double>> map = util.loadPairFreqMap();
		
		try {
			output(map);
			//output2File(map);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static Map<String, Map<String, Double>> configPairMap(
			Model model, String p1, String p2) {
		Map<String, Map<String, Double>> map = new TreeMap<String, Map<String, Double>>();
		List<SaamPerson> list = loadOntologies(model);
		int len = list.size();
		int i = 0; 
		PersonProperty pro1, pro2;
		for (SaamPerson s : list) {
			i ++;
			pro1 = s.getProperty(p1);
			pro2 = s.getProperty(p2);
			boolean p1Notnull = (pro1 != null && pro1.getValue() != null);
			boolean p2Notnull = (pro2 != null && pro2.getValue() != null);
			Map<String, Double> subMap = null;
			
			if (p1Notnull && p2Notnull) {
				String s1 = pro1.getValue().get(0);
				String s2 = pro2.getValue().get(0);
				if (!isParsable(s1) || !isParsable(s2))
					continue;
				subMap = map.get(s1);
				if (subMap == null) {
					subMap = new TreeMap<String, Double>();
				}
				Double count = subMap.get(s2);
				if (count == null)
					count = 0d;
				count += 1.0 / len;
				subMap.put(s2, count.doubleValue());
				map.put(s1, subMap);

				
			} else {
				/*
				if (p1Notnull) {
					String s1 = pro1.getValue().get(0);
					if (!isParsable(s1))
						continue;
					String s2 = "__EMPTY2__";
					subMap = map.get(s1);
					if (subMap == null) {
						subMap = new TreeMap<String, Integer>();
					}
					Integer total = subMap.get(s2);
					if (total == null) {
						total = 0;
					}
					total ++;
					subMap.put(s2, total);
					map.put(s1, subMap);
				} else  if (p2Notnull){
					String s1 = "__EMPTY1__";
					String s2 = pro2.getValue().get(0);
					if (!isParsable(s2))
						continue;
					subMap = map.get(s1);
					if (subMap == null) {
						subMap = new TreeMap<String, Integer>();
					}
					Integer count = subMap.get(s2);
					if (count == null)
						count = 0;
					count ++;
					subMap.put(s2, count);
					map.put(s1, subMap);
				} else {
					String s1 = "__EMPTY1__";
					String s2 = "__EMPTY2__";
					subMap = map.get(s1);
					if (subMap == null) {
						subMap = new TreeMap<String, Integer>();
					}
					Integer count = subMap.get(s2);
					if (count == null)
						count = 0;
					count ++;
					subMap.put(s2, count);
					map.put(s1, subMap);
				}*/
			}
		}
		System.out.println(i + " rows processed.");
		return map;
	}

	private static void output(Map<String, Map<String, Double>> map) throws JSONException {
		int total = 0, subTotal = 0;
		for (String str : map.keySet()) {
			System.out.print(str + ":");
			subTotal = 0;
			Map<String, Double>subMap = map.get(str);
			for (String s2 : subMap.keySet()) {
				System.out.print("\t" + s2 + ":" + subMap.get(s2));
				subTotal += subMap.get(s2);
				
			}
			total += subTotal;
			System.out.print(subTotal + "\n");
		}
		
		System.out.println("total:" + total);
		
		
	}

	private static void output2File(
			Map<String, Map<String, Double>> map) throws JSONException {
		double total = 0, subTotal = 0;
		DecimalFormat df = new DecimalFormat("0.0000000");
		JSONArray arr = new JSONArray();
		for (String str : map.keySet()) {
			JSONObject obj = new JSONObject();
			JSONArray subArr = new JSONArray();
			System.out.print(str + ":");
			subTotal = 0;
			Map<String, Double>subMap = map.get(str);
			for (String s2 : subMap.keySet()) {
				JSONObject subObj = new JSONObject();
				//System.out.print("\t" + s2 + ":" + subMap.get(s2));
				subTotal += subMap.get(s2);
				subObj.put(s2, df.format(subMap.get(s2)));
				subArr.put(subObj);
			}
			obj.put(str, subArr);
			arr.put(obj);
			total += subTotal;
			System.out.print(df.format(subTotal) + "\n");
		}
		
		System.out.println("total:" + df.format(total));
		
		File file = new File(Constants.PATH_RATIO_FILE + "birthYear_deathYear.json");
		if (file.exists())
			file.delete();
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.writeBytes(arr.toString(2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static List<SaamPerson> loadOntologies(Model model) {
		String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";	// property to retrieve all the subjects from models.
		Property RDF = ResourceFactory.createProperty(RDF_TYPE);
		ResIterator iter = model.listSubjectsWithProperty(RDF);
		
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
				PersonProperty p = new PersonProperty();
				p.setPredicate(pred);
				
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
	
	private static boolean isParsable(String str) {
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
}
