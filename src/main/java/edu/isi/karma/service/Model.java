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

package edu.isi.karma.service;

import java.util.ArrayList;
import java.util.List;

public class Model {
	
	private String id;
	private String baseUri;
	
	private List<Atom> atoms;

	public Model(String id) {
		this.id = id;
		atoms = new ArrayList<Atom>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getUri() {
		String uri = "";
		if (getBaseUri() != null) uri += getBaseUri();
		if (getId() != null) uri += getId();
		return uri;
	}

	public List<Atom> getAtoms() {
		return atoms;
	}

	public void setAtoms(List<Atom> atoms) {
		this.atoms = atoms;
	}

	public void print() {
		System.out.println("model id=" + this.getId());
		System.out.println(getLogicalForm());
//		for (Atom atom : atoms) {
//			System.out.println("@@@@@@@@@@@@@@@");
//			if (atom != null) atom.print();
//		}
	}
	
	public String getLogicalForm() {
		String logicalForm = "";
		String separator = " /\\ ";
		for (Atom atom : atoms) {
			if (atom != null) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = ((ClassAtom)atom);
					logicalForm += classAtom.getClassPredicate().getLocalName();
					logicalForm += "(";
					logicalForm += classAtom.getArgument1().getId();
					logicalForm += ")";
					logicalForm += separator;				
				}
				else if (atom instanceof PropertyAtom) {
					PropertyAtom propertyAtom = ((PropertyAtom)atom);
					logicalForm += propertyAtom.getPropertyPredicate().getLocalName();
					logicalForm += "(";
					logicalForm += propertyAtom.getArgument1().getId();
					logicalForm += ",";
					logicalForm += propertyAtom.getArgument2().getId();
					logicalForm += ")";
					logicalForm += separator;				
				}			
			}
		}		
		int index = logicalForm.lastIndexOf(separator);
		if (index != -1)
			logicalForm = logicalForm.substring(0, index);
		
		return logicalForm;
	}

}
