package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.cleaning.features.RecordClassifier;

public class OptimizePartition {
	public HashMap<String, Partition> existedPartition = new HashMap<String, Partition>();
	public HashSet<String> existedFringe = new HashSet<String>();
	public HashMap<String, Double> key2Partition = new HashMap<String, Double>();
	public HashMap<String, Boolean> legalParitions = new HashMap<String, Boolean>();
	public ProgSynthesis pSynthesis;
	public PartitionClassifierType classifier;
	public HashMap<Vector<Partition>, Double> fringe = new HashMap<Vector<Partition>, Double>();
	public int failedCnt = 0;
	public Logger ulogger = LoggerFactory.getLogger(RecordClassifier.class);
	public OptimizePartition(ProgSynthesis ps,PartitionClassifierType classifier) {
		pSynthesis = ps;
		this.classifier = classifier;
	}
	public String getPartitionKey(int a, int b, Vector<Partition> pars) {
		String s = a + "," + b;
		
		for (Partition p : pars) {
			s += p.getHashKey() + "\n===\n";
		}
		return s.trim();
	}

	public Entry<Vector<Partition>, Double> findSkey(
			HashMap<Vector<Partition>, Double> fringe) {
		Entry<Vector<Partition>, Double> minEntry = null;

		for (Map.Entry<Vector<Partition>, Double> entry : fringe.entrySet()) {
			if (minEntry == null || entry.getValue() < minEntry.getValue()) {
				minEntry = entry;
			}
		}
		return minEntry;
	}

	public boolean existClassifer(Vector<Partition> pars) {
		RecordClassifier rClassifier = (RecordClassifier)this.classifier;
		rClassifier.init();
		int cnt = 0;
		for (Partition p : pars) {
			String label = "attr_" + cnt;
			cnt++;
			for (int i = 0; i < p.orgNodes.size(); i++) {
				String data = UtilTools.print(p.orgNodes.get(i));
				rClassifier.addTrainingData(data, label);
			}
		}
		rClassifier.learnClassifer();
		return rClassifier.selfVerify();
	}

	public Vector<Partition> astarSearch(Vector<Partition> pars) {
		if (pars.size() == 1) {
			return pars;
		}
		fringe = new HashMap<Vector<Partition>, Double>();
		fringe.put(pars, Double.MAX_VALUE);
		Vector<Partition> res = new Vector<Partition>();
		while (!fringe.isEmpty()) {
			//ulogger.info("Fringe size: "+fringe.size());
			// choose one with minimal value from fringe to expand
			Entry<Vector<Partition>, Double> elem = this.findSkey(fringe);
			// extend from current elem
			HashMap<Vector<Partition>, Double> candidates = this
					.generateCandidates(elem.getKey());
			// check if partitoning ends
			if (candidates == null) {
				// check whether exist consistent classifier
				if(elem.getKey().size() == 1)
				{
					res = elem.getKey();
					break;
				}
				if (existClassifer(elem.getKey())) {
					res = elem.getKey();
					break;
				}
			}
			// update the fringe
			fringe = candidates;
			
		}
		return res;
	}

	public HashMap<Vector<Partition>, Double> generateCandidates(
			Vector<Partition> pars) {
		HashMap<Vector<Partition>, Double> res = null;
		for (int i = 0; i < pars.size(); i++) {
			for (int j = i + 1; j < pars.size(); j++) {
				double s = 0;
				String lkey = this.getPartitionKey(i, j, pars);
				if (this.key2Partition.containsKey(lkey)) {
					s = key2Partition.get(lkey);
				} else {
					s = getCompScore(i, j, pars);
					key2Partition.put(lkey, s);
				}
				if (s == Double.MAX_VALUE) {
					continue;
				}
				if (i != -1 && j != -1) {
					Vector<Partition> next = UpdatePartitions(i, j, pars);
					if(res == null)
						res = new HashMap<Vector<Partition>, Double>();
					if(!fringe.containsKey(next))
						res.put(next, s);
				}
			}
		}
		return res;
	}

	public Vector<Partition> UpdatePartitions(int i, int j,
			Vector<Partition> pars) {
		String key = getMergedKey(pars.get(i), pars.get(j));
		Partition p;
		if(!existedPartition.containsKey(key))
		{
			p = pars.get(i).mergewith(pars.get(j));
			existedPartition.put(key, p);
		}
		else
		{
			p = existedPartition.get(key);
		}
		Vector<Partition> res = new Vector<Partition>();
		res.addAll(pars);
		res.set(i, p);
		res.remove(j);
		return res;
	}
	
	// return number of partitions as the distance. 
	// if two partitions are not compatitable, return the largest double
	public double getCompScore2(int i, int j, Vector<Partition> pars)
	{
		String key = this.getMergedKey(pars.get(i), pars.get(j));
		Partition p;
		if(!existedPartition.containsKey(key))
		{
			p = pars.get(i).mergewith(pars.get(j));
			existedPartition.put(key, p);
		}
		else
		{
			p = existedPartition.get(key);
		}
		if (p == null || !isLegalPartition(p)) {
			failedCnt ++;
			return Double.MAX_VALUE;
		}
		else
		{
			return pars.size()-1;
		}
	}
	// return Num_of_partition/mergable_paritions as distance function 
	// if two partitions are not compatitable, return the largest double
	public double getCompScore(int i, int j, Vector<Partition> pars) {
		String key = this.getMergedKey(pars.get(i), pars.get(j));
		Partition p;
		if(!existedPartition.containsKey(key))
		{
			p = pars.get(i).mergewith(pars.get(j));
			existedPartition.put(key, p);
		}
		else
		{
			p = existedPartition.get(key);
		}
		if (p == null || !isLegalPartition(p)) {
			return Double.MAX_VALUE;
		}
		int validCnt = 0;
		for (int x = 0; x < pars.size(); x++) {
			if (x == i || x == j) {
				continue;
			}
			Partition q = p.mergewith(pars.get(x));
			if (q != null && isLegalPartition(p)) {
				validCnt++;
			}
		}
		return (pars.size()-1)*1.0/validCnt;
	}

	public boolean isLegalPartition(Partition p) {
		String key = p.getHashKey();
		if (legalParitions.containsKey(key)) {
			return legalParitions.get(key);
		}
		// test whether its subset fails
		for (String k : legalParitions.keySet()) {
			if (!legalParitions.get(k)) // false
			{
				if (key.indexOf(k) != -1) {
					return false;
				}
			}
		}
		Vector<Partition> xPar = new Vector<Partition>();
		xPar.add(p);
		Collection<ProgramRule> cpr = pSynthesis.producePrograms(xPar);
		if (cpr == null || cpr.size() == 0) {
			legalParitions.put(key, false);
			return false;
		} else {
			legalParitions.put(key, true);
			return true;
		}
	}
	//speed optimization
	public String getMergedKey(Partition a, Partition b)
	{
		ArrayList<String> xArrayList = new ArrayList<String>();
		for(int i = 0; i< a.orgNodes.size(); i++)
		{
			String line = UtilTools.print(a.orgNodes.get(i));
			xArrayList.add(line);
		}
		for(int i = 0; i< b.orgNodes.size(); i++)
		{
			String line = UtilTools.print(b.orgNodes.get(i));
			xArrayList.add(line);
		}
		Collections.sort(xArrayList);
		return xArrayList.toString();
	}
	public static void main(String[] args) {

	}
}
