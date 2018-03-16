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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.common.HttpMethods;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.model.Argument;
import edu.isi.karma.rep.model.ArgumentType;
import edu.isi.karma.rep.model.ClassAtom;
import edu.isi.karma.rep.model.IndividualPropertyAtom;
import edu.isi.karma.rep.model.Model;

public class WebService extends Source {
	
	static Logger logger = LoggerFactory.getLogger(WebService.class);

	private String method;
	private String address;
	private URL urlExample;
	private String operationName;

	private List<Attribute> inputAttributes;
	private List<Attribute> outputAttributes;

	private List<String> variables;

	private Model inputModel;
	private Model outputModel;
	
	HashMap<String, Attribute> attIdToAttMap;

	private HashMap<String, Attribute> hNodeIdToAttribute;

	public WebService(String id, URL urlExample) {
		super(id, URLManager.getOperationName(urlExample));
		this.urlExample= urlExample;
		this.hNodeIdToAttribute = new HashMap<>();
		this.variables = new ArrayList<>();
		this.inputAttributes = new ArrayList<>();
		this.outputAttributes = new ArrayList<>();
		this.attIdToAttMap = new HashMap<>();
	}
	
	public WebService(String id, String name, URL urlExample) {
		super(id, name);
		this.urlExample= urlExample;
		this.setMethod(HttpMethods.GET.name());
		this.urlExample = urlExample;
		this.inputAttributes = new ArrayList<>();
		this.outputAttributes = new ArrayList<>();
		this.attIdToAttMap = new HashMap<>();
	}
	
	public WebService(String id, String addressTemplate) {
		super(id);
		this.address= addressTemplate;
		this.inputAttributes = new ArrayList<>();
		this.outputAttributes = new ArrayList<>();
		this.attIdToAttMap = new HashMap<>();
	}
	
	public WebService(String id, String name, String addressTemplate) {
		super(id);
		this.setName(name);
		this.address= addressTemplate;
		this.inputAttributes = new ArrayList<>();
		this.outputAttributes = new ArrayList<>();
		this.attIdToAttMap = new HashMap<>();
	}

	public WebService(String id, String name, URL urlExample, String method) {
		super(id, name);
		this.urlExample = urlExample;
		this.setMethod(method);
		this.inputAttributes = new ArrayList<>();
		this.outputAttributes = new ArrayList<>();
		this.attIdToAttMap = new HashMap<>();
	}

	public String getOperationName() {
		if (operationName == null)
			this.operationName = URLManager.getOperationName(this.urlExample);
		
		return operationName;
	}

	
	public void setVariables(List<String> variables) {
		this.variables = variables;
	}

	/**
	 * This method takes a map of attribute Ids and their values and return the invocation URL. 
	 * If there are some mandatory attributes that are not provided in the input map, this function returns 
	 * them in missingAttributes.
	 * @param attIdToValue
	 * @param missingAttributes
	 * @return
	 */
	public String getPopulatedAddress(Map<String, String> attIdToValue, List<Attribute> missingAttributes) {
		String address = this.getAddress();
		String populatedAddress = address;
		
		if (missingAttributes == null)
			missingAttributes = new ArrayList<>();
		
		for (Attribute att : this.inputAttributes) {
			
			String attId = att.getId();
			
			String value = attIdToValue.get(att.getId());
			String groundedIn = att.getGroundedIn();

			// the input attribute is not in the url.
			if (groundedIn == null || groundedIn.trim().length() == 0) {
				logger.debug("The attribute " + attId + " grounding parameter is not specified.");
				continue;
			}
			
			if (value == null) { // input attribute is not in the input map
				
				if (att.getRequirement() == AttributeRequirement.MANDATORY ||
						// FIXME: later when we are able to model the attribute mandatory/optional,
						// we have to remove the next line. currently we consider every 
						// input attribute is a necessary
						att.getRequirement() == AttributeRequirement.NONE) {
					logger.debug("No value is given for the mandatory attribute " + attId);
					missingAttributes.add(att);
				} else {
					// remove the attribute from the url if it exists there.
					populatedAddress = populatedAddress.replaceAll("&" + att.getName() + "=", "");
					populatedAddress = populatedAddress.replaceAll(att.getName() + "=", "");
					populatedAddress = populatedAddress.replaceAll("\\{" + groundedIn.trim() + "\\}", "");
				}
			} else {

				logger.debug("att: " + attId);
				logger.debug("grounded in: " + groundedIn.trim());
				logger.debug("value: " + value.trim());

				populatedAddress = populatedAddress.replaceAll("\\{" + groundedIn.trim() + "\\}", value);
			}
			
		}
		return populatedAddress;
	}

	public Attribute getAttribute(String id) {
		return this.attIdToAttMap.get(id);
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

	public Attribute getInputAttributeByName(String name) {
		if (this.inputAttributes == null)
			return null;
		
		for (Attribute att : this.inputAttributes)
			if (att.getName().equalsIgnoreCase(name))
				return att;
		
		return null;
	}
	
	public Attribute getOutputAttributeByName(String name) {
		if (this.outputAttributes == null)
			return null;
		
		for (Attribute att : this.outputAttributes)
			if (att.getName().equalsIgnoreCase(name))
				return att;
		
		return null;
	}
	
	public HashMap<String, Attribute> gethNodeIdToAttribute() {
		return hNodeIdToAttribute;
	}
	
	public void sethNodeIdToAttribute(HashMap<String, Attribute> hNodeIdToAttribute) {
		this.hNodeIdToAttribute = hNodeIdToAttribute;
	}

	private void doGrounding() {
		String str = this.urlExample.toString();
		try {
			str = URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		
		if (str == null || str.length() == 0) {
			this.address = "";
			return;
		}
		
		if (this.inputAttributes == null) {
			this.address = str;
			return;
		}
		
		String params = "";
		String[] addressParts = str.split("\\?");
		if (addressParts.length == 2) params = addressParts[1];
		
		// This only works for Web APIs and not RESTful APIs
		for (int i = 0; i < this.inputAttributes.size(); i++) {
			String name = this.inputAttributes.get(i).getName();
			String groundVar = "p" + String.valueOf(i+1);
			int index = params.indexOf(name);
			String temp = params.substring(index);
			if (temp.indexOf("&") != -1)
				temp = temp.substring(0, temp.indexOf("&"));
			if (temp.indexOf("=") != -1)
				temp = temp.substring(temp.indexOf("=") + 1);

			params = params.replaceFirst(Pattern.quote(temp.trim()), "{" + groundVar + "}");
			this.inputAttributes.get(i).setGroundedIn(groundVar);
		}
		
		if (params.length() > 0)
			this.address = addressParts[0] + "?" + params;
		else
			this.address = str;

	}
	
	public void updateModel(DirectedWeightedMultigraph<Node, LabeledLink> treeModel) {
		
		if (treeModel == null)
			return;
		
		List<Node> inputAttributesNodes = new ArrayList<>();
		List<Node> outputAttributesNodes = new ArrayList<>();
		
		HashMap<String, Argument> vertexIdToArgument = new HashMap<>();

		this.hNodeIdToAttribute.clear();
		buildHNodeId2AttributeMapping();
		
		// set the rdf ids of all the vertices. The rdf id of leaf vertices are the attribute ids. 
		String hNodeId;
		for (Node n : treeModel.vertexSet()) {
			if (n instanceof ColumnNode) {
				logger.debug("Node " + n.getLocalId() + " is a column node.");
				hNodeId = ((ColumnNode)n).getHNodeId();
			} else {
				logger.debug("Node " + n.getLocalId() + " is an intermediate node.");
				String variableId = "v" + String.valueOf(variables.size() + 1);
				variables.add(variableId);
				vertexIdToArgument.put(n.getId(), new Argument(variableId, variableId, ArgumentType.VARIABLE));
				continue;
			}
			
			Attribute att = this.hNodeIdToAttribute.get(hNodeId);
			if (att == null) {
				logger.error("No attribute is associated to the column with semantic type " + n.getId());
				continue;
			}
			
			vertexIdToArgument.put(n.getId(), new Argument(att.getId(), att.getId(), ArgumentType.ATTRIBUTE));
			
			if (att.getIOType() == IOType.INPUT) {
				inputAttributesNodes.add(n);
			}
			if (att.getIOType() == IOType.OUTPUT) {
				outputAttributesNodes.add(n);
			}
		}

		
		List<String> inputModelVertexes = new ArrayList<>();
		List<String> inputModelEdges = new ArrayList<>();		
		
		Model inputModel = getInputModel(treeModel, inputAttributesNodes, 
				inputModelVertexes, inputModelEdges,
				vertexIdToArgument);
		
		this.setInputModel(inputModel);
		
		Model outputModel = getOutputModel(treeModel, 
				inputModelVertexes, inputModelEdges,
				vertexIdToArgument);
		
		this.setOutputModel(outputModel);
		
	}
	
	private Model getInputModel(DirectedWeightedMultigraph<Node, LabeledLink> treeModel, 
			List<Node> inputNodes, List<String> inputModelVertexes, List<String> inputModelEdges,
			HashMap<String, Argument> vertexIdToArgument) {

		if (treeModel == null)
			return null;
				
		logger.debug("compute the steiner tree from the alignment tree with input nodes as steiner nodes ...");
		UndirectedGraph<Node, LabeledLink> undirectedGraph =
				new AsUndirectedGraph<>(treeModel);
		List<Node> steinerNodes = inputNodes;
		SteinerTree steinerTree = new SteinerTree(GraphUtil.asDefaultGraph(undirectedGraph), steinerNodes);
		

		Model m = new Model("inputModel");
		if (steinerTree == null || steinerTree.getLabeledSteinerTree() == null)
			return m;
		
		for (Node n : steinerTree.getLabeledSteinerTree().vertexSet()) {
			
			inputModelVertexes.add(n.getId());
			
			if (n instanceof ColumnNode || n instanceof LiteralNode)
				continue;
			
			if (vertexIdToArgument.get(n.getId()) == null)
				continue;
			
			Label classPredicate = new Label(n.getLabel().getUri(), n.getLabel().getNs(), n.getLabel().getPrefix(),
					n.getLabel().getRdfsLabel(), n.getLabel().getRdfsComment());

			ClassAtom classAtom = new ClassAtom(classPredicate, vertexIdToArgument.get(n.getId()));
			m.getAtoms().add(classAtom);
		}
		
		for (LabeledLink e : steinerTree.getLabeledSteinerTree().edgeSet()) {

			inputModelEdges.add(e.getId());
			
			if (vertexIdToArgument.get(e.getSource().getId()) == null || 
					vertexIdToArgument.get(e.getTarget().getId()) == null)
				continue;
			
			Label propertyPredicate = new Label(e.getLabel().getUri(), e.getLabel().getNs(), e.getLabel().getPrefix(),
					e.getLabel().getRdfsLabel(), e.getLabel().getRdfsComment());
			IndividualPropertyAtom propertyAtom;
			
//			// has_subclass is from source to target, we substitute this with a rdfs:subClassOf from target to source
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

		return m;
	}

	private Model getOutputModel(DirectedWeightedMultigraph<Node, LabeledLink> treeModel, 
			List<String> inputModelVertexes, List<String> inputModelEdges,
			HashMap<String, Argument> vertexIdToArgument) {

		if (treeModel == null)
			return null;

		Model m = new Model("outputModel");
		
		for (Node n : treeModel.vertexSet()) {
			
			if (inputModelVertexes.indexOf(n.getId()) != -1)
				continue;
			
			if (n instanceof ColumnNode || n instanceof LiteralNode)
				continue;
			
			if (vertexIdToArgument.get(n.getId()) == null)
				continue;
			
			
			Label classPredicate = new Label(n.getLabel().getUri(), n.getLabel().getNs(), n.getLabel().getPrefix(),
					n.getLabel().getRdfsLabel(), n.getLabel().getRdfsComment());

			ClassAtom classAtom = new ClassAtom(classPredicate, vertexIdToArgument.get(n.getId()));
			m.getAtoms().add(classAtom);
		}
		
		for (LabeledLink e : treeModel.edgeSet()) {
			
			if (inputModelEdges.indexOf(e.getId()) != -1)
				continue;
			
			if (vertexIdToArgument.get(e.getSource().getId()) == null || 
					vertexIdToArgument.get(e.getTarget().getId()) == null)
				continue;
			
			Label propertyPredicate = new Label(e.getLabel().getUri(), e.getLabel().getNs(), e.getLabel().getPrefix(),
					e.getLabel().getRdfsLabel(), e.getLabel().getRdfsComment());
			IndividualPropertyAtom propertyAtom;
			
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
	
	@Override
	public void print() {
		System.out.println("********************************************");
		System.out.println("Service: " + getInfo());
		System.out.println("********************************************");
		System.out.println("Variables: ");
		if (this.variables != null) {
			for (String v : this.variables)
				System.out.print(v + ", ");
			System.out.println();
		}
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
