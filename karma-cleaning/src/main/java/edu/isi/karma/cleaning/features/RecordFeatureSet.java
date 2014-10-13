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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

public class RecordFeatureSet {
	public String record;
	public HashSet<String> labels = new HashSet<String>();
	public String[] xStrings = { "=","\\|","#", ";", ",", "!", "~", "@", "\\$", "%", "\\^",
			"&", "\\*", "\\(", "\\)", "_", "-", "\\{", "\\}", "\\[", "\\]", "\\\"", "\\\'", ":",
			"\\?", "<", ">", "\\.", "/", "\\\\", "\\d+", "[A-Z]+", "[a-z]+", "[\\s]" };
	//public String[] xStrings = {"\\d+"};
	public String[] vocabs = {};
	public String[] getLabels()
	{
		return labels.toArray(new String[labels.size()]);
	}

	public RecordFeatureSet() {

	}
	public void init()
	{
		this.labels = new HashSet<String>();
	}
	public void removeFeatures(ArrayList<Integer> fs)
	{
		ArrayList<String> xList = new ArrayList<String>();
		for(int i = 0; i<xStrings.length; i++)
		{
			boolean found = false;
			for(int j = 0; j < fs.size(); j++)
			{
				if(i == fs.get(j))
				{
					found = true;
					break;
				}
			}
			if(!found)
				xList.add(xStrings[i]);
		}
		xStrings = xList.toArray(new String[xList.size()]);
	}
	public void addVocabulary(String[] vocb)
	{
		if(vocb == null)
		{
			return;
		}
		//update xString to constain new Vocabs
		ArrayList<String> res = new ArrayList<String>();
		for(String syb: xStrings)
		{
			res.add(syb);
		}
		for (String s : vocb) {
			boolean find = false;
			for(String syb:res)
			{
				if(s.indexOf(syb)!= -1)
				{
					find = true;
					break;
				}
			}
			if(!find)
				res.add(s);
		}
		this.xStrings = res.toArray(new String[res.size()]);
	}
	public Collection<Feature> computeFeatures(String record, String label) {
		Vector<Feature> xCollection = new Vector<Feature>();
		for (String c : xStrings) {
			Feature f = new RecordCntFeatures(c, record, c);
			xCollection.add(f);
		}
		if (!labels.contains(label)) {
			this.labels.add(label);
		}
		return xCollection;
	}

	public Collection<String> getFeatureNames() {
		Vector<String> x = new Vector<String>();
		for (String s : xStrings) {
			if (s.compareTo("\"") == 0) {
				s = "Quote";
			}
			if (s.compareTo(",") == 0) {
				s = "Comma";
			}
			if (s.compareTo("\\\"") == 0) {
				s = "DbQuto";
			}
			if (s.compareTo("\\\'") == 0) {
				s = "SgQuto";
			}
			x.add("attr_" + s);
		}
		return x;
	}

	public static void main(String[] args) {

	}
}
