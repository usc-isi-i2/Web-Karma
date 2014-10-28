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

//type count feature
public class Feature2 implements RecFeature {

	public int type;
	public Vector<TNode> xNodes = new Vector<TNode>();
	public double weight = 1.0;

	public Feature2(int type, Vector<TNode> xNodes, double weight) {
		this.type = type;
		this.xNodes = xNodes;
		this.weight = weight;
	}

	public String getName() {
		return type + "";
	}

	public double computerScore() {
		double res = 0.0;
		if (xNodes == null)
			return 0.0;
		for (TNode x : xNodes) {
			if (x.type == type)
				res += 1;
		}
		return res * weight;
	}

}
