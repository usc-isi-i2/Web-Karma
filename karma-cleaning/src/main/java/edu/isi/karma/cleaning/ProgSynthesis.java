package edu.isi.karma.cleaning;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.isi.karma.cleaning.Research.ConfigParameters;
import edu.isi.karma.cleaning.Research.Prober;
public class ProgSynthesis {
	public static int time_limit = 20;
	Vector<Vector<TNode>> orgVector = new Vector<Vector<TNode>>();
	Vector<Vector<TNode>> tarVector = new Vector<Vector<TNode>>();
	String bestRuleString = "";
	// for tracking the stats
	public long learnspan = 0;
	public long genspan = 0;
	public long ruleNo = 0;
	public PartitionClassifierType classifier;
	public String[] vocab = null;
	public HashMap<String, Boolean> legalParitions = new HashMap<String, Boolean>();
	public MyLogger logger = new MyLogger();
	public void inite(Vector<String[]> examples) {
		for (int i = 0; i < examples.size(); i++) {
			Ruler r = new Ruler();
			r.setNewInput(examples.get(i)[0]);
			orgVector.add(r.vec);
			Ruler r1 = new Ruler();
			r1.setNewInput(examples.get(i)[1]);
			tarVector.add(r1.vec);
		}
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

	public Vector<Partition> ProducePartitions(boolean condense) {
		Vector<Partition> pars = this.initePartitions();
		int size = pars.size();
		while (condense) {
			this.mergePartitions(pars);
			if (size == pars.size()) {
				break;
			} else {
				size = pars.size();
			}
		}
		return pars;
	}

	public Collection<ProgramRule> producePrograms(Vector<Partition> pars) {
		Program prog = new Program(pars, this.vocab);
		HashSet<ProgramRule> rules = new HashSet<ProgramRule>();
		int prog_cnt = 1;
		int i = 0;
		long startTime = System.currentTimeMillis();
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
				if ((System.currentTimeMillis() - startTime) / 1000 >= time_limit) {
					findRule = false;
					break;
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
				rules.add(r);
			this.ruleNo += termCnt; // accumulate the no of rules while the
			i++;
		}
		return rules;
	}

	public Collection<ProgramRule> run_main() {
		long t1 = System.currentTimeMillis();
		Vector<Partition> vp = this.ProducePartitions(true);
		long t2 = System.currentTimeMillis();
		Collection<ProgramRule> cpr = this.producePrograms(vp);
		long t3 = System.currentTimeMillis();
		learnspan = (t2 - t1);
		genspan = (t3 - t2);
		if (cpr == null || cpr.size() == 0) {
			t1 = System.currentTimeMillis();
			vp = this.ProducePartitions(false);
			t2 = System.currentTimeMillis();
			cpr = this.producePrograms(vp);
			t3 = System.currentTimeMillis();
			learnspan += t2 - t1;
			genspan += t3 - t2;
		}
		if(ConfigParameters.debug==1)
		{
			MyLogger.logsth(Prober.PartitionDisplay1(vp));
		}
		Traces.AllSegs.clear();
		return cpr;
	}

	public String validRule(ProgramRule p, Vector<Partition> vp) {
		for (Partition px : vp) {
			for (int i = 0; i < px.orgNodes.size(); i++) {
				String s1 = UtilTools.print(px.orgNodes.get(i));
				String labelString = p.getClassForValue(s1);
				if (labelString.compareTo(px.label) != 0) {
					return "NO_CLASIF";
				}
				InterpreterType worker = p.getWorkerForClass(labelString);
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
