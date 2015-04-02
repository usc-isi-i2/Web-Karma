package edu.isi.karma.cleaning;

import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

public class Segment implements GrammarTreeNode {
	public Vector<Section> section = new Vector<Section>();
	public static final int cxtsize_limit = 4;
	public static final int time_limit = 5;
	public String tarString = "";
	public static final String LEFTPOS = "leftpos";
	public static final String RIGHTPOS = "rightpos";
	public static final int CONST = -1;
	public static final int UNDFN = -2;
	public int start = 0; // start position in tarNodes, Token index
	public int end = 0; // end position in tarNodes, Token index
	public Vector<int[]> mappings; // corresponding multiple sources areas in org, Token indexes 
	public boolean isinloop = false;
	public Vector<TNode> constNodes = new Vector<TNode>();
	public String repString = "";
	public int curState = 0;
	public Vector<String> segStrings = new Vector<String>();
	public int VersionSP_size = 0;
	public String program = "null";

	public Segment(Vector<TNode> cont) {
		constNodes = cont;
		for(TNode n:cont)
		{
			this.tarString += n.text;
		}
		createTotalOrderVector();
	}

	public Segment(int start, int end, Vector<TNode> cont) {
		this.start = start;
		this.end = end;
		this.constNodes = cont;
		for(TNode n:cont)
		{
			this.tarString += n.text;
		}
		this.createTotalOrderVector();
	}

	public Segment(Vector<Section> sections, boolean loop) {
		this.section = sections;
		this.isinloop = loop;
		this.createTotalOrderVector();
	}

	public Segment(int start, int end, Vector<int[]> mapping,
			Vector<TNode> orgNodes, Vector<TNode> tarNodes) {
		this.start = start;
		this.end = end;
		this.mappings = mapping;
		for (int i = start; i < end; i++) {
			tarString += tarNodes.get(i).text;
		}
		initSections(orgNodes);
		repString = "";
		if (tarNodes.size() == 0) {
			repString += "EMPTY";
		} else {
			repString += tarNodes.get(this.start).getType();
			if (end > start + 1)
				repString += tarNodes.get(this.end - 1).getType();
		}
		this.createTotalOrderVector();
	}

	/*
	 * public void setSections(Vector<Position[]> sections) { Vector<Section> s
	 * = new Vector<Section>(); for(Position[] p:sections) { Section sx = new
	 * Section(p,this.isinloop); s.add(sx); } this.section = s;
	 * this.createTotalOrderVector(); }
	 */
	public Vector<TNode> getLeftCxt(int c, Vector<TNode> x) {
		int i = cxtsize_limit;
		Vector<TNode> res = new Vector<TNode>();
		while (i > 0) {
			if ((c - i) < 0) {
				i--;
				continue;
			}
			res.add(x.get(c - i));
			i--;
		}
		return res;
	}

	public Vector<TNode> getRightCxt(int c, Vector<TNode> x) {
		int i = 0;
		Vector<TNode> res = new Vector<TNode>();
		while (i < cxtsize_limit) {
			if ((c + i) >= x.size())
				break;
			res.add(x.get(c + i));
			i++;
		}
		return res;
	}

	// if valid segment reture the first program
	// else reture "null"
	public String verifySpace() {
		String ruleString = "null";
		if (this.isConstSegment()) {
			String mdString = "";
			for (TNode t : this.constNodes) {
				mdString += t.text;
			}
			mdString = "\'" + mdString + "\'";
			this.program = mdString;
			return mdString;
		}
		for(int i = curState; i< this.section.size(); i++){
			Section s = this.section.get(i);
			curState = i; //update current state;
			s.isinloop = this.isinloop;
			ruleString = s.verifySpace();			
			if (ruleString.indexOf("null") == -1) {
				this.program = ruleString;
				return ruleString;
			}
		}
		this.program = ruleString;
		return ruleString;
	}
	//only called for the first example
	public void initSections(Vector<TNode> orgNodes) {
		for (int[] elem : mappings) {
			int s = elem[0];
			int e = elem[1];
			// record the data
			Vector<String> orgStrings = new Vector<String>();
			Vector<String> tarStrings = new Vector<String>();
			String org = "";
			for (int i = 0; i < orgNodes.size(); i++) {
				org += orgNodes.get(i).text;
			}
			orgStrings.add(org);
			tarStrings.add(tarString);
			// create the startPosition
			Vector<Integer> sset = new Vector<Integer>();
			sset = UtilTools.getStringPos(s, orgNodes);
			Vector<String> tars = new Vector<String>();
			tars.add(sset.get(0).toString());
			Position sPosition = new Position(sset, getLeftCxt(s, orgNodes),
					getRightCxt(s, orgNodes), orgStrings, tars, this.isinloop);
			sPosition.isinloop = this.isinloop;
			// create the endPosition
			Vector<Integer> eset = new Vector<Integer>();
			eset = UtilTools.getStringPos(e, orgNodes);
			Vector<String> tars1 = new Vector<String>();
			tars1.add(eset.get(0).toString());
			Position ePosition = new Position(eset, getLeftCxt(e, orgNodes),
					getRightCxt(e, orgNodes), orgStrings, tars1, this.isinloop);
			ePosition.isinloop = this.isinloop;

			if (sPosition != null && ePosition != null) {
				Position[] pair = { sPosition, ePosition };

				Section xsec = new Section(pair, orgStrings, tarStrings,
						isinloop);
				if(elem.length> 2)
					xsec.convert = elem[2];
				section.add(xsec);
			}
		}
	}

	public void setinLoop(boolean res) {
		this.isinloop = res;
		for (Section pair : section) {
			pair.isinloop = res;
		}
	}

	public void setCnt(Vector<TNode> cnst) {
		for (TNode t : cnst) {
			constNodes.add(t);
		}
	}

	public boolean isConstSegment() {
		if (this.constNodes.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public Segment mergewith(Segment s) {
		if (this.isConstSegment()) {
			if (s.isConstSegment()) {
				if (this.constNodes.size() != s.constNodes.size()) {
					return null;
				} else {
					for (int i = 0; i < this.constNodes.size(); i++) {
						if (!this.constNodes.get(i).sameNode(
								s.constNodes.get(i))) {
							return null;
						}
					}
				}
			} else {
				return null;
			}
			Segment res = new Segment(0, 0, this.constNodes);
			return res;
		}
		// merge the position
		HashSet<String> uniqueKeys = new HashSet<String>();
		Vector<Section> newSections = new Vector<Section>();
		for (Section x : this.section) {
			for (Section y : s.section) {
				GrammarTreeNode zSection = x.mergewith(y);
				if (zSection != null) {
					String ukey = zSection.toString();
					if (!uniqueKeys.contains(ukey)) {
						newSections.add((Section) zSection);
						uniqueKeys.add(ukey);
					}
				}
			}
		}
		if (newSections.size() == 0)
			return null;
		boolean loop = this.isinloop || s.isinloop;
		Segment res = new Segment(newSections, loop);
		return res;
	}

	public String getrepString() {
		if (this.constNodes.size() > 0 && this.repString.length() == 0) {
			repString = UtilTools.print(this.constNodes);
			return repString;

		} else {
			return this.repString;
		}

	}

	public String toString() {
		if (this.isConstSegment()) {
			return "<" + this.constNodes + ">";
		} else {
			String s = "<";
			if (this.isinloop) {
				s += "loop";
			}
			for (Section x : this.section) {
				s += x.toString();
			}
			s += ">";
			return s;
		}
	}

	private double score = 0.0;

	public double getScore() {
		double r = score;
		this.score = 0.0;
		return r;
	}

	public Vector<Integer> rules = new Vector<Integer>();

	public void createTotalOrderVector() {
		SortedMap<Double, Vector<Integer>> xmap = new TreeMap<Double, Vector<Integer>>();
		for (int i = 0; i < section.size(); i++) {
			Double double1 = 0.0;
			// reverse the order to get higher values sorted in front
			double1 += section.get(i).getScore();
			double key = double1;
			if (xmap.containsKey(key)) {
				xmap.get(key).add(i);
			} else {
				Vector<Integer> vi = new Vector<Integer>();
				vi.add(i);
				xmap.put(key, vi);
			}
		}
		while (!xmap.isEmpty()) {
			Double x = xmap.firstKey();
			Vector<Integer> v = xmap.get(x);
			// add the vth pair's rules
			for (Integer e : v) {
				rules.add(e);
			}
			xmap.remove(x);
		}
		// String mdString = "";
		// if(this.isConstSegment())
		// {
		// for(TNode t:this.constNodes)
		// {
		// mdString += t.text;
		// }
		// //mdString= UtilTools.escape(mdString);
		// mdString = "\'"+mdString+"\'";
		// rules.add(mdString);
		// }
		rules.add(0);
		for (Section s : section) {
			this.VersionSP_size += s.size();
		}
		this.VersionSP_size++; // constant segment
	}

	public long size() {
		return this.VersionSP_size;
	}

	public void emptyState() {
		this.curState = 0;
		for (Section s : section) {
			s.emptyState();
		}
	}

	public String toProgram() {
		String s =  verifySpace();
		return s;
	}

	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		GrammarTreeNode s;
		if (a.getNodeType().compareTo("loop") == 0) {
			Loop p = (Loop) a;
			s = p.mergewith(this);
			return s;
		} else {
			s = this.mergewith((Segment) a);
			return s;
		}
	}

	public String getRule(long index) {
		if (index >= this.size() || index < 0) {
			return "null";
		} else {
			for (int i = 0; i < rules.size(); i++) {
				if (rules.get(i) == -1) {
					String mdString = "";
					if (this.isConstSegment()) {
						for (TNode t : this.constNodes) {
							mdString += t.text;
						}
						mdString = "\'" + mdString + "\'";
						return mdString;
					} else {
						return "null";
					}
				}
				if (index < section.get(rules.get(i)).size()) {
					section.get(rules.get(i)).isinloop = this.isinloop;
					return section.get(rules.get(i)).getRule(index);
				} else {
					index = (index - section.get(rules.get(i)).size());
				}
			}
			return "null";
		}
	}

	public String getNodeType() {
		return "segment";
	}

	@Override
	public String getProgram() {
		return this.program;
	}
}
