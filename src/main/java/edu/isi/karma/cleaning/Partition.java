package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

public class Partition implements GrammarTreeNode {
	public Traces trace;
	public Vector<Vector<TNode>> orgNodes;
	public Vector<Vector<TNode>> tarNodes;
	public Vector<String> mapping = new Vector<String>();
	public String label; // the class label of current partition
	public String cls;

	public Partition() {

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

	public void setTraces(Traces t) {
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

	public String getHashKey() {
		String s = "";
		ArrayList<String> lines = new ArrayList<String>();
		for (int i = 0; i < this.orgNodes.size(); i++) {
			String line = UtilTools.print(this.orgNodes.get(i)) + "   "
					+ UtilTools.print(this.tarNodes.get(i)) + "\n";
			lines.add(line);
		}
		Collections.sort(lines);
		for (String l : lines) {
			s += l;
		}
		return s;
	}

	public String toString() {
		String s = "partition:" + this.label + "\n";
		s += "Examples:\n";
		ArrayList<String> lines = new ArrayList<String>();
		for (int i = 0; i < this.orgNodes.size(); i++) {
			String line = this.orgNodes.get(i).toString() + "   "
					+ this.tarNodes.get(i).toString() + "\n";
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

	public String toProgram() {
		// //randomly choose a Template
		// Iterator<String> iterator = this.templates.keySet().iterator();
		// String[] inds = new String[this.templates.keySet().size()];
		// double[] prob = new double[inds.length];
		// int i = 0;
		// double totalLength = 0;
		// while(iterator.hasNext())
		// {
		// String key = iterator.next();
		// inds[i] = key;
		// int size = templates.get(key).get(0).size();
		// prob[i] = 1.0/(size*1.0);
		// totalLength += prob[i];
		// i++;
		// }
		// for(int j = 0; j<inds.length; j++)
		// {
		//
		// prob[j] = prob[j]*1.0/totalLength;
		// }
		// int clen = UtilTools.multinominalSampler(prob);
		// String key = inds[clen];
		// int k = UtilTools.randChoose(templates.get(key).size());
		// String r = templates.get(key).get(k).toProgram();
		// //String r =
		// String.format("(not getClass(\"%s\",value)==\'attr_0\',len(%s))",this.cls,"\"\'"+this.label+"\'\"");
		// score = templates.get(key).get(k).getScore();
		return this.trace.toProgram();

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

	@Override
	public void createTotalOrderVector() {
		// TODO Auto-generated method stub

	}

	@Override
	public void emptyState() {
		// TODO Auto-generated method stub

	}
}
