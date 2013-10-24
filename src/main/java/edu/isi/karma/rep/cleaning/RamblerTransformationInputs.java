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
package edu.isi.karma.rep.cleaning;

import java.util.Collection;


public class RamblerTransformationInputs implements TransformationInputs {

	private Collection<TransformationExample> examples;
	private ValueCollection inputValues;
	//private Transformation preferedTransformation;
	public RamblerTransformationInputs(Collection<TransformationExample> examples,ValueCollection inputValues)
	{
		this.examples = examples;
		this.inputValues = inputValues;
	}
	public Collection<TransformationExample> getExamples() {
		// TODO Auto-generated method stub
		return this.examples;
	}
	public ValueCollection getInputValues() {
		// TODO Auto-generated method stub
		return this.inputValues;
	}
	public void setPreferredRule(Transformation t) {
		// TODO Auto-generated method stub
		//this.preferedTransformation = t;
	}
}
