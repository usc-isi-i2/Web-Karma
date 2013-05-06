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

package edu.isi.karma.cleaning.features;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion.Static;

import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;

public class RecordFeatureSet {
	public Collection<Feature> features;
	public String record;
	public String label;
	public String[] xStrings = { "#", ";", ",", "!", "~", "@", "$", "%", "^",
			"&", "\\*", "\\(", "\\)", "_", "-", "\\{", "\\}", "\\[", "\\]", "\\\"", "\\\'", ":",
			"\\?", "<", ">", "\\.", "/", "\\\\", "\\d+", "[A-Z]+", "[a-z]+", "[\\s]" };
	//public String[] xStrings = {"\\d+"};
	public String[] vocabs;

	public RecordFeatureSet() {

	}

	// convert the records to tokensequences and then construct the vocabulary
	public void initialize(Vector<String> Records) {
		HashSet<String> hSet = new HashSet<String>();
		for (String s : Records) {
			Ruler r = new Ruler();
			r.setNewInput(s);
			for (TNode t : r.vec) {
				if (!hSet.contains(t.text)) {
					hSet.add(t.text);
				}
			}
		}
		vocabs = hSet.toArray(new String[hSet.size()]);
	}

	public Collection<Feature> computeFeatures(String record, String label) {
		Vector<Feature> xCollection = new Vector<Feature>();
		for (String c : xStrings) {
			Feature f = new RecordCntFeatures(c, record, c);
			xCollection.add(f);
		}
		for(String c:vocabs)
		{
			Feature f = new RecordTextFeature(c, record);
			xCollection.add(f);
		}
		// to add other type of features
		return xCollection;
	}

	public Collection<String> getFeatureNames() {
		Vector<String> x = new Vector<String>();
		int cnt = 0;
		for (String s : xStrings) {
			x.add("attr_" + cnt);
			cnt++;
		}
		for (String s : vocabs) {
			x.add("attr_" + cnt);
			cnt++;
		}
		return x;
	}

	public static void main(String[] args) {

	}
}
