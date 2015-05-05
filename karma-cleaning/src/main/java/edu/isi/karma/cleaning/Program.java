package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

public class Program implements GrammarTreeNode {
	public Vector<Partition> partitions = new Vector<Partition>();
	public String cls = "";
	public double score = 0.0;
	public PartitionClassifierType classifier;
	public DataPreProcessor dpPreProcessor;
	public String program = "null";

	public Program(Vector<Partition> pars, PartitionClassifierType classifier, DataPreProcessor dpp) {
		this.partitions = pars;
		this.dpPreProcessor = dpp;
		for (int i = 0; i < this.partitions.size(); i++) {
			this.partitions.get(i).setLabel("attr_" + i);
			// if(ConfigParameters.debug == 1)
			// System.out.println(this.partitions.get(i).toString());
		}
		if(pars.size()>1)
		{
			this.classifier = classifier;
		}
		this.emptyState();
	}

	public void learnClassifier() {
		PartitionClassifier pcf = new PartitionClassifier();
		PartitionClassifierType classifier = pcf.create2(this.partitions,
				this.classifier,this.dpPreProcessor);
		this.classifier = classifier;
		// this.cls = pcf.clssettingString;
		// this.cls = "x";
		// for(Partition p:this.partitions)
		// {
		// p.cls = this.cls;
		// }
	}

	public double getScore() {
		double r = score;
		this.score = 0.0;
		return r;
	}

	public String toProgram() {
		if (this.partitions.size() > 1) {
			String res = "switch([";
			for (Partition p : this.partitions) {
				String r = String.format(
						"(getClass(\"%s\",value)[1:7]==%s,%s)", p.cls, p.label,
						p.toProgram());
				res += r + ",";
				score += p.getScore();
			}
			score = score / this.partitions.size();
			res = res.substring(0, res.length() - 1);
			res += "])";
			this.program = res;
			return res;
		} else {
			String s = partitions.get(0).toProgram();
			score = this.partitions.get(0).getScore();
			this.program = s;
			return s;
		}
	}
	public void filterUnlabeledData(ProgramRule pr)
	{
		for(Partition p:this.partitions)
		{
			Iterator<String> iter = p.orgUnlabeledData.iterator();
			while(iter.hasNext())
			{
				String val = iter.next();
				String res = pr.getWorkerForClass(p.label).execute(val);
				if(res == null || res.length() == 0)
				{
					iter.remove();
				}
			}
		}
	}
	public ProgramRule toProgram2(Messager msger){
		ProgramRule pr = new ProgramRule(this);
		ProgramAdaptator programAdaptator = new ProgramAdaptator();
		if (this.partitions.size() > 1) {
			//StopWatch spw1 = new Log4JStopWatch("Codeblock-genprogram");
			for (Partition p : this.partitions) {
				if (p.tarNodes.get(0).size() == 0) {
					pr.addRule(p.label, "substr(value,0,0)");
					continue;
				}
				//String rule = p.toProgram();
				String key = p.getHashKey(); 
				String rule = "null";
				if(msger.exp2program.containsKey(key))
				{
					rule = msger.exp2program.get(key);
				}
				else
				{
					ArrayList<Partition> xpars = new ArrayList<Partition>();
					xpars.add(p);
					ArrayList<String[]> examples = UtilTools.extractExamplesinPartition(xpars);
					rule = programAdaptator.adapt(msger.exp2Partition, msger.exp2program, examples);
				}
				if (rule.contains("null"))
					return null;
				pr.addRule(p.label, rule);
				score += p.getScore();
			}
			score = score / this.partitions.size();
			filterUnlabeledData(pr);
			this.learnClassifier();
			return pr;
		} else {
			if (partitions.size() <= 0) {
				return null;
			}
			if (partitions.get(0).tarNodes.get(0).size() == 0) {
				pr.addRule(partitions.get(0).label, "substr(value,0,0)");
				return pr;
			}
			String key = partitions.get(0).getHashKey();
			String s = "null";
			if(msger.exp2program.containsKey(key))
			{
				s = msger.exp2program.get(key);
			}
			else
			{
				ArrayList<Partition> xpars = new ArrayList<Partition>();
				xpars.add(partitions.get(0));
				ArrayList<String[]> examples = UtilTools.extractExamplesinPartition(xpars);
				s = programAdaptator.adapt(msger.exp2Partition, msger.exp2program, examples);
			}
			if (s.contains("null"))
				return null;
			score = this.partitions.get(0).getScore();
			pr.addRule(partitions.get(0).label, s);
			return pr;
		}
	}
	public ProgramRule toProgram1() {
		ProgramRule pr = new ProgramRule(this);
		if (this.partitions.size() > 1) {
			for (Partition p : this.partitions) {
				if (p.tarNodes.get(0).size() == 0) {
					pr.addRule(p.label, "substr(value,0,0)");
					continue;
				}
				StopWatch stopWatch = new Log4JStopWatch("toProgram1");
				String rule = p.toProgram();
				stopWatch.stop();

				if (rule.contains("null"))
					return null;
				pr.addRule(p.label, rule);
				score += p.getScore();
			}
			score = score / this.partitions.size();
			filterUnlabeledData(pr);
			this.learnClassifier();
			return pr;
		} else {
			if (partitions.size() <= 0) {
				return null;
			}
			if (partitions.get(0).tarNodes.get(0).size() == 0) {
				pr.addRule(partitions.get(0).label, "substr(value,0,0)");
				return pr;
			}
			String s = partitions.get(0).toProgram();
			if (s.contains("null"))
				return null;
			score = this.partitions.get(0).getScore();
			pr.addRule(partitions.get(0).label, s);
			return pr;
		}
	}

	public String toString() {
		String resString = "";
		for (Partition p : this.partitions) {
			resString += p.toString() + "\n";
			/*for(String s:p.orgUnlabeledData)
			{
				resString += s+"\n";
			}*/
		}
		return resString;
	}

	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNodeType() {
		return "program";
	}

	public String getrepString() {
		return "Program";
	}

	public void createTotalOrderVector() {
		// TODO Auto-generated method stub

	}

	public void emptyState() {
		// TODO Auto-generated method stub
		for(Partition p:partitions)
		{
			p.emptyState();
		}
	}

	public long size() {
		long size = 0;
		for (Partition p : partitions) {
			size += p.size();
		}
		return size;
	}

	@Override
	public String getProgram() {
		return this.program;
	}

	@Override
	public ArrayList<String> genAtomicPrograms() {
		// TODO Auto-generated method stub
		return null;
	}
}
