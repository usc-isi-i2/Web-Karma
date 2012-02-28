/**
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This code was developed by the Information Integration Group as part 
 *  of the Karma project at the Information Sciences Institute of the 
 *  University of Southern California.  For more information, publications, 
 *  and related projects, please see: http://www.isi.edu/integration
 *
 */

package edu.isi.karma.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.LinkType;
import edu.isi.karma.modeling.alignment.NodeType;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SynonymSemanticTypes;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.mediator.gav.util.MediatorUtil;

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
	private DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> steinerTree;
	/**
	 * The root of the Steiner tree.
	 */
	private Vertex root;
	
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

	/**
	 * All data property nodes. At the very end I check if any of the data property nodes (the leaves of the tree)
	 * have synonym sem types. If they do, I add them. I check this at the very end not during the tree traversal 
	 * because I want to make sure that I have computed all possible keys. I need the URIs of the classes at this point.
	 */
	private ArrayList<Vertex> dataProperties = new ArrayList<Vertex>();
	
	private int uriIndex = 0;
	
	/**
	 * The source prefix used during RDF generation.
	 */
	private String rdfSourcePrefix;
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
	 * the ontology model
	 */
	private OntModel model;
	
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
	public SourceDescription(RepFactory factory, DirectedWeightedMultigraph<Vertex, 
			LabeledWeightedEdge> steinerTree, Vertex root, Worksheet worksheet, String rdfSourcePrefix, boolean generateInverse, 
			boolean useColumnNames){
		this.factory=factory;
		this.steinerTree = steinerTree;
		this.root=root;
		this.useColumnNames = useColumnNames;
		this.rdfSourcePrefix=rdfSourcePrefix;
		this.generateInverse = generateInverse;
		model = OntologyManager.Instance().getOntModel();
		this.worksheet=worksheet;
	}
	
	/**
	 * Generates a source description given a Steiner tree
	 * representation of the alignment.
	 * @return
	 * 		a source description.
	 * @throws KarmaException
	 * For a table without nested tables the source description contains column names.
	 * SD(ColumnName1, ColumnName2)
	 * For a table with nested tables the source description contains HNodePaths.
	 * SD(HN1/HN2/HN3, HN1/HN2/HN4) - the ids of the HNodes.
	 */
	public String generateSourceDescription() throws KarmaException{
		System.out.println("THE TREE******************");
		//GraphUtil.printGraph(steinerTree);
		
		StringBuffer s = new StringBuffer();
		generateSourceDescription(root, s);
		//generate statements for synonym sem types
		//do this only at the end, so I make sure that I already have all keys computed
		String stmt = generateSynonymStatements();
		s.append(stmt);
		
		String rule =  "SourceDescription(";
		int i=0;
		for(String attr:ruleAttributes){
			if(i++>0) rule +=",";
			rule += addBacktick(attr);
		}
		rule += ") -> \n" + s.toString();
		if(rdfSourcePrefix==null)
			rdfSourcePrefix = "http://localhost:8080/";
		//add a delimiter if it doesn't exist, otherwise the URIs will not be well formed
		if(!rdfSourcePrefix.endsWith("/") && !rdfSourcePrefix.endsWith("#"))
			rdfSourcePrefix += "/";
		String namespace = "s:'" + rdfSourcePrefix + "'";
		String sourceDescription = "NAMESPACES:\n\n" + namespace + "\n\nLAV_RULES:\n\n" + rule;
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
	private void generateSourceDescription(Vertex v, StringBuffer s) throws KarmaException{
		//System.out.println("Generate SD for node:" + v.getLabel() + " type:" + v.getNodeType());
		if(v.getNodeType()==NodeType.Class){
			String stmt = generateClassStatement(v);
			if(s.length()!=0) s.append(" ^ ");
			s.append(stmt);
			Set<LabeledWeightedEdge> edges = steinerTree.outgoingEdgesOf(v);
			for(LabeledWeightedEdge e:edges){
				Vertex child = steinerTree.getEdgeTarget(e);
				if(e.getLinkType()==LinkType.DataProperty){
					//get the child node, which should be a DataProperty node
					if(child.getNodeType()!=NodeType.DataProperty){
						throw new KarmaException("Node " + child.getLabel() + " should be of type NodeType.DataProperty");
					}
					else{
						stmt = generateDataPropertyStatement(v,e,child);
						s.append("\n ^ " + stmt);
						//data property nodes can have synonym sem types
						//generate statements for the synonyms too
						//do this only at the end, so I make sure that I already have a keys computed
						dataProperties.add(child);
					}
				}
				else if(e.getLinkType()==LinkType.ObjectProperty){
					//get the child node, which should be a DataProperty node
					if(child.getNodeType()!=NodeType.Class){
						throw new KarmaException("Node " + child.getLabel() + " should be of type NodeType.Class");
					}
					else{
						stmt = generateObjectPropertyStatement(v,e,child);
						s.append("\n ^ " + stmt);
					}
				}
				else if(e.getLinkType()==LinkType.HasSubClass){
					//do nothing for this link
				}
				generateSourceDescription(child,s);
			}
		}
		else{
			//it is a DataProperty node, so I reached the leaves => do nothing
		}
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
	private String generateClassStatement(Vertex v) {
		String key = findKey(v);
		String s = "`" + v.getLabel() + "`(uri(" + key + "))"; 
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
	private String generateDataPropertyStatement(Vertex v,
			LabeledWeightedEdge e, Vertex child) throws KarmaException {
		if(child.getNodeType()!=NodeType.DataProperty){
			throw new KarmaException("Node " + child.getLabel()+ " should be of type NodeType.DataProperty");
		}
		if(e.getLinkType()!=LinkType.DataProperty){
			throw new KarmaException("Edge " + e.getLabel() + " should be of type LinkType.DataProperty");
		}
		if(v.getNodeType()!=NodeType.Class){
			throw new KarmaException("Node " + v.getLabel() + " should be of type NodeType.Class");
		}
		//find the key of Class v
		String key = findKey(v);
		if(key==null){
			throw new KarmaException("Key for " + v.getLabel() + " is NULL. This should not happen!");
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
		
		String dataAttribute = factory.getHNode(child.getSemanticType().getHNodeId()).getColumnName();
		if(!useColumnNames){
			dataAttribute = factory.getHNode(child.getSemanticType().getHNodeId()).getHNodePath(factory).toColumnNames();
		}
		ruleAttributes.add(dataAttribute);
		String propertyName = e.getLabel();
		if(e.isInverse()){
			throw new KarmaException("A data property cannot be an inverse_of:" + propertyName);
		}
		String s = "`" + propertyName + "`(uri(" + key + ")," + addBacktick(dataAttribute) + ")";
		//System.out.println("DataProperty:" + s);
		return s;
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
	private String generateObjectPropertyStatement(Vertex v,
			LabeledWeightedEdge e, Vertex child) throws KarmaException {
		if(v.getNodeType()!=NodeType.Class){
			throw new KarmaException("Node " + v.getLabel() + " should be of type NodeType.Class");
		}
		if(child.getNodeType()!=NodeType.Class){
			throw new KarmaException("Node " + child.getLabel() + " should be of type NodeType.Class");
		}
		if(e.getLinkType()!=LinkType.ObjectProperty){
			throw new KarmaException("Edge " + e.getLabel() + " should be of type LinkType.ObjectProperty");
		}
		//find the key of Class v
		String key1 = findKey(v);
		if(key1==null){
			throw new KarmaException("Key for " + v.getLabel() + " is NULL. This should not happen!");
		}
		String key2 = findKey(child);
		String propertyName = e.getLabel();

		String s = "`" + propertyName + "`(uri(" + key1 + "),uri(" + key2 + "))";
		s += addInverseProperty(propertyName, key1,key2);
		
		if(e.isInverse()){
			//propertyName = TableRDFGenerator.inverseProperty + propertyName;
			s = "`" + propertyName + "`(uri(" + key2 + "),uri(" + key1 + "))";
			s += addInverseProperty(propertyName, key2,key1);
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
		String s = "";
		//for each leaf of the tree
		for(Vertex child: dataProperties){
			SynonymSemanticTypes synonyms = worksheet.getSemanticTypes().getSynonymTypesForHNodeId(child.getSemanticType().getHNodeId());
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
	private String generateSynonymStatement(Vertex child, SemanticType st) {
		if(st.isClass()){
			//semantic type is a Class
			String key = findKey(child);
			String s = "`" + st.getType() + "`(uri(" + key + "))"; 
			return s;
		}
		else{
			//it's a data property
			//find the key of this property's domain
			String key = findKey(st.getDomain());
			
			String dataAttribute = factory.getHNode(child.getSemanticType().getHNodeId()).getColumnName();
			if(!useColumnNames){
				dataAttribute = factory.getHNode(child.getSemanticType().getHNodeId()).getHNodePath(factory).toColumnNames();
			}
			ruleAttributes.add(dataAttribute);
			String propertyName = st.getType();
			String s = "`" + propertyName + "`(uri(" + key + ")," + addBacktick(dataAttribute) + ")";
			//System.out.println("DataProperty:" + s);
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
		//see if this property has an inverse property, and if it does add it to the SD
		ObjectProperty op = model.getObjectProperty(propertyName);
		//one or the other will be null
		OntProperty inverseProp1 = op.getInverse();
		OntProperty inverseProp2 = op.getInverseOf();

		//logger.info("Inverse prop for " + propertyName + " is " + inverseProp1 + " " + inverseProp2);
		
		if(inverseProp1!=null && generateInverse){
			//add the inverse property
			s += " \n ^ " + "`" + inverseProp1 + "`(uri(" + key2 + "),uri(" + key1 + "))";
		}
		if(inverseProp2!=null && generateInverse){
			//add the inverse property
			s += " \n ^ " + "`" + inverseProp2 + "`(uri(" + key2 + "),uri(" + key1 + "))";
		}
		
		return s;
	}
	
	/**
	 * For a node that is a Class, find the key(column name) associated with this class.
	 * <br> If node is associated with a column, that column is the key (the column was mapped to a Class)
	 * <br> Else, look at all this node's children, and see which one is a key. If
	 * <br> no key is found generate a gensym URI (return a uri index for this class)
	 * @param v
	 * 		the node
	 * @return
	 * 		the key associated with this class, or a uri index (for generating gensym URIs) 
	 * 		if no key is found.
	 */
	private String findKey(Vertex v){
		//check if it is not in the map
		//I use the vertex IDs because there can be several columns mapped to the same
		//class, so we have to distinguish between the key for these classes
		//logger.info("Get Key for " + v.getLabel() + " with ID=" + v.getID());
		boolean isGensym=false;
		String key = uriMap.get(v.getID());
		if(key!=null){
			//logger.info("Key for " + v.getID() + " is " + key);
			return key;
		}
		//it's not there, so look for it
		//System.out.println("Semantic Type="+v.getSemanticType());
		if(v.getSemanticType()==null){
			//this node is not associated with a column
			//look in the child nodes to find the key to be used when generating the URI
			Set<LabeledWeightedEdge> edges = steinerTree.outgoingEdgesOf(v);
			for(LabeledWeightedEdge e:edges){
				//get the child node
				Vertex n = steinerTree.getEdgeTarget(e);
				if(n.getNodeType()==NodeType.DataProperty){
					//see if it is a key
					if(n.getSemanticType().isPartOfKey()){
						//it's a key
						//System.out.println("part of a key ... " + n.getLabel() + n.getID());
						if(useColumnNames){
							key = factory.getHNode(n.getSemanticType().getHNodeId()).getColumnName();
						}else{
							key=factory.getHNode(n.getSemanticType().getHNodeId()).getHNodePath(factory).toColumnNames();
						}
						ruleAttributes.add(key);
						break;
					}
				}
			}
			if(key==null){
				//I looked at all children and I did not find a key
				//generate gensym index
				key = String.valueOf(uriIndex++);
				isGensym=true;
			}
		}
		else{
			//the node is associated with a column => it's the key
			if(useColumnNames){
				key = factory.getHNode(v.getSemanticType().getHNodeId()).getColumnName();
			}else{
				key=factory.getHNode(v.getSemanticType().getHNodeId()).getHNodePath(factory).toColumnNames();				
			}
			ruleAttributes.add(key);
		}
		//I have to do it here because I don't want backticks for the gensyms
		if(!isGensym)
			key = addBacktick(key);
		
		classNameToId.put(v.getLabel(), v.getID());
		uriMap.put(v.getID(), key);
		//logger.info("Key for " + v.getID() + " is " + key);
		return key;
	}
	
	/**
	 * Returns the key for the given Class. If this Class is not present in the Steiner tree,
	 * return a gensym.
	 * @param domain
	 * 		a class.
	 * @return
	 * 		the key for the given Class.
	 */
	private String findKey(String domain){
		//get the id of a class node that corresponds to this class
		String id = classNameToId.get(domain);
		if(id==null){
			//no class found , so generate a gensym
			String gensym = String.valueOf(uriIndex++);
			return gensym;
		}
		String key = uriMap.get(id);
		if(key==null){
			//no class found , so generate a gensym
			String gensym = String.valueOf(uriIndex++);
			return gensym;			
		}
		return key;
	}
	
	private String addBacktick(String s){
		//this method has to be enhanced to add backtick if we have columns with
		//"strange" chars
		if(!useColumnNames)
			return MediatorUtil.addBacktick(s);
		else return s;
	}
}
