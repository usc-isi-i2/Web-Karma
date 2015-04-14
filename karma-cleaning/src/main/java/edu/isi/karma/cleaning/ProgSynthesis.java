package edu.isi.karma.cleaning;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.cleaning.Research.ConfigParameters;
import edu.isi.karma.cleaning.features.Feature;
import edu.isi.karma.cleaning.features.RecordClassifier;
import edu.isi.karma.cleaning.features.RecordFeatureSet;

public class ProgSynthesis {
	public static int time_limit = 20;
	Vector<Vector<TNode>> orgVector = new Vector<Vector<TNode>>();
	Vector<Vector<TNode>> tarVector = new Vector<Vector<TNode>>();
	String bestRuleString = "";
	// for tracking the stats
	public long learnspan = 0;
	public long genspan = 0;
	public long ruleNo = 0;
	public HashMap<String, double[]> string2Vector = new HashMap<String, double[]>();
	public PartitionClassifierType classifier;
	public RecordFeatureSet featureSet = null;
	public Vector<Vector<String[]>> constraints = new Vector<Vector<String[]>>();
	public HashMap<String, Boolean> legalParitions = new HashMap<String, Boolean>();
	public MyLogger logger = new MyLogger();
	private static Logger ulogger = LoggerFactory
			.getLogger(ProgSynthesis.class);
	public Program myprog;
	public OptimizePartition partitioner; // A* search for a consistent program
	public ExampleCluster partiCluster;
	public DataPreProcessor dataPreProcessor;
	public Messager msGer;

	public ProgSynthesis() {
	}

	public void inite(Vector<String[]> examples, String[] vocb) {
		for (int i = 0; i < examples.size(); i++) {
			Ruler r = new Ruler();
			r.setNewInput(examples.get(i)[0]);
			orgVector.add(r.vec);
			Ruler r1 = new Ruler();
			r1.setNewInput(examples.get(i)[1]);
			tarVector.add(r1.vec);
		}
		RecordFeatureSet rSet = new RecordFeatureSet();
		rSet.addVocabulary(vocb);
		this.featureSet = rSet;
		RecordClassifier r2 = new RecordClassifier(rSet);
		this.classifier = r2;
		partitioner = new OptimizePartition(this, r2);
	}
	
	public void inite(Vector<String[]> examples, DataPreProcessor dp, Vector<Vector<String[]>> constraints) {
		for (int i = 0; i < examples.size(); i++) {
			Ruler r = new Ruler();
			r.setNewInput(examples.get(i)[0]);
			orgVector.add(r.vec);
			Ruler r1 = new Ruler();
			r1.setNewInput(examples.get(i)[1]);
			tarVector.add(r1.vec);
		}
		featureSet = dp.rfs;
		RecordClassifier r2 = new RecordClassifier(featureSet);
		this.classifier = r2;
		this.constraints = constraints;
		string2Vector = dp.getStandardData();
	}
	public void inite(Vector<String[]> examples, DataPreProcessor dp, Messager msger) {
		for (int i = 0; i < examples.size(); i++) {
			Ruler r = new Ruler();
			r.setNewInput(examples.get(i)[0]);
			orgVector.add(r.vec);
			Ruler r1 = new Ruler();
			r1.setNewInput(examples.get(i)[1]);
			tarVector.add(r1.vec);
		}
		featureSet = dp.rfs;
		RecordClassifier r2 = new RecordClassifier(featureSet);
		this.classifier = r2;
		string2Vector = dp.getStandardData();
		this.dataPreProcessor = dp;
		this.msGer = msger;
	}
	public Vector<Vector<Integer>> generateCrossIndex(Vector<Integer> poss,
			Vector<Vector<Integer>> p, int index) {
		Vector<Vector<Integer>> qVector = new Vector<Vector<Integer>>();
		if (index >= poss.size()) {
			return p;
		}
		int curleng = poss.get(index);
		if (p.size() == 0) {
			for (int i = 0; i < curleng; i++) {
				Vector<Integer> x = new Vector<Integer>();
				x.add(i);
				qVector.add(x);
			}
		} else {
			for (int j = 0; j < p.size(); j++) {
				for (int i = 0; i < curleng; i++) {
					@SuppressWarnings("unchecked")
					Vector<Integer> x = (Vector<Integer>) p.get(j).clone();
					x.add(i);
					qVector.add(x);
				}
			}
		}
		qVector = generateCrossIndex(poss, qVector, index + 1);
		return qVector;
	}

	public double getCompScore(int i, int j, Vector<Partition> pars) {
		Partition p = pars.get(i).mergewith(pars.get(j));
		if (p == null || !isLegalPartition(p)) {
			return -Double.MAX_VALUE;
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
		return validCnt;
	}

	public Vector<Partition> initePartitions() {
		Vector<Partition> pars = new Vector<Partition>();
		// inite partition for each example
		for (int i = 0; i < orgVector.size(); i++) {
			Vector<Vector<TNode>> ovt = new Vector<Vector<TNode>>();
			Vector<Vector<TNode>> tvt = new Vector<Vector<TNode>>();
			ovt.add(this.orgVector.get(i));
			tvt.add(this.tarVector.get(i));
			Partition pt = new Partition(ovt, tvt);
			pars.add(pt);
		}
		return pars;
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
		Collection<ProgramRule> cpr = this.producePrograms(xPar);
		if (cpr == null || cpr.size() == 0) {
			legalParitions.put(key, false);
			return false;
		} else {
			legalParitions.put(key, true);
			return true;
		}
	}

	public void mergePartitions(Vector<Partition> pars) {
		double maxScore = 0;
		int[] pos = { -1, -1 };
		for (int i = 0; i < pars.size(); i++) {
			for (int j = i + 1; j < pars.size(); j++) {
				double s = getCompScore(i, j, pars);
				if (s < 0) {
					continue;
				}
				if (s >= maxScore) {
					pos[0] = i;
					pos[1] = j;
					maxScore = s;
				}
			}
		}
		if (pos[0] != -1 && pos[1] != -1)
			UpdatePartitions(pos[0], pos[1], pars);
	}

	public void UpdatePartitions(int i, int j, Vector<Partition> pars) {
		Partition p = pars.get(i).mergewith(pars.get(j));
		pars.set(i, p);
		pars.remove(j);
	}

	public String getBestRule() {
		return this.bestRuleString;
	}

	public double[] getFeatureArray(String s) {
		Collection<Feature> cfeat = featureSet.computeFeatures(s, "");
		Feature[] x = cfeat.toArray(new Feature[cfeat.size()]);
		double[] res = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			res[i] = x[i].getScore();
		}
		return res;
	}

	public Vector<Partition> ProducePartitions(boolean condense) {
		Vector<Partition> pars = this.initePartitions();
		partiCluster = new ExampleCluster(this, pars, string2Vector);
		partiCluster.updateConstraints(msGer.getConstraints());
		partiCluster.updateWeights(msGer.weights);
		if (condense) {
			pars = partiCluster.cluster_weigthEuclidean(pars);
		}
		this.ruleNo = pars.size();
		/*
		 * int size = pars.size(); while (condense) {
		 * this.mergePartitions(pars); if (size == pars.size()) { break; } else
		 * { size = pars.size(); } }
		 */
		return pars;
	}
	
	public Collection<ProgramRule> producePrograms(Vector<Partition> pars) {
		Program prog = new Program(pars, this.classifier,this.dataPreProcessor);
		this.myprog = prog;
		HashSet<ProgramRule> rules = new HashSet<ProgramRule>();
		int prog_cnt = 1;
		int i = 0;
		while (i < prog_cnt) {
			ProgramRule r = prog.toProgram1();
			if (r == null)
				return null;
			String xString = "";
			int termCnt = 0;
			boolean findRule = true;
			while ((xString = this.validRule(r, pars)) != "GOOD" && findRule) {
				if (xString.compareTo("NO_CLASIF") == 0) {
					return null; // indistinguishable classes.
				}
				for (Partition p : prog.partitions) {
					if (p.label.compareTo(xString) == 0) {
						String newRule = p.toProgram();
						if (ConfigParameters.debug == 1)
							System.out.println("updated Rule: " + p.label
									+ ": " + newRule);
						if (newRule.contains("null")) {
							findRule = false;
							break;
						}
						r.updateClassworker(xString, newRule);
					}
				}
				termCnt++;
			}
			if (findRule)
			{
				rules.add(r);
			}
			i++;
		}
		return rules;
	} 
	public Collection<ProgramRule> run_main_all()
	{
		long t1 = System.currentTimeMillis();
		Vector<Partition> par = this.ProducePartitions(true);
		Program prog = new Program(par, this.classifier,this.dataPreProcessor);
		Collection<ProgramRule> cpr = this.produceProgram_all(par,prog);
		Traces.AllSegs.clear();
		//record the learning time
		this.learnspan = (long) ((System.currentTimeMillis()-t1)*1.0/1000);
		return cpr;
	}
	public Collection<ProgramRule> adaptive_main()
	{
		StopWatch stopWatch0 = new Log4JStopWatch("adaptive_main");
		long t1 = System.currentTimeMillis();
		StopWatch stopWatch = new Log4JStopWatch("adaptive_producePartition");
		Vector<Partition> par = this.adaptive_producePartition();
		stopWatch.stop();
		StopWatch stopWatch1 = new Log4JStopWatch("adaptive_produceProgram");
		Collection<ProgramRule> cpr = this.adaptive_produceProgram(par);
		stopWatch1.stop();
		Traces.AllSegs.clear();
		//record the learning time
		this.learnspan = (long) ((System.currentTimeMillis()-t1)*1.0/1000);
		stopWatch0.stop();
		return cpr;
	}
	public Collection<ProgramRule> produceProgram_all(Vector<Partition> pars, Program prog)
	{
		this.myprog = prog;
		HashSet<ProgramRule> rules = new HashSet<ProgramRule>();
		int prog_cnt = Integer.MAX_VALUE;
		int i = 0;
		while(i < prog_cnt) {
			ProgramRule r = prog.toProgram1();
			//System.out.println(""+r.toString());
			if (r == null)
				break;
			String xString = "";
			int termCnt = 0;
			boolean findRule = true;
			while ((xString = this.validRule(r, pars)) != "GOOD" && findRule) {
				if (xString.compareTo("NO_CLASIF") == 0) {
					break; // indistinguishable classes.
				}
				for (Partition p : prog.partitions) {
					if (p.label.compareTo(xString) == 0) {
						String newRule = p.toProgram();
						if (ConfigParameters.debug == 1)
							System.out.println("updated Rule: " + p.label
									+ ": " + newRule);
						if (newRule.contains("null")) {
							findRule = false;
							break;
						}
						r.updateClassworker(xString, newRule);
					}
				}
				termCnt++;
			}
			if (findRule)
			{
				rules.add(r);
			}
			i++;
		}
		return rules;
	}
	public Collection<ProgramRule> adaptive_produceProgram(Vector<Partition> pars)
	{		
		Program prog = new Program(pars, this.classifier,this.dataPreProcessor);
		this.myprog = prog;
		HashSet<ProgramRule> rules = new HashSet<ProgramRule>();
		int prog_cnt = 1;
		int i = 0;
		while (i < prog_cnt) {
			ProgramRule r = prog.toProgram2(msGer);
			if (r == null)
				return null;
			String xString = "";
			int termCnt = 0;
			boolean findRule = true;
			while ((xString = this.validRule(r, pars)) != "GOOD" && findRule) {
				if (xString.compareTo("NO_CLASIF") == 0) {
					return null; // indistinguishable classes.
				}
				for (Partition p : prog.partitions) {
					if (p.label.compareTo(xString) == 0) {
						String newRule = p.toProgram();
						if (ConfigParameters.debug == 1)
							System.out.println("updated Rule: " + p.label
									+ ": " + newRule);
						if (newRule.contains("null")) {
							findRule = false;
							break;
						}
						r.updateClassworker(xString, newRule);
					}
				}
				termCnt++;
			}
			if (findRule)
			{
				rules.add(r);
			}
			i++;
		}
		return rules;
	}
	public Vector<Partition> adaptive_producePartition()
	{	
		Vector<Partition> pars = this.initePartitions();
		partiCluster = new ExampleCluster(this, pars, string2Vector);
		partiCluster.updateConstraints(msGer.getConstraints());
		partiCluster.updateWeights(msGer.weights);
		//update the program space and hypothesis space
		pars = partiCluster.adaptive_cluster_weightEuclidean(pars);
		this.ruleNo = pars.size();
		return pars;
	}
	public Collection<ProgramRule> run_main() {
		long t1 = System.currentTimeMillis();
		StopWatch stopWatch0 = new Log4JStopWatch("main");
		StopWatch stopWatch = new Log4JStopWatch("producePartition");
		Vector<Partition> vp = this.ProducePartitions(true);
		stopWatch.stop();
		StopWatch stopWatch1 = new Log4JStopWatch("producePrograms");
		Collection<ProgramRule> cpr = this.producePrograms(vp);
		stopWatch1.stop();
		Traces.AllSegs.clear();
		stopWatch0.stop();
		this.learnspan = (long) ((System.currentTimeMillis()-t1)*1.0/1000);
		return cpr;
	}

	public String validRule(ProgramRule p, Vector<Partition> vp) {
		for (Partition px : vp) {
			for (int i = 0; i < px.orgNodes.size(); i++) {
				String s1 = UtilTools.print(px.orgNodes.get(i));
				String labelString = p.getClassForValue(s1);
				if (labelString.compareTo(px.label) != 0) {
					//logger.error(vp.toString());
					ulogger.error("classification error on examples " + s1
							+ "as: " + labelString);
					// return "NO_CLASIF";
				}
				InterpreterType worker = p.getWorkerForClass(px.label);
				String s2 = "";
				try {
					s2 = new String(worker.execute(s1).getBytes(), "UTF-8");
				} catch (Exception e) {
					return "DECODE_ERROR";
				}
				String s3 = UtilTools.print(px.tarNodes.get(i));
				if (s3.compareTo(s2) != 0) {
					return labelString;
				}
			}
		}
		return "GOOD";
	}
}
