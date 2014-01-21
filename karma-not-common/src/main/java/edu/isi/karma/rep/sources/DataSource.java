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

package edu.isi.karma.rep.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.model.Argument;
import edu.isi.karma.rep.model.ArgumentType;
import edu.isi.karma.rep.model.ClassAtom;
import edu.isi.karma.rep.model.IndividualPropertyAtom;
import edu.isi.karma.rep.model.Model;
import edu.isi.karma.util.RandomGUID;

public class DataSource extends Source {

	static Logger logger = LoggerFactory.getLogger(DataSource.class);

	private List<Attribute> attributes;
	private Model model;
	private List<String> variables;

	HashMap<String, Attribute> attIdToAttMap;

	public DataSource(String id) {
		super(id);
		variables = new ArrayList<String>();
		attributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
	}
	
	public DataSource(String id, String name) {
		super(id, name);
		variables = new ArrayList<String>();
		attributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
	}
	
	public DataSource(String name, DirectedWeightedMultigraph<Node, Link> treeModel) {
		super(new RandomGUID().toString());
		this.setName(name);
		variables = new ArrayList<String>();
		attributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
		this.updateModel(treeModel);
	}
	
	public DataSource(DirectedWeightedMultigraph<Node, Link> treeModel) {
		super(new RandomGUID().toString());
		variables = new ArrayList<String>();
		attributes = new ArrayList<Attribute>();
		attIdToAttMap = new HashMap<String, Attribute>();
		this.updateModel(treeModel);
	}
	
	public String getUri() {
		return ModelingConfiguration.getKarmaSourcePrefix() + getId() + "#";
	}
	public List<Attribute> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}

	public Attribute getAttribute(String id) {
		return this.attIdToAttMap.get(id);
	}
	
	public void setAttributes(List<Attribute> attributes) {
		
		if (this.attributes != null)
			this.attributes.clear();
		
		for (Attribute att : attributes)
			att.setBaseUri(this.getUri());
		for (Attribute att : attributes)
			attIdToAttMap.put(att.getId(), att);
		
		this.attributes = attributes;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		if (model != null)
			model.setBaseUri(this.getUri());
		this.model = model;
	}

	public List<String> getVariables() {
		return variables;
	}

	public void setVariables(List<String> variables) {
		this.variables = variables;
	}
	
	private void updateModel(DirectedWeightedMultigraph<Node, Link> treeModel) {
		
		if (treeModel == null)
			return;
		
		Model m = new Model("model");
		
		HashMap<String, Argument> vertexIdToArgument = new HashMap<String, Argument>();
		List<Attribute> attributeList = new ArrayList<Attribute>();
		
		// get the column name associated to the hNodeIds to assign to attribute names 
		// set the rdf ids of all the vertices.
		for (Node n : treeModel.vertexSet()) {
			if (n instanceof ColumnNode) {
				logger.debug("Vertex " + n.getLocalId() + " is a column node.");
				String hNodeId = ((ColumnNode)n).getHNodeId();
				String attId = "att" + String.valueOf(attributeList.size() + 1);
				Attribute att = new Attribute(attId, this.getUri(), n.getLabel().getLocalName(), IOType.NONE, AttributeRequirement.NONE);
				att.sethNodeId(hNodeId);
				attributeList.add(att);
				
				vertexIdToArgument.put(n.getId(), new Argument(att.getId(), att.getId(), ArgumentType.ATTRIBUTE));
			} else {
				logger.debug("Vertex " + n.getLocalId() + " is an intermediate node.");
				String variableId = "v" + String.valueOf(variables.size() + 1);
				this.variables.add(variableId);

				vertexIdToArgument.put(n.getId(), new Argument(variableId, variableId, ArgumentType.VARIABLE));
			}
		}

		for (Node n : treeModel.vertexSet()) {
			
			if (n instanceof ColumnNode || n instanceof LiteralNode)
				continue;
			
			if (vertexIdToArgument.get(n.getId()) == null)
				continue;
			
			Label classPredicate = new Label(n.getLabel().getUri(), n.getLabel().getNs(), n.getLabel().getPrefix());

			ClassAtom classAtom = new ClassAtom(classPredicate, vertexIdToArgument.get(n.getId()));
			m.getAtoms().add(classAtom);
		}
		
		for (Link e : treeModel.edgeSet()) {
			
			if (vertexIdToArgument.get(e.getSource().getId()) == null || 
					vertexIdToArgument.get(e.getTarget().getId()) == null)
				continue;

			Label propertyPredicate = new Label(e.getLabel().getUri(), e.getLabel().getNs(), e.getLabel().getPrefix());
			IndividualPropertyAtom propertyAtom = null;
			
			// has_subclass is from source to target, we substitute this with a rdfs:subClassOf from target to source
//			if (propertyPredicate.getUriString().equalsIgnoreCase(ModelingParams.HAS_SUBCLASS_URI)){
//				Label subClassPredicate = new Label(ModelingParams.SUBCLASS_URI, Namespaces.OWL, Prefixes.OWL);
//				propertyAtom = new IndividualPropertyAtom(subClassPredicate, 
//						vertexIdToArgument.get(e.getTarget().getId()),
//						vertexIdToArgument.get(e.getSource().getId()));
//			} else {
				propertyAtom = new IndividualPropertyAtom(propertyPredicate, 
						vertexIdToArgument.get(e.getSource().getId()),
						vertexIdToArgument.get(e.getTarget().getId()));
//			}
			m.getAtoms().add(propertyAtom);
		}
		
		// will update the hashmap.
		setAttributes(attributeList);
		this.setModel(m);
		
	}

	public String getInfo() {
		String s = "";
		s += "uri=" + this.getUri() + "\n";
		s += "id=" + this.getId() + ", ";
		s += "name=" + this.getName() + ", ";
		return s;
	}
	
	@Override
	public void print() {
		System.out.println("********************************************");
		System.out.println("Source: " + getInfo());
		System.out.println("********************************************");
		System.out.println("Attributes: ");
		for (Attribute p : getAttributes())
			p.print();
		System.out.print("Model: ");
		if (this.model != null) {
			System.out.println(model.getUri());
			this.model.print();
		}
	}

}
