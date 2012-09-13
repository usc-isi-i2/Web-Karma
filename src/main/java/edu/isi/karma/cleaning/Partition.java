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
			if(templates.get(i).size()>0)
				s += templates.get(i).get(0).toProgram()+"\n";
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
		Iterator<Integer> iterator = this.templates.keySet().iterator();
		int[] inds = new int[this.templates.keySet().size()];
		double[] prob = new double[inds.length];
		int i = 0;
		double totalLength = 0;
		while(iterator.hasNext())
		{
			inds[i] = iterator.next();
			prob[i] = 1.0/(inds[i]*1.0);
			totalLength += prob[i];
			i++;
		}
		for(int j = 0; j<inds.length; j++)
		{
			
			prob[j] = prob[j]*1.0/totalLength;
		}
		int clen = UtilTools.multinominalSampler(prob);
		clen = inds[clen];
		int k = UtilTools.randChoose(templates.get(clen).size());
		String r = templates.get(clen).get(k).toProgram();
		//String r = String.format("(not getClass(\"%s\",value)==\'attr_0\',len(%s))",this.cls,"\"\'"+this.label+"\'\"");
		score = templates.get(clen).get(k).getScore();
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
