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
import java.util.Iterator;
import java.util.Vector;

import edu.isi.karma.cleaning.ProgramRule;
import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;

public class RecordFeatureSet {
	public Collection<Feature> features;
	public String record;
	public HashSet<String> labels = new HashSet<String>();
	public String[] xStrings = { "#", ";", ",", "!", "~", "@", "$", "%", "^",
			"&", "\\*", "\\(", "\\)", "_", "-", "\\{", "\\}", "\\[", "\\]", "\\\"", "\\\'", ":",
			"\\?", "<", ">", "\\.", "/", "\\\\", "\\d+", "[A-Z]+", "[a-z]+", "[\\s]" };
	//public String[] xStrings = {"\\d+"};
	public String[] vocabs;
	public String[] getLabels()
	{
		return labels.toArray(new String[labels.size()]);
	}
	public RecordFeatureSet() {

	}

	// convert the records to tokensequences and then construct the vocabulary
	public void initialize(Vector<String> Records) {
		HashMap<String,Integer> hSet = new HashMap<String,Integer>();
		for (String s : Records) {
			Ruler r = new Ruler();
			r.setNewInput(s);
			for (TNode t : r.vec) {
				if (!hSet.containsKey(t.text)) {
					hSet.put(t.text, 1);
				}
				else
				{
					hSet.put(t.text, hSet.get(t.text)+1);
				}
			}
		}
		//filter vocabs
		//appear at least three times
		//not blank
		Vector<String> vs = new Vector<String>();
		for(String key:hSet.keySet())
		{
			if(key.trim().length()==0)
				continue;
			if(hSet.get(key)<3)
			{
				continue;
			}
			vs.add(key);
		}
		HashSet<String> words = new HashSet<String>();
		for (String k:vs)
		{
			if(!words.contains(k) && k!= null)
			{
				words.add(k);
			}
		}
		if(this.vocabs != null && this.vocabs.length >0)
		{
		  for(String w:this.vocabs)
		  {
			  if(!words.contains(w) && w != null)
			  {
				  words.add(w);
			  }
		  }
		}
		// remove the redundant token
		Iterator<String> it = words.iterator();
		while(it.hasNext())
		{
			String val = it.next();
			if (val.compareTo("DIGITs")==0)
			{
				it.remove();
				continue;
			}
			if(val.length() == 1)
			{
				if(!Character.isLetterOrDigit(val.charAt(0)))
				{
					it.remove();
					continue;
				}
			}
		}
		this.vocabs = words.toArray(new String[words.size()]);
	}

	public Collection<Feature> computeFeatures(String record, String label) {
		Vector<Feature> xCollection = new Vector<Feature>();
		for (String c : xStrings) {
			Feature f = new RecordCntFeatures(c, record, c);
			xCollection.add(f);
		}
		if(this.vocabs.length>0)
		{
			for(String c:vocabs)
			{
				Feature f = new RecordTextFeature(c, record);
				xCollection.add(f);
			}
		}
		if(!labels.contains(label))
		{
			this.labels.add(label);
		}
		return xCollection;
	}

	public Collection<String> getFeatureNames() {
		Vector<String> x = new Vector<String>();
		for (String s : xStrings) {
			if (s.compareTo("\"") == 0)
			{
				s = "Quote";
			}
			if(s.compareTo(",") == 0)
			{
				s = "Comma";
			}
			if(s.compareTo("\\\"")==0)
			{
				s = "DbQuto";
			}
			if(s.compareTo("\\\'")==0)
			{
				s = "SgQuto";
			}
			x.add("attr_" + s);
		}
		for (String s : vocabs) {
			{
				s = "Quote";
			}
			if(s.indexOf(",") != -1)
			{
				s = s.replaceAll(",", "Comma");
			}
			if(s.indexOf("\"")!=-1)
			{
				s = s.replaceAll("\"", "DbQuto");
			}
			if(s.indexOf("\'")!=-1)
			{
				s = s.replaceAll("\'", "SgQuto");
			}
			x.add("attr_" + s);
		}
		return x;
	}

	public static void main(String[] args) {

	}
}
