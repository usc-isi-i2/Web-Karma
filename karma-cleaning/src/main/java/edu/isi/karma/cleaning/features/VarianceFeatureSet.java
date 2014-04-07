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
import java.util.Iterator;
import java.util.Vector;

class Varfeature implements Feature {

	String name = "";
	double score = 0.0;

	public Varfeature(double a, double b, String fname) {
		score = a - b;
		name = fname + "_var";
	}

	public String getName() {

		return this.name;
	}

	public double getScore() {

		return score;
	}

}

public class VarianceFeatureSet implements FeatureSet {

	public VarianceFeatureSet() {
	}

	public Collection<Feature> computeFeatures(Collection<String> oldexamples,
			Collection<String> newexamples) {

		Vector<Feature> fs = new Vector<Feature>();
		RegularityFeatureSet rf1 = new RegularityFeatureSet();
		Collection<Feature> x = rf1.computeFeatures(oldexamples, newexamples);
		RegularityFeatureSet rf2 = new RegularityFeatureSet();
		Collection<Feature> y = rf2.computeFeatures(oldexamples, newexamples);
		Iterator<Feature> i1 = x.iterator();
		Iterator<Feature> i2 = y.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			Feature f1 = i1.next();
			Feature f2 = i2.next();
			if (f1.getName().compareTo(f2.getName()) == 0) {
				Varfeature vf = new Varfeature(f1.getScore(), f2.getScore(),
						f1.getName());
				fs.add(vf);
			}
		}
		return fs;
	}

	public Collection<String> getFeatureNames() {

		return null;
	}
}
