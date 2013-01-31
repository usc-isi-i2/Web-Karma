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

package edu.isi.karma.rep.model;

import edu.isi.karma.rep.alignment.Label;

/**
 * A data valued property atom consists of an OWL data property and two arguments, 
 * the first representing an OWL individual, and the second a data value.
 * @author mohsen
 *
 */
public class DatavaluedPropertyAtom extends Atom {

	private Label propertyPredicate;
	private Argument argument1;
	private Argument argument2;
	
	public DatavaluedPropertyAtom(Label propertyPredicate, Argument argument1, Argument argument2) {
		this.propertyPredicate = propertyPredicate;
		this.argument1 = argument1;
		this.argument2 = argument2;
	}

	public Label getPropertyPredicate() {
		return propertyPredicate;
	}

	public Argument getArgument1() {
		return argument1;
	}	
	
	public Argument getArgument2() {
		return argument2;
	}

	public void print() {
		System.out.println("property predicate uri: " + propertyPredicate.getUri());
		System.out.println("property predicate ns: " + propertyPredicate.getNs());
		System.out.println("property predicate prefix: " + propertyPredicate.getPrefix());
		System.out.println("argument1: " + argument1.getId());
		System.out.println("argument2: " + argument2.getId());
	}	
	
}
