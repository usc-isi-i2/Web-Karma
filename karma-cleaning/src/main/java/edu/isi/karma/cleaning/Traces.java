package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Traces implements GrammarTreeNode {
	public static final int time_limit = 20;
	public Vector<TNode> orgNodes;
	public Vector<TNode> tarNodes;
	public Vector<Vector<GrammarTreeNode>> rawlines; // used for program adaptation
	public HashMap<Integer, Template> traceline = new HashMap<Integer, Template>();
	public HashMap<Integer, HashMap<String, Template>> loopline = new HashMap<Integer, HashMap<String, Template>>();
	public int curState = 0;
	public Vector<Template> totalOrderVector = new Vector<Template>();
	// keep all the segment expression to prevent repeated construction
	public static HashMap<String, Segment> AllSegs = new HashMap<String, Segment>();
	public String program = "null";
	public Traces()
	{
		
	}
	public Traces(Vector<TNode> org, Vector<TNode> tar) {
		this.orgNodes = org;
		this.tarNodes = tar;
		this.createTraces();
		createTotalOrderVector();
	}

	public void createTotalOrderVector() {
		ArrayList<Integer> xArrayList = new ArrayList<Integer>();
		xArrayList.addAll(traceline.keySet());
		xArrayList.addAll(loopline.keySet());
		Integer[] a = new Integer[xArrayList.size()];
		Arrays.sort(xArrayList.toArray(a));
		for (Integer elem : a) {
			if (loopline.get(elem) != null) {
				totalOrderVector.addAll(loopline.get(elem).values());
			}
			if (traceline.get(elem) != null) {
				totalOrderVector.add(traceline.get(elem));
			}
		}
	}

	public Traces(HashMap<Integer, Template> t,
			HashMap<Integer, HashMap<String, Template>> l) {
		this.traceline = t;
		this.loopline = l;
		createTotalOrderVector();
	}

	// initialize the tree to represent the grammar tree
	public void createTraces() {
		Vector<Vector<Segment>> lines = new Vector<Vector<Segment>>();
		HashMap<Integer, Vector<Segment>> pos2Segs = new HashMap<Integer, Vector<Segment>>();
		Vector<Segment> children = findSegs(0);
		Vector<Vector<Segment>> tlines = new Vector<Vector<Segment>>();
		for (Segment c : children) {
			Vector<Segment> vs = new Vector<Segment>();
			vs.add(c);
			tlines.add(vs);
		}
		// find all possible segments starting from a position
		while (tlines.size() > 0) {
			Vector<Vector<Segment>> nlines = new Vector<Vector<Segment>>();
			Vector<Segment> segs = tlines.remove(0);
			int curPos = segs.get(segs.size() - 1).end;
			if (curPos == 0) // target string is empty
			{
				lines.add(segs);
				break;
			}
			//use previous created next segments 
			if (pos2Segs.containsKey(curPos))
				children = pos2Segs.get(curPos);
			else {
				children = findSegs(curPos);
			}
			//reach the end and find one trace.[is this a valid sequence?]
			if (children == null || children.size() == 0) {
				lines.add(segs);
			} 
			//create a new sequence for each new children node. add them into nlines
			for (Segment s : children) {
				Vector<Segment> tmp = new Vector<Segment>();
				tmp.addAll(segs);
				tmp.add(s);
				nlines.add(tmp);
			}
			//update the sequence that requiring expanding.
			tlines.addAll(nlines);
		}
		
		Vector<Vector<GrammarTreeNode>> vSeg = new Vector<Vector<GrammarTreeNode>>();
		//Vector<Vector<GrammarTreeNode>> lSeg = new Vector<Vector<GrammarTreeNode>>();
		for (Vector<Segment> vs : lines) {
			Vector<GrammarTreeNode> vsGrammarTreeNodes = UtilTools
					.convertSegVector(vs);
			vSeg.add(vsGrammarTreeNodes);
		}
		// detect loops
		// verify loops
		/*for (Vector<Segment> vgt : lines) {
			Vector<Vector<GrammarTreeNode>> lLine = this.genLoop(vgt);
			if (lLine != null)
				lSeg.addAll(lLine);
		}*/
		// consolidate
		this.rawlines = vSeg;
		this.traceline = consolidateDiffSize(vSeg);
		//this.loopline = consolidateDiffLoop(lSeg);
	}

	Vector<Segment> findSegs(int pos){
		Vector<Segment> ret = new Vector<Segment>();
		ret = SegmentMapper.findMapping(orgNodes, tarNodes, pos);
		return ret;
	}
	// find all segments starting from pos
	/*Vector<Segment> findSegs(int pos) {
		Vector<Segment> segs = new Vector<Segment>();
		if (tarNodes.size() == 0) {
			int[] mapping = { 0, 0 };
			Vector<int[]> corrm = new Vector<int[]>();
			corrm.add(mapping);
			Segment s = new Segment(0, 0, corrm, orgNodes, tarNodes);
			segs.add(s);
			return segs;
		}
		if (pos >= tarNodes.size())
			return segs;
		Vector<TNode> tmp = new Vector<TNode>();
		tmp.add(tarNodes.get(pos));
		// identify the const string
		int q = Ruler.Search(orgNodes, tmp, 0);
		if (q == -1) {
			int cnt = pos;
			Vector<TNode> tvec = new Vector<TNode>();
			while (q == -1) {
				tvec.add(tarNodes.get(cnt));
				cnt++;
				tmp.clear();
				if (cnt >= tarNodes.size())
					break;
				tmp.add(tarNodes.get(cnt));
				q = Ruler.Search(orgNodes, tmp, 0);
			}
			String key = UtilTools.print(this.orgNodes)+UtilTools.print(this.tarNodes) + pos + cnt;
			Segment seg;
			if (AllSegs.containsKey(key)) {
				seg = AllSegs.get(key);
			} else {
				seg = new Segment(pos, cnt, tvec);
				AllSegs.put(key, seg);
			}
			segs.add(seg);
			return segs;
		}
		for (int i = pos; i < tarNodes.size(); i++) {
			Vector<TNode> tvec = new Vector<TNode>();
			for (int j = pos; j <= i; j++) {
				tvec.add(tarNodes.get(j));
			}
			Vector<Integer> mappings = new Vector<Integer>();
			int r = Ruler.Search(orgNodes, tvec, 0);
			while (r != -1) {
				mappings.add(r);
				r = Ruler.Search(orgNodes, tvec, r + 1);
			}
			if (mappings.size() > 1) {
				Vector<int[]> corrm = new Vector<int[]>();
				for (int t : mappings) {
					int[] m = { t, t + tvec.size() };
					corrm.add(m);
				}
				// create a segment now
				String key = UtilTools.print(this.orgNodes)+UtilTools.print(this.tarNodes) + pos + (i + 1);
				Segment s;
				if (AllSegs.containsKey(key)) {
					s = AllSegs.get(key);
				} else {
					s = new Segment(pos, i + 1, corrm, orgNodes, tarNodes);
					AllSegs.put(key, s);
				}
				if (s.section.size() > 0)
					segs.add(s);
				continue;
			} else if (mappings.size() == 1) {
				Vector<int[]> corrm = new Vector<int[]>();
				// creating based on whether can find segment with one more
				// token
				if (i >= (tarNodes.size() - 1)) {
					int[] m = { mappings.get(0), mappings.get(0) + tvec.size() };
					corrm.add(m);
					String key = UtilTools.print(this.tarNodes) + UtilTools.print(this.orgNodes)+pos+(i+1);
					Segment s;
					if (AllSegs.containsKey(key)) {
						s = AllSegs.get(key);
					} else {
						s = new Segment(pos, i + 1, corrm, orgNodes, tarNodes);
						AllSegs.put(key, s);
					}
					if (s.section.size() > 0)
						segs.add(s);
				} else {
					tvec.add(tarNodes.get(i + 1));
					int p = Ruler.Search(orgNodes, tvec, 0);
					Vector<TNode> repToken = new Vector<TNode>();
					repToken.add(tarNodes.get(i + 1));
					int rind = 0;
					int tokenCnt = 0;
					while ((rind = Ruler.Search(orgNodes, repToken, rind)) != -1) {
						rind++;
						tokenCnt++;
					}
					if (p == -1
							|| (tokenCnt > 1 && tarNodes.get(i + 1).text
									.compareTo(" ") != 0)) {
						int[] m = { mappings.get(0),
								mappings.get(0) + tvec.size() - 1 };
						corrm.add(m);
						String key = UtilTools.print(this.orgNodes)+UtilTools.print(this.tarNodes) + pos
								+ (i + 1);
						Segment s;
						if (AllSegs.containsKey(key)) {
							s = AllSegs.get(key);
						} else {
							s = new Segment(pos, i + 1, corrm, orgNodes,
									tarNodes);
							AllSegs.put(key, s);
						}
						if (s.section.size() > 0)
							segs.add(s);
					} else {
						continue;
					}
				}
			} else {
				break;
			}
		}
		return segs;
	}*/

	public Traces mergewith(Traces t) {
		// merge segment lines
		Set<Integer> keyset = new HashSet<Integer>(this.traceline.keySet());
		keyset.retainAll(t.traceline.keySet());
		HashMap<Integer, Template> nLines = new HashMap<Integer, Template>();
		HashMap<Integer, HashMap<String, Template>> lLines = new HashMap<Integer, HashMap<String, Template>>();
		for (Integer index : keyset) {
			Template line1 = this.traceline.get(index);
			Template line2 = t.traceline.get(index);
			// System.out.println(""+line1+"\n");
			// System.out.println(""+line2+"\n");
			Template nLine = (Template) line1.mergewith(line2);
			if (nLine == null)
				continue;
			nLines.put(index, nLine);
		}
		// merge loop and segment lines
		HashMap<Integer, HashMap<String, Vector<Template>>> allLoops = new HashMap<Integer, HashMap<String, Vector<Template>>>();
		Set<Integer> keyset1 = new HashSet<Integer>(this.traceline.keySet());
		keyset1.retainAll(t.loopline.keySet());
		for (Integer index : keyset1) {
			Template line1 = this.traceline.get(index);
			Collection<String> line2s = t.loopline.get(index).keySet();
			for (String line2 : line2s) {
				// System.out.println(""+line1+"\n");
				// System.out.println(""+t.loopline.get(index).get(line2)+"\n");
				Template nLine = (Template) line1.mergewith(t.loopline.get(
						index).get(line2));
				if (nLine == null)
					continue;
				if (allLoops.containsKey(index)) {
					if (allLoops.get(index).containsKey(line2)) {
						allLoops.get(index).get(line2).add(nLine);
					} else {
						Vector<Template> vTemplates = new Vector<Template>();
						vTemplates.add(nLine);
						allLoops.get(index).put(line2, vTemplates);
					}
				} else {
					Vector<Template> tgt = new Vector<Template>();
					tgt.add(nLine);
					HashMap<String, Vector<Template>> tHashMap = new HashMap<String, Vector<Template>>();
					tHashMap.put(line2, tgt);
					allLoops.put(index, tHashMap);
				}
			}
		}
		// merge loop and segment
		keyset1 = new HashSet<Integer>(this.loopline.keySet());
		keyset1.retainAll(t.traceline.keySet());
		for (Integer index : keyset1) {
			Collection<String> line2s = this.loopline.get(index).keySet();
			Template line1 = t.traceline.get(index);
			for (String line2 : line2s) {
				// System.out.println(""+line1+"\n");
				// System.out.println(""+this.loopline.get(index).get(line2)+"\n");
				Template nLine = (Template) line1.mergewith(this.loopline.get(
						index).get(line2));
				if (nLine == null)
					continue;
				if (allLoops.containsKey(index)) {
					if (allLoops.get(index).containsKey(line2)) {
						allLoops.get(index).get(line2).add(nLine);
					} else {
						Vector<Template> vTemplates = new Vector<Template>();
						vTemplates.add(nLine);
						allLoops.get(index).put(line2, vTemplates);
					}
				} else {
					Vector<Template> tgt = new Vector<Template>();
					tgt.add(nLine);
					HashMap<String, Vector<Template>> tHashMap = new HashMap<String, Vector<Template>>();
					tHashMap.put(line2, tgt);
					allLoops.put(index, tHashMap);
				}
			}
		}
		// merge loop and loop
		keyset1 = new HashSet<Integer>(this.loopline.keySet());
		keyset1.retainAll(t.loopline.keySet());
		for (Integer index : keyset1) {
			Collection<String> line1s = this.loopline.get(index).keySet();
			Collection<String> line2s = t.loopline.get(index).keySet();
			for (String line1 : line1s) {
				for (String line2 : line2s) {
					if (line1.compareTo(line2) != 0) {
						continue;
					}
					// System.out.println(""+this.loopline.get(index).get(line1)+"\n");
					// System.out.println(""+t.loopline.get(index).get(line2)+"\n");
					Template nLine = (Template) this.loopline.get(index)
							.get(line1)
							.mergewith(t.loopline.get(index).get(line2));
					if (nLine == null)
						continue;
					if (allLoops.containsKey(index)) {
						if (allLoops.get(index).containsKey(line2)) {
							allLoops.get(index).get(line2).add(nLine);
						} else {
							Vector<Template> vTemplates = new Vector<Template>();
							vTemplates.add(nLine);
							allLoops.get(index).put(line2, vTemplates);
						}
					} else {
						Vector<Template> tgt = new Vector<Template>();
						tgt.add(nLine);
						HashMap<String, Vector<Template>> tHashMap = new HashMap<String, Vector<Template>>();
						tHashMap.put(line2, tgt);
						allLoops.put(index, tHashMap);
					}
				}
			}
		}
		for (Integer key : allLoops.keySet()) {
			for (String subkey : allLoops.get(key).keySet()) {
				Template xline = this
						.consolidate(allLoops.get(key).get(subkey));
				if (lLines.containsKey(key)) {
					lLines.get(key).put(subkey, xline);
				} else {
					HashMap<String, Template> xHashMap = new HashMap<String, Template>();
					xHashMap.put(subkey, xline);
					lLines.put(key, xHashMap);
				}

			}
		}
		if (lLines.keySet().size() == 0 && nLines.keySet().size() == 0)
			return null;
		Traces rTraces = new Traces(nLines, lLines);
		return rTraces;
	}

	public GrammarTreeNode union(GrammarTreeNode x, GrammarTreeNode y) {
		if (x.getNodeType().compareTo("segment") == 0
				&& y.getNodeType().compareTo("segment") == 0) {
			Segment s = (Segment) x;
			Segment t = (Segment) y;
			Vector<Section> sec = new Vector<Section>();
			HashSet<String> hset = new HashSet<String>();
			for (Section nSection : s.section) {
				String key = nSection.toString();
				if (hset.contains(key)) {
					continue;
				} else {
					hset.add(key);
					sec.add(nSection);
				}
			}
			for (Section nSection : t.section) {
				String key = nSection.toString();
				if (hset.contains(key)) {
					continue;
				} else {
					hset.add(key);
					sec.add(nSection);
				}
			}
			boolean loop = s.isinloop || t.isinloop;
			Segment r;
			if (s.isConstSegment() && t.isConstSegment())
				r = new Segment(sec, loop);
			else
				r = new Segment(sec, loop);
			return r;
		}
		if (x.getNodeType().compareTo("loop") == 0
				&& y.getNodeType().compareTo("loop") == 0) {
			Loop s = (Loop) x;
			Loop t = (Loop) y;
			Vector<Section> sec = new Vector<Section>();
			HashSet<String> hset = new HashSet<String>();
			for (Section nSection : s.loopbody.section) {
				String key = nSection.toString();
				if (hset.contains(key)) {
					continue;
				} else {
					hset.add(key);
					sec.add(nSection);
				}
			}
			for (Section nSection : t.loopbody.section) {
				String key = nSection.toString();
				if (hset.contains(key)) {
					continue;
				} else {
					hset.add(key);
					sec.add(nSection);
				}
			}
			Loop r;
			if (s.looptype == t.looptype) {
				Segment loopbody;
				if (s.loopbody.isConstSegment() && t.loopbody.isConstSegment())
					loopbody = s.loopbody;
				else {
					loopbody = new Segment(sec, true);
				}
				r = new Loop(loopbody, t.looptype);
			} else {
				return null;
			}
			return r;
		}
		return null;
	}

	public HashMap<Integer, HashMap<String, Template>> consolidateDiffLoop(
			Vector<Vector<GrammarTreeNode>> paths) {
		HashMap<Integer, HashMap<String, Template>> resHashMap = new HashMap<Integer, HashMap<String, Template>>();
		HashMap<Integer, HashMap<String, Vector<Template>>> tmpStore = new HashMap<Integer, HashMap<String, Vector<Template>>>();
		for (Vector<GrammarTreeNode> vg : paths) {
			int key = vg.size();
			String subkey = "";
			for (GrammarTreeNode nodetype : vg) {
				subkey += nodetype.getNodeType();
			}
			if (tmpStore.containsKey(key)) {
				if (tmpStore.get(key).containsKey(subkey)) {
					tmpStore.get(key).get(subkey).add(new Template(vg));
				} else {
					Vector<Template> vte = new Vector<Template>();
					vte.add(new Template(vg));
					tmpStore.get(key).put(subkey, vte);
				}
			} else {
				Vector<Template> xVector = new Vector<Template>();
				if (!isLegalVS(vg))
					continue;
				xVector.add(new Template(vg));
				HashMap<String, Vector<Template>> xHashMap = new HashMap<String, Vector<Template>>();
				xHashMap.put(subkey, xVector);
				tmpStore.put(key, xHashMap);
			}
		}
		for (Integer key : tmpStore.keySet()) {
			for (String kInteger : tmpStore.get(key).keySet()) {
				Template x = this.consolidate(tmpStore.get(key).get(kInteger));
				if (resHashMap.containsKey(key)) {
					resHashMap.get(key).put(kInteger, x);
				} else {
					HashMap<String, Template> hashMap = new HashMap<String, Template>();
					hashMap.put(kInteger, x);
					resHashMap.put(key, hashMap);
				}
			}
		}
		return resHashMap;
	}

	public HashMap<Integer, Template> consolidateDiffSize(
			Vector<Vector<GrammarTreeNode>> paths) {
		HashMap<Integer, Template> resHashMap = new HashMap<Integer, Template>();
		HashMap<Integer, Vector<Template>> tmpStore = new HashMap<Integer, Vector<Template>>();
		for (Vector<GrammarTreeNode> vg : paths) {
			int key = vg.size();
			if (tmpStore.containsKey(key)) {
				tmpStore.get(key).add(new Template(vg));
			} else {
				Vector<Template> xVector = new Vector<Template>();
				if (!isLegalVS(vg))
					continue;
				xVector.add(new Template(vg));
				tmpStore.put(key, xVector);
			}
		}
		for (Integer key : tmpStore.keySet()) {
			// System.out.println(""+tmpStore.get(key).toString());
			Template x = this.consolidate(tmpStore.get(key));
			resHashMap.put(key, x);
		}
		// System.out.println("end consolidating");
		return resHashMap;
	}

	public boolean isLegalVS(Vector<GrammarTreeNode> x) {
		for (GrammarTreeNode t : x) {
			if (t.size() <= 0)
				return false;
		}
		return true;
	}
	public Vector<GrammarTreeNode> consolidate_tool(Vector<Vector<GrammarTreeNode>> lists)
	{
		Vector<GrammarTreeNode> res = lists.get(0);
		Vector<GrammarTreeNode> result = new Vector<GrammarTreeNode>();
		for (int i = 1; i < lists.size(); i++) {
			result = new Vector<GrammarTreeNode>();
			for (int j = 0; j < res.size(); j++) {
				GrammarTreeNode t = this.union(res.get(j), lists.get(i).get(j));
				result.add(t);
			}
			res = result;
		}
		return res;
	}
	public Template consolidate(Vector<Template> paths) {
		Vector<GrammarTreeNode> res = paths.get(0).body;
		Vector<GrammarTreeNode> result = new Vector<GrammarTreeNode>();
		for (int i = 1; i < paths.size(); i++) {

			result = new Vector<GrammarTreeNode>();
			for (int j = 0; j < res.size(); j++) {
				GrammarTreeNode t = this.union(res.get(j),
						paths.get(i).body.get(j));
				result.add(t);
			}
			res = result;
		}
		return new Template(res);
	}

	public Vector<Vector<GrammarTreeNode>> loopPathes = new Vector<Vector<GrammarTreeNode>>();

	public Vector<Vector<GrammarTreeNode>> genLoop(Vector<Segment> curPath) {
		// cluster chunk with the same head and tail token
		Vector<Vector<GrammarTreeNode>> res = new Vector<Vector<GrammarTreeNode>>();
		HashMap<String, Vector<Integer>> map = new HashMap<String, Vector<Integer>>();
		for (int i = 0; i < curPath.size(); i++) {
			String rep = curPath.get(i).repString;
			if (map.containsKey(rep)) {
				map.get(rep).add(i);
			} else {
				Vector<Integer> vIntegers = new Vector<Integer>();
				vIntegers.add(i);
				map.put(rep, vIntegers);
			}
		}
		for (String key : map.keySet()) {
			Vector<Integer> v = map.get(key);
			if (v.size() <= 1 || !this.verfiyLoop(v, curPath))
				continue;
			Vector<Vector<GrammarTreeNode>> vtn = this.detectLoop(v, curPath);
			if (vtn != null && vtn.size() > 0) {
				res.addAll(vtn);
			}
		}
		if (res.size() == 0)
			return null;
		else
			return res;
	}

	// if there is a cross of the mapping. the loop doesn't exist
	public boolean verfiyLoop(Vector<Integer> vx, Vector<Segment> segs) {
		boolean res = true;
		int pre = -1;
		for (int i : vx) {
			Segment s = segs.get(i);
			if (s.isConstSegment()) {
				return true;
			}
			int pivot = pre;
			if (s.mappings.size() >= 0) {
				boolean isFind = false;
				for (int[] n : s.mappings) {
					if (n[0] > pivot && !isFind) {
						pre = n[0];
						isFind = true;
					} else if (n[0] < pre && n[0] > pivot && isFind) {
						pre = n[0];
					}
				}
				if (!isFind) {
					return false;
				}
			}
		}
		return res;
	}

	public Vector<GrammarTreeNode> subVector(Vector<Segment> nodes, int start,
			int end) {
		if (start >= end || start < 0 || end > nodes.size())
			return null;
		Vector<GrammarTreeNode> vgt = new Vector<GrammarTreeNode>();
		for (int i = start; i < end; i++) {
			vgt.add(nodes.get(i));
		}
		return vgt;
	}

	// merge the longest mergable chunk.
	// if None chunk could be merged together return null
	public Vector<GrammarTreeNode> createLoop(Vector<GrammarTreeNode> nodes,
			int span) {
		Vector<GrammarTreeNode> res = new Vector<GrammarTreeNode>();
		Vector<Vector<GrammarTreeNode>> gt = new Vector<Vector<GrammarTreeNode>>();
		int pos = 0;
		while (pos < nodes.size()) {
			Vector<GrammarTreeNode> x = new Vector<GrammarTreeNode>();
			for (int k = pos; k < pos + span && k < nodes.size(); k++) {
				x.add(nodes.get(k));
			}
			gt.add(x);
			pos += span;
		}
		// no loop if only one chunk
		if (gt.size() == 1) {
			return null;
		}
		int itercnt = 1; // count of the span
		int curStartPos = 0;// the lowest position the first element
		int presize = gt.size();
		while (gt.size() > 1) {
			Vector<Vector<GrammarTreeNode>> tmp_gt = new Vector<Vector<GrammarTreeNode>>();
			boolean isLegal = true;
			for (int j = 0; j < gt.size() - 1; j++) {
				isLegal = true;
				Vector<GrammarTreeNode> elem = new Vector<GrammarTreeNode>();
				Vector<GrammarTreeNode> mx = gt.get(j);
				Vector<GrammarTreeNode> nx = gt.get(j + 1);
				if (mx.size() != nx.size())
					return null;

				for (int r = 0; r < mx.size(); r++) {
					GrammarTreeNode treeNode = mx.get(r).mergewith(nx.get(r));
					if (treeNode == null) {
						isLegal = false;
						break;
					}
					elem.add(treeNode);
				}
				if (isLegal) {
					tmp_gt.add(elem);
				}
				if (!isLegal && tmp_gt.size() == 0) {
					curStartPos += span;
				}
			}
			if (!isLegal)
				return null; // nothing merged in this iteration;
			gt = tmp_gt;
			itercnt++;
			if (gt.size() == presize) {
				break;
			} else {
				presize = gt.size();
			}
		}
		// only detect one loop
		if (gt.size() > 1)
			return null;
		//
		for (int i = 0; i < nodes.size(); i++) {
			if (i < curStartPos) {
				res.add(nodes.get(i));
			} else if (i == curStartPos + itercnt * span - 1) {
				Vector<GrammarTreeNode> body = gt.get(0);
				if (span == 1) {
					Loop loop = new Loop((Segment) body.get(0), Loop.LOOP_BOTH);
					res.add(loop);
					continue;
				}
				for (int j = 0; j < body.size(); j++) {
					if (j == 0) {
						Loop loop = new Loop((Segment) body.get(j),
								Loop.LOOP_START);
						res.add(loop);
					} else if (j == body.size() - 1) {
						Loop loop = new Loop((Segment) body.get(j),
								Loop.LOOP_END);
						res.add(loop);
					} else {
						Loop loop = new Loop((Segment) body.get(j),
								Loop.LOOP_MID);
						res.add(loop);
					}
				}
			} else if (i >= curStartPos + itercnt * span) {
				res.add(nodes.get(i));
			}
		}
		return res;
	}

	public Vector<Vector<GrammarTreeNode>> detectLoop(Vector<Integer> rep,
			Vector<Segment> curPath) {
		int span = rep.get(1) - rep.get(0);
		Vector<Vector<GrammarTreeNode>> resVector = new Vector<Vector<GrammarTreeNode>>();
		Vector<GrammarTreeNode> nodelist = new Vector<GrammarTreeNode>();
		if (span == 1) {
			int startpos = rep.get(0);
			int endpos = rep.get(rep.size() - 1);
			for (int i = 0; i < curPath.size(); i++) {
				if (i < startpos) {
					nodelist.add(curPath.get(i));
				} else if (i == endpos) {
					Vector<GrammarTreeNode> sublist = this.createLoop(
							subVector(curPath, startpos, endpos + 1), 1);
					if (sublist == null)
						return null;
					nodelist.addAll(sublist);
				} else if (i > endpos) {
					nodelist.add(curPath.get(i));
				}
			}
		} else {
			int startpos = rep.get(0);
			int endpos = rep.get(rep.size() - 1);
			// left overflow
			if ((startpos - span + 1) < 0 && endpos + span <= curPath.size()) {
				for (int i = 0; i < curPath.size(); i++) {
					if (i < startpos) {
						nodelist.add(curPath.get(i));
					}
					if (i == endpos + span - 1) {
						Vector<GrammarTreeNode> sublist = this.createLoop(
								subVector(curPath, startpos, endpos + span),
								span);
						if (sublist == null)
							return null;
						nodelist.addAll(sublist);
					} else if (i >= endpos + span) {
						nodelist.add(curPath.get(i));
					}
				}
			}
			// right overflow
			else if ((startpos - span + 1) >= 0
					&& endpos + span > curPath.size()) {
				for (int i = 0; i < curPath.size(); i++) {
					if (i < startpos - span + 1) {
						nodelist.add(curPath.get(i));
					}
					if (i == endpos) {
						Vector<GrammarTreeNode> sublist = this.createLoop(
								subVector(curPath, startpos - span + 1,
										endpos + 1), span);
						if (sublist == null)
							return null;
						nodelist.addAll(sublist);
					} else if (i > endpos) {
						nodelist.add(curPath.get(i));
					}
				}
			}
			// two direction overflow
			else if ((startpos - span + 1) < 0
					&& endpos + span >= curPath.size()) {
				// skip the startpos
				for (int i = 0; i < curPath.size(); i++) {
					if (i <= startpos) {
						nodelist.add(curPath.get(i));
					}
					if (i == endpos) {
						Vector<GrammarTreeNode> sublist = this.createLoop(
								subVector(curPath, startpos + 1, endpos + 1),
								span);
						if (sublist == null) {
							nodelist.clear();
							break;
						}
						nodelist.addAll(sublist);
					} else if (i > endpos) {
						nodelist.add(curPath.get(i));
					}
				}
				Vector<GrammarTreeNode> nodelist1 = new Vector<GrammarTreeNode>();
				// skip the endpos
				for (int i = 0; i < curPath.size(); i++) {
					if (i < startpos) {
						nodelist1.add(curPath.get(i));
					}
					if (i == endpos - 1) {
						Vector<GrammarTreeNode> sublist = this.createLoop(
								subVector(curPath, startpos, endpos), span);
						if (sublist == null) {
							nodelist1.clear();
							break;
						}
						nodelist1.addAll(sublist);
					} else if (i >= endpos) {
						nodelist1.add(curPath.get(i));
					}
				}
				if (nodelist1.size() > 0) {
					resVector.add(nodelist1);
				}
			} else {
				for (int i = 0; i < curPath.size(); i++) {
					// shift left
					if (i < startpos - span + 1) {
						nodelist.add(curPath.get(i));
					}
					if (i == endpos) {
						Vector<GrammarTreeNode> sublist = this.createLoop(
								subVector(curPath, startpos - span + 1,
										endpos + 1), span);
						if (sublist == null) {
							nodelist.clear();
							break;
						}
						nodelist.addAll(sublist);
					} else if (i > endpos) {
						nodelist.add(curPath.get(i));
					}
				}
				Vector<GrammarTreeNode> nodelist1 = new Vector<GrammarTreeNode>();
				for (int i = 0; i < curPath.size(); i++) {
					// shift right
					if (i < startpos) {
						nodelist1.add(curPath.get(i));
					}
					if (i == endpos + span - 1) {
						Vector<GrammarTreeNode> sublist = this.createLoop(
								subVector(curPath, startpos, endpos + span),
								span);
						if (sublist == null) {
							nodelist1.clear();
							break;
						}
						nodelist1.addAll(sublist);
					} else if (i >= endpos + span) {
						nodelist1.add(curPath.get(i));
					}
				}
				if (nodelist1.size() > 0)
					resVector.add(nodelist1);
			}
		}
		if (nodelist.size() != 0) {
			resVector.add(nodelist);
		}
		return resVector;
	}

	public void tracePrint() {
		System.out.println("===============printing trace here===============");
		for (Integer key : this.traceline.keySet()) {
			System.out.println("" + traceline.get(key));
		}
		for (Integer key : this.loopline.keySet()) {
			System.out.println("" + loopline.get(key));
		}
	}

	public void emptyState() {
		for (GrammarTreeNode t : this.totalOrderVector) {
			t.emptyState();
		}
	}

	public String toProgram() {
		String resString = "";
		while (curState < totalOrderVector.size()) {
			resString = this.totalOrderVector.get(curState).toProgram();
			if (!resString.contains("null")) {
				this.program = resString;
				return resString;
			} else {
				this.totalOrderVector.get(curState).emptyState();//some segments may work
				curState++;
			}
		}
		this.program = "null";
		return "null";
	}

	public long size() {
		long size = 0;
		for (Template t : this.totalOrderVector) {
			size += t.size();
		}
		return size;
	}

	@Override
	public GrammarTreeNode mergewith(GrammarTreeNode a) {
		Traces t = (Traces) a;

		return this.mergewith(t);
	}

	@Override
	public String getNodeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getrepString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProgram() {
		return this.program;
	}
}
