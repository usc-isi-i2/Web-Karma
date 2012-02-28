/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *  
 *    This code was developed by the Information Integration Group as part 
 *    of the Karma project at the Information Sciences Institute of the 
 *    University of Southern California.  For more information, publications, 
 *    and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.mediator.rule;


import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.Rule;


/**
 * Defines a Rule.
 * @author mariam
 *
 */
public class GLAVRule extends Rule{
	
	//GLAV_RULES: antecedent -> consequent (multiple predicates in antecedent and consequent)

	public GLAVRule(){}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Rule#clone()
	 */
	public GLAVRule clone(){
		GLAVRule r = new GLAVRule();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			r.antecedent.add(p.clone());
		}
		for(int i=0; i<consequent.size(); i++){
			Predicate p = consequent.get(i);
			r.consequent.add(p.clone());
		}
		return r;
	}

	public boolean isValid(){
		//I am not sure yet what this method should do for LAV rules
		return true;
	}

}
