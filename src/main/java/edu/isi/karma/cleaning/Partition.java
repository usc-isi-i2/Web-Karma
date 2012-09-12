package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class Partition implements GrammarTreeNode {
	public HashMap<Integer,Vector<Template>> templates;
	public Vector<Vector<TNode>> orgNodes;
	public Vector<Vector<TNode>> tarNodes;
	public String label; // the class label of current partition
	public String cls;
	public Partition()
	{	
	}
	public Partition(HashMap<Integer,Vector<Template>> tp, Vector<Vector<TNode>> org,Vector<Vector<TNode>> tar)
	{
		this.templates = tp;
		this.orgNodes = org;
		this.tarNodes = tar;
	}
	public void setTemplates(HashMap<Integer,Vector<Template>> tmps)
	{
		this.templates = tmps;
	}
	public void setExamples(Vector<Vector<TNode>> orgNodes,Vector<Vector<TNode>> tarNodes)
	{
		this.orgNodes = orgNodes;
		this.tarNodes = tarNodes;
	}
	public void setLabel(String option)
	{
		this.label = option;
	}
	public Partition mergewith(Partition b)
	{
		HashMap<Integer,Vector<Template>> ntemp = new HashMap<Integer,Vector<Template>>();
		//merge the templates
		for(Integer ind:templates.keySet())
		{
			if(!b.templates.containsKey(ind))
			{
				continue;
			}
			Vector<Template> t1 = templates.get(ind);
			Vector<Template> t2 = b.templates.get(ind);
			Vector<Template> xyz = new Vector<Template>();
			for(Template ta:t1)
			{
				for(Template tb:t2)
				{
					Template t = ta.mergewith(tb);
					if(t!=null)
						xyz.add(t);
				}
			}
			if(xyz.size()!= 0)
			{
				ntemp.put(ind, xyz);
			}
		}
		//add the examples
		Vector<Vector<TNode>> norg = new Vector<Vector<TNode>>();
		Vector<Vector<TNode>> ntar = new Vector<Vector<TNode>>();
		norg.addAll(this.orgNodes);
		norg.addAll(b.orgNodes);
		ntar.addAll(this.tarNodes);
		ntar.addAll(b.tarNodes);
		if(ntemp.keySet().size()!=0)
		{
			Partition p = new Partition(ntemp, norg, ntar);
			return p;
		}
		else {
			return null;
		}	
	}
	public String toString()
	{
		String s = "partition:"+this.label+"\n";
		for(Integer i:this.templates.keySet())
		{
			for(Template t:this.templates.get(i))
			{
				s += t.toProgram()+"\n";
			}
		}
		s += "Examples:\n";
		for(int i = 0; i<this.orgNodes.size(); i++)
		{
			s+= this.orgNodes.get(i).toString()+"   "+this.tarNodes.get(i).toString()+"\n";
		}
		return s;
	}
	private double score = 0.0;
	public double getScore()
	{
		double r =  score;
		this.score = 0.0;
		return r;
	}
	@Override
	public String toProgram() {
		//randomly choose a Template
		Vector<Template> t = new Vector<Template>();
		Iterator<Integer> iterator = this.templates.keySet().iterator();
		while(iterator.hasNext())
		{
			t.addAll(this.templates.get(iterator.next()));
		}
		int i = UtilTools.randChoose(t.size());
		String r = t.get(i).toProgram();
		//String r = String.format("(not getClass(\"%s\",value)==\'attr_0\',len(%s))",this.cls,"\"\'"+this.label+"\'\"");
		score = t.get(i).getScore();
		return r;
	}
	@Override
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Partition p = (Partition)a;
		p = this.mergewith(p);
		return p;
	}
	public String getNodeType()
	{
		return "partition";
	}
}
