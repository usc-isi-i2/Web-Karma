package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class Partition implements GrammarTreeNode {
	public Traces trace;
	public Vector<Vector<TNode>> orgNodes;
	public Vector<Vector<TNode>> tarNodes;
	public Vector<String> orgUnlabeledData = new Vector<String>();
	public Vector<String> mapping = new Vector<String>();
	public String label; // the class label of current partition
	public String cls;
	public String program = "";
	public Partition()
	{	
	}
	public Partition(Traces t,Vector<Vector<TNode>> orgNodes, Vector<Vector<TNode>> tarNodes)
	{
		this.trace = t;
		this.orgNodes = orgNodes;
		this.tarNodes = tarNodes;
		
	}
	public long size() {
		return trace.size();
	}

	public Partition(Vector<Vector<TNode>> org, Vector<Vector<TNode>> tar) {
		this.orgNodes = org;
		this.tarNodes = tar;
		Vector<Traces> ts = new Vector<Traces>();
		for (int i = 0; i < orgNodes.size(); i++) {
			Traces t = new Traces(orgNodes.get(i), tarNodes.get(i));
			ts.add(t);
		}
		Traces iterTraces = ts.get(0);
		for (int i = 1; i < ts.size(); i++) {
			iterTraces = iterTraces.mergewith(ts.get(i));
		}
		this.trace = iterTraces;
	}
	public void setunLabeledData(Vector<String> orgUdata)
	{
		this.orgUnlabeledData = orgUdata;
	}
	public void setTraces(Traces t)
	{
		this.trace = t;
	}

	public void setExamples(Vector<Vector<TNode>> orgNodes,
			Vector<Vector<TNode>> tarNodes) {
		this.orgNodes = orgNodes;
		this.tarNodes = tarNodes;
	}

	public void setLabel(String option) {
		this.label = option;
	}

	public Partition mergewith(Partition b) {
		Traces mt = this.trace.mergewith(b.trace);
		// add the examples
		Vector<Vector<TNode>> norg = new Vector<Vector<TNode>>();
		Vector<Vector<TNode>> ntar = new Vector<Vector<TNode>>();
		norg.addAll(this.orgNodes);
		norg.addAll(b.orgNodes);
		ntar.addAll(this.tarNodes);
		ntar.addAll(b.tarNodes);
		if (mt != null) {
			Partition p = new Partition();
			p.setExamples(norg, ntar);
			p.setTraces(mt);
			return p;
		} else {
			return null;
		}
	}
	@Override
	public int hashCode()
	{
		String s = this.getHashKey();
		return new HashCodeBuilder(17, 31).append(s).toHashCode();
	}
	public String getHashKey()
	{
		ArrayList<Partition> pars = new ArrayList<Partition>();
		pars.add(this);
		return Partition.getStringKey(pars);
 	}
	public static String getStringKey(ArrayList<Partition> pars)
	{
		String s = "";
		ArrayList<String[]> lines = new ArrayList<String[]>();
		for(Partition p: pars)
		{
			for (int i = 0; i < p.orgNodes.size(); i++) {
				String s1 = UtilTools.print(p.orgNodes.get(i));
				String s2 = UtilTools.print(p.tarNodes.get(i));
				String[] line = {s1,s2};
				lines.add(line);
			}
		}
		s = UtilTools.createkey(lines);
		return s.trim();
	}

	public String toString() {
		String s = "partition:" + this.label + "\n";
		s += "Examples:\n";
		ArrayList<String> lines = new ArrayList<String>();
		for(int i = 0; i<this.orgNodes.size(); i++)
		{
			String line= UtilTools.print(this.orgNodes.get(i))+"   "+UtilTools.print(this.tarNodes.get(i))+"\n";
			lines.add(line);
		}
		Collections.sort(lines);
		s += Arrays.toString(lines.toArray());
		return s;
	}

	private double score = 0.0;

	public double getScore() {
		double r = score;
		this.score = 0.0;
		return r;
	}

	public ArrayList<String> genAtomicPrograms(){
		ArrayList<String> ret = new ArrayList<String>();
		ret.addAll(trace.genAtomicPrograms());
		return ret;
	}
	public String toProgram() {
		String res =  this.trace.toProgram();
		this.program = res;
		return res;

	}

	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Partition p = (Partition) a;
		p = this.mergewith(p);
		return p;
	}

	public String getNodeType() {
		return "partition";
	}

	public String getrepString() {
		return "Partition";
	}

	public void createTotalOrderVector() {
		
	}

	public void emptyState() {
		this.trace.emptyState();
	}

	@Override
	public String getProgram() {
		return this.program;
	}
}
