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

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;
import au.com.bytecode.opencsv.CSVReader;

public class RecordDistiller {
	// {anchor:{"Id": , "Count": , "LefContext":[], "RigContext":[]}
	public static int cxt_limit = 3;
	public int totalnumber = 0;
	public HashMap<String, Anchor> anchors = new HashMap<String, Anchor>();

	public void readRecord(String ID, Vector<TNode> record) {
		HashMap<String, Integer> curIndices = new HashMap<String, Integer>();
		for (int i = 1; i < record.size() - 1; i++) // skip the start and end
													// token
		{
			TNode t = record.get(i);
			String type = t.getType();
			String anchor = type;
			if (curIndices.containsKey(type)) {
				int cnt = curIndices.get(type);
				curIndices.put(type, cnt + 1);
				anchor += cnt;
			} else {
				curIndices.put(type, 0);
				anchor += "0";
			}
			// get left and right context
			String lcxt = "";
			String rcxt = "";
			for (int j = i; j < i + RecordDistiller.cxt_limit
					&& j < record.size(); j++) {
				rcxt += record.get(j).getType();
			}
			for (int j = i; j >= 0 && j > i - cxt_limit; j--) {
				lcxt += record.get(j).getType();
			}
			// update the anchor repository
			if (this.anchors.containsKey(anchor)) {
				Anchor an = this.anchors.get(anchor);
				an.IDs.add(ID);
				an.count += 1;
				an.lefCxt.put(ID, lcxt);
				an.rigCxt.put(ID, rcxt);
			} else {
				Vector<String> Ids = new Vector<String>();
				Ids.add(ID);
				HashMap<String, String> vlcxt = new HashMap<String, String>();
				vlcxt.put(ID, lcxt);
				HashMap<String, String> vrcxt = new HashMap<String, String>();
				vrcxt.put(ID, rcxt);
				Anchor nan = new Anchor(anchor, Ids, 1, vlcxt, vrcxt);
				anchors.put(anchor, nan);
			}
		}
	}

	// identify the anchor tokens
	public void idenAnchor(int total) {
		Vector<String> dels = new Vector<String>();
		for (String a : anchors.keySet()) {
			int count = anchors.get(a).count;
			// if an anchor appears in more 10% records, it's a valid anchor
			if (count * 1.0 / total < 0.1) {
				dels.add(a);
			}
		}
		for (String s : dels) {
			anchors.remove(s);
		}
	}

	// identify the representative records of one anchor.
	// minimal set
	public HashSet<String> idenAnchorRecords(String anchor) {
		HashMap<String, Vector<String>> lcxt2ids = new HashMap<String, Vector<String>>();
		HashMap<String, Vector<String>> rcxt2ids = new HashMap<String, Vector<String>>();
		for (String Id : this.anchors.get(anchor).lefCxt.keySet()) {
			String s = this.anchors.get(anchor).lefCxt.get(Id);
			boolean isnew = true;
			for (String elem : lcxt2ids.keySet()) {
				if (elem.indexOf(s) == 0) {
					s = elem;
					isnew = false;
				}
			}
			if (isnew) {
				Vector<String> vs = new Vector<String>();
				vs.add(Id);
				lcxt2ids.put(s, vs);
			} else {
				lcxt2ids.get(s).add(Id);
			}
		}
		for (String Id : this.anchors.get(anchor).rigCxt.keySet()) {
			String s = this.anchors.get(anchor).rigCxt.get(Id);
			boolean isnew = true;
			for (String elem : rcxt2ids.keySet()) {
				if (elem.indexOf(s) == 0) {
					s = elem;
					isnew = false;
				}
			}
			if (isnew) {
				Vector<String> vs = new Vector<String>();
				vs.add(Id);
				rcxt2ids.put(s, vs);
			} else {
				rcxt2ids.get(s).add(Id);
			}
		}
		// generate candiate set
		HashSet<String> result = new HashSet<String>();
		for (String cxt : lcxt2ids.keySet()) {
			if (lcxt2ids.get(cxt).size() != 0) {
				String idString = lcxt2ids.get(cxt).get(0);
				if (!result.contains(idString)) {
					result.add(idString);
				}
			}
		}
		for (String cxt : rcxt2ids.keySet()) {
			if (rcxt2ids.get(cxt).size() != 0) {
				String idString = rcxt2ids.get(cxt).get(0);
				if (!result.contains(idString)) {
					result.add(idString);
				}
			}
		}
		return result;
	}

	// merge the record sets generated by each anchor
	// return the final Record ID list
	public HashSet<String> refineRecords() {
		HashSet<String> ids = new HashSet<String>();
		// find the union of the ids of all anchors
		for (String anchor : this.anchors.keySet()) {
			HashSet<String> set = this.idenAnchorRecords(anchor);
			ids.addAll(set);
		}
		return ids;
	}

	public static void main(String[] args) {
		String dirpath = "/Users/bowu/Research/testdata/TestSingleFile";
		RecordDistiller distiller = new RecordDistiller();
		File nf = new File(dirpath);
		File[] allfiles = nf.listFiles();
		for (File f : allfiles) {
			try {
				if (f.getName().indexOf(".csv") == (f.getName().length() - 4)) {
					@SuppressWarnings("resource")
					CSVReader cr = new CSVReader(new FileReader(f), ',', '"',
							'\0');
					String[] pair;
					int id = 0;
					HashMap<String, String> id2String = new HashMap<String, String>();
					while ((pair = cr.readNext()) != null) {
						if (pair == null || pair.length <= 1)
							break;
						Ruler ruler = new Ruler();
						ruler.setNewInput(pair[0]);
						distiller.readRecord("" + id, ruler.vec);
						id2String.put("" + id, pair[0]);
						id++;
					}
					distiller.idenAnchor(id2String.keySet().size());
					HashSet<String> allids = distiller.refineRecords();
					for (String xid : allids) {
						System.out.println(id2String.get(xid));
					}
					double compressRate = (allids.size() * 1.0)
							/ id2String.keySet().size();
					for (String name : distiller.anchors.keySet()) {
						System.out.println("Anchor: " + name);
						for (String dString : distiller.anchors.get(name).IDs) {
							System.out.print(" " + dString);
						}
						System.out.println("\n");
					}
					System.out.println("" + compressRate);
				}
			} catch (Exception e) {
				System.out.println("" + e.toString());
			}
		}
	}
}

class Anchor {
	public String name;
	public Vector<String> IDs;
	public int count;
	// id 2 the left context
	public HashMap<String, String> lefCxt = new HashMap<String, String>();
	// id 2 the right context
	public HashMap<String, String> rigCxt = new HashMap<String, String>();

	public Anchor(String anchor, Vector<String> Ids, int count,
			HashMap<String, String> lcxt, HashMap<String, String> rcxt) {
		this.name = anchor;
		this.IDs = Ids;
		this.count = count;
		this.lefCxt = lcxt;
		this.rigCxt = rcxt;
	}
}
