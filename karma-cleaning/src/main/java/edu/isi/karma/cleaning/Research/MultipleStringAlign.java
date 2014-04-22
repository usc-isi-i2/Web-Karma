/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.cleaning.Research;

import java.util.Vector;

public class MultipleStringAlign {
	public MultipleStringAlign(Vector<String[]> strs) {

	}

	// generate all the possible next state in DFA
	public void generateChildren(ANode a) {
		Vector<Vector<int[]>> multiSeqMapping = new Vector<Vector<int[]>>();
		Vector<Long> indexes = new Vector<Long>();
		for (int i = 0; i < a.exps.size(); i++) {
			String[] example = a.exps.get(i);
			int pos = a.orgPos.get(i);
			Vector<int[]> mappings = findNext(example[0], example[1], pos);
			// tarpos, orgstartpos, orgendpos
			Vector<int[]> nmapping = new Vector<int[]>();
			for (int[] elem : mappings) {
				int[] el = { pos, elem[0], elem[1] };
				nmapping.add(el);
			}
			indexes.add((long) nmapping.size());
			multiSeqMapping.add(nmapping);
		}
		// generate combinations
		Vector<Vector<Integer>> configs = new Vector<Vector<Integer>>();
		getCrossIndex(indexes, configs);
		// using a combination to generate the ANode
		for (int i = 0; i < configs.size(); i++) {
			Vector<Integer> poses = configs.get(i);
			Vector<Integer> orgPos = new Vector<Integer>();
			Vector<Integer> tarPos = new Vector<Integer>();
			Vector<Integer> length = new Vector<Integer>();
			for (int j = 0; j < poses.size(); j++) {
				int[] e = multiSeqMapping.get(j).get(poses.get(j));
				orgPos.add(e[1]);
				tarPos.add(e[0]);
				length.add(e[2] - e[1]);
			}
			ANode aNode = new ANode(orgPos, tarPos, length, a.exps);
			if (aNode.isvalid())
				a.addChild(aNode);
		}
	}

	// iteratively generate the combinations
	public void getCrossIndex(Vector<Long> indexs,
			Vector<Vector<Integer>> configs) {
		int k = indexs.size();
		int[] com = new int[k];
		for (int i = 0; i < k; i++)
			com[i] = 0;
		while (com[k - 1] < indexs.get(k - 1)) {
			Vector<Integer> res = new Vector<Integer>();
			for (int i = 0; i < k; i++) {
				res.add(com[i]);
			}
			configs.add(res);
			int t = k - 1;
			while (t != 0 && com[t] == indexs.get(t) - 1)
				t--;
			com[t]++;
			if (t == 0 && com[t] >= indexs.get(0)) {
				break;
			}
			for (int i = t + 1; i < k; i++)
				com[i] = 0;
		}
	}

	// {[orgstartPos, orgendPos ], ...}
	@SuppressWarnings("unused")
	public Vector<int[]> findNext(String org, String tar, int pos) {
		Vector<int[]> segs = new Vector<int[]>();
		if (tar.length() == 0) {
			int[] elem = { -1, 0 };
			segs.add(elem);
			return segs;
		}
		if (pos >= tar.length())
			return segs;
		String tmp = "";
		tmp += tar.charAt(pos);
		int q = org.indexOf(tmp);
		if (q == -1) {
			int cnt = pos;
			String tvec = "";
			while (q == -1) {
				tvec += tar.charAt(cnt);
				cnt++;
				tmp = "";
				if (cnt >= tar.length())
					break;
				tmp += tar.charAt(cnt);
				q = org.indexOf(tmp);
			}
			int[] elem = { -1, cnt - pos };
			segs.add(elem);
			return segs;
		}
		for (int i = pos; i < tar.length(); i++) {
			String tvec = "";
			for (int j = pos; j <= i; j++) {
				tvec += tar.charAt(j);
			}
			Vector<Integer> mappings = new Vector<Integer>();
			int r = org.indexOf(tvec);
			while (r != -1) {
				mappings.add(r);
				r = org.indexOf(tvec, r + 1);
			}
			if (mappings.size() > 1) {
				Vector<int[]> corrm = new Vector<int[]>();
				for (int t : mappings) {
					int[] m = { t, t + tvec.length() };
					corrm.add(m);
				}
				// create a segment now
				segs.addAll(corrm);
				continue;
			} else if (mappings.size() == 1) {
				Vector<int[]> corrm = new Vector<int[]>();
				// creating based on whether can find segment with one more
				// token
				if (i >= (tar.length() - 1)) {
					int[] m = { mappings.get(0),
							mappings.get(0) + tvec.length() };
					corrm.add(m);
					segs.addAll(corrm);
				} else {
					tvec += tar.charAt(i + 1);
					int p = org.indexOf(tvec, 0);
					String repToken = "";
					repToken += tar.charAt(i + 1);
					int rind = 0;
					int tokenCnt = 0;
					while ((rind = org.indexOf(repToken, rind)) != -1) {
						rind++;
						tokenCnt++;
					}
					if (p == -1 || tokenCnt > 1) {
						int[] m = { mappings.get(0),
								mappings.get(0) + tvec.length() - 1 };
						corrm.add(m);
						segs.addAll(corrm);
					} else {
						continue;
					}
				}
			} else {
				break;
			}
		}
		return segs;
	}
}
