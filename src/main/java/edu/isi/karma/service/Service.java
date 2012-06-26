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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpMethods;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.NodeType;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.alignment.URI;
import edu.isi.karma.modeling.alignment.Vertex;

public class Service {
	
	static Logger logger = Logger.getLogger(Service.class);

	public static final String KARMA_SERVICE_PREFIX = "http://isi.edu/integration/karma/services/";

	private String id;
	private String name;
	private String method;
	private String address;
	private URL urlExample;
	private String description;
	private String operationName;

	private List<Attribute> inputAttributes;
	private List<Attribute> outputAttributes;

	private List<String> variables;

	private Model inputModel;
	private Model outputModel;
	
	HashMap<String, Attribute> attIdToAttMap;

	private HashMap<String, Attribute> hNodeIdToAttribute;

	public Service(String id, URL urlExample) {
		this.id = id;
		this.urlExample= urlExample;
		this.name = getOperationName();
		hNodeIdToAttribute = new HashMap<String, Attribute>();
		variables = new ArrayList<String>();
		inputAttributes = new ArrayList<Attribute>();
		outputAttributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
	}
	
	public Service(String id, String name, URL urlExample) {
		this.id = id;
		this.setName(name);
		this.urlExample= urlExample;
		this.setMethod(HttpMethods.GET);
		this.urlExample = urlExample;
		inputAttributes = new ArrayList<Attribute>();
		outputAttributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
	}
	
	public Service(String id, String addressTemplate) {
		this.id = id;
		this.address= addressTemplate;
		inputAttributes = new ArrayList<Attribute>();
		outputAttributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
	}
	
	public Service(String id, String name, String addressTemplate) {
		this.id = id;
		this.setName(name);
		this.address= addressTemplate;
		inputAttributes = new ArrayList<Attribute>();
		outputAttributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
	}

	public Service(String id, String name, URL urlExample, String method) {
		this.id = id;
		this.setName(name);
		this.urlExample = urlExample;
		this.setMethod(method);
		inputAttributes = new ArrayList<Attribute>();
		outputAttributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
	}

	public String getUri() {
		return KARMA_SERVICE_PREFIX + getId() + "#";
	}

	public String getOperationName() {
		if (operationName == null)
			this.operationName = URLManager.getOperationName(this.urlExample);
		
		return operationName;
	}

	public String getPopulatedAddress(Map<String, String> attIdToValue) {
		String address = this.getAddress();
		String populatedAddress = address;
		
		for (String attId : attIdToValue.keySet()) {
			Attribute att = this.attIdToAttMap.get(attId);
			if (att == null) {
				logger.debug("Cannot find the attribute " + attId + " in this service.");
				return null;
			}
			if (!att.getIOType().equalsIgnoreCase(IOType.INPUT)) {
				logger.debug("The type of the attribute " + attId + " is not INPUT.");
				return null;
			}
			
			String groundedIn = att.getGroundedIn();
			if (groundedIn == null || groundedIn.trim().length() == 0) {
				logger.debug("The attribute " + attId + " grounding parameter is not specified.");
				return null;
			}
			
			String value = attIdToValue.get(attId);
			if (value == null) {// || value.trim().length() == 0) {
				logger.debug("No value is given for attribute " + attId);
				return null;
			}
			
			logger.debug("att: " + attId);
			logger.debug("grounded in: " + groundedIn.trim());
			logger.debug("value: " + value.trim());
			
			populatedAddress = populatedAddress.replaceAll("\\{" + groundedIn.trim() + "\\}", value);
		}
		
		return populatedAddress;
	}

	public Attribute getAttribute(String id) {
		return this.attIdToAttMap.get(id);
	}
	
	public String getId() {
		return id;
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

	public List<Attribute> getInputAttributes() {
		return Collections.unmodifiableList(inputAttributes);
	}

	public void setInputAttributes(List<Attribute> inputAttributes) {
		if (inputAttributes != null)
			for (Attribute att : inputAttributes)
				att.setBaseUri(this.getUri());
		if (inputAttributes != null)
			for (Attribute att : inputAttributes)
				attIdToAttMap.put(att.getId(), att);
		this.inputAttributes = inputAttributes;
	}

	public List<Attribute> getOutputAttributes() {
		return Collections.unmodifiableList(outputAttributes);
	}

	public void setOutputAttributes(List<Attribute> outputAttributes) {
		if (outputAttributes != null)
			for (Attribute att : outputAttributes)
				att.setBaseUri(this.getUri());
		if (outputAttributes != null)
			for (Attribute att : outputAttributes)
				attIdToAttMap.put(att.getId(), att);
		this.outputAttributes = outputAttributes;
	}

	public Model getInputModel() {
		return inputModel;
	}

	public void setInputModel(Model inputModel) {
		if (inputModel != null)
			inputModel.setBaseUri(this.getUri());
		this.inputModel = inputModel;
	}
	
	public Model getOutputModel() {
		return outputModel;
	}

	public void setOutputModel(Model outputModel) {
		if (inputModel != null)
			outputModel.setBaseUri(this.getUri());
		this.outputModel = outputModel;
	}
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getAddress() {
		if (address == null)
			doGrounding();
		
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public List<String> getVariables() {
		return variables;
	}

	public HashMap<String, Attribute> gethNodeIdToAttribute() {
		return hNodeIdToAttribute;
	}
	
	public void sethNodeIdToAttribute(HashMap<String, Attribute> hNodeIdToAttribute) {
		this.hNodeIdToAttribute = hNodeIdToAttribute;
	}

	private void doGrounding() {
		String str = this.urlExample.toString();
		
		if (str == null || str.length() == 0) {
			this.address = "";
			return;
		}
		
		if (this.inputAttributes == null) {
			this.address = str;
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
		
		this.address = str;
	}
	
	public void updateModel(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeModel) {
		
		if (treeModel == null)
			return;
		
		List<Vertex> inputAttributesNodes = new ArrayList<Vertex>();
		List<Vertex> outputAttributesNodes = new ArrayList<Vertex>();
		
		HashMap<String, Argument> vertexIdToArgument = new HashMap<String, Argument>();

		this.hNodeIdToAttribute.clear();
		buildHNodeId2AttributeMapping();
		
		// set the rdf ids of all the vertices. The rdf id of leaf vertices are the attribute ids. 
		String hNodeId = "";
		for (Vertex v : treeModel.vertexSet()) {
			if (v.getSemanticType() != null && v.getSemanticType().getHNodeId() != null) {
				logger.debug("Vertex " + v.getLocalID() + " is a semantic type associated to a source columns.");
				hNodeId = v.getSemanticType().getHNodeId();
			} else {
				logger.debug("Vertex " + v.getLocalID() + " is an intermediate node.");
				String variableId = "v" + String.valueOf(variables.size() + 1);
				variables.add(variableId);
				vertexIdToArgument.put(v.getID(), new Argument(variableId, variableId, ArgumentType.VARIABLE));
				continue;
			}
			
			Attribute att = this.hNodeIdToAttribute.get(hNodeId);
			if (att == null) {
				logger.error("No attribute is associated to the column with semantic type " + v.getID());
				continue;
			}
			
			vertexIdToArgument.put(v.getID(), new Argument(att.getId(), att.getId(), ArgumentType.ATTRIBUTE));
			
			if (att.getIOType() == IOType.INPUT) {
				inputAttributesNodes.add(v);
			}
			if (att.getIOType() == IOType.OUTPUT) {
				outputAttributesNodes.add(v);
			}
		}

		
		List<String> inputModelVertexes = new ArrayList<String>();
		List<String> inputModelEdges = new ArrayList<String>();		
		
		Model inputModel = getInputModel(treeModel, inputAttributesNodes, 
				inputModelVertexes, inputModelEdges,
				vertexIdToArgument);
		
		this.setInputModel(inputModel);
		
		Model outputModel = getOutputModel(treeModel, 
				inputModelVertexes, inputModelEdges,
				vertexIdToArgument);
		
		this.setOutputModel(outputModel);
		
	}
	
	private Model getInputModel(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeModel, 
			List<Vertex> inputNodes, List<String> inputModelVertexes, List<String> inputModelEdges,
			HashMap<String, Argument> vertexIdToArgument) {

		if (treeModel == null)
			return null;
				
		logger.debug("compute the steiner tree from the alignment tree with input nodes as steiner nodes ...");
		UndirectedGraph<Vertex, LabeledWeightedEdge> undirectedGraph = 
			new AsUndirectedGraph<Vertex, LabeledWeightedEdge>(treeModel);
		List<Vertex> steinerNodes = inputNodes;
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);


		Model m = new Model("inputModel");
		for (Vertex v : steinerTree.getSteinerTree().vertexSet()) {
			
			inputModelVertexes.add(v.getID());
			
			if (v.getNodeType() == NodeType.DataProperty)
				continue;
			
			if (vertexIdToArgument.get(v.getID()) == null)
				continue;
			
			URI classPredicate = new URI(v.getUriString(), v.getNs(), v.getPrefix());

			ClassAtom classAtom = new ClassAtom(classPredicate, vertexIdToArgument.get(v.getID()));
			m.getAtoms().add(classAtom);
		}
		
		for (LabeledWeightedEdge e : steinerTree.getSteinerTree().edgeSet()) {
			
			inputModelEdges.add(e.getID());
			
			if (vertexIdToArgument.get(e.getSource().getID()) == null || 
					vertexIdToArgument.get(e.getTarget().getID()) == null)
				continue;
			
			URI propertyPredicate = new URI(e.getUriString(), e.getNs(), e.getPrefix());

			PropertyAtom propertyAtom = new PropertyAtom(propertyPredicate, 
					vertexIdToArgument.get(e.getSource().getID()),
					vertexIdToArgument.get(e.getTarget().getID()));
			m.getAtoms().add(propertyAtom);
		}

		return m;
	}

	private Model getOutputModel(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeModel, 
			List<String> inputModelVertexes, List<String> inputModelEdges,
			HashMap<String, Argument> vertexIdToArgument) {

		if (treeModel == null)
			return null;

		Model m = new Model("outputModel");
		
		for (Vertex v : treeModel.vertexSet()) {
			
			if (inputModelVertexes.indexOf(v.getID()) != -1)
				continue;
			
			if (v.getNodeType() == NodeType.DataProperty)
				continue;
			
			if (vertexIdToArgument.get(v.getID()) == null)
				continue;
			
			
			URI classPredicate = new URI(v.getUriString(), v.getNs(), v.getPrefix());

			ClassAtom classAtom = new ClassAtom(classPredicate, vertexIdToArgument.get(v.getID()));
			m.getAtoms().add(classAtom);
		}
		
		for (LabeledWeightedEdge e : treeModel.edgeSet()) {
			
			if (inputModelEdges.indexOf(e.getID()) != -1)
				continue;
			
			if (vertexIdToArgument.get(e.getSource().getID()) == null || 
					vertexIdToArgument.get(e.getTarget().getID()) == null)
				continue;
			
			URI propertyPredicate = new URI(e.getUriString(), e.getNs(), e.getPrefix());

			PropertyAtom propertyAtom = new PropertyAtom(propertyPredicate, 
					vertexIdToArgument.get(e.getSource().getID()),
					vertexIdToArgument.get(e.getTarget().getID()));

			m.getAtoms().add(propertyAtom);
		}
		
		return m;
	}

	public void buildHNodeId2AttributeMapping() {
		for (Attribute att : getInputAttributes()) 
			if (att.gethNodeId() != null)
				this.hNodeIdToAttribute.put(att.gethNodeId(), att);
		for (Attribute att : getOutputAttributes()) 
			if (att.gethNodeId() != null)
				this.hNodeIdToAttribute.put(att.gethNodeId(), att);
	}
	
	public String getInfo() {
		String s = "";
		
		s += "uri=" + this.getUri() + "\n";
		s += "id=" + this.getId() + ", ";
		s += "name=" + this.getName() + ", ";
		s += "address=" + this.getAddress() + ", ";
		s += "method=" + this.getMethod();
		
		return s;
	}
	
	public void print() {
		System.out.println("********************************************");
		System.out.println("Service: " + getInfo());
		System.out.println("********************************************");
		System.out.println("Input Attributes: ");
		if (this.inputAttributes != null)
			for (Attribute p : this.inputAttributes)
				p.print();
		System.out.println("********************************************");
		System.out.println("Input Model: ");
		if (this.inputModel != null) {
			System.out.println(inputModel.getUri());
			this.inputModel.print();
		}
		System.out.println("********************************************");
		System.out.println("Output Attributes: ");
		if (this.outputAttributes != null)
			for (Attribute p : getOutputAttributes())
				p.print();
		System.out.println("********************************************");
		System.out.println("Output Model: ");
		if (this.outputModel != null) {
			System.out.println(outputModel.getUri());
			this.outputModel.print();
		}
	}


}
