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

import java.util.Vector;

import edu.isi.karma.cleaning.Ruler;
import edu.isi.karma.cleaning.TNode;

//only to test whether a substring exist
public class RecordTextFeature implements Feature {
	public double score = 1.0;
	public String text = "";
	public Vector<TNode> nodes = new Vector<TNode>();
	public String value = "";

	public RecordTextFeature(String text, String value) {
		this.text = text;
		this.value = value;
		Ruler ruler = new Ruler();
		ruler.setNewInput(value);
		nodes = ruler.vec;
		this.score = computeScore();
	}
	public double computeScore()
	{
		int cnt = 0;
		for(TNode t:nodes)
		{
			if(t.text.compareTo(text)==0)
			{
				cnt += 1;
			}
		}
		return cnt;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "attr_" + text;
	}

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return this.score;
	}

}
