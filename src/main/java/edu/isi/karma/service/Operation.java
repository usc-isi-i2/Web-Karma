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

import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Name;
import edu.isi.karma.modeling.alignment.NodeType;
import edu.isi.karma.modeling.alignment.Vertex;

public class Operation {

	private String id;
	private String name;
	private String description;

	private String method;
	private String address;
	private String addressTemplate;

	private List<Attribute> inputAttributes;
	private List<Attribute> outputAttributes;
	
	private Model inputModel;
	private Model outputModel;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Model getInputModel() {
		return inputModel;
	}

	public void setInputModel(Model inputModel) {
		this.inputModel = inputModel;
	}

	public Model getOutputModel() {
		return outputModel;
	}

	public void setOutputModel(Model outputModel) {
		this.outputModel = outputModel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<Attribute> getInputAttributes() {
		return inputAttributes;
	}

	public void setInputAttributes(List<Attribute> inputAttributes) {
		this.inputAttributes = inputAttributes;
	}

	public List<Attribute> getOutputAttributes() {
		return outputAttributes;
	}

	public void setOutputAttributes(List<Attribute> outputAttributes) {
		this.outputAttributes = outputAttributes;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	
	public String getAddressTemplate() {
		if (addressTemplate == null)
			doGrounding();
		
		return addressTemplate;
	}

	private void doGrounding() {
		String str = this.getAddress();
		
		if (this.address == null || this.address.length() == 0) {
			this.addressTemplate = "";
			return;
		}
		
		if (this.inputAttributes == null) {
			this.addressTemplate = this.address;
			return;
		}
		
		// This only works for Web APIs and not RESTful APIs
		for (int i = 0; i < this.inputAttributes.size(); i++) {
			String name = this.inputAttributes.get(i).getName();
			String groundVar = "p" + String.valueOf(i+1);
			int index = str.indexOf(name);
			String temp = str.substring(index);
			if (temp.indexOf("&") != -1)
				temp = temp.substring(0, temp.indexOf("&"));
			if (temp.indexOf("=") != -1)
				temp = temp.substring(temp.indexOf("=") + 1);
			
			str = str.replaceFirst(temp.trim(), "{" + groundVar + "}");
			this.inputAttributes.get(i).setGroundedIn(groundVar);
		}
		
		this.addressTemplate = str;
	}

	public void updateModel(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> operationTreeModel) {
		
		if (operationTreeModel == null)
			return;
		
		Model inputModel = getInputModel(operationTreeModel);
		this.setInputModel(inputModel);
		
		Model outputModel = getOutputModel(operationTreeModel, inputModel);
		this.setOutputModel(outputModel);
		
	}
	
	private Model getInputModel(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> operationTreeModel) {

		if (operationTreeModel == null)
			return null;
		
		Model m = new Model();
		return m;
	}

	private Model getOutputModel(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> operationTreeModel, Model inputModel) {

		if (operationTreeModel == null)
			return null;
		
		if (operationTreeModel.vertexSet() == null)
			return null;
		
		Model m = new Model();
		
		for (Vertex v : operationTreeModel.vertexSet()) {
			if (v.getNodeType() == NodeType.DataProperty)
				continue;
			
			Name classPredicate = new Name(v.getUri(), v.getNs(), v.getPrefix());
			Name argument1 = new Name(v.getLocalID(), "", "");

			ClassAtom classAtom = new ClassAtom(classPredicate, argument1);
			m.getAtoms().add(classAtom);
		}
		
		for (LabeledWeightedEdge e : operationTreeModel.edgeSet()) {
			Name propertyPredicate = new Name(e.getUri(), e.getNs(), e.getPrefix());
			Name argument1 = new Name(e.getSource().getLocalID(), "", "");
			Name argument2 = new Name(e.getTarget().getLocalID(), "", "");

			PropertyAtom propertyAtom = new PropertyAtom(propertyPredicate, argument1, argument2);
			m.getAtoms().add(propertyAtom);
		}
		
		return m;
	}

	public void print() {
		System.out.println("name: " + this.getName());
		System.out.println("description: " + this.getDescription());
		System.out.println("----------------------");
		System.out.println("input attributeeters: ");
		for (Attribute p : getInputAttributes())
			p.print();
		System.out.println("----------------------");
		System.out.println("output attributeeters: ");
		for (Attribute p : getOutputAttributes())
			p.print();
	}
	
}