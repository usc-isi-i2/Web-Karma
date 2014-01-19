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

import java.util.Vector;

import edu.isi.karma.cleaning.TNode;

//counting text feature
public class Feature1 implements RecFeature {
	public String target;
	public Vector<TNode> xNodes = new Vector<TNode>();
	public double weight = 1.0;

	public Feature1(String tar, Vector<TNode> xNodes, double weight) {
		target = tar;
		this.xNodes = xNodes;
		this.weight = weight;
	}

	public String getName() {
		return target;
	}

	public double computerScore() {
		double res = 0.0;
		if (xNodes == null)
			return 0.0;
		for (TNode x : xNodes) {
			if (x.text.compareTo(target) == 0)
				res += 1;
		}
		return res * this.weight;
	}
}
