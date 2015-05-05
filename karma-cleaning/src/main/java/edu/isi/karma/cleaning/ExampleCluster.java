package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;


import edu.isi.karma.cleaning.features.Feature;

public class ExampleCluster {
	public HashMap<String, Boolean> legalParitions = new HashMap<String, Boolean>();
	Vector<Partition> examples = new Vector<Partition>();
	HashSet<String> exampleInputs = new HashSet<String>();
	Vector<Vector<String[]>> constraints = new Vector<Vector<String[]>>();
	public ProgSynthesis pSynthesis; // data
	HashMap<String, Vector<String>> uorgclusters = new HashMap<String, Vector<String>>();
	HashMap<String, double[]> string2Vector = new HashMap<String, double[]>();
	int unlabelDataAmount = 5;
	double assignThreshold = 0.1;
	public int featuresize = 0;
	public int failedCnt = 0;
	public double[] weights = {};

	public static enum method {
		CP, CPIC, SP, SPIC, DP, DPIC
	};

	public static method option = method.DPIC;

	/*
	 * example are (id, (input, output)) constrain: true->must link constraints,
	 * false->cannot link constraints
	 */
	public ExampleCluster() {

	}

	public ExampleCluster(ProgSynthesis pSynthesis, Vector<Partition> examples,
			HashMap<String, double[]> sData) {
		this.pSynthesis = pSynthesis;
		this.examples = examples;
		this.string2Vector = sData;
		this.featuresize = pSynthesis.featureSet.getFeatureNames().size();
		weights = new double[featuresize];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 1;
		}
		for (Partition p : examples) {
			for (Vector<TNode> nodes : p.orgNodes) {
				exampleInputs.add(UtilTools.print(nodes));
			}
		}
		init();
	}

	public void updateWeights(double[] weight) {
		if (weight != null)
			this.weights = weight;
	}

	// accepting constraints from previous iterations
	public void updateConstraints(Vector<Vector<String[]>> cnts) {
		if (option == method.CPIC || option == method.DPIC
				|| option == method.SPIC) {
			HashMap<String, Vector<String[]>> xHashMap1 = new HashMap<String, Vector<String[]>>();
			for (Vector<String[]> group : cnts) {
				xHashMap1.put(constraintKey(group), group);
			}
			HashSet<String> xHashSet2 = new HashSet<String>();
			for (Vector<String[]> group : constraints) {
				xHashSet2.add(constraintKey(group));
			}
			for (String k : xHashMap1.keySet()) {
				if (!xHashSet2.contains(k)) {
					constraints.add(xHashMap1.get(k));
				}
			}
			// update islegal ds
			for (Vector<String[]> group : constraints) {
				ArrayList<String> g = new ArrayList<String>();
				for (String[] p : group) {
					String line = String.format("%s, %s\n", p[0], p[1]);
					g.add(line);
				}
				String res = "";
				res = UtilTools.createkey(new ArrayList<String[]>(group));
				legalParitions.put(res, false);
			}
		}
	}

	public Vector<Vector<String[]>> getConstraints() {
		return constraints;
	}

	public String constraintKey(Vector<String[]> group) {
		ArrayList<String> xArrayList = new ArrayList<String>();
		for (String[] e : group) {
			xArrayList.add(Arrays.toString(e));
		}
		Collections.sort(xArrayList);
		return xArrayList.toString();
	}

	public void init() {
		if (option == method.DP || option == method.DPIC) {
			this.unlabelDataAmount = 5;
		} else {
			this.unlabelDataAmount = 0;
		}
	}

	public void diagnose() {
		System.out.println("" + this.pSynthesis.featureSet.getFeatureNames());
		System.out.println("" + Arrays.toString(weights));
	}

	// adaptive partition program learning
	public Vector<Partition> adaptive_cluster_weightEuclidean(
			Vector<Partition> pars) {
		// single example
		if (pars.size() == 1) {
			ProgramAdaptator pAdapter = new ProgramAdaptator();
			ArrayList<String[]> exps = UtilTools
					.extractExamplesinPartition(pars);
			pAdapter.adapt(pSynthesis.msGer.exp2Partition,
					pSynthesis.msGer.exp2program, exps);
			return pars;
		}
		while (true) {
			// find partitions with the smallest distance
			double mindist = Double.MAX_VALUE;
			int x_ind = -1;
			int y_ind = -1;
			/***/
			for (int i = 0; i < pars.size(); i++) {
				for (int j = i + 1; j < pars.size(); j++) {
					String key = getStringKey(pars.get(i), pars.get(j));
					boolean good = true;
					for (String k : legalParitions.keySet()) {
						if (!legalParitions.get(k) && key.indexOf(k) != -1) {
							good = false;
							break;
						}
					}
					if (!good) {
						legalParitions.put(key, false);
						continue;
					}
					double par_dist = getDistance(pars.get(i), pars.get(j),
							pars);
					if (par_dist < mindist) {
						mindist = par_dist;
						x_ind = i;
						y_ind = j;
					}
				}
			}
			if (x_ind == -1 || y_ind == -1) {
				break;
			}
			/* print the partitioning information* */
			// ProgTracker.printPartitions(pars.get(x_ind), pars.get(y_ind));
			/***/
			ArrayList<Partition> tmppartitions = new ArrayList<Partition>();
			tmppartitions.add(pars.get(x_ind));
			tmppartitions.add(pars.get(y_ind));
			String tmpkey = Partition.getStringKey(tmppartitions);
			Partition z = null;
			if(pSynthesis.msGer.exp2Partition.containsKey(tmpkey)){
				z = pSynthesis.msGer.exp2Partition.get(tmpkey);
			}
			else{
				z = pars.get(x_ind).mergewith(pars.get(y_ind));
			}

			if (adaptive_isLegalPartition(z)) {
				legalParitions.put(z.getHashKey(), true);
				// update the partition vector
				pars = UpdatePartitions(x_ind, y_ind, pars);
				continue;
			} else {
				legalParitions.put(
						getStringKey(pars.get(x_ind), pars.get(y_ind)), false);
				// update the constraints
				Vector<String[]> clique = new Vector<String[]>();
				for (int k = 0; k < pars.get(x_ind).orgNodes.size(); k++) {
					String org = UtilTools.print(pars.get(x_ind).orgNodes
							.get(k));
					String tar = UtilTools.print(pars.get(x_ind).tarNodes
							.get(k));
					String[] pair = { org, tar };
					clique.add(pair);
				}
				for (int k = 0; k < pars.get(y_ind).orgNodes.size(); k++) {
					String org = UtilTools.print(pars.get(y_ind).orgNodes
							.get(k));
					String tar = UtilTools.print(pars.get(y_ind).tarNodes
							.get(k));
					String[] pair = { org, tar };
					clique.add(pair);
				}
				constraints.add(clique);
				// update the distance metrics
				updateDistanceMetric(pars);
			}
		}
		if (pars.size() > 1)
			updateDistanceMetric(pars); // tune the final weight given the
										// current
		// clusters
		// assign ulabled data to each partition
		for (Partition p : pars) {
			p.orgUnlabeledData.clear();
		}
		if (pars.size() >= 2) {
			assignUnlabeledData(pars);
		}
		//this.diagnose();
		return pars;
	}

	// use distorted distance function to cluster partitions
	public Vector<Partition> cluster_weigthEuclidean(Vector<Partition> pars) {
		// if (this.constraints.size() > 0)
		// updateDistanceMetric(pars);

		while (true) {
			// find partitions with the smallest distance
			double mindist = Double.MAX_VALUE;
			int x_ind = -1;
			int y_ind = -1;
			/* print the partitioning information* */
			// ProgTracker.printPartition(pars);
			// ProgTracker.printConstraints(constraints);
			/***/
			for (int i = 0; i < pars.size(); i++) {
				for (int j = i + 1; j < pars.size(); j++) {
					String key = getStringKey(pars.get(i), pars.get(j));
					boolean good = true;
					for (String k : legalParitions.keySet()) {
						if (!legalParitions.get(k) && key.indexOf(k) != -1) {
							good = false;
							break;
						}
					}
					if (!good) {
						legalParitions.put(key, false);
						continue;
					}
					// double par_dist = getDistance(pars.get(i), pars.get(j));
					// double par_dist = getCompScore(pars.get(i), pars.get(j),
					// pars);// sumit heuristic
					double par_dist = getDistance(pars.get(i), pars.get(j),
							pars);
					if (par_dist < mindist) {
						mindist = par_dist;
						x_ind = i;
						y_ind = j;
					}
				}
			}
			if (x_ind == -1 || y_ind == -1) {
				break;
			}
			/* print the partitioning information* */
			// ProgTracker.printPartitions(pars.get(x_ind), pars.get(y_ind));
			/***/
			Partition z = pars.get(x_ind).mergewith(pars.get(y_ind));
			if (isLegalPartition(z)) {
				legalParitions.put(z.getHashKey(), true);
				// update the partition vector
				pars = UpdatePartitions(x_ind, y_ind, pars);
				continue;
			} else {
				legalParitions.put(
						getStringKey(pars.get(x_ind), pars.get(y_ind)), false);
				// update the constraints
				Vector<String[]> clique = new Vector<String[]>();
				for (int k = 0; k < pars.get(x_ind).orgNodes.size(); k++) {
					String org = UtilTools.print(pars.get(x_ind).orgNodes
							.get(k));
					String tar = UtilTools.print(pars.get(x_ind).tarNodes
							.get(k));
					String[] pair = { org, tar };
					clique.add(pair);
				}
				for (int k = 0; k < pars.get(y_ind).orgNodes.size(); k++) {
					String org = UtilTools.print(pars.get(y_ind).orgNodes
							.get(k));
					String tar = UtilTools.print(pars.get(y_ind).tarNodes
							.get(k));
					String[] pair = { org, tar };
					clique.add(pair);
				}
				constraints.add(clique);
				// update the distance metrics
				updateDistanceMetric(pars);
			}
		}
		if (pars.size() > 1)
			updateDistanceMetric(pars); // tune the final weight given the
										// current
		// clusters
		// assign ulabled data to each partition
		for (Partition p : pars) {
			p.orgUnlabeledData.clear();
		}
		if (pars.size() >= 2) {
			assignUnlabeledData(pars);
		}
		this.diagnose();
		return pars;
	}

	public void assignUnlabeledData(Vector<Partition> pars) {
		HashMap<String, Double> dists = new HashMap<String, Double>();
		// find the distance between partitions
		for (int i = 0; i < pars.size(); i++) {
			for (int j = i + 1; j < pars.size(); j++) {
				String d = getStringKey(pars.get(i), pars.get(j));
				if (!dists.containsKey(d)) {
					dists.put(d, getDistance(pars.get(i), pars.get(j)));
				}
			}
		}
		HashMap<Partition, HashMap<String, Double>> testResult = new HashMap<Partition, HashMap<String, Double>>();

		for (String val : string2Vector.keySet()) {
			Partition p_index = null;
			Partition p_index_2nd = null;
			double min_val = Double.MAX_VALUE;
			double min_val_2nd = Double.MAX_VALUE;
			// find the two shortest distances.
			for (Partition p : pars) {
				double dist = getDistance(val, p);
				// /
				// /System.out.println(String.format("%s, %s: %f", val, p.label,
				// dist));
				// /
				if (dist < min_val) {
					min_val_2nd = min_val;
					p_index_2nd = p_index;
					min_val = dist;
					p_index = p;

				} else if (dist >= min_val && dist < min_val_2nd) {
					min_val_2nd = dist;
					p_index_2nd = p;
				}

			}
			double var_dist = min_val_2nd - min_val;
			String tKey = getStringKey(p_index_2nd, p_index);
			double pDist = dists.get(tKey);
			if (var_dist > pDist * assignThreshold) {
				if (!testResult.containsKey(p_index)) {
					HashMap<String, Double> cluster = new HashMap<String, Double>();
					cluster.put(val, min_val);
					testResult.put(p_index, cluster);
				} else {
					testResult.get(p_index).put(val, min_val);
				}
			}
			/*
			 * if (p_index.orgUnlabeledData.size() < 10) {
			 * p_index.orgUnlabeledData.add(val); }
			 */
		}
		for (Partition key : testResult.keySet()) {
			Map<?, ?> dicttmp = UtilTools.sortByComparator(testResult.get(key));
			/** print unlabeled data **/
			// System.out.println("Partition: " + key.label);
			// ProgTracker.printUnlabeledData(dicttmp);
			/****/
			int cnt = 0;
			for (Object xkey : dicttmp.keySet()) {
				if (cnt < unlabelDataAmount * key.orgNodes.size()) {
					if (exampleInputs.contains((String) xkey))// exclude
																// examples from
																// unlabeled
																// data
						continue;
					key.orgUnlabeledData.add((String) xkey);
					cnt++;
				} else {
					break;
				}
			}
		}
	}

	// current return all the examples.
	// monitor whether all constraints are positive.
	public ArrayList<String> findMaximalSeperated() {
		ArrayList<String> dists = new ArrayList<String>();
		for (Partition e : examples) {
			for (Vector<TNode> elem : e.orgNodes) {
				String line = UtilTools.print(elem);
				dists.add(line);
			}
		}
		return dists;
	}

	public ArrayList<ArrayList<Double>> convertStringSetToContrainMatrix(
			ArrayList<String> strings) {
		ArrayList<ArrayList<Double>> res = new ArrayList<ArrayList<Double>>();
		for (int i = 0; i < strings.size(); i++) {
			for (int j = i + 1; j < strings.size(); j++) {
				String s1 = strings.get(i);
				String s2 = strings.get(j);
				double[] s1_vec = getFeatureArray(s1);
				double[] s2_vec = getFeatureArray(s2);
				ArrayList<Double> xArrayList = new ArrayList<Double>();
				for (int k = 0; k < s1_vec.length; k++) {
					xArrayList.add(s2_vec[k] - s1_vec[k]); // does sign matter?
				}
				res.add(xArrayList);
			}
		}
		return res;
	}

	// input with current contraints
	public void updateDistanceMetric(Vector<Partition> pars) {
		if (option == method.DP || option == method.DPIC) {
			GradientDecendOptimizer gdo = new GradientDecendOptimizer();
			// calculate example array and individual groups
			ArrayList<double[]> centers = new ArrayList<double[]>();
			ArrayList<ArrayList<double[]>> individuals = new ArrayList<ArrayList<double[]>>();
			for (Partition p : pars) {
				ArrayList<double[]> list = new ArrayList<double[]>();
				double[] center = new double[featuresize];
				center = UtilTools.initArray(center, 0);
				for (Vector<TNode> org : p.orgNodes) {
					String res = UtilTools.print(org);
					double[] vec = string2Vector.get(res);
					list.add(vec);
					center = addArray(center, vec);
				}
				individuals.add(list);
				center = UtilTools.produce(1.0 / p.orgNodes.size(), center);
				centers.add(center);
			}
			// calculate instance array
			ArrayList<double[]> instances = new ArrayList<double[]>();
			for (String s : string2Vector.keySet()) {
				double[] elem = string2Vector.get(s);
				instances.add(elem);
			}
			// calculate constraint array
			ArrayList<ArrayList<double[]>> constraintgroup = new ArrayList<ArrayList<double[]>>();
			for (Vector<String[]> consts : constraints) {
				ArrayList<double[]> group = new ArrayList<double[]>();
				for (String[] exp : consts) {
					double[] e = string2Vector.get(exp[0]);
					group.add(e);
				}
				constraintgroup.add(group);
			}
			double[] w = gdo.doOptimize(centers, instances, constraintgroup,
					individuals, this.weights);
			this.weights = w;
		} else {
			this.weights = new double[featuresize];
			weights = UtilTools.initArray(this.weights, 1);
		}
	}

	public double[] getFeatureArray(String s) {
		Collection<Feature> cfeat = pSynthesis.featureSet
				.computeFeatures(s, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		double[] res = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			res[i] = x[i].getScore();
		}
		return res;
	}

	public double getDistance(String a, Partition b) {
		// find a string
		double[] x = string2Vector.get(a);
		double mindist = Double.MAX_VALUE;
		for (Vector<TNode> orgs : b.orgNodes) {
			double[] e = string2Vector.get(UtilTools.print(orgs));
			double d = getDistance(x, e);
			if (d < mindist)
				mindist = d;
		}
		return mindist;
	}

	public double getDistance(Partition a, Partition b, Vector<Partition> pars) {
		double res = 0.0;
		if (option == method.DPIC || option == method.DP) {
			res = getDistance(a, b); // use the closest points' distance as the
										// cluster distance
		} else if (option == method.SP || option == method.SPIC) {
			res = getCompScore(a, b, pars);
		} else {
			double[] x = getPartitionVector(a);
			double[] y = getPartitionVector(b);
			res = UtilTools.distance(x, y);
		}
		return res;
	}

	/*
	 * public double getDistance(Partition a, Partition b) { double[] x =
	 * getPartitionVector(a); double[] y = getPartitionVector(b); return
	 * getDistance(x, y); }
	 */

	public double getDistance(Partition a, Partition b) {
		double mindist = Double.MAX_VALUE;
		for (Vector<TNode> orgs : a.orgNodes) {
			String e = UtilTools.print(orgs);
			double d = getDistance(e, b);
			if (d < mindist)
				mindist = d;
		}
		return mindist;
	}

	public double getCompScore(Partition a, Partition b, Vector<Partition> pars) {
		String key = getStringKey(a, b);
		for (String ekey : legalParitions.keySet()) {
			if (key.indexOf(ekey) != -1 && !legalParitions.get(ekey)) {
				return Double.MAX_VALUE;
			}
		}
		Partition p = a.mergewith(b);
		if (!isLegalPartition(p)) {
			legalParitions.put(key, false);
			// update the constraints
			Vector<String[]> clique = new Vector<String[]>();
			for (int k = 0; k < a.orgNodes.size(); k++) {
				String org = UtilTools.print(a.orgNodes.get(k));
				String tar = UtilTools.print(a.tarNodes.get(k));
				String[] pair = { org, tar };
				clique.add(pair);
			}
			for (int k = 0; k < b.orgNodes.size(); k++) {
				String org = UtilTools.print(b.orgNodes.get(k));
				String tar = UtilTools.print(b.tarNodes.get(k));
				String[] pair = { org, tar };
				clique.add(pair);
			}
			constraints.add(clique);
			return Double.MAX_VALUE;
		}
		double validCnt = 1e-3; // avoid overflowing
		for (int x = 0; x < pars.size(); x++) {
			if (pars.get(x) == a || pars.get(x) == b) {
				continue;
			}
			Partition q = p.mergewith(pars.get(x));
			if (isLegalPartition(q)) {
				validCnt++;
			}
		}
		return 1.0 / validCnt;
	}

	public double getDistance(double[] a, double[] b) {
		double sum = 0.0;
		for (int i = 0; i < a.length; i++) {
			sum += Math.pow(a[i] - b[i], 2) * weights[i];
		}

		return Math.sqrt(sum);
	}
	//check b can be copied from a
	public boolean iscovered(String a, String b)
	{
		String[] elems = b.split("\\*");
		boolean covered = true;
		for(String e: elems)
		{
			if(a.indexOf(e)== -1)
			{
				covered = false;
				break;
			}
		}
		return covered;
	}
	public boolean adaptive_isLegalPartition(Partition p) {
		if (p == null) {
			failedCnt++;
			return false;
		}
		
		String key = p.getHashKey();
		if(pSynthesis.msGer.exp2program.containsKey(key))
			return true;
		if (legalParitions.containsKey(key)) {
			return legalParitions.get(key);
		}
		// test whether its subset fails
		for (String k : legalParitions.keySet()) {
			if (!legalParitions.get(k)) // false
			{
				if (iscovered(key, k)) {
					return false;
				}
			}
		}
		ProgramAdaptator pAdapter = new ProgramAdaptator();
		ArrayList<Partition> nPs = new ArrayList<Partition>();
		nPs.add(p);
		ArrayList<String[]> examps = UtilTools.extractExamplesinPartition(nPs);
		String fprogram = pAdapter.adapt(pSynthesis.msGer.exp2Partition,pSynthesis.msGer.exp2program, examps);
		if (fprogram.indexOf("null")!= -1) {
			failedCnt++;
			legalParitions.put(key, false);
			return false;
		} else {
			legalParitions.put(key, true);
			return true;
		}
	}

	public boolean isLegalPartition(Partition p) {
		if (p == null) {
			failedCnt++;
			return false;
		}
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
			failedCnt++;
			legalParitions.put(key, false);
			return false;
		} else {
			legalParitions.put(key, true);
			return true;
		}
	}

	public Vector<Partition> UpdatePartitions(int i, int j,
			Vector<Partition> pars) {
		Partition p = pars.get(i).mergewith(pars.get(j));
		Vector<Partition> res = new Vector<Partition>();
		res.addAll(pars);
		res.set(i, p);
		res.remove(j);
		return res;
	}

	public String getStringKey(Partition a, Partition b) {
		ArrayList<Partition> pars = new ArrayList<Partition>();
		pars.add(a);
		pars.add(b);
		String res = Partition.getStringKey(pars);
		return res;
	}

	// get a vector that can represent a partition
	public double[] getPartitionVector(Partition p) {

		ArrayList<double[]> vecs = new ArrayList<double[]>();
		for (Vector<TNode> orgs : p.orgNodes) {
			vecs.add(string2Vector.get(UtilTools.print(orgs)));
		}
		double[] vec = UtilTools.sum(vecs);
		vec = UtilTools.produce(1.0 / p.orgNodes.size(), vec);
		return vec;
	}

	public double[] addArray(double[] a, double[] b) {
		double[] x = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			x[i] = a[i] + b[i];
		}
		return x;
	}
}
