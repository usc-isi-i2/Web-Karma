package edu.isi.karma.er.test.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.compare.impl.StringJaroWinklerComparatorImpl;
import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.entity.PersonProperty;
import edu.isi.karma.er.helper.entity.SaamPerson;

public class TestRandomFrequency {

	// private static String ATTR = "http://americanart.si.edu/saam/fullName";
	private static StringComparator comp = new StringJaroWinklerComparatorImpl();
	// private NumberComparator comp = new NumberComparatorImpl(0, 2012, 10);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// testCalcFreq();
		
		//testRandomFreq();
		//loadDataToCSV();
		generateString();
	}
	
	private static void generateString() {
		TestRandomFrequency t = new TestRandomFrequency();
		
		List<SaamPerson> perList = t.loadModel();
		int len = perList.size();
		System.out.println("finish loading model:" + len);
		
		Random r = new Random();
		String s1, s2;
		double score;
		List<String> list1, list2;
		SaamPerson p1, p2;
		int n1, n2, times = 100;
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < times; ) {
			n1 = r.nextInt(len);
			n2 = r.nextInt(len);
			if (n1 != n2) {
				p1 = perList.get(n1);
				p2 = perList.get(n2);
				
				if (p1.getFullName() != null && p2.getFullName() != null) {
					list1 = p1.getFullName().getValue();
					list2 = p2.getFullName().getValue();
					if (list1 != null && list2 != null) {
						
						s1 = list1.get(0);
						s2 = list2.get(0);
						score = comp.getSimilarity(s1, s2);
						if (score > 0.98) {
							i++;
							sb.append(score + "," + s1 + "," + s2 + "\n");
						}
					}
				}
			}
		}
		File file = new File(Constants.PATH_BASE + "qgram-string-example.csv");
		if (file.exists())
			file.delete();
		RandomAccessFile raf = null;
		try {
			file.createNewFile();
			raf = new RandomAccessFile(file, "rw");
			raf.writeBytes(sb.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void testRandomFreq() {
		long t0 = System.currentTimeMillis();
		TestRandomFrequency t = new TestRandomFrequency();
		//NumberComparator comp = new NumberComparatorImpl(0, 2012, 100);
		List<SaamPerson> perList = t.loadModel();
		int len = perList.size();
		System.out.println("finish loading model:" + len);
		StringBuffer sb = new StringBuffer("total:" + len + "\n");
		//int times = 10000;
		int n1, n2, N = 21;
		double[] thres = new double[N];
		int[] counts = new int[N];
		for (int i = 0; i < thres.length; i++) {
			thres[i] = 1 - i*0.01;
		}
		
		Random r = new Random();
		String s1, s2;
		double score;
		List<String> list1, list2;
		SaamPerson p1, p2;
		
		int[] timesArr = {100000000};//, 200000000 ,300000000};//, 40000000, 50000000};//, 60000000, 70000000, 80000000, 90000000};
		//int[] timesArr = {80000000, 90000000, 100000000};
		for (int times : timesArr) {
			for (int i = 0; i < N; i++) 
				counts[i] = 0;
			System.out.println("times:" + times);
			for (int i = 0; i < times; ) {
				n1 = r.nextInt(len);
				n2 = r.nextInt(len);
				if (n1 != n2) {
					p1 = perList.get(n1);
					p2 = perList.get(n2);
					
					if (p1.getFullName() != null && p2.getFullName() != null) {
						list1 = p1.getFullName().getValue();
						list2 = p2.getFullName().getValue();
						if (list1 != null && list2 != null) {
							i++;
							s1 = list1.get(0);
							s2 = list2.get(0);
							//try {
							//	num1 = Integer.parseInt(s1);
							//	num2 = Integer.parseInt(s2);
							//	score = comp.getSimilarity(num1, num2);
								score = comp.getSimilarity(s1, s2);
								int j;
								for (j = 0; j < N; j++) {
									if (score >= thres[j]) {
										break;
									}
								}
								if (j < N)
									counts[j] ++;
							//} catch (NumberFormatException e) {
								
							//}
						}
					}
				}
			}
			
			sb.append("times:" + times + "\n");
			for (int i = 0; i < N; i++) {
				sb.append(thres[i] + ":" + counts[i] + "\n");
			}
		}
		File file = new File(Constants.PATH_BASE + "jw_random_frequency.result");
		if (file.exists())
			file.delete();
		RandomAccessFile raf = null;
		try {
			file.createNewFile();
			raf = new RandomAccessFile(file, "rw");
			raf.writeBytes(sb.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("totalTime:" + (System.currentTimeMillis() - t0));
	}
	
	public static void loadDataToCSV() {
		File path = new File(Constants.PATH_BASE);
		RandomAccessFile raf = null;
		String line ;
		int N = 21;
		//StringBuffer sb = new StringBuffer();
		Map<String, List<String[]>> map = new TreeMap<String, List<String[]>>();
		try {
			for (File file : path.listFiles()) {
				if (file.isFile() && file.getName().startsWith("jw_random_frequency")) {
					raf = new RandomAccessFile(file, "rw");
					String times = null;
					String[] thres = null, freqs = null;
					
					line = raf.readLine();
					//total = line.substring(line.indexOf(':')+1);
					int i = 0;
					//System.out.println("file:" + file.getName());
					while ((line = raf.readLine())!= null) {
						if (line.indexOf("times") > -1 ) {
							if (thres != null) {
								/*
								System.out.println("times:" + times);
								for (int j = 0; j < N; j++) {
									System.out.print(',' + thres[j]);
								}
								System.out.println();
								for (int j = 0; j < N; j++) {
									System.out.print(',' + freqs[j]);
								}
								System.out.println();
								*/
								List<String[]> list = map.get(times);
								if (list == null) {
									list = new ArrayList<String[]>();
								}
								list.add(freqs);
								map.put(times, list);
							}
							thres = new String[N];
							freqs = new String[N];
							times = line.substring(line.indexOf(':') + 1);
							i = 0;
						} else if (line.indexOf(':') > -1) {
							thres[i] = line.substring(0, line.indexOf(':'));
							freqs[i] = line.substring(line.indexOf(':') + 1);
							i ++;
						}
					}
					/*
					System.out.println("times:" + times);
					for (int j = 0; j < N; j++) {
						System.out.print(',' + thres[j]);
					}
					System.out.println();
					for (int j = 0; j < N; j++) {
						System.out.print(',' + freqs[j]);
					}
					System.out.println();
					*/
					List<String[]> list = map.get(times);
					if (list == null) {
						list = new ArrayList<String[]>();
					}
					list.add(freqs);
					map.put(times, list);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (String key : map.keySet()) {
			List<String[]> list = map.get(key);
			System.out.println("\n");
			System.out.println("times:" + key);
			for (String[] strs : list) {
				int total = 0;
				for (String str : strs) {
					int num = Integer.parseInt(str);
					total += num;
					System.out.print(total + ",");
				}
				System.out.println();
			}
		}
	}

	public static void testCalcFreq() {
		long t0 = System.currentTimeMillis();
		TestRandomFrequency t = new TestRandomFrequency();
		double[] thresholds = {0.975, 0.95, 0.9, 0.85, 0.80};
		List<SaamPerson> perList = t.loadModel();
		StringBuffer sb = new StringBuffer();
		//sb.append("loading model:" + (System.currentTimeMillis() - t0)).append("\n");
		System.out.println("loading model:" + (System.currentTimeMillis() - t0));
		
		int i = 200;
		List<String> list = t.randomStringList(i, perList);
		
		for (double threshold : thresholds) {
			//sb.append("threshold:" + threshold).append("\n");
			System.out.println("threshold:" + threshold);
			for (int k = 0; k < 1; k++) {
				t0 = System.currentTimeMillis();
					//System.out.println("random:" + (System.currentTimeMillis()-t0));
					t0 = System.currentTimeMillis();
					double fre = t.calcAverFreq(threshold, list, perList);
					//System.out.println("calculation:" + (System.currentTimeMillis() - t0));
					//sb.append(i + "\t" + fre).append("\n");
					System.out.println(i + "\t" + fre);
				
				//sb.append("[" + (k+1) + "] total time:" + (System.currentTimeMillis() - t0)).append("\n");
				//System.out.println("[" + (k+1) + "] total time:" + (System.currentTimeMillis() - t0));
			}
		}
		
		File file = new File(Constants.PATH_BASE + "frequency.result");
		if (file.exists())
			file.delete();
		RandomAccessFile raf = null;
		try {
			file.createNewFile();
			raf = new RandomAccessFile(file, "rw");
			raf.writeBytes(sb.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	private List<SaamPerson> loadModel() {
		Model srcModel = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "dbpedia/").getDefaultModel();
		//Model srcModel = FileManager.get().loadModel(Constants.PATH_N3_FILE + "dbpedia_fullname_birth_death_dbpprop.n3");
		
		return loadOntologies(srcModel);
	}
	
	
	
	private List<String> randomStringList(int num, List<SaamPerson> perList) {
		
		List<String> list = new Vector<String>();
		
		
		int len = perList.size();
		
		Random r = new Random();
		PersonProperty st = null;
		int i = 0, j = 0; 
		System.out.print("random i:");
		while (i < num) {
			int n = r.nextInt(len/num) + 1;
			
			j += n;
			System.out.print("\t" + j);
			st = perList.get(j % len).getFullName();
			if (st != null && st.getValue().size() > 0) {
				list.add(st.getValue().get(0));
				i ++;
			}
		}
		System.out.println();
		
		return list;
	}
	/*
	private double calcAverFreq(double sim, List<String> srcList) {
		double sumFreq = 0, freq;
		int len = srcList.size();
		String s1, s2;
		
		int n1, n2, i = 0, j;
		for (i = 0; i < len; i++) {
			freq = 0;
			if (++i % 100 == 0) {
				System.out.println(i + " rows processed ");
			}
			s1 = srcList.get(i);
			for (j = 0; j  < len; j ++) {
				//if (i != j) {
					s2 = srcList.get(j);
					/*
						try {
							n1 = Integer.parseInt(s1);
							n2 = Integer.parseInt(s2);
							if (comp.getSimilarity(n1, n2) > sim) {
								freq += 1;
							}
						} catch (NumberFormatException nfe) {
							
						}
					* /		
						if (comp.getSimilarity(s1, s2) > sim)		{
							freq ++;
						}
					
				//}
			}
			sumFreq += freq;
		}
		return sumFreq / (len * len);
	}
	*/
	private double calcAverFreq(double sim, List<String> srcList, List<SaamPerson> perList) {
		double sumFreq = 0, freq;
		
		int len = srcList.size();
		System.out.println("size:" + len + " | " + perList.size());
		// int n1, n2;
		long i = 0;
		long t = System.currentTimeMillis();
		String s2;
		for (SaamPerson sp : perList) {
			freq = 0;
			if (++i % 100000 == 0) {
				System.out.println(i + " rows processed in " + (System.currentTimeMillis() - t) / 1000 + " s.  sum= " + sumFreq);
				t = System.currentTimeMillis();
				//if (i % 500000 == 0)
				//	break;
			}
			if (sp.getFullName() != null && sp.getFullName().getValue().size() > 0)
				s2 = sp.getFullName().getValue().get(0);
			else 
				continue;
			for (String s1 : srcList) {
				
				/*	
					try {
						n1 = Integer.parseInt(s1);
						n2 = Integer.parseInt(s2);
						if (comp.getSimilarity(n1, n2) > sim) {
							freq += 1;
						}
					} catch (NumberFormatException nfe) {
						
					}
				*/			
					if (comp.getSimilarity(s1, s2) > sim)		{
						freq ++;
					}
				
			}
			sumFreq += freq;
		}
		return (sumFreq / len);
	}

	private List<SaamPerson> loadOntologies(Model model) {
		List<SaamPerson> list = new Vector<SaamPerson>();
		Property RDF = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		ResIterator iter = model.listResourcesWithProperty(RDF);
		int count = 0;
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
			count ++;
			if (count % 100000 == 0) {
				System.out.println(count + " rows prcessed.");
			}
		}
		return list;
	}
}
