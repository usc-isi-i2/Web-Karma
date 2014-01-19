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

package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.Vector;

import edu.isi.karma.rep.cleaning.RamblerTransformationExample;
import edu.isi.karma.rep.cleaning.RamblerTransformationInputs;
import edu.isi.karma.rep.cleaning.RamblerTransformationOutput;
import edu.isi.karma.rep.cleaning.RamblerValueCollection;
import edu.isi.karma.rep.cleaning.TransformationExample;
import edu.isi.karma.rep.cleaning.ValueCollection;

public class SampleCode {
	public static void main(String[] args) {
		// raw input data in hashmap.
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("id1", "1912-2001");
		data.put("id2", "1860-1945");
		data.put("id3", "1860-1945");
		// use the hashmap to create valuecollect object
		RamblerValueCollection rv = new RamblerValueCollection(data);
		// create examples
		RamblerTransformationExample re = new RamblerTransformationExample(
				"1912-2001", "1912", "id1");
		Vector<TransformationExample> examples = new Vector<TransformationExample>();
		examples.add(re);
		// use the raw data and examples to create an input object
		RamblerTransformationInputs ri = new RamblerTransformationInputs(
				examples, rv);
		// generate the output object
		RamblerTransformationOutput ro = new RamblerTransformationOutput(ri);

		// get the transformed value in JSON format
		for (String ruleid : ro.getTransformations().keySet()) {
			ValueCollection vCollection = ro.getTransformedValues(ruleid);
			System.out.println("" + vCollection.getJson());
		}
		// ro.getRecommandedNextExample() will return the id of recommended
		// examples.
		// this interface hasn't been implemented yet.
	}
}
