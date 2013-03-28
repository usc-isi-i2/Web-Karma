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

package edu.isi.karma.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SynonymSemanticTypes;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.mediator.gav.util.MediatorUtil;
import edu.isi.mediator.rdf.RDFDomainModel;

/**
 * Class that generates a source description given a Steiner tree 
 * representation of the alignment.
 *  
 * @author Maria Muslea(USC/ISI)
 *
 */
public class SourceDescription {

	private RepFactory factory;
	/**
	 * The Steiner tree.
	 */
	private DirectedWeightedMultigraph<Node, Link> steinerTree;
	/**
	 * The root of the Steiner tree.
	 */
	private Node root;
	
	/**
	 * Needed in order to get synonym semantic types.
	 */
	private Worksheet worksheet;
	
	/**
	 * List of attributes that appear in the rule.
	 * Will be added to the rules head.
	 */
	private HashSet<String> ruleAttributes = new HashSet<String>();
	
	/**
	 * A Map that contains indices used for gensym URIs.
	 * key=a class; value=the index used for the gensym uri (e.g., 1 for uri(1), 2 for uri(2)
	 */
	private HashMap<String, String> uriMap = new HashMap<String, String>();

	/**
	 * key=name of a class; value = the id of a vertex for that class; save just the first ID that you find for a class node
	 * this is needed for synonym semantic types where we just have the type without having the
	 * link in a steiner tree, so all we have are class names and property names. In order to generate
	 * a URI for a class I need to know the ID of a vertex in the steiner tree for that class, so I can get the key.
	 */
	private HashMap<String, String> classNameToId = new HashMap<String, String>();

	private int uriIndex = 0;
	
	/**
	 * The source prefix & namespace used during RDF generation.
	 */
	private String rdfSourcePrefix;
	private String rdfSourceNamespace;
		
	/**
	 * All prefix/namespaces combinations used in this SD.
	 * key=prefix:namespace; val=givenPrefix (to this prefix:namespace combination)
	 * If the same prefix appears again with a different 
	 * namespace assigned to it, prefix is renamed.
	 */
	HashMap<String, String> seenPrefixNamespaceCombination = new HashMap<String, String>();
	
	/**
	 * All assigned prefixes.
	 */
	ArrayList<String> assignedPrefixes = new ArrayList<String>();
	
	/**
	 * All namespaces used in this SD in format need for mediator domain model:
	 * prefix:'namespace'
	 */
	Set<String> allNamespaces = new HashSet<String>();

	/**
	 * If the same prefix appears again with a different 
	 * namespace assigned to it, prefix is renamed.
	 */
	private int prefixIndex = 0;
	

	/**
	 * true if column names should be used in the Rule; 
	 * false if HNodePath should be used instead of the column names.
	 */
	private boolean useColumnNames = false;
	
	/**
	 * if equals to true the SD will contain all inverse properties 
	 * for the properties present in the alignment;
	 * add this: 
		OntProperty inverseProp = model.getObjectProperty(propertyName).getInverse();
	 */
	private boolean generateInverse = true;
	
	/**
	 * the ontology manager
	 */
	private OntologyManager ontMgr;
	/**
	 * List of properties inverted
	 */
	Set<String> reversedLinkIds;
	
	static Logger logger = Logger.getLogger(SourceDescription.class);

	/**
	 * @param factory
	 * @param steinerTree
	 * @param root
	 * 		the root of the Steiner tree.
	 * @param generateInverse
	 * 		true - add all inverse properties to the SD
	 * 		false - do not add inverse properties to the SD;
	 * 				SD will contain ONLY properties defined in the alignment
	 * @param useColumnNames
	 * 		true if column names should be used in the Rule
	 * 		false if HNodePath should be used instead of the column names.
	 * 
	 * <br>If the SD will be used "outside" of Karma to generate RDF from a datasource use 
	 * useColumnNames=true as the SD should contain column names that are in the
	 * datasource to be modeled.
	 * <br>useColumnNames=false if the SD is used internally.
	 */
	public SourceDescription(Workspace workspace, Alignment alignment, Worksheet worksheet, String sourcePrefix,String sourceNamespace, boolean generateInverse, 
			boolean useColumnNames){
		
		//the tree is not directed anymore, so we have to transform it before we can use it
		DirectedWeightedMultigraph<Node, Link> sTree = alignment.getSteinerTree();
//      Mohsen: I apply the updateLinkDirection() before returning the Steiner tree.
//		@SuppressWarnings("unchecked")
//		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> treeClone = (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>) sTree.clone();
//		// Reversing the inverse links
//		alignment.updateLinksDirections(alignment.GetTreeRoot(), null, treeClone);

		reversedLinkIds = new HashSet<String>();
		this.factory=workspace.getFactory();
		this.root=alignment.GetTreeRoot();
		this.steinerTree=edu.isi.karma.modeling.alignment.GraphUtil.treeToRootedTree(sTree, this.root, reversedLinkIds);
		this.useColumnNames = useColumnNames;
		this.rdfSourcePrefix=sourcePrefix;
		this.rdfSourceNamespace=sourceNamespace;
		this.generateInverse = generateInverse;
		ontMgr = workspace.getOntologyManager();
		this.worksheet=worksheet;
		
		//add source prefix
		if(rdfSourceNamespace==null)
			rdfSourceNamespace = "http://localhost:8080/";
		if(rdfSourcePrefix==null)
			rdfSourcePrefix = "s";
		//add a delimiter if it doesn't exist, otherwise the URIs will not be well formed
		if(!rdfSourceNamespace.endsWith("/") && !rdfSourceNamespace.endsWith("#"))
			rdfSourceNamespace += "/";
		seenPrefixNamespaceCombination.put(rdfSourcePrefix+ ":"+rdfSourceNamespace,rdfSourcePrefix);
		assignedPrefixes.add(rdfSourcePrefix);
		allNamespaces.add(RDFDomainModel.SOURCE_PREFIX + rdfSourcePrefix+ ":'"+rdfSourceNamespace+"'");
	}
	
	/**
	 * Generates a source description given a Steiner tree
	 * representation of the alignment.
	 * @return
	 * 		a source description.
	 * @throws KarmaException
	 * For a table without nested tables the source description contains column names.
	 * SD(ColumnName2, ColumnName3)
	 * For a table with nested tables the source description contains HNodePaths (transformed in ColumnNames)
	 * SD(ColumnName1/ColumnName2, ColumnName1/ColumnName3) - the ids of the HNodes.
	 */
	public String generateSourceDescription() throws KarmaException{
		//System.out.println("THE TREE******************");
		//GraphUtil.printGraph(steinerTree);
		
		StringBuffer s = new StringBuffer();
		generateSourceDescription(root, s);
		//generate statements for synonym sem types
		//do this only at the end, so I make sure that I already have all keys computed
		String stmt = generateSynonymStatements();
		if(stmt!=null)
			s.append(stmt);
		
		String rule =  "SourceDescription(";
		int i=0;
		for(String attr:ruleAttributes){
			if(i++>0) rule +=",";
			rule += addBacktick(attr);
		}
		rule += ") -> \n" + s.toString();

		String sourceDescription = "NAMESPACES:\n\n" + getAllNamespaces() + "\n\nLAV_RULES:\n\n" + rule;
		logger.debug("SourceDescription:\n"+sourceDescription);
		return sourceDescription;
	}
	
	/**
	 * Recursively generates a source description given the root of a Steiner tree.
	 * @param v
	 * 		the root of a Steiner tree.
	 * @param s
	 * 		the string that represents the source description (start with empty string).
	 * @throws KarmaException
	 * The Algorithm:
	 * 1. While v is a Class nodes
	 * 2. generate a class statement (generateClassStatement(v) - Person(uri(Name)))
	 * 2. for each outgoing edge e of v
	 * 3. if e is DataProperty, generateDataPropertyStatement (hasName(uri(Name), Name))
	 * 4. if e is ObjectProperty, generateObjectPropertyStatement (isAddress(uri(Name), uri(Address)))
	 * 5. if e is HasSubClass do nothing
	 * 6. generateSourceDescription(getEdgeTarget(e))
	 * 7. stop when v is DataProperty (leaves are reached)
	 */
	private void generateSourceDescription(Node v, StringBuffer s) throws KarmaException{
		//System.out.println("Generate SD for node:" + v.getUri() + " type:" + v.getType());
		if(v==null){
			throw new KarmaException("Align source before generating SourceDescription.");
		}
		if(v.getType()==NodeType.InternalNode){
			String stmt = generateClassStatement(v);
			if(s.length()!=0) s.append(" ^ ");
			s.append(stmt);
			Set<Link> edges = steinerTree.outgoingEdgesOf(v);
			for(Link e:edges){
				Node child = steinerTree.getEdgeTarget(e);
				if(e.getType()==LinkType.DataPropertyLink){
					//get the child node, which should be a DataProperty node
					if(child.getType()!=NodeType.ColumnNode){
						throw new KarmaException("Node " + child.getId() + " should be of type NodeType.ColumnNode");
					}
					else{
						stmt = generateDataPropertyStatement(v,e,child);
						s.append("\n ^ " + stmt);
					}
				} else if(e.getType()==LinkType.ObjectPropertyLink){
					//get the child node, which should be a Internal class instance node
					if(child.getType()!=NodeType.InternalNode){
						throw new KarmaException("Node " + child.getId() + " should be of type NodeType.InternalNode");
					}
					else{
						stmt = generateObjectPropertyStatement(v,e,child);
						s.append("\n ^ " + stmt);
					}
				} else if(e.getType()==LinkType.SubClassLink){
					//I have to include this, otherwise I lose the "connection" between the classes
					stmt = generateObjectPropertyStatement(v,e,child);
					s.append("\n ^ " + stmt);
				} else if (e.getType() == LinkType.ClassInstanceLink) {
//					// ^ `expand@Email`(uri(`expand@Title_URI`),uri(PersonID))    -----------> URI + property
//					stmt = generateClassInstanceExpandStatement(v,e,child);
//					s.append("\n ^ " + stmt);
					
				} else if (e.getType() == LinkType.DataPropertyOfColumnLink) {
					stmt = generateDataPropertyOfColumnExpandStatement(v,e,child);
					s.append("\n ^ " + stmt);
				} else if (e.getType() == LinkType.ObjectPropertySpecializationLink) {
					stmt = generateObjectPropertySpecializationExpandStatement(v,e,child);
					s.append("\n ^ " + stmt);
				} else if (e.getType() == LinkType.ColumnSubClassLink) {
					stmt = generateColumnSubClassLinkExpandStatement(v,e,child);
					s.append("\n ^ " + stmt);
					// Do nothing. It is taken care of in the findKey method
				}
				generateSourceDescription(child,s);
			}
		}
		else{
			//it is a DataProperty node, so I reached the leaves => do nothing
		}
	}
	
	private String generateObjectPropertySpecializationExpandStatement(Node v,
			Link e, Node child) throws KarmaException {
		if(child.getType()!=NodeType.ColumnNode){
			throw new KarmaException("Node " + child.getLabel().getUri()+ " should be of type NodeType.ColumnNode");
		}
		
		//find the key of Class v
		String key1 = findKey(v);
		if(key1 == null){
			throw new KarmaException("Key for " + v.getLabel().getUri() + " is NULL. This should not happen!");
		}
		
		ObjectPropertySpecializationLink oLink = (ObjectPropertySpecializationLink) e;
		Link specializedLink = oLink.getSpecializedLink();
		String key2 = findKey(specializedLink.getTarget());
		if(key2 == null){
			throw new KarmaException("Key for " + specializedLink.getTarget().getLabel().getUri() + " is NULL. This should not happen!");
		}
		
		String hNodeId = ((ColumnNode) child).getHNodeId();
		String propertyAttribute = factory.getHNode(hNodeId).getColumnName();
		if(!useColumnNames){
			propertyAttribute = factory.getHNode(hNodeId).getHNodePath(factory).toColumnNamePath();
		}
		String propertyName = "expand@" + propertyAttribute;
		
		return "`" + propertyName + "`(uri(" + key1 + "),uri(" + key2 + "))";
	}

	// `expand@PositionType`(uri(PersonID))    ---------> subclass
	private String generateColumnSubClassLinkExpandStatement(Node v, Link e,
			Node child) throws KarmaException {
		if(child.getType()!=NodeType.ColumnNode){
			throw new KarmaException("Node " + child.getLabel().getUri()+ " should be of type NodeType.ColumnNode");
		}
		
		//find the key of Class v
		String key = findKey(v);
		if(key==null){
			throw new KarmaException("Key for " + v.getLabel().getUri() + " is NULL. This should not happen!");
		}
		
		String hNodeId = ((ColumnNode) child).getHNodeId();
		String dataAttribute = factory.getHNode(hNodeId).getColumnName();
		if(!useColumnNames){
			dataAttribute = factory.getHNode(hNodeId).getHNodePath(factory).toColumnNamePath();
		}
		ruleAttributes.add(dataAttribute);
		
		if(reversedLinkIds.contains(e.getId())){
			throw new KarmaException("A data property cannot be an inverse_of:" + e.getLabel().getUri());
		}
		String s = "`expand@" + dataAttribute + "`(uri(" + key + "))";
		return s;
		
	}

	// ^ `expand@Title`(uri(PersonID),Title)     --------------> property
	private String generateDataPropertyOfColumnExpandStatement(Node v, Link e,
			Node child) throws KarmaException {
		if(child.getType()!=NodeType.ColumnNode){
			throw new KarmaException("Node " + child.getLabel().getUri()+ " should be of type NodeType.ColumnNode");
		}
		
		//find the key of Class v
		String key = findKey(v);
		if(key==null){
			throw new KarmaException("Key for " + v.getLabel().getUri() + " is NULL. This should not happen!");
		}
		
		DataPropertyOfColumnLink dLink = (DataPropertyOfColumnLink) e;
		String hNodeId = ((ColumnNode) child).getHNodeId();
		String specializedHNodeId = dLink.getSpecializedColumnHNodeId();
		String dataAttribute = factory.getHNode(specializedHNodeId).getColumnName();
		String propertyAttribute = factory.getHNode(hNodeId).getColumnName();
		String rdfLiteralType = ((ColumnNode) child).getRdfLiteralType(); 
		if(!useColumnNames){
			dataAttribute = factory.getHNode(specializedHNodeId).getHNodePath(factory).toColumnNamePath();
			propertyAttribute = factory.getHNode(hNodeId).getHNodePath(factory).toColumnNamePath();
		}
		ruleAttributes.add(dataAttribute);
		String propertyName = "expand@" + propertyAttribute;
		if (rdfLiteralType != null && !rdfLiteralType.equals(""))
			propertyName += "@@" + rdfLiteralType; 
		if(reversedLinkIds.contains(e.getId())){
			throw new KarmaException("A data property cannot be an inverse_of:" + e.getLabel().getUri());
		}
		String s = "`" + propertyName + "`(uri(" + key + ")," + addBacktick(dataAttribute) + ")";
		return s;
	}


	/**
	 * Generates a unary predicate for a Class.
	 * <br> Finds the key for this Class, and uses that key to create the URI.
	 * @param v
	 * 		a Class node.
	 * @return
	 * 		a unary predicate for a Class.
	 * Example: Person(uri(Name)) or Person(uri(1))
	 */
	private String generateClassStatement(Node v) {
		String key = findKey(v);
		String s = "`" + getPrefix(v.getLabel().getPrefix(), v.getLabel().getNs()) + ":" + v.getLabel().getLocalName() + "`(uri(" + key + "))"; 
		//System.out.println("Class:" + s);
		return s;
	}

	/**
	 * Generates a binary predicate for a DataProperty.
	 * @param v
	 * 		the root node that corresponds to the class
	 * @param e
	 * 		the edge between the nodes (of type DataProperty)
	 * @param child
	 * 		the child node of type DataProperty
	 * @return
	 * 		a binary predicate for a DataProperty.
	 * <br>EXample: hasName(uri(Name), Name)
	 * @throws KarmaException 
	 */
	private String generateDataPropertyStatement(Node v,
			Link e, Node child) throws KarmaException {
		if(child.getType()!=NodeType.ColumnNode){
			throw new KarmaException("Node " + child.getLabel().getUri()+ " should be of type NodeType.ColumnNode");
		}
		if(e.getType()!=LinkType.DataPropertyLink){
			throw new KarmaException("Edge " + e.getLabel().getUri() + " should be of type LinkType.DataPropertyLink");
		}
		if(v.getType()!=NodeType.InternalNode){
			throw new KarmaException("Node " + v.getLabel().getUri() + " should be of type NodeType.InternalNode");
		}
		//find the key of Class v
		String key = findKey(v);
		if(key==null){
			throw new KarmaException("Key for " + v.getLabel().getUri() + " is NULL. This should not happen!");
		}
		
		////////
		/*
		String tableId = factory.getHNode(child.getSemanticType().getHNodeId()).getHTableId();
		HTable t = factory.getHTable(tableId);
		List<HNodePath> ps = t.getAllPaths();
		for(HNodePath p:ps){
			System.out.println("PATH:"+p.toString());
		}
		*/
		/////////
		String hNodeId = ((ColumnNode) child).getHNodeId();
		String rdfLiteralType = ((ColumnNode) child).getRdfLiteralType(); 
		String dataAttribute = factory.getHNode(hNodeId).getColumnName();
		//System.out.println("COLUMN name='"+dataAttribute+"'");
		if(!useColumnNames){
			dataAttribute = factory.getHNode(hNodeId).getHNodePath(factory).toColumnNamePath();
		}
		ruleAttributes.add(dataAttribute);
		String propertyName = getPrefix(e.getLabel().getPrefix(), e.getLabel().getNs()) + ":" + e.getLabel().getLocalName();
		if(reversedLinkIds.contains(e.getId())){
			throw new KarmaException("A data property cannot be an inverse_of:" + e.getLabel().getUri());
		}
		if (rdfLiteralType != null && !rdfLiteralType.equals("")) {
			return "`" + propertyName + "@@" + rdfLiteralType + "`(uri(" + key + ")," + addBacktick(dataAttribute) + ")";
		} else {
			return "`" + propertyName + "`(uri(" + key + ")," + addBacktick(dataAttribute) + ")";
		}
	}

	/**
	 * Generates a binary predicate for a ObjectProperty.
	 * @param v
	 * 		the root node.
	 * @param e
	 * 		the edge between the nodes (of type ObjectProperty)
	 * @param child
	 * 		the child node of type Class
	 * @return
	 * 		a binary predicate for a ObjectProperty.
	 * <br>EXample: isAddress(uri(Name), uri(Address))
	 * @throws KarmaException 
	 */
	private String generateObjectPropertyStatement(Node v,
			Link e, Node child) throws KarmaException {
		if(v.getType()!=NodeType.InternalNode){
			throw new KarmaException("Node " + v.getLabel().getUri() + " should be of type NodeType.Class");
		}
		if(child.getType()!=NodeType.InternalNode){
			throw new KarmaException("Node " + child.getLabel().getUri() + " should be of type NodeType.Class");
		}
		if(e.getType()!=LinkType.ObjectPropertyLink && e.getType()!=LinkType.SubClassLink){
			throw new KarmaException("Edge " + e.getLabel().getUri() + " should be of type LinkType.ObjectProperty");
		}
		//find the key of Class v
		String key1 = findKey(v);
		if(key1==null){
			throw new KarmaException("Key for " + v.getLabel().getUri() + " is NULL. This should not happen!");
		}
		String key2 = findKey(child);
		String propertyName = getPrefix(e.getLabel().getPrefix(), e.getLabel().getNs()) + ":" + e.getLabel().getLocalName();

		String s = "`" + propertyName + "`(uri(" + key1 + "),uri(" + key2 + "))";
		if(generateInverse)
			s += addInverseProperty(e.getLabel().getUri(), key1,key2);
		
		if(reversedLinkIds.contains(e.getId())){
			//propertyName = TableRDFGenerator.inverseProperty + propertyName;
			s = "`" + propertyName + "`(uri(" + key2 + "),uri(" + key1 + "))";
			s += addInverseProperty(e.getLabel().getUri(), key2,key1);
		}

		//System.out.println("ObjectProperty:" + s);
		return s;
	}

	/**
	 * Given all the data property nodes, computes the statements for synonym semantic types.
	 * @return
	 * 	the statements for synonym semantic types.
	 */
	private String generateSynonymStatements() {
		//System.out.println("Generate synonym statements ....");
		String s = "";
		//for each leaf of the tree
		for(Node child: steinerTree.vertexSet()){
			//System.out.println("Get semantic type for " + child.getLocalLabel());
			if(!(child instanceof ColumnNode)){
				//only if the node id mapped to a column it will have a semantic type
				//System.out.println("No semantic type for " + child.getLocalLabel());
				continue;
			}
			SynonymSemanticTypes synonyms = worksheet.getSemanticTypes().getSynonymTypesForHNodeId(((ColumnNode)child).getHNodeId());
			//System.out.println("Syn for " + factory.getHNode(child.getSemanticType().getHNodeId()).getColumnName() + " is " + synonyms);
			if(synonyms!=null){
				List<SemanticType> semT = synonyms.getSynonyms();
				for(SemanticType st: semT){
					String stmt = generateSynonymStatement(child,st);
					if(stmt!=null)
						s += "\n ^ " + stmt;
				}
			}
		}
		if(s.trim().isEmpty())
			return null;
		else return s;
	}


	/**
	 * Returns the statement for one semantic type.
	 * @param child
	 * 		a DataProperty node in the tree.
	 * @param st
	 * 		a SemanticType
	 * @return
	 */
	private String generateSynonymStatement(Node child, SemanticType st) {
		ColumnNode columnNode = (ColumnNode) child;
		String rdfLiteralType = ((ColumnNode) child).getRdfLiteralType();
		
		if(st.isClass()){
			String s = "";
			//semantic type is a Class
			String key = findKey(child);
			//logger.info("Sem Type is a class:"+st.getType());
			String className = getPropertyWithPrefix(st.getType());
			if (rdfLiteralType != null && !rdfLiteralType.equals(""))
				s = "`" + className + "@@" + rdfLiteralType + "`(uri(" + key + "))";
			else
				s = "`" + className + "`(uri(" + key + "))";
			return s;
		}
		else{
			String domainClass = st.getDomain().getUri();
			logger.info("Domain=" + domainClass);
			String s = "";
			String id = classNameToId.get(domainClass);
			if(id==null){
				//there is no class statement for this class (it is a class used in synonym property but nowhere else)
				//this class is not present in the Steiner tree
				//generate a gensym key
				String key = String.valueOf(uriIndex++);
				uriMap.put(domainClass,key);
				//I don't have an id for this class because I don't have a vertex in the graph for it
				classNameToId.put(domainClass,domainClass);
				id=st.getDomain().getUri();
				//add a class statement for this class
				s += "`" + domainClass + "`(uri(" + key + ")) \n ^ "; 
			}
			//it's a data property
			//find the key of this property's domain
			String key = uriMap.get(id);
			
			String dataAttribute = factory.getHNode(columnNode.getHNodeId()).getColumnName();
			if(!useColumnNames){
				dataAttribute = factory.getHNode(columnNode.getHNodeId()).getHNodePath(factory).toColumnNamePath();
			}
			ruleAttributes.add(dataAttribute);
			String propertyName = getPropertyWithPrefix(st.getType());
			if (rdfLiteralType != null && !rdfLiteralType.equals(""))
				s = "`" + propertyName + "@@" + rdfLiteralType + "`(uri(" + key + ")," + addBacktick(dataAttribute) + ")";
			else
				s = "`" + propertyName + "`(uri(" + key + ")," + addBacktick(dataAttribute) + ")";
			return s;
		}
	}

	/** Returns a binary predicate corresponding to the inverse of propertyName, if the inverse exists.
	 * @param propertyName
	 * 		a property name
	 * @param key1
	 * 		value used for second URI
	 * @param key2
	 * 		value used for first URI
	 * @return
	 * 		a binary predicate corresponding to the inverse of propertyName, if the inverse exists.
	 * Example: inversePredicate(uri(key2), uri(key1))
	 */
	private String addInverseProperty(String propertyName, String key1, String key2){
		String s = "";
		
		//this can happen if propertyName is not an Object property; it could be a subclass
		if (!ontMgr.isObjectProperty(propertyName))
			return s;
		//see if this property has an inverse property, and if it does add it to the SD

		//one or the other will be null
		Label inversePropLabel = ontMgr.getInverseProperty(propertyName);
		Label inverseOfPropLabel = ontMgr.getInverseOfProperty(propertyName);

		//logger.info("Inverse prop for " + propertyName + " is " + inverseProp1 + " " + inverseProp2);
		
		if(inversePropLabel!=null && generateInverse){
			//add the inverse property
			String prop = getPropertyWithPrefix(inversePropLabel);
			s += " \n ^ " + "`" + prop + "`(uri(" + key2 + "),uri(" + key1 + "))";
		}
		if(inverseOfPropLabel!=null && generateInverse){
			//add the inverse property
			String prop = getPropertyWithPrefix(inverseOfPropLabel);
			s += " \n ^ " + "`" + prop + "`(uri(" + key2 + "),uri(" + key1 + "))";
		}
		
		return s;
	}
	
	/** Finds the namespace & prefix for this property, adds namespace to namespaces map,
	 * and returns "prefix:propertyName".
	 * @param prop
	 * 		a property
	 * @return
	 */
	private String getPropertyWithPrefix(Label label){
		if(label==null)
			return null;
		String namespace = label.getNs();
		String prefix = label.getPrefix();
		String propWithPrefix = getPrefix(prefix, namespace) + ":" + label.getLocalName();
		return propWithPrefix;
	}
	
	/**
	 * For a node that is a Class, find the key(column name) associated with this class.
	 * <br> If node is associated with a column, that column is the key (the column was mapped to a Class)
	 * <br> Else, look at all this node's children, and see which one is a key. If
	 * <br> no key is found generate a gensym URI (return a uri index for this class)
	 * <br> A key can also be a combination of several columns (compound key). In this case we generate a 
	 * <br> concat("_",col1,col2,...), so the uri will be uri(concat("_",col1,col2,...))
	 * @param v
	 * 		the node
	 * @return
	 * 		the key associated with this class, or a uri index (for generating gensym URIs) 
	 * 		if no key is found.
	 */
	private String findKey(Node v){
		//check if it is not in the map
		//I use the vertex IDs because there can be several columns mapped to the same
		//class, so we have to distinguish between the key for these classes
		//logger.info("Get Key for " + v.getUri() + " with ID=" + v.getID());
		boolean isGensym=false;
		boolean isCompoundKey=false;
		String key = uriMap.get(v.getId());
		if(key!=null){
			//logger.info("Key for " + v.getID() + " is " + key);
			return key;
		}
		//it's not there, so look for it
		//System.out.println("Semantic Type="+v.getSemanticType());
		if(!(v instanceof ColumnNode)){
			//this node is not associated with a column
			//look in the child nodes to find the key to be used when generating the URI
			Set<Link> edges = steinerTree.outgoingEdgesOf(v);
			//I could have more than 1 key
			List<String> keys = new ArrayList<String>();
			for(Link e:edges){
				if (e.getType() == LinkType.ClassInstanceLink) {
					keys.clear();
					//get the child node
					Node n = steinerTree.getEdgeTarget(e);
					if(n.getType()==NodeType.ColumnNode){
						String hNodeId = ((ColumnNode) n).getHNodeId();
						if(useColumnNames){
							key = factory.getHNode(hNodeId).getColumnName();
						}else{
							key = factory.getHNode(hNodeId).getHNodePath(factory).toColumnNamePath();
						}
						ruleAttributes.add(key);
						keys.add("expand@" + key);
					}
					break;
				}
				//get the child node
				Node n = steinerTree.getEdgeTarget(e);
				if(n.getType()==NodeType.ColumnNode){
					String hNodeId = ((ColumnNode) n).getHNodeId();
					//see if it is a key
					if(e.getKeyType() == LinkKeyInfo.PartOfKey){
						//it's a key
						//System.out.println("part of a key ... " + n.getUri() + n.getID());
						if(useColumnNames){
							key = factory.getHNode(hNodeId).getColumnName();
						}else{
							key=factory.getHNode(hNodeId).getHNodePath(factory).toColumnNamePath();
						}
						ruleAttributes.add(key);
						keys.add(key);
					}
				}
			}
			if(keys.isEmpty()){
				//I looked at all children and I did not find a key
				//generate gensym index
				key = String.valueOf(uriIndex++);
				isGensym=true;
			}
			else if(keys.size()==1){
				//I only have 1 key
				key=keys.get(0);
			}
			else{
				//I have more than 1 key, so I have to construct a concat statement that will be the key
				key = "";
				for(int i=0; i<keys.size();i++){
					if(i>0) key+=",";
					key += addBacktick(keys.get(i));
				}
				isCompoundKey=true;
			}
		} else{
			String hNodeId = ((ColumnNode) v).getHNodeId();
			//the node is associated with a column => it's the key
			if(useColumnNames){
				key = factory.getHNode(hNodeId).getColumnName();
			}else{
				key=factory.getHNode(hNodeId).getHNodePath(factory).toColumnNamePath();				
			}
			ruleAttributes.add(key);
		}
		//I have to do it here because I don't want backticks for the gensyms
		if(!isGensym && !isCompoundKey)
			key = addBacktick(key);
		
		classNameToId.put(v.getLabel().getUri(), v.getId());
		uriMap.put(v.getId(), key);
		//logger.info("Key for " + v.getID() + " is " + key);
		return key;
	}
		
	/**
	 * Returns all namespaces used in the SD in the format needed by the domain model.
	 * @return
	 * 		all namespaces used in the SD in the format needed by the domain model.
	 */
	private String getAllNamespaces(){
		String allNs="";
		Iterator<String> namespace = allNamespaces.iterator();
		while(namespace.hasNext()){
			String oneNs = namespace.next();
			String ns = "\n" + oneNs;
			allNs += ns;
		}

		return allNs;
	}
	
	/**
	 * Returns the prefix of the given namespace.
	 * @param prefix
	 * 		the prefix of the namespace given as argument.
	 * @param namespace
	 * 		the namespace that corresponds to the prefix given as argument
	 * @return
	 * 		the prefix of the given namespace.
	 * 1. if prefix is empty assign it a value "ont0"
	 * 2. if prefix:namespace combination already has a namespace assigned, return it
	 *    else if prefix was already assigned, generate new prefix
	 *         else return prefix
	 * (we do this to ensure that we don't have the same prefix mapped to different namespaces; this
	 * can happen if we import several ontologies, with overlapping prefix names)
	 */
	private String getPrefix(String prefix, String namespace){
		String fullName = prefix + ":" + namespace;

		if(prefix==null || prefix.trim().isEmpty()){
			//generate a prefix
			prefix = "ont0";				
		}
		
		String givenPrefix = seenPrefixNamespaceCombination.get(fullName);
		if(givenPrefix==null){
			//see if prefix was already assigned to a namespace
			if(assignedPrefixes.contains(prefix)){
				//was already assigned => rename the input prefix
				String newPref = prefix + (prefixIndex++);
				seenPrefixNamespaceCombination.put(fullName, newPref);
				assignedPrefixes.add(newPref);
				allNamespaces.add(newPref+":'"+namespace+"'");
				return newPref;
			}
			else{
				//was not assigned, so I can assign it
				seenPrefixNamespaceCombination.put(fullName, prefix);
				assignedPrefixes.add(prefix);
				allNamespaces.add(prefix+":'"+namespace+"'");
				return prefix;
			}
		}
		else return givenPrefix;

	}

	private String addBacktick(String s){
		//this method has to be enhanced to add backtick if we have columns with
		//"strange" chars
		if(!useColumnNames)
			return MediatorUtil.addBacktick(s);
		else return s;
	}
	
}
