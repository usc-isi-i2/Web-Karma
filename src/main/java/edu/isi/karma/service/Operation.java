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
import edu.isi.karma.modeling.alignment.Vertex;

public class Operation {

	private String name;
	private String description;

	private String method;

	private List<Param> inputParams;
	private List<Param> outputParams;

	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> operationTreeModel = null;
	
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

	public List<Param> getInputParams() {
		return inputParams;
	}

	public void setInputParams(List<Param> inputParams) {
		this.inputParams = inputParams;
	}

	public List<Param> getOutputParams() {
		return outputParams;
	}

	public void setOutputParams(List<Param> outputParams) {
		this.outputParams = outputParams;
	}

	public DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> getOperationTreeModel() {
		return operationTreeModel;
	}

	public void setOperationTreeModel(
			DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> operationTreeModel) {
		this.operationTreeModel = operationTreeModel;
	}

	public Rule getRule() {
		
		if (this.operationTreeModel == null)
			return null;
		
		Rule rule = new Rule();
		
		rule.setHead(getRuleHead());
		rule.setBody(getRuleBody());
		
		return rule;
	}
	
	private Clause getRuleHead() {

		if (this.operationTreeModel == null)
			return null;
		
		Clause head = new Clause();
		return head;
	}

	private Clause getRuleBody() {

		if (this.operationTreeModel == null)
			return null;
		
		Clause body = new Clause();
		return body;
	}

	public void print() {
		System.out.println("name: " + this.getName());
		System.out.println("description: " + this.getDescription());
		System.out.println("----------------------");
		System.out.println("input parameters: ");
		for (Param p : getInputParams())
			p.print();
		System.out.println("----------------------");
		System.out.println("output parameters: ");
		for (Param p : getOutputParams())
			p.print();
	}
	
}