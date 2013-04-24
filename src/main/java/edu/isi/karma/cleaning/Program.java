package edu.isi.karma.cleaning;

import java.util.Vector;

import edu.isi.karma.cleaning.features.RecordClassifier;

public class Program implements GrammarTreeNode {
	public Vector<Partition> partitions = new Vector<Partition>();
	public String cls = "";
	public double score = 0.0;
	public PartitionClassifierType classifier;
	public Program(Vector<Partition> pars)
	{
		this.partitions = pars;
		for(int i=0;i<this.partitions.size();i++)
		{
			this.partitions.get(i).setLabel("attr_"+i);
			if(ConfigParameters.debug == 1)
				System.out.println(this.partitions.get(i).toString());
		}
		if(partitions.size()>1)
			this.learnClassifier();
	}
	public void learnClassifier()
	{
		PartitionClassifier pcf = new PartitionClassifier();
		//PartitionClassifierType classifier= pcf.create(this.partitions);
		PartitionClassifierType classifier= pcf.create2(this.partitions);
		this.classifier = classifier;
//		this.cls = pcf.clssettingString;
		//this.cls = "x";
//		for(Partition p:this.partitions)
//		{
//			p.cls = this.cls;
//		}
	}
	public double getScore()
	{
		double r =  score;
		this.score = 0.0;
		return r;
	}
	
	public String toProgram() {
		if(this.partitions.size()>1)
		{
			String res = "switch([";
			for(Partition p:this.partitions)
			{
				String r = String.format("(getClass(\"%s\",value)[1:7]==%s,%s)",p.cls,p.label,p.toProgram());
				res += r+",";
				score += p.getScore();
			}
			score = score/this.partitions.size();
			res = res.substring(0,res.length()-1);
			res += "])";
			return res;
		}
		else
		{
			String s = partitions.get(0).toProgram(); 
			score = this.partitions.get(0).getScore();
			return s;
		}
	}
	public ProgramRule toProgram1() {
		ProgramRule pr = new ProgramRule(this);
		if(this.partitions.size()>1)
		{
			for(Partition p:this.partitions)
			{
				String rule = p.toProgram();
				if(rule.contains("null"))
					return null;
				pr.addRule(p.label, rule);
				if(ConfigParameters.debug == 1)
					System.out.println(pr.getStringRule(p.label));
				score += p.getScore();
			}
			score = score/this.partitions.size();
			
			return pr;
		}
		else
		{
			String s = partitions.get(0).toProgram();
			if(s.contains("null"))
				return null;
			if(ConfigParameters.debug == 1)
				System.out.println(""+s);
			score = this.partitions.get(0).getScore();
			pr.addRule(partitions.get(0).label, s);
			return pr;
		}
	}
	public String toString()
	{
		String resString = "";
		for(Partition p:this.partitions)
		{
			resString = p.toString()+"\n";
		}
		return resString;
	}

	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		// TODO Auto-generated method stub
		return null;
	}
	public String getNodeType()
	{
		return "program";
	}
	public String getrepString()
	{
		return "Program";
	}
	@Override
	public void createTotalOrderVector() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void emptyState() {
		// TODO Auto-generated method stub
		
	}
	public long size()
	{
		long size = 0;
		for(Partition p:partitions)
		{
			size += p.size();
		}
		return size;
	}
}
