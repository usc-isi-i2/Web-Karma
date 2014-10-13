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

package edu.isi.karma.cleaning.QuestionableRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;
import edu.isi.karma.cleaning.UtilTools;

public class FeatureVector {
	String[] symbol = { "#", ";", ",", "!", "~", "@", "$", "%", "^", "&", "*",
			"(", ")", "_", "-", "{", "}", "[", "]", "\"", "'", ":", "?", "<",
			">", "." };
	int[] types = { TNode.NUMTYP, TNode.SYBSTYP, TNode.LWRDTYP, TNode.UWRDTYP };
	Vector<RecFeature> x = new Vector<RecFeature>();
	HashSet<String> dictionary = new HashSet<String>();
	public int size;

	public FeatureVector(HashSet<String> dic) {
		this.dictionary = dic;
	}

	public int size() {
		return this.symbol.length + this.types.length + 1
				+ this.dictionary.size();
	}

	public Vector<String> getNames() {
		Vector<String> names = new Vector<String>();
		for (RecFeature f : this.x) {
			String name = f.getName();
			if (name.compareTo(",") == 0) {
				name = "Comma";
			}
			names.add(name);
		}
		return names;
	}

	public Vector<RecFeature> createVector(String raw, String color) {
		if (raw.contains("<_START>")) {
			raw = raw.replace("<_START>", "");
		}
		if (raw.contains("<_END>")) {
			raw = raw.replace("<_END>", "");
		}
		Vector<RecFeature> v = new Vector<RecFeature>();
		Ruler r = new Ruler();
		r.setNewInput(raw);
		Vector<TNode> vt = new Vector<TNode>();
		vt = r.vec;
		HashMap<String, String> tmp = new HashMap<String, String>();
		try {
			UtilTools.StringColorCode(raw, color, tmp);
		} catch (Exception e) {
			tmp.put("Org", raw);
			tmp.put("Tar", "ERROR");
			tmp.put("Orgdis", raw);
			tmp.put("Tardis", "ERROR");
		}
		String tar = tmp.get("Tar");
		r.setNewInput(tar);
		Vector<TNode> tarNodes = r.vec;
		constructVector(vt, tarNodes, color, v, this.dictionary);
		this.size = v.size();
		this.x = v;
		return v;
	}

	// should normalize the feature values
	public void constructVector(Vector<TNode> t, Vector<TNode> tarNodes,
			String color, Vector<RecFeature> v, HashSet<String> dic) {
		double w = 0.25;
		double w1 = w / (symbol.length);
		for (String s : symbol) {
			Feature1 feature1 = new Feature1(s, tarNodes, w1);
			v.add(feature1);
		}
		double w2 = w / types.length;
		for (int type : types) {
			Feature2 feature2 = new Feature2(type, tarNodes, w2);
			v.add(feature2);
		}
		double w3 = w;
		Feature3 feature3 = new Feature3(color, w3);
		v.add(feature3);
		double w4 = w / dic.size();
		for (String s : dic) {
			Feature4 f4 = new Feature4(s, t, tarNodes, w4);
			v.add(f4);
		}
	}
}
