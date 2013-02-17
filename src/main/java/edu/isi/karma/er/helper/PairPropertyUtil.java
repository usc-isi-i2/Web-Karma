package edu.isi.karma.er.helper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PairPropertyUtil {

	public Map<String, Map<String, Double>> loadPairFreqMap() {
		File file = new File(Constants.PATH_RATIO_FILE + "birthYear_deathYear.json");
		if (!file.exists()) {
			throw new IllegalArgumentException("file name " + file.getAbsolutePath() + " does not exist.");
		}
		return loadPairFreqMap(file);
	}

	public Map<String, Map<String, Double>> loadPairFreqMap(File file) {
		JSONArray arr = loadConfig(file);
		Map<String, Map<String, Double>> map = new TreeMap<String, Map<String, Double>>();
		try {
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.getJSONObject(i);
				if (obj.keys().hasNext()) {
					String str = obj.keys().next().toString();
					JSONArray subArr = obj.getJSONArray(str);
					Map<String, Double> subMap = new TreeMap<String, Double>();
					for (int j = 0; j < subArr.length(); j++) {
						JSONObject subObj = subArr.getJSONObject(j);
						if (subObj.keys().hasNext()) {
							String s2 = subObj.keys().next().toString();
							subMap.put(s2, subObj.getDouble(s2));
						}
					}
					map.put(str, subMap);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public double getPairCount(Map<String, Map<String, Double>> map, String s1, String s2) {
		double count = 0;
		if (map.containsKey(s1) && map.get(s1).containsKey(s2))
			count = map.get(s1).get(s2);
		return count;
	}
	
	public double getRangePairCount(Map<String, Map<String, Double>> map, String vs1, String vs2, String ws1, String ws2) {
		double sum = 0;
		int v1, v2, w1, w2, t;
		try {
			v1 = Integer.parseInt(vs1);
			v2 = Integer.parseInt(vs2);
			w1 = Integer.parseInt(ws1);
			w2 = Integer.parseInt(ws2);
			if (v1 > v2) { t = v1; v1 = v2; v2 = t;}
			if (w1 > w2) { t = w1; w1 = w2; w2 = t;}
			for (int i = v1; i <= v2; i++) {
				for (int j = w1; j <= w2; j++) {
					//sum *= 1-Math.sqrt(getPairCount(map, String.valueOf(i), String.valueOf(j)));
					sum += getPairCount(map, String.valueOf(i), String.valueOf(j));
				}
			} 
		} catch (NumberFormatException nfe) {
			
		}
		return sum;
	}
	
	public JSONArray loadConfig(File file) {
		RandomAccessFile raf = null;
		StringBuffer sb = new StringBuffer();
		JSONArray arr = null;
		
		try {
			String str = null;
			raf = new java.io.RandomAccessFile(file, "rw");
			while ((str = raf.readLine()) != null) {
				sb.append("\n").append(str);
			}
			arr = new JSONArray(sb.toString());
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return arr;
	}
}
