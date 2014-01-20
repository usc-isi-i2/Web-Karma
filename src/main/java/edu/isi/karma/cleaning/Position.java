package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class Position implements GrammarTreeNode {
	public Vector<TNode> leftContextNodes = new Vector<TNode>();
	public Vector<TNode> rightContextNodes = new Vector<TNode>();
	public Vector<Integer> absPosition = new Vector<Integer>();
	public Vector<Integer> counters = new Vector<Integer>();
	public Vector<String> orgStrings = new Vector<String>();
	public Vector<String> tarStrings = new Vector<String>();
	public boolean isinloop = false;
	public int curState = 0;
	public static Interpretor itInterpretor = null;
	public int fixedlength = 0;

	public Position(Vector<Integer> absPos, Vector<TNode> lcxt,
			Vector<TNode> rcxt, Vector<String> orgStrings,
			Vector<String> tarStrings, boolean loop) {
		this.absPosition = absPos;
		this.orgStrings.addAll(orgStrings);
		this.tarStrings.addAll(tarStrings);
		if (itInterpretor == null)
			itInterpretor = new Interpretor();
		// occurance of a reg pattern
		this.counters.add(-1);
		this.counters.add(1);
		this.leftContextNodes = lcxt;
		this.rightContextNodes = rcxt;
		this.isinloop = loop;
		createTotalOrderVector();
	}

	public Position(Position p, boolean loop) {
		this.absPosition = p.absPosition;
		// occurance of a reg pattern
		this.counters.add(-1);
		this.counters.add(1);
		this.leftContextNodes = p.leftContextNodes;
		this.rightContextNodes = p.rightContextNodes;
		this.isinloop = loop;
		createTotalOrderVector();
	}

	public void getString(Vector<TNode> x, int cur, String path, Double value,
			HashMap<String, Double> smap, boolean isleft) {
		if (fixedlength == 0) {
			if (!smap.keySet().contains(path)) {
				String res = UtilTools.escape(path);
				if (!smap.containsKey(res) && res.length() != 0)
					smap.put(res, value); // store the string of all sizes
			}
		}
		if (x == null || x.size() == 0) {
			return;
		}
		if (cur >= x.size() || cur < 0) {
			if (fixedlength == 1) {
				if (!smap.keySet().contains(path)) {
					String res = UtilTools.escape(path);
					if (!smap.containsKey(res) && res.length() != 0)
						smap.put(res, value); // store the string of all sizes
				}
			}
			return;
		}
		TNode t = x.get(cur);
		if (t.text.compareTo("ANYTOK") != 0 && t.text.length() > 0) {
			if (!isleft)
				getString(x, cur + 1, path + t.text, value + 2, smap, false);
			else {
				getString(x, cur - 1, t.text + path, value + 2, smap, true);
			}
		}
		String s = "";
		if (t.type == TNode.NUMTYP) {
			s += "NUM";
		} else if (t.type == TNode.WORD) {
			s += "WORD";
		} else if (t.type == TNode.SYBSTYP) {
			s += "SYB";
		} else if (t.type == TNode.BNKTYP) {
			s += "BNK";
		} else if (t.type == TNode.UWRDTYP) {
			s += "UWRD";
		} else if (t.type == TNode.LWRDTYP) {
			s += "LWRD";
		} else if (t.type == TNode.STARTTYP) {
			s += "START";
		} else if (t.type == TNode.ENDTYP) {
			s += "END";
		} else if (t.type == TNode.ANYTYP) {
			s += "ANYTYP";
		} else {
			s += "" + t.getType();
		}
		if (!isleft)
			getString(x, cur + 1, path + s, value + 1, smap, false);
		else {
			getString(x, cur - 1, s + path, value + 1, smap, true);
		}
	}

	// option: left or right context
	public Vector<TNode> mergeCNXT(Vector<TNode> a, Vector<TNode> b,
			String option) {
		Vector<TNode> xNodes = new Vector<TNode>();
		if (a == null || b == null)
			return null;
		else {
			int leng = Math.min(a.size(), b.size());
			if (option.compareTo(Segment.LEFTPOS) == 0) {
				for (int i = 1; i <= leng; i++) {

					TNode t = a.get(a.size() - i);
					TNode t1 = b.get(b.size() - i);
					if (t == null || t1 == null) {
						break;
					}
					if (t.mergableType(t1) == -1) {
						break;
					} else {
						int type = t.mergableType(t1);
						if (t.text.compareTo(t1.text) == 0) {
							TNode tx = new TNode(type, t.text);
							xNodes.add(0, tx);
						} else {
							TNode tx = new TNode(type, "ANYTOK");
							xNodes.add(0, tx);
						}
					}
				}
			} else if (option.compareTo(Segment.RIGHTPOS) == 0) {
				for (int i = 0; i < leng; i++) {

					TNode t = a.get(i);
					TNode t1 = b.get(i);
					if (t == null || t1 == null) {
						break;
					}
					if (t.mergableType(t1) == -1) {
						break;
					} else {
						int type = t.mergableType(t1);
						if (t.text.compareTo(t1.text) == 0) {
							TNode tx = new TNode(type, t.text);
							xNodes.add(tx);
						} else {
							TNode tx = new TNode(type, "ANYTOK");
							xNodes.add(tx);
						}
					}
				}
			}
		}
		if (xNodes.size() == 0)
			return null;
		return xNodes;
	}

	public Position mergewith(Position b) {
		if (this == null || b == null)
			return null;
		Vector<Integer> tmpIntegers = new Vector<Integer>();
		tmpIntegers.addAll(this.absPosition);
		tmpIntegers.retainAll(b.absPosition);
		Vector<Integer> tmpIntegers2 = new Vector<Integer>();
		tmpIntegers2.addAll(this.counters);
		tmpIntegers2.retainAll(b.counters);
		Vector<TNode> tl = b.leftContextNodes;
		Vector<TNode> tr = b.rightContextNodes;
		Vector<TNode> g_lcxtNodes = mergeCNXT(this.leftContextNodes, tl,
				Segment.LEFTPOS);
		Vector<TNode> g_rcxtNodes = mergeCNXT(this.rightContextNodes, tr,
				Segment.RIGHTPOS);
		// this.leftContextNodes = g_lcxtNodes;
		// this.rightContextNodes = g_rcxtNodes;
		if (tmpIntegers.size() == 0 && g_lcxtNodes == null
				&& g_rcxtNodes == null)
			return null;
		boolean loop = this.isinloop || b.isinloop;

		Vector<String> aStrings = new Vector<String>();
		Vector<String> bStrings = new Vector<String>();

		if (this.orgStrings.size() == 1
				&& this.orgStrings.size() == b.tarStrings.size()
				&& this.orgStrings.get(0).compareTo(b.orgStrings.get(0)) == 0) {
			aStrings.addAll(this.orgStrings);
			String[] s1 = this.tarStrings.get(0).split(",");
			String[] s2 = b.tarStrings.get(0).split(",");
			HashSet<Integer> hset = new HashSet<Integer>();
			for (int x = 0; x < s1.length; x++) {
				int v = Integer.valueOf(s1[x]);
				if (!hset.contains(v)) {
					hset.add(v);
				}
			}
			for (int x = 0; x < s2.length; x++) {
				int v = Integer.valueOf(s2[x]);
				if (!hset.contains(v)) {
					hset.add(v);
				}
			}
			SortedSet<Integer> poses = new TreeSet<Integer>(hset);
			Iterator<Integer> iter = poses.iterator();
			String rep = "";
			while (iter.hasNext()) {
				rep += iter.next() + ",";
			}
			rep = rep.substring(0, rep.length() - 1);
			bStrings.add(rep);
		} else {
			aStrings.addAll(this.orgStrings);
			aStrings.addAll(b.orgStrings);
			bStrings.addAll(this.tarStrings);
			bStrings.addAll(b.tarStrings);
		}
		return new Position(tmpIntegers, g_lcxtNodes, g_rcxtNodes, aStrings,
				bStrings, loop);
	}

	public void setinLoop(boolean res) {
		this.isinloop = res;
	}

	// score sum(gToken)/size
	public double getScore() {
		double sum = 0.0;
		int lsize = 0;
		if (this.leftContextNodes != null) {
			lsize = leftContextNodes.size();
		}
		int rsize = 0;
		if (this.rightContextNodes != null) {
			rsize = rightContextNodes.size();
		}
		if (lsize == 0 && rsize == 0)
			return 1;
		else {
			for (int i = 0; i < lsize; i++) {
				if (leftContextNodes.get(i).text.compareTo("ANYTOK") != 0
						&& leftContextNodes.get(i).type != TNode.ANYTYP) {
					sum++;
				}
			}
			for (int i = 0; i < rsize; i++) {
				if (rightContextNodes.get(i).text.compareTo("ANYTOK") != 0
						&& rightContextNodes.get(i).type != TNode.ANYTYP) {
					sum++;
				}
			}
			return sum * 1.0 / (lsize + rsize);
		}
	}

	public void emptyState() {
		this.curState = 0;
	}

	public Vector<String> rules = new Vector<String>();

	public String toProgram() {
		if (curState >= rules.size())
			return "null";
		String rule = rules.get(curState);
		if (!isinloop)
			rule = rule.replace("counter", counters.get(1) + "");
		curState++;
		return rule;
	}

	public String getRule(int index) {
		if (index >= rules.size())
			return "null";
		String rule = rules.get(index);
		if (!isinloop)
			rule = rule.replace("counter", counters.get(1) + "");
		return rule;
	}

	public long size() {
		return this.rules.size();
	}

	public String VerifySpace(int itercnt) {
		String rule = "null";
		int ruleNo = 0;
		while (ruleNo < this.rules.size()) {
			rule = getRule(ruleNo);
			ruleNo++;
			// System.out.println("verifying..."+rule);
			if (isinloop) {
				// replace the counter with number and verify it
				if (rule.indexOf("counter") == -1) {
					return "null";
				}
				boolean isvalid = true;
				for (int j = 0; j < this.orgStrings.size(); j++) {
					int cnt = 1;
					String r = "";
					while (r.indexOf("None") == -1) {
						String tmpRule = rule.replace("counter",
								String.valueOf(cnt));
						ProgramRule programRule = new ProgramRule(tmpRule);
						String val = programRule.transform(this.orgStrings
								.get(j));
						if (val.indexOf("None") != -1)
							break;
						r += val + ",";
						cnt++;
					}
					if (r.length() <= 1)
						return "null";
					if (this.tarStrings.get(j).compareTo(
							r.substring(0, r.length() - 1)) != 0) {
						isvalid = false;
						break;
					}
				}
				if (isvalid) {
					if (itercnt == 0)
						return rule;
					else
						itercnt--; // valid number - 1
				}

			} else {
				ProgramRule pr = new ProgramRule(rule);
				boolean isValid = true;
				for (int k = 0; k < this.orgStrings.size(); k++) {
					String val = String.valueOf(pr.transform(this.orgStrings
							.get(k)));
					if (this.tarStrings.get(k).compareTo(val) != 0) {
						isValid = false;
					}
				}
				if (isValid) {
					if (itercnt == 0)
						return rule;
					else
						itercnt--;
				}
			}
		}
		return "null";
	}

	public void createTotalOrderVector() {
		HashMap<String, Double> lMap = new HashMap<String, Double>();
		HashMap<String, Double> rMap = new HashMap<String, Double>();
		if (this.leftContextNodes != null) {
			String path = "";
			getString(this.leftContextNodes, this.leftContextNodes.size() - 1,
					path, 1.0, lMap, true);
		} else {
			lMap.put("ANY", 1.0);
		}
		if (this.rightContextNodes != null) {
			String path = "";
			getString(this.rightContextNodes, 0, path, 1.0, rMap, false);
		} else {

			rMap.put("ANY", 1.0);
		}
		String reString = "";
		String reString2 = "";
		String reString3 = "";
		SortedMap<Double, Vector<String>> sortedMap = new TreeMap<Double, Vector<String>>();
		String negString = "";
		for (String a : lMap.keySet()) {
			for (String b : rMap.keySet()) {
				if (a.compareTo(b) == 0 && a.compareTo("ANY") == 0)
					continue;
				Double key = lMap.get(a) + rMap.get(b);
				reString = String.format(
						"indexOf(value,\'%s\',\'%s\',1*counter)", a, b);
				reString2 = String.format(
						"indexOf(value,\'%s\',\'%s\',2*counter)", a, b);
				reString3 = String.format(
						"indexOf(value,\'%s\',\'%s\',3*counter)", a, b);
				negString = String.format(
						"indexOf(value,\'%s\',\'%s\',-1*counter)", a, b);
				if (sortedMap.containsKey(key)) {
					sortedMap.get(key).add(reString);
					sortedMap.get(key).add(reString2);
					sortedMap.get(key).add(reString3);
					sortedMap.get(key).add(negString);
				} else {
					Vector<String> svec = new Vector<String>();
					svec.add(reString);
					svec.add(reString2);
					svec.add(reString3);
					svec.add(negString);
					sortedMap.put(key, svec);
				}
			}
		}
		while (!sortedMap.isEmpty()) {
			Double key = sortedMap.firstKey();
			rules.addAll(sortedMap.get(key));
			sortedMap.remove(key);
		}
		// append the absolute position to the end
		for (int k = 0; k < this.absPosition.size(); k++) {
			String line = String.format("%d", this.absPosition.get(k));
			rules.add(line);
		}
	}

	public String toString() {
		return "(" + UtilTools.print(this.leftContextNodes) + ","
				+ UtilTools.print(this.rightContextNodes) + ")";
	}

	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Position p = (Position) a;
		p = this.mergewith(p);
		return p;
	}

	public String getNodeType() {
		return "position";
	}

	public String getrepString() {
		return this.toString();
	}
}
