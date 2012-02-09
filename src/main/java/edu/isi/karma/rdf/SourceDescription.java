package edu.isi.karma.rdf;

import java.util.HashMap;
import java.util.HashSet;
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
	 * List of attributes that appear in the rule.
	 * Will be added to the rules head.
	 */
	private HashSet<String> ruleAttributes = new HashSet<String>();
	
	/**
	 * A Map that contains indices used for gensym URIs.
	 * key=a class; value=the index used for the gensym uri (e.g., 1 for uri(1), 2 for uri(2)
	 */
	private HashMap<String, String> uriMap = new HashMap<String, String>();
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
			LabeledWeightedEdge> steinerTree, Vertex root, String rdfSourcePrefix, boolean generateInverse, 
			boolean useColumnNames){
		this.factory=factory;
		this.steinerTree = steinerTree;
		this.root=root;
		this.useColumnNames = useColumnNames;
		this.rdfSourcePrefix=rdfSourcePrefix;
		this.generateInverse = generateInverse;
		model = OntologyManager.Instance().getOntModel();
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
		//System.out.println("THE TREE");
		//GraphUtil.printGraph(steinerTree);
		
		StringBuffer s = new StringBuffer();
		generateSourceDescription(root, s);
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

		//see if this property has an inverse property, and if it does add that too
		ObjectProperty op = model.getObjectProperty(propertyName);
		OntProperty inverseProp = op.getInverse();

		String s = "`" + propertyName + "`(uri(" + key1 + "),uri(" + key2 + "))";
		if(inverseProp!=null && generateInverse){
			//add the inverse property
			s += " \n ^" + "`" + inverseProp + "`(uri(" + key2 + "),uri(" + key1 + "))";
		}
		
		if(e.isInverse()){
			//propertyName = TableRDFGenerator.inverseProperty + propertyName;
			s = "`" + propertyName + "`(uri(" + key2 + "),uri(" + key1 + "))";
			if(inverseProp!=null && generateInverse){
				//add the inverse property
				s += " \n ^" + "`" + inverseProp + "`(uri(" + key1 + "),uri(" + key2 + "))";
			}
		}

		//System.out.println("ObjectProperty:" + s);
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
		uriMap.put(v.getID(), key);
		//logger.info("Key for " + v.getID() + " is " + key);
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
