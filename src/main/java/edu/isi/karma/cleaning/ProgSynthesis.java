package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class ProgSynthesis {
	Vector<Vector<TNode>> orgVector = new Vector<Vector<TNode>>();
	Vector<Vector<TNode>> tarVector = new Vector<Vector<TNode>>();
	String bestRuleString = "";
	public PartitionClassifierType classifier;

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
					Vector<Integer> x = (Vector<Integer>) p.get(j).clone();
					x.add(i);
					qVector.add(x);
				}
			}
		}
		qVector = generateCrossIndex(poss, qVector, index + 1);
		return qVector;
	}

	public Vector<Template> crossMerge(Vector<Vector<Template>> tmps) {
		Vector<Vector<Integer>> p = new Vector<Vector<Integer>>();
		Vector<Integer> poss = new Vector<Integer>();
		for (Vector<Template> t : tmps) {
			poss.add(t.size());
		}
		p = this.generateCrossIndex(poss, p, 0);
		Vector<Template> resTemplates = new Vector<Template>();
		for (int i = 0; i < p.size(); i++) {
			Template t = tmps.get(0).get(p.get(i).get(0));
			boolean doable = true;
			for (int j = 1; j < p.get(i).size(); j++) {
				Template t1 = tmps.get(j).get(p.get(i).get(j));
				t = t.mergewith(t1);
				if (t == null) {
					doable = false;
					break;
				}
			}
			if (doable) {
				resTemplates.add(t);
			}
		}
		return resTemplates;
	}

	public double getCompScore(int i, int j, Vector<Partition> pars) {
		Partition p = pars.get(i).mergewith(pars.get(j));
		if(p==null)
		{
			return -Double.MAX_VALUE;
		}
		int validCnt = 0;
		for (int x = 0; x < pars.size(); x++) {
			if (x == i || x == j) {
				continue;
			}
			Partition q = p.mergewith(pars.get(x));
			if (q != null) {
				validCnt++;
			}
		}
		return validCnt;
	}

	public Vector<Partition> initePartitions(Vector<HashMap<Integer, Vector<Template>>> xyz) {
		Vector<Partition> pars = new Vector<Partition>();
		// inite partition for each example
		for (int i = 0; i < xyz.size(); i++) {
			HashMap<Integer, Vector<Template>> tx = xyz.get(i);
			Vector<Vector<TNode>> ovt = new Vector<Vector<TNode>>();
			Vector<Vector<TNode>> tvt = new Vector<Vector<TNode>>();
			ovt.add(this.orgVector.get(i));
			tvt.add(this.tarVector.get(i));
			HashMap<String, Vector<Template>> hsv = Partition.condenseTemplate(tx);
			Partition pt = new Partition(hsv, ovt, tvt);
			pars.add(pt);
		}
		return pars;
	}

	public void mergePartitions(Vector<Partition> pars) {
		double maxScore = 0;
		int[] pos = { -1, -1 };
		for (int i = 0; i < pars.size(); i++) {
			for (int j = i + 1; j < pars.size(); j++) {
				double s = getCompScore(i, j, pars);
				if(s <0)
				{
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

	public HashMap<Integer, Vector<String>> run() {
		// generate mapping segmentList and put it into a hashmap
		Vector<HashMap<Integer, Vector<Template>>> xyz = new Vector<HashMap<Integer, Vector<Template>>>();
		for (int i = 0; i < this.orgVector.size(); i++) {
			Vector<Vector<int[]>> mapping = Alignment.map(orgVector.get(i),
					tarVector.get(i));
			HashMap<Integer, Vector<Template>> segs = Alignment
					.genSegseqList(mapping);
			HashMap<String, Segment> segdict = new HashMap<String, Segment>();
			// initialize the temple with examples
			for (Integer j : segs.keySet()) {
				Vector<Template> temps = segs.get(j);
				for (Template elem : temps) {
					elem.initeDescription(orgVector.get(i), tarVector.get(i),segdict);
				}
			}
			xyz.add(segs);
		}

		Set<Integer> keysSet = xyz.get(0).keySet();
		HashMap<Integer, Vector<String>> dicts = new HashMap<Integer, Vector<String>>();
		for (Integer key : keysSet) {
			Vector<Vector<Template>> tVector = new Vector<Vector<Template>>();
			boolean doable = true;
			for (int i = 0; i < xyz.size(); i++) {
				if (!xyz.get(i).containsKey(key)) {
					doable = false;
					break;
				}
				tVector.add(xyz.get(i).get(key));
			}
			if (!doable) {
				continue;
			}
			Vector<Template> xTemplates = crossMerge(tVector);
			Vector<String> reStrings = new Vector<String>();
			for (Template t : xTemplates) {
				System.out.println("" + t.toString());
				// System.out.println(""+t.toProgram());
				reStrings.add(t.toProgram());
			}
			dicts.put(key, reStrings);
		}
		return dicts;
	}
	public HashSet<String> run_sumit() {
		// generate mapping segmentList and put it into a hashmap
		Vector<HashMap<Integer, Vector<Template>>> xyz = new Vector<HashMap<Integer, Vector<Template>>>();
		
		for (int i = 0; i < this.orgVector.size(); i++) {
			HashMap<String, Segment> segdict = new HashMap<String, Segment>();
			HashMap<Integer, Vector<Template>> segs = new HashMap<Integer, Vector<Template>>();
			HashSet<String> traces = new HashSet<String>();
			HashMap<String, String> dict = new HashMap<String, String>();
			Alignment.SumitTraceGen(0, orgVector.get(i),tarVector.get(i), "", traces, dict);
			for(String s:traces)
			{
				Vector<Segment> vsSegments =Alignment.genSumitSegments(s,segdict);
				Vector<GrammarTreeNode> vgt = new Vector<GrammarTreeNode>();
				for(Segment sv:vsSegments)
				{
					vgt.add((GrammarTreeNode)sv);
				}
				Template template = new Template(vgt,0);
				if(segs.containsKey(template.size()))
				{
					segs.get(template.size()).add(template);
				}
				else {
					Vector<Template> vtTemplates = new Vector<Template>();
					vtTemplates.add(template);
					segs.put(template.size(), vtTemplates);
				}
			}
			segdict.clear(); // cleared for initialization 
			HashMap<Integer, Vector<Template>> newsegs = new HashMap<Integer, Vector<Template>>();
			// initialize the temple with examples
			for (Integer j : segs.keySet()) {
				Vector<Template> temps = segs.get(j);
				for (Template elem : temps) {
					elem.initeDescription(orgVector.get(i), tarVector.get(i),segdict);
					Vector<Template> vts = elem.produceVariations();
					vts.add(elem);
					for(int p=0;p<vts.size();p++)
					{
						int leng = vts.get(p).size();
						if(newsegs.containsKey(leng))
						{
							newsegs.get(leng).add(vts.get(p));
						}
						else {
							Vector<Template> vtep = new Vector<Template>();
							vtep.add(vts.get(p));
							newsegs.put(leng, vtep);
						}
					}	
				}
			}
			xyz.add(newsegs);
		}
		Vector<Partition> pars = this.initePartitions(xyz);
		int size = pars.size();
		while(true)
		{
			this.mergePartitions(pars);
			if(size == pars.size())
			{
				break;
			}
			else
			{
				size = pars.size();
			}
		}
		Program prog = new Program(pars);
		//return a list of randomly choosen rules
		HashSet<String> rules = new HashSet<String>();
		Interpretor it = new Interpretor();
		double max = -1;
		for(int i = 0; i<50; i++)
		{
			String r = prog.toProgram();
			double s = prog.getScore();
			if(this.validRule(r, it))
			{
				rules.add(r);
				if(s>max)
				{
					this.bestRuleString = r;
					System.out.println("<<<<<<<<BestRule: "+bestRuleString+" , "+s);
					max = s;
				}
			}
		}
		return rules;	
	}
	public String run_partition() {
		// generate mapping segmentList and put it into a hashmap
		Vector<HashMap<Integer, Vector<Template>>> xyz = new Vector<HashMap<Integer, Vector<Template>>>();
		for (int i = 0; i < this.orgVector.size(); i++) {
			Vector<Vector<int[]>> mapping = Alignment.map(orgVector.get(i),
					tarVector.get(i));
			HashMap<Integer, Vector<Template>> segs = Alignment
					.genSegseqList(mapping);
			HashMap<Integer, Vector<Template>> newsegs = new HashMap<Integer, Vector<Template>>();
			HashMap<String, Segment> segdict = new HashMap<String, Segment>();
			// initialize the temple with examples
			for (Integer j : segs.keySet()) {
				Vector<Template> temps = segs.get(j);
				for (Template elem : temps) {
					elem.initeDescription(orgVector.get(i), tarVector.get(i),segdict);
					Vector<Template> vts = elem.produceVariations();
					vts.add(elem);
					for(int p=0;p<vts.size();p++)
					{
						int leng = vts.get(p).size();
						if(newsegs.containsKey(leng))
						{
							newsegs.get(leng).add(vts.get(p));
						}
						else {
							Vector<Template> vtep = new Vector<Template>();
							vtep.add(vts.get(p));
							newsegs.put(leng, vtep);
						}
					}	
				}
			}
			xyz.add(newsegs);
		}
		Vector<Partition> pars = this.initePartitions(xyz);
		int size = pars.size();
		while(true)
		{
			this.mergePartitions(pars);
			if(size == pars.size())
			{
				break;
			}
			else
			{
				size = pars.size();
			}
		}
		Program prog = new Program(pars);
		//return a list of randomly choosen rules
		return prog.toProgram();	
	}
	public String getBestRule()
	{
		return this.bestRuleString;
	}
	public HashSet<String> run_main() {
		// generate mapping segmentList and put it into a hashmap
		Vector<HashMap<Integer, Vector<Template>>> xyz = new Vector<HashMap<Integer, Vector<Template>>>();
		for (int i = 0; i < this.orgVector.size(); i++) {
			Vector<Vector<int[]>> mapping = Alignment.map(orgVector.get(i),
					tarVector.get(i));
			HashMap<Integer, Vector<Template>> segs = Alignment
					.genSegseqList(mapping);
			HashMap<Integer, Vector<Template>> newsegs = new HashMap<Integer, Vector<Template>>();
			HashMap<String, Segment> segdict = new HashMap<String, Segment>();
			// initialize the temple with examples
			for (Integer j : segs.keySet()) {
				Vector<Template> temps = segs.get(j);
				for (Template elem : temps) {
					elem.initeDescription(orgVector.get(i), tarVector.get(i),segdict);
					Vector<Template> vts = elem.produceVariations();
					vts.add(elem);
					for(int p=0;p<vts.size();p++)
					{
						int leng = vts.get(p).size();
						if(newsegs.containsKey(leng))
						{
							newsegs.get(leng).add(vts.get(p));
						}
						else 
						{
							Vector<Template> vtep = new Vector<Template>();
							vtep.add(vts.get(p));
							newsegs.put(leng, vtep);
						}
					}	
				}
			}
			xyz.add(newsegs);
		}
		Vector<Partition> pars = this.initePartitions(xyz);
		int size = pars.size();
		while(true)
		{
			this.mergePartitions(pars);
			if(size == pars.size())
			{
				break;
			}
			else
			{
				size = pars.size();
			}
		}
		Program prog = new Program(pars);
		//return a list of randomly choosen rules
		HashSet<String> rules = new HashSet<String>();
		Interpretor it = new Interpretor();
		double max = -1;
		for(int i = 0; i<50; i++)
		{
			String r = prog.toProgram();
			double s = prog.getScore();
			if(this.validRule(r, it))
			{
				rules.add(r);
				if(s>max)
				{
					this.bestRuleString = r;
					System.out.println("<<<<<<<<BestRule: "+bestRuleString+" , "+s);
					max = s;
				}
			}
		}
		return rules;	
	}
	public boolean validRule(String p,Interpretor it)
	{
		InterpreterType worker = it.create(p);
		boolean res = true;
		for(int i=0; i<orgVector.size();i++)
		{
			String s1 = UtilTools.print(orgVector.get(i));
			String s2 = worker.execute(s1);
			String s3 = UtilTools.print(tarVector.get(i));
			if(s3.compareTo(s2)!=0)
			{
				res = false;
				break;
			}
		}
		return res;
	}
}
