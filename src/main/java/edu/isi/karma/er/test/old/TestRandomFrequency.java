package edu.isi.karma.er.test.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Random;
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
	private StringComparator comp = new StringJaroWinklerComparatorImpl();
	// private NumberComparator comp = new NumberComparatorImpl(0, 2012, 10);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
}
