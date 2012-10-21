package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class Partition implements GrammarTreeNode {
	public HashMap<String,Vector<Template>> templates;
	public Vector<Vector<TNode>> orgNodes;
	public Vector<Vector<TNode>> tarNodes;
	public String label; // the class label of current partition
	public String cls;
	public Partition()
	{	
		
	}
	public static HashMap<String, Vector<Template>> condenseTemplate(HashMap<Integer, Vector<Template>> tp)
	{
		HashMap<String, Vector<Template>> xHashMap = new HashMap<String, Vector<Template>>();
		for(int ind: tp.keySet())
		{
			for(Template t:tp.get(ind))
			{
				String rep = "";
				for(HashMap<String,GrammarTreeNode> hgtn:t.segmentlist)
				{
					GrammarTreeNode gtn = hgtn.get(hgtn.keySet().iterator().next());
					rep += gtn.getNodeType();
					if(gtn.getNodeType().compareTo("loop")==0)
					{
						rep += ((Loop)gtn).loopbody.size();
					}
				}
				if(xHashMap.containsKey(ind+""))
				{
					xHashMap.get(ind+"").add(t);
				}
				else
				{
					Vector<Template> vt = new Vector<Template>();
					vt.add(t);
					xHashMap.put(ind+"", vt);
				}
			}
		}
		HashMap<String, Vector<Template>> nvt = new HashMap<String, Vector<Template>>();
		for(String key: xHashMap.keySet())
		{
			Vector<Template> vtp = xHashMap.get(key);
			Template t0 = vtp.get(0);
			for(int j = 1; j<vtp.size(); j++)
			{
				t0 = t0.TempUnion(vtp.get(j));
			}
			Vector<Template> t = new Vector<Template>();
			t.add(t0);
			nvt.put(key, t);
		}
		return nvt;
	}
	public Partition(HashMap<String,Vector<Template>> tp, Vector<Vector<TNode>> org,Vector<Vector<TNode>> tar)
	{
		this.templates = tp;
		this.orgNodes = org;
		this.tarNodes = tar;
	}
	public void setTemplates(HashMap<String,Vector<Template>> tmps)
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
		HashMap<String,Vector<Template>> ntemp = new HashMap<String,Vector<Template>>();
		//merge the templates
		for(String ind:templates.keySet())
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
		for(String i:this.templates.keySet())
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
	public String toProgram() {
		//randomly choose a Template
		Iterator<String> iterator = this.templates.keySet().iterator();
		String[] inds = new String[this.templates.keySet().size()];
		double[] prob = new double[inds.length];
		int i = 0;
		double totalLength = 0;
		while(iterator.hasNext())
		{
			String key = iterator.next();
			inds[i] = key;
			int size = templates.get(key).get(0).size();
			prob[i] = 1.0/(size*1.0);
			totalLength += prob[i];
			i++;
		}
		for(int j = 0; j<inds.length; j++)
		{
			
			prob[j] = prob[j]*1.0/totalLength;
		}
		int clen = UtilTools.multinominalSampler(prob);
		String key = inds[clen];
		int k = UtilTools.randChoose(templates.get(key).size());
		String r = templates.get(key).get(k).toProgram();
		//String r = String.format("(not getClass(\"%s\",value)==\'attr_0\',len(%s))",this.cls,"\"\'"+this.label+"\'\"");
		score = templates.get(key).get(k).getScore();
		return r;
	}
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
