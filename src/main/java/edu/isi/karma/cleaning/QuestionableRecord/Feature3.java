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

// reverse count feature
public class Feature3 implements RecFeature {
	int rcnt = 0;
	String resString;
	double weight = 1.0;

	// calculate the number of reverse order
	public Feature3(String res, double weight) {
		this.resString = res;
	}

	public String getName() {
		return resString;
	}

	@Override
	public double computerScore() {
		int pre = -1;
		double cnt = 0;
		for (int c = 0; c < resString.length(); c++) {
			if (Character.isDigit(resString.charAt(c))) {
				if (pre != -1) {
					pre = c;
					continue;
				}
				if (c < pre) {
					pre = c;
					cnt++;
				}
			}
		}
		return cnt * this.weight;
	}

}
