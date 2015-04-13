package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import edu.isi.karma.cleaning.internalfunlibrary.InternalTransformationLibrary;
import edu.isi.karma.cleaning.internalfunlibrary.TransformFunction;

class Dataitem {
	public int[] range = { -1, -1 };
	public int funcid = -1;
	public int tarpos = -1;
	public int tarend = -1;
}

public class SegmentMapper {
	public static InternalTransformationLibrary itfl = new InternalTransformationLibrary();

	// only try to find one segment whose starting pos in target is pos
	public static Vector<Segment> findMapping(Vector<TNode> org,
			Vector<TNode> tar, int pos) {
		Vector<Segment> res = new Vector<Segment>();
		Dataitem root = new Dataitem();
		root.tarpos = pos;
		ArrayList<Dataitem> path = new ArrayList<Dataitem>();
		ArrayList<ArrayList<Dataitem>> ret = new ArrayList<ArrayList<Dataitem>>();
		
		recursiveSearch(org, tar, root, path,ret);
		
		res = convert2Segments(ret, org, tar);
		return res;
	}
	public static Vector<Segment> convert2Segments(ArrayList<ArrayList<Dataitem>> repo , Vector<TNode> org, Vector<TNode> tar){
		HashMap<String, ArrayList<Dataitem>> groups = new HashMap<String,ArrayList<Dataitem>>();
		Vector<Segment> ret = new Vector<Segment>();
		for(ArrayList<Dataitem> line: repo){
			if(line.size() == 0){
				continue;
			}
			Dataitem item =convert(line, org, tar);
			if(item.range[0] < 0){
				//create constant segment and return
				Vector<TNode> cont = new Vector<TNode>();
				for(int ptr = item.tarpos; ptr<= item.tarend; ptr++){
					cont.add(tar.get(ptr));
				}
				Segment seg = new Segment(item.tarpos, item.tarend+1, cont);
				ret.add(seg);
				return ret;
			}
			String key = item.tarpos + ", "+ item.tarend; //key for same segment. a segment can contain multiple sections.
			if(groups.containsKey(key))
			{
				groups.get(key).add(item);
			}
			else{
				ArrayList<Dataitem> tmp = new ArrayList<Dataitem>();
				tmp.add(item);
				groups.put(key, tmp);
			}			
		}
		for(String key: groups.keySet()){
			if(groups.get(key).size() <= 0 )
			{
				continue;
			}
			Vector<int[]> kmappings = new Vector<int[]>();
			for(Dataitem elem: groups.get(key)){
				int[] xtmp = {elem.range[0], elem.range[1], elem.funcid};
				kmappings.add(xtmp);
			}
			int start = groups.get(key).get(0).tarpos;
			int end = groups.get(key).get(0).tarend;
			Segment seg = new Segment(start, end+1, kmappings, org, tar);
			ret.add(seg);			
		}
		return ret;
	}
	public static Dataitem convert(ArrayList<Dataitem> path, Vector<TNode> org, Vector<TNode> tar) {
		int start = 0;
		int end = 0;
		int[] mapping = {Integer.MAX_VALUE, Integer.MIN_VALUE, InternalTransformationLibrary.Functions.NonExist.getValue()};
		for(int i = 0; i < path.size(); i++){
			if(i == 0)
				start = path.get(i).tarpos;
			if(i == path.size() -1)
				end = path.get(i).tarpos;
			if(path.get(i).range[0] < mapping[0])
			{
				mapping[0] = path.get(i).range[0];
			}
			if(path.get(i).range[1] > mapping[1]){
				mapping[1] = path.get(i).range[1];
			}
		}
		mapping[1] += 1;		
		if(path.size() <= 0)
			return null;
		mapping[2] = path.get(0).funcid;
		Dataitem ret = new Dataitem();
		ret.funcid = mapping[2];
		ret.tarend = end;
		ret.tarpos = start;
		ret.range[0] = mapping[0];
		ret.range[1] = mapping[1];
		return ret;
	}

	public static void recursiveSearch(Vector<TNode> org, Vector<TNode> tar,
			Dataitem root, ArrayList<Dataitem> path, ArrayList<ArrayList<Dataitem>> repo) {
		if (root.tarpos >= tar.size()) {
			repo.add(path);
		}
		Vector<Dataitem> updated = makeOneMove(org, tar, root);
		if(updated.size() == 0){
			repo.add(path);
		}
		
		for (Dataitem elem : updated) {
			ArrayList<Dataitem> newlist = new ArrayList<Dataitem>();
			newlist.addAll(path);
			newlist.add(elem);
			Dataitem child = new Dataitem();
			child.range[0] = elem.range[0];
			child.range[1] = elem.range[1];
			child.funcid = elem.funcid;
			child.tarpos = elem.tarpos + 1;
			recursiveSearch(org, tar, child, newlist, repo);
		}
		return;
	}
	//match one token in the target token seq
	public static Vector<Dataitem> makeOneMove(Vector<TNode> org,
			Vector<TNode> tar, Dataitem root) {
		Vector<Dataitem> ret = new Vector<Dataitem>();
		int tpos = root.tarpos;
		if(tpos >= tar.size())
		{
			return ret;
		}
		TNode t = tar.get(tpos);
		String tstr = t.text;
		boolean nomapping = true;
		boolean followChecking = false;
		int start = 0;
		int end = org.size();
		//if not the first time, only resume searching not perform full range search
		if(root.range[1] != -1){
			start = root.range[1]+1;
			end = start+1;
		}
		for (int i = start; i < end; i++) {
			String prefix = "";
			if (root.funcid == InternalTransformationLibrary.Functions.NonExist.getValue()) {
				for (TransformFunction tf : itfl.getAllFuncs()) {
					int pos = checkOneFunction(tf, prefix, org, i, tstr);
					if (pos != -1) {
						Dataitem nd = new Dataitem();
						nd.range[0] = i;
						nd.range[1] = pos - 1;
						nd.funcid = tf.getId();
						nd.tarpos = root.tarpos;
						nomapping = false;
						ret.add(nd);
					}
				}				
			} else {
				followChecking = true;
				int pos = checkOneFunction(itfl.getFunc(root.funcid), prefix,
						org, i, tstr);
				if (pos != -1) {
					Dataitem nd = new Dataitem();
					nd.range[0] = i;
					nd.range[1] = pos - 1;
					nd.funcid = root.funcid;
					nd.tarpos = root.tarpos;
					nomapping = false;
					ret.add(nd);
				}
			}
		}
		if(nomapping && !followChecking)
		{
			//insert constant
			Dataitem nd = new Dataitem();
			nd.range[0] = -1;
			nd.range[1] = -1;
			nd.funcid = -2;
			nd.tarpos = root.tarpos;
			ret.add(nd);
		}
		return ret;
	}

	// return the ending pos if successful else return -1
	public static int checkOneFunction(TransformFunction tf, String prefix,
			Vector<TNode> org, int i, String tar) {
		if(tf == null)
			return -1;
		if (i >= org.size()) {
			if (prefix.compareTo(tar) == 0) {
				return i;
			} else {
				return -1;
			}
		}
		if (prefix.compareTo(tar) == 0)
			return i;
		Vector<TNode> tNodes = new Vector<TNode>();
		tNodes.add(org.get(i));
		String con = tf.convert(tNodes);
		if(con == null || con.isEmpty())
			return -1;
		String tmp = prefix + tf.convert(tNodes);
		if (tar.indexOf(tmp) != 0) {
			return -1;
		} else {
			int t = checkOneFunction(tf, tmp, org, i + 1, tar);
			return t;
		}
	}

	public static void main(String[] args) {
		String[] s1 = {"<_START>PA2050 <_END>"};
		String[] s2 = {"2050 PA"};
		for (int i = 0; i < s1.length; i++) {
			Vector<TNode> ts1 = UtilTools.convertStringtoTNodes(s1[i]);
			Vector<TNode> ts2 = UtilTools.convertStringtoTNodes(s2[i]);
			Vector<Segment> ret = SegmentMapper.findMapping(ts1, ts2, 0);
			System.out.println("" + ret);
		}
	}

}
