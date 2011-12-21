package edu.isi.karma.rdf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.LinkType;
import edu.isi.karma.modeling.alignment.NodeType;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.mediator.rdf.TableRDFGenerator;

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
	
	static Logger logger = Logger.getLogger(SourceDescription.class);

	/**
	 * @param factory
	 * @param steinerTree
	 * @param root
	 * 		the root of the Steiner tree.
	 */
	public SourceDescription(RepFactory factory, DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> steinerTree, Vertex root){
		this.factory=factory;
		this.steinerTree = steinerTree;
		this.root=root;
	}
	
	/**
	 * Generates a source description given a Steiner tree
	 * representation of the alignment.
	 * @return
	 * 		a source description.
	 * @throws KarmaException
	 */
	public String generateSourceDescription() throws KarmaException{
		StringBuffer s = new StringBuffer();
		generateSourceDescription(root, s);
		String rule =  "SourceDescription(";
		rule += ruleAttributes.toString().substring(1,ruleAttributes.toString().length()-1);
		rule += ") -> \n" + s.toString();
		String namespace = "s:'http://www.isi.edu/'";
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
	 * 1. for each outgoing edge e of v
	 * 2. if e is DataProperty, generateDataPropertyStatement (hasName(uri(Name), Name))
	 * 3. if e is ObjectProperty, generateObjectPropertyStatement (isAddress(uri(Name), uri(Address)))
	 * 4. if e is HasSubClass do nothing
	 * 5. generateSourceDescription(getEdgeTarget(e))
	 * 6. stop when v is DataProperty (leaves are reached)
	 */
	private void generateSourceDescription(Vertex v, StringBuffer s) throws KarmaException{
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
		String key = uriMap.get(v.getLabel());
		if(key==null){
			throw new KarmaException("Key for " + v.getLabel() + " is NULL. This should not happen!");
		}
		String dataAttribute = factory.getHNode(child.getSemanticType().getHNodeId()).getColumnName();
		ruleAttributes.add(dataAttribute);
		String propertyName = e.getLabel();
		if(e.isInverse())
			propertyName = TableRDFGenerator.inverseProperty + propertyName;
		String s = "`" + propertyName + "`(uri(" + key + ")," + dataAttribute + ")";
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
		String key1 = uriMap.get(v.getLabel());
		if(key1==null){
			throw new KarmaException("Key for " + v.getLabel() + " is NULL. This should not happen!");
		}
		String key2 = findKey(child);
		String propertyName = e.getLabel();
		if(e.isInverse())
			propertyName = TableRDFGenerator.inverseProperty + propertyName;
		String s = "`" + propertyName + "`(uri(" + key1 + "),uri(" + key2 + "))";
		//System.out.println("ObjectProperty:" + s);
		return s;
	}

	/**
	 * For a node that is a Class, find the key(column name) associated with this class.
	 * <br> If node is associated with a column, that column is the key.
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
		logger.debug("Get Key for " + v.getLabel() + " ...");
		String key = uriMap.get(v.getLabel());
		if(key!=null)
			return key;
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
						key = factory.getHNode(n.getSemanticType().getHNodeId()).getColumnName();
						ruleAttributes.add(key);
						break;
					}
				}
			}
			if(key==null){
				//I looked at all children and I did not find a key
				//generate gensym index
				key = String.valueOf(uriIndex++);
			}
		}
		else{
			key = factory.getHNode(v.getSemanticType().getHNodeId()).getColumnName();
			ruleAttributes.add(key);
		}
		uriMap.put(v.getLabel(), key);
		logger.debug("Key for " + v.getLabel() + " is " + key);
		return key;
	}
}
