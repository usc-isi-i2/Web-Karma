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
		/* sample frequency for QGram
		nameList.add(new SimilarityFrequency(0.975, 0.0000011));
		nameList.add(new SimilarityFrequency(0.95, 0.00000137));
		nameList.add(new SimilarityFrequency(0.9, 0.00000381));
		nameList.add(new SimilarityFrequency(0.85, 0.0000161));
		nameList.add(new SimilarityFrequency(0.80, 0.0000806));
		nameList.add(new SimilarityFrequency(0.7, 0.000837));
		
		nameList.add(new SimilarityFrequency(0.999999, 0.000000002);
		nameList.add(new SimilarityFrequency(0.99, 0.0000003183));
		nameList.add(new SimilarityFrequency(0.98, 0.0000003275));
		nameList.add(new SimilarityFrequency(0.97, 0.0000004022));
		nameList.add(new SimilarityFrequency(0.96, 0.0000004918));
		nameList.add(new SimilarityFrequency(0.95, 0.0000005261));
		nameList.add(new SimilarityFrequency(0.94, 0.0000006521));
		nameList.add(new SimilarityFrequency(0.93, 0.0000007769));
		nameList.add(new SimilarityFrequency(0.92, 0.0000010166));
		nameList.add(new SimilarityFrequency(0.91, 0.0000011663));
		nameList.add(new SimilarityFrequency(0.90, 0.0000014298));
		nameList.add(new SimilarityFrequency(0.89, 0.0000017477));
		nameList.add(new SimilarityFrequency(0.88, 0.0000021021));
		nameList.add(new SimilarityFrequency(0.87, 0.0000026224));
		nameList.add(new SimilarityFrequency(0.86, 0.0000032315));
		nameList.add(new SimilarityFrequency(0.85, 0.0000040707));
		nameList.add(new SimilarityFrequency(0.84, 0.0000047562));
		nameList.add(new SimilarityFrequency(0.83, 0.0000058987));
		nameList.add(new SimilarityFrequency(0.82, 0.0000070961));
		nameList.add(new SimilarityFrequency(0.81, 0.0000088097));
		nameList.add(new SimilarityFrequency(0.80, 0.0000115049));
		
		/* sample frequency for JaroWinkler */
		/*
		nameList.add(new SimilarityFrequency(0.975, 0.00000118));
		nameList.add(new SimilarityFrequency(0.95, 0.000004627));
		nameList.add(new SimilarityFrequency(0.9, 0.000351392));
		nameList.add(new SimilarityFrequency(0.85, 0.001918920));
		nameList.add(new SimilarityFrequency(0.80, 0.002764621)); 
		nameList.add(new SimilarityFrequency(0.7, 0.01));*/
		nameList.add(new SimilarityFrequency(0.999999, 0.000000016));
		nameList.add(new SimilarityFrequency(0.99, 0.000000023));
		nameList.add(new SimilarityFrequency(0.98, 0.0000000796));
		nameList.add(new SimilarityFrequency(0.97, 0.000000280));
		nameList.add(new SimilarityFrequency(0.96, 0.000008239));
		nameList.add(new SimilarityFrequency(0.95, 0.0000029437));
		nameList.add(new SimilarityFrequency(0.94, 0.000008769)); 
		nameList.add(new SimilarityFrequency(0.93, 0.000025324));
		nameList.add(new SimilarityFrequency(0.92, 0.000060224));
		nameList.add(new SimilarityFrequency(0.91, 0.00014636));
		nameList.add(new SimilarityFrequency(0.90, 0.00026212));
		nameList.add(new SimilarityFrequency(0.89, 0.00044969));
		nameList.add(new SimilarityFrequency(0.88, 0.00070759));
		nameList.add(new SimilarityFrequency(0.87, 0.00098796));
		nameList.add(new SimilarityFrequency(0.86, 0.00126755));
		nameList.add(new SimilarityFrequency(0.85, 0.00151347));
		nameList.add(new SimilarityFrequency(0.84, 0.0017195));
		nameList.add(new SimilarityFrequency(0.83, 0.0019026));
		nameList.add(new SimilarityFrequency(0.82, 0.0020911));
		nameList.add(new SimilarityFrequency(0.81, 0.0022926));
		nameList.add(new SimilarityFrequency(0.80, 0.0024964));
		//nameList.add(new SimilarityFrequency(0.7, 0.01));
		map.put("http://americanart.si.edu/saam/fullName", nameList);
		
		List<SimilarityFrequency> birthList = new ArrayList<SimilarityFrequency>();
		/*
		birthList.add(new SimilarityFrequency(0.999999, 0.00011));
		birthList.add(new SimilarityFrequency(0.975, 0.00654));
		birthList.add(new SimilarityFrequency(0.95, 0.01933));
		birthList.add(new SimilarityFrequency(0.9, 0.03390));
		birthList.add(new SimilarityFrequency(0.85, 0.04568));
		birthList.add(new SimilarityFrequency(0.8, 0.06137));
		birthList.add(new SimilarityFrequency(0.7, 0.09562));
		*/
		birthList.add(new SimilarityFrequency(0.999999, 0.006655));
		birthList.add(new SimilarityFrequency(0.99, 0.019945));
		birthList.add(new SimilarityFrequency(0.98, 0.0332));
		birthList.add(new SimilarityFrequency(0.97, 0.04636));
		birthList.add(new SimilarityFrequency(0.96, 0.05940));
		birthList.add(new SimilarityFrequency(0.95, 0.07227));
		birthList.add(new SimilarityFrequency(0.94, 0.08500));
		birthList.add(new SimilarityFrequency(0.93, 0.09753));
		birthList.add(new SimilarityFrequency(0.92, 0.10991));
		birthList.add(new SimilarityFrequency(0.91, 0.12209));
		birthList.add(new SimilarityFrequency(0.9, 0.13409));
		birthList.add(new SimilarityFrequency(0.89, 0.14594));
		birthList.add(new SimilarityFrequency(0.88, 0.15760));
		birthList.add(new SimilarityFrequency(0.87, 0.16912));
		birthList.add(new SimilarityFrequency(0.86, 0.18049));
		birthList.add(new SimilarityFrequency(0.85, 0.19170));
		birthList.add(new SimilarityFrequency(0.84, 0.20279));
		birthList.add(new SimilarityFrequency(0.83, 0.21373));
		birthList.add(new SimilarityFrequency(0.82, 0.22454));
		birthList.add(new SimilarityFrequency(0.81, 0.23520));
		birthList.add(new SimilarityFrequency(0.8, 0.24578));
		map.put("http://americanart.si.edu/saam/birthYear", birthList);
		
		List<SimilarityFrequency> deathList = new ArrayList<SimilarityFrequency>();
		/*
		deathList.add(new SimilarityFrequency(0.999999, 0.00011));
		deathList.add(new SimilarityFrequency(0.975, 0.00564));
		deathList.add(new SimilarityFrequency(0.95, 0.01571));
		deathList.add(new SimilarityFrequency(0.9, 0.02781));
		deathList.add(new SimilarityFrequency(0.85, 0.03772));
		deathList.add(new SimilarityFrequency(0.8, 0.04952));
		deathList.add(new SimilarityFrequency(0.7, 0.08081));
		*/
		deathList.add(new SimilarityFrequency(0.999999, 0.005725));
		deathList.add(new SimilarityFrequency(0.99, 0.01703));
		deathList.add(new SimilarityFrequency(0.98, 0.02809));
		deathList.add(new SimilarityFrequency(0.97, 0.03886));
		deathList.add(new SimilarityFrequency(0.96, 0.04934));
		deathList.add(new SimilarityFrequency(0.95, 0.05958));
		deathList.add(new SimilarityFrequency(0.94, 0.06955));
		deathList.add(new SimilarityFrequency(0.93, 0.07932));
		deathList.add(new SimilarityFrequency(0.92, 0.08892));
		deathList.add(new SimilarityFrequency(0.91, 0.09835));
		deathList.add(new SimilarityFrequency(0.90, 0.10764));
		deathList.add(new SimilarityFrequency(0.89, 0.11680));
		deathList.add(new SimilarityFrequency(0.88, 0.12582));
		deathList.add(new SimilarityFrequency(0.87, 0.13473));
		deathList.add(new SimilarityFrequency(0.86, 0.14354));
		deathList.add(new SimilarityFrequency(0.85, 0.15222));
		deathList.add(new SimilarityFrequency(0.84, 0.16081));
		deathList.add(new SimilarityFrequency(0.83, 0.16929));
		deathList.add(new SimilarityFrequency(0.82, 0.17763));
		deathList.add(new SimilarityFrequency(0.81, 0.18587));
		deathList.add(new SimilarityFrequency(0.80, 0.19402));
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
