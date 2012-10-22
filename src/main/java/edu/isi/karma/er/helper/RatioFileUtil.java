package edu.isi.karma.er.helper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.er.helper.entity.SimilarityFrequency;


public class RatioFileUtil {

	/**
	 * Get calculated ratio from a ratio files.
	 * @param ratioFile, the file contains ratio results calculated before.
	 * @return a map of ratio map in which key for string and value for the ratio of key string. 
	 */
	public Map<String, Double> getRatioMap(String ratioFile) {
		Map<String, Double> ratioMap = new HashMap<String, Double>();
		File file = new File(ratioFile);
		
		if (file == null || !file.exists()) {
			throw new IllegalArgumentException("the file input " + file.getAbsolutePath() + " does not exist actually.");
		}
		
		RandomAccessFile raf = null;
		
		
		try {
			String str = null;
			raf = new java.io.RandomAccessFile(file, "rw");
			
			
			str = raf.readLine();	// read the first line which contains the total number of data, should look like '__total__:15532'
			if (str.indexOf("__total__") < 0 || str.indexOf(":") < 0) {
				throw new IllegalArgumentException("It seems something wrong with the file format.");
			}
			
			double total = Double.parseDouble(str.split(":")[2]);
			ratioMap.put("__total__", total);
			
			while ((str = raf.readLine()) != null) {			// read a text line each time from ratio file
				String[] lines = str.split(":");
				Double value = null;
				if (lines.length >= 3) {						// 2 elements separated by ':' for a text line.
					try {
						value = Double.parseDouble(lines[2]);	// if the 2nd element can be parsed as a double, then it's a valid record
						ratioMap.put(lines[0], value / total);
					} catch (NumberFormatException nfd) {}
				}
			}
			
		} catch (IOException e) {
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
		
		return ratioMap;
	}
	
	public Map<String, Double> getDefaultRatio() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("http://americanart.si.edu/saam/birthYear", 1.0 / 1025); //1025.0 / 466020);
		map.put("http://americanart.si.edu/saam/deathYear", 1.0 / 1397); //1397.0/ 206280);
		map.put("http://americanart.si.edu/saam/associatedYear", 1.0 / 125); // 125.0 / 16717);
		map.put("http://americanart.si.edu/saam/fullName", 1.0 / 1045595);   // 1045595.0 / 1067256);
		map.put("http://americanart.si.edu/saam/city", 1.0 / 7164); 	 // 7164.0 / 27031);
		map.put("http://americanart.si.edu/saam/state", 1.0 / 216); 	 // 216.0 / 22749);
		map.put("http://americanart.si.edu/saam/country", 1.0 / 124); 	 // 124.0 / 21237);
		
		return map;
	}
	
	public Map<String, List<SimilarityFrequency>> loadDefaultFrequency() {
		Map<String, List<SimilarityFrequency>> map = new HashMap<String, List<SimilarityFrequency>>();
		List<SimilarityFrequency> nameList = new ArrayList<SimilarityFrequency>();
		nameList.add(new SimilarityFrequency(0.975, 0.0000011));
		nameList.add(new SimilarityFrequency(0.95, 0.00000137));
		nameList.add(new SimilarityFrequency(0.9, 0.00000381));
		nameList.add(new SimilarityFrequency(0.85, 0.0000161));
		nameList.add(new SimilarityFrequency(0.80, 0.0000806));
		nameList.add(new SimilarityFrequency(0.7, 0.000837));
		map.put("http://americanart.si.edu/saam/fullName", nameList);
		
		List<SimilarityFrequency> birthList = new ArrayList<SimilarityFrequency>();
		birthList.add(new SimilarityFrequency(0.975, 0.00654));
		birthList.add(new SimilarityFrequency(0.95, 0.01933));
		birthList.add(new SimilarityFrequency(0.9, 0.03390));
		birthList.add(new SimilarityFrequency(0.85, 0.04568));
		birthList.add(new SimilarityFrequency(0.8, 0.06137));
		birthList.add(new SimilarityFrequency(0.7, 0.09562));
		map.put("http://americanart.si.edu/saam/birthYear", birthList);
		
		List<SimilarityFrequency> deathList = new ArrayList<SimilarityFrequency>();
		deathList.add(new SimilarityFrequency(0.975, 0.0564));
		deathList.add(new SimilarityFrequency(0.95, 0.01571));
		deathList.add(new SimilarityFrequency(0.9, 0.02781));
		deathList.add(new SimilarityFrequency(0.85, 0.03772));
		deathList.add(new SimilarityFrequency(0.8, 0.04952));
		deathList.add(new SimilarityFrequency(0.7, 0.08081));
		map.put("http://americanart.si.edu/saam/deathYear", deathList);
		
		return map;
	}
	
	public double queryFrequency(String predicate, double similarity) {
		double freq = 0;
		Map<String, List<SimilarityFrequency>> map = loadDefaultFrequency();
		List<SimilarityFrequency> list = map.get(predicate);
		int i = 0; 
		if (list == null) 
			return freq;
		for (i = 0; i < list.size(); i++) {
			SimilarityFrequency f = list.get(i);
			if (similarity >= f.getSimilarity()) {
				break;
			}
		}
		if (i < list.size())
			freq = list.get(i).getFrequency();
		
		return freq;
	}
}
