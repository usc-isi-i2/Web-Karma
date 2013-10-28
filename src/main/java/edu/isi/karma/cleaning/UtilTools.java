package edu.isi.karma.cleaning;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

public class UtilTools {
	public static int index = 0;
	public static Vector<String> results = new Vector<String>();
	public static Vector<Integer> getStringPos(int tokenpos,Vector<TNode> example)
	{
		Vector<Integer> poss = new Vector<Integer>();
		if(tokenpos < 0)
			return poss;
		int pos = 0;
		int strleng = 0;
		for(int i=0;i<example.size();i++)
		{
			strleng += example.get(i).text.length();
		}
		for(int i = 0; i<tokenpos;i++)
		{
			pos += example.get(i).text.length();
		}
		poss.add(pos); // forward position
		poss.add(pos-strleng); // backward position
		return poss;
	}
	public static Vector<GrammarTreeNode> convertSegVector(Vector<Segment> x)
	{
		Vector<GrammarTreeNode> res = new Vector<GrammarTreeNode>();
		for(Segment e:x)
		{
			res.add(e);
		}
		return res;
	}
	public static int multinominalSampler(double[] probs) {
		Random r = new Random();
		double x = r.nextDouble();
		if (x <= probs[0]) {
			return 0;
		}
		x -= probs[0];
		for (int i = 1; i < probs.length; i++) {
			if (x <= probs[i]) {
				return i;
			}
			x -= probs[i];
		}
		return 0;
	}

	public static int randChoose(int n) {
		Random r = new Random();
		return r.nextInt(n);
	}

	public static String print(Vector<TNode> x) {
		String str = "";
		if(x == null)
			return "null";
		for (TNode t : x)
			if(t.text.compareTo("ANYTOK")==0)
				str += t.getType();
			else
				str += t.text;
		return str;
	}

	public static boolean samesteplength(Vector<Integer> s) {
		if (s.size() <= 1)
			return false;
		if (s.size() == 2) {
			if (s.get(1) - s.get(0) >= 1) {
				return true;
			} else {
				return false;
			}
		}
		int span = s.get(1) - s.get(0);
		for (int i = 2; i < s.size(); i++) {
			if ((s.get(i) - s.get(i - 1)) != span)
				return false;
		}
		return true;
	}
	public static void StringColorCode(String org, String res,
			HashMap<String, String> dict) throws Exception{
		int segmentCnt = 0;
		Vector<int[]> allUpdates = new Vector<int[]>();
		String pat = "((?<=\\{_L\\})|(?=\\{_L\\}))";
		String pat1 = "((?<=\\{_S\\})|(?=\\{_S\\}))";
		String orgdis = "";
		String tardis = "";
		String tar = "";
		String[] st = res.split(pat);
		boolean inloop = false;
		for (String token : st) {
			if (token.compareTo("{_L}") == 0 && !inloop) {
				inloop = true;
				continue;
			}
			if (token.compareTo("{_L}") == 0 && inloop) {
				inloop = false;
				continue;
			}
			String[] st1 = token.split(pat1);
			for (String str : st1) {
				if (str.compareTo("{_S}") == 0 || str.compareTo("{_S}") == 0) {
					continue;
				}
				if (str.indexOf("{_C}") != -1) {
					String[] pos = str.split("\\{_C\\}");
					int[] poses = { Integer.valueOf(pos[0]),
							Integer.valueOf(pos[1]),segmentCnt};
					boolean findPos = false;
					for (int i = 0; i < allUpdates.size(); i++) {
						int[] cur = allUpdates.get(i);
						if (poses[0] <= cur[0]) {
							findPos = true;
							allUpdates.add(i, poses);
							break; // avoid infinite adding
						}
					}
					if(!findPos)
					{
						allUpdates.add(poses);
					}
					String tarseg = org.substring(Integer.valueOf(pos[0]),
							Integer.valueOf(pos[1]));

					if (inloop) {

						tardis += String.format(
								"<span class=\"a%d\">%s</span>", segmentCnt,
								tarseg);
						// orgdis +=
						// String.format("<span class=\"a%d\">%s</span>",
						// segmentCnt,tarseg);
						tar += tarseg;
					} else {
						tardis += String.format(
								"<span class=\"a%d\">%s</span>", segmentCnt,
								tarseg);
						// orgdis +=
						// String.format("<span class=\"a%d\">%s</span>",
						// segmentCnt,tarseg);
						segmentCnt++;
						tar += tarseg;
					}

				} else {
					tardis += String.format("<span class=\"ins\">%s</span>",
							str);
					tar += str;
				}
			}
		}
		int pre = 0;
		for(int[] update:allUpdates)
		{
			if(update[0] >= pre)
			{
				orgdis += org.substring(pre,update[0]);
				orgdis += String.format(
						"<span class=\"a%d\">%s</span>", update[2],
						org.substring(update[0],update[1]));
				pre = update[1];
			}
		}
		if(org.length() > pre)
			orgdis += org.substring(pre);
		dict.put("Org", org);
		dict.put("Tar", tar);
		dict.put("Orgdis", orgdis);
		dict.put("Tardis", tardis);
	}
	public static String escape(String s) {
		s = s.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
		HashMap<String, String> dict = new HashMap<String, String>();
		dict.put("\\(", "\\\\(");
		dict.put("\\)", "\\\\)");
		dict.put("\\+", "\\\\+");
		dict.put("\\.", "\\\\.");
		dict.put("\\?", "\\\\?");
		dict.put("\\$", "\\\\\\$");
		dict.put("\\*", "\\\\*");
		dict.put("\\^", "\\\\^");
		dict.put("\\]", "\\\\]");
		dict.put("\\[", "\\\\[");
		dict.put("\\/", "\\\\/");
		dict.put("\\'", "\\\\'");
		dict.put("\\\"", "\\\\\"");
		for (String key : dict.keySet()) {
			s = s.replaceAll(key, dict.get(key));
		}
		return s;
	}
	public static Vector<TNode> subtokenseqs(int a, int b, Vector<TNode> org) {
		Vector<TNode> xNodes = new Vector<TNode>();
		if (a < 0 || b >= org.size() || a > b) {
			return null;
		} else {
			for (int i = a; i <= b; i++) {
				xNodes.add(org.get(i));
			}
			return xNodes;
		}
	}

	public static void clearTmpVars() {
		results.clear();
		index = 0;
	}

	public static Vector<String> buildDict(Collection<String> data)
	{
		HashMap<String,Integer> mapHashSet = new HashMap<String, Integer>();
		for(String pair:data)
		{
			String s1 = pair;
			if (s1.contains("<_START>"))
			{
				s1 = s1.replace("<_START>", "");
			}
			if (s1.contains("<_END>"))
			{
				s1 = s1.replace("<_END>", "");
			}
			Ruler r = new Ruler();
			r.setNewInput(s1);
			Vector<TNode> v = r.vec;
			HashSet<String> curRow = new HashSet<String>();
			for (TNode t : v)
			{
				String k = t.text;
				k = k.replaceAll("[0-9]+", "DIGITs");
				if(k.trim().length() ==0)
					continue;
				//only consider K once in one row
				if(curRow.contains(k))
				{
					continue;
				}
				else
				{
					curRow.add(k);
				}
				if (mapHashSet.containsKey(k))
				{
					mapHashSet.put(k,mapHashSet.get(k)+1);
				}
				else
				{
					mapHashSet.put(k,1);
				}
			}
		}
		//prune infrequent terms
		int thresdhold =5;
		Iterator<Entry<String, Integer>> iter = mapHashSet.entrySet().iterator();
		while(iter.hasNext())
		{
			Entry<String, Integer> e = iter.next();
			if(e.getValue() < thresdhold)
			{
				iter.remove();
			}
		}
		Vector<String> res = new Vector<String>();
		res.addAll(mapHashSet.keySet());
		return res;
	}
}
