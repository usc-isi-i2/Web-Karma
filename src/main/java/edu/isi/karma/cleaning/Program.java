package edu.isi.karma.cleaning;

import java.util.Vector;

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
			this.partitions.get(i).setLabel("\'attr_"+i+"\'");
			System.out.println(this.partitions.get(i).toString());
		}
		if(partitions.size()>1)
			this.learnClassifier();
	}
	public void learnClassifier()
	{
		PartitionClassifier pcf = new PartitionClassifier();
		PartitionClassifierType classifier= pcf.create(this.partitions);
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
				pr.addRule(p.label, p.toProgram());
				score += p.getScore();
			}
			score = score/this.partitions.size();
			
			return pr;
		}
		else
		{
			String s = partitions.get(0).toProgram(); 
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
}
