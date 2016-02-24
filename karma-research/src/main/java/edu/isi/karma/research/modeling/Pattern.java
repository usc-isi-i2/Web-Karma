package edu.isi.karma.research.modeling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.util.RandomGUID;

public class Pattern {
	
	static Logger logger = LoggerFactory.getLogger(Pattern.class);

	private String id;
	private int length; 
	private int frequency; // number of times this pattern appears in LOD
	private List<String> types;
	private DirectedWeightedMultigraph<Node, LabeledLink> graph;
	private NodeIdFactory nodeIdFactory;
	
	private Pattern(String id, 
			int length, 
			int frequency, 
//			List<String> types,
			DirectedWeightedMultigraph<Node, LabeledLink> graph,
			NodeIdFactory nodeIdFactory) {
		this.id = id;
		this.length = length;
		this.frequency = frequency;
//		this.types = types;
		this.graph = graph;
		this.nodeIdFactory = nodeIdFactory;
	}

	public Pattern(int length, 
			int frequency, 
//			List<String> types,
			DirectedWeightedMultigraph<Node, LabeledLink> graph,
			NodeIdFactory nodeIdFactory) {
		this.id = "p" + length + new RandomGUID().toString();
		this.length = length;
		this.frequency = frequency;
//		this.types = types;
		this.graph = graph;
		this.nodeIdFactory = nodeIdFactory;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Pattern p = (Pattern) obj;
        return this.id.equals(p.getId());
    }
    
    @Override
    public int hashCode() {
    	return this.getId().hashCode();
    }
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public List<String> getTypes() {
		return types;
	}
	public void setTypes(List<String> types) {
		this.types = types;
	}
	public DirectedWeightedMultigraph<Node, LabeledLink> getGraph() {
		return graph;
	}
	public void setGraph(DirectedWeightedMultigraph<Node, LabeledLink> graph) {
		this.graph = graph;
	}
	public NodeIdFactory getNodeIdFactory() {
		return nodeIdFactory;
	}

	public String toSparql(String graphIRI) {
		if (this.length == 1)
			return this.toCountSparql(graphIRI);
		else
			return this.toExistsSparql(graphIRI);
	}
	
	public String toCountSparql(String graphIRI) {

		String sparql = "";
		
		if (this.graph == null || this.graph.edgeSet().size() == 0)
			return sparql;
		
		HashMap<Node,String> nodeVariables = new HashMap<Node,String>();
		Node source, target;
		String variablePrefix = "?var";
		int variableIndex = 1;
		String sourceVarName,targetVarName;
		
		sparql += "SELECT (COUNT(*) as ?count) \n";  
		if (graphIRI != null && !graphIRI.isEmpty()) {
			sparql += "FROM <" + graphIRI + "> \n";
		}
		sparql += "WHERE { \n";
		
		for (LabeledLink l : this.graph.edgeSet()) {
			source = l.getSource();
			target = l.getTarget();
			if (!(source instanceof InternalNode) || 
					!(target instanceof InternalNode))
				continue;
			sourceVarName = nodeVariables.get(source);
			if (sourceVarName == null) {
				sourceVarName = variablePrefix + (variableIndex++);
				sparql += sourceVarName + " rdf:type " + "<" + source.getUri() + ">. \n";
				nodeVariables.put(source, sourceVarName);
			}
			targetVarName = nodeVariables.get(target);
			if (targetVarName == null) {
				targetVarName = variablePrefix + (variableIndex++);
				sparql += targetVarName + " rdf:type " + "<" + target.getUri() + ">. \n";
				nodeVariables.put(target, targetVarName);
			}
			sparql += sourceVarName + " <" + l.getUri() + "> " + targetVarName + ". \n";
		}
		
		// to assert the not equal condition between instances of the classes with same uris but different ids
		Function<Node, String> sameUriNodes = new Function<Node, String>() {
			  @Override public String apply(final Node n) {
				  if (n == null || n.getLabel() == null)
					  return null;
				  return n.getLabel().getUri();
			  }
			};

		Multimap<String, Node> index = Multimaps.index(graph.vertexSet(), sameUriNodes);
		for (String s : index.keySet()) {
			Collection<Node> nodeGroup = index.get(s);
			if (nodeGroup != null && !nodeGroup.isEmpty()) {
				List<Node> nodeList = new LinkedList<Node>(nodeGroup);
				for (int i = 0; i < nodeList.size() - 1; i++) {
					for (int j = i + 1; j < nodeList.size(); j++) {
						sparql += "FILTER (" + nodeVariables.get(nodeList.get(i)) + " != " +
								nodeVariables.get(nodeList.get(j)) + "). \n";
					}
				}
			}
		}

		sparql += "}";
		
		return sparql;
	}
	
	public String toExistsSparql(String graphIRI) {

		String sparql = "";
		
		if (this.graph == null || this.graph.edgeSet().size() == 0)
			return sparql;
		
		HashMap<Node,String> nodeVariables = new HashMap<Node,String>();
		Node source, target;
		String variablePrefix = "?var";
		int variableIndex = 1;
		String sourceVarName,targetVarName;
		
		sparql += "SELECT (COUNT(*) as ?count) \n";
		sparql += "{ \n";
		sparql += "SELECT ?var1 \n";  
		if (graphIRI != null && !graphIRI.isEmpty()) {
			sparql += "FROM <" + graphIRI + "> \n";
		}
		sparql += "WHERE { \n";
		
		for (LabeledLink l : this.graph.edgeSet()) {
			source = l.getSource();
			target = l.getTarget();
			if (!(source instanceof InternalNode) || 
					!(target instanceof InternalNode))
				continue;
			sourceVarName = nodeVariables.get(source);
			if (sourceVarName == null) {
				sourceVarName = variablePrefix + (variableIndex++);
				sparql += sourceVarName + " rdf:type " + "<" + source.getUri() + ">. \n";
				nodeVariables.put(source, sourceVarName);
			}
			targetVarName = nodeVariables.get(target);
			if (targetVarName == null) {
				targetVarName = variablePrefix + (variableIndex++);
				sparql += targetVarName + " rdf:type " + "<" + target.getUri() + ">. \n";
				nodeVariables.put(target, targetVarName);
			}
			sparql += sourceVarName + " <" + l.getUri() + "> " + targetVarName + ". \n";
		}
		
		// to assert the not equal condition between instances of the classes with same uris but different ids
		Function<Node, String> sameUriNodes = new Function<Node, String>() {
			  @Override public String apply(final Node n) {
				  if (n == null || n.getLabel() == null)
					  return null;
				  return n.getLabel().getUri();
			  }
			};

		Multimap<String, Node> index = Multimaps.index(graph.vertexSet(), sameUriNodes);
		for (String s : index.keySet()) {
			Collection<Node> nodeGroup = index.get(s);
			if (nodeGroup != null && !nodeGroup.isEmpty()) {
				List<Node> nodeList = new LinkedList<Node>(nodeGroup);
				for (int i = 0; i < nodeList.size() - 1; i++) {
					for (int j = i + 1; j < nodeList.size(); j++) {
						sparql += "FILTER (" + nodeVariables.get(nodeList.get(i)) + " != " +
								nodeVariables.get(nodeList.get(j)) + "). \n";
					}
				}
			}
		}

		sparql += "} \n";
		sparql += "LIMIT 1 \n";
		sparql += "}";
		
		return sparql;
	}
	
	public String getLabel() {
		if (this.graph == null || this.graph.edgeSet().size() == 0)
			return null;
		List<String> linkLabels = new ArrayList<String>();
		for (LabeledLink l : this.graph.edgeSet()) {
			String label = LinkIdFactory.getLinkId(l.getUri(), l.getSource().getUri(), l.getTarget().getUri());
			linkLabels.add(label);
		}
		Collections.sort(linkLabels);
		StringBuffer label = new StringBuffer();
		for (String s : linkLabels)
			if (s != null)
				label.append(s);
		return label.toString();
	}
	
	public void writeJson(String filename) throws IOException {
		
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileOutputStream out = new FileOutputStream(file); 
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("    ");
		try {
			writeModel(writer);
		} catch (Exception e) {
			logger.error("error in writing the pattern in json!");
	    	e.printStackTrace();
	     } finally {
			writer.close();
		}
		
	}
	
	private void writeModel(JsonWriter writer) throws IOException {
		String nullStr = null;
		writer.beginObject();
		writer.name("id").value(this.getId());
		writer.name("length").value(this.getLength());
		writer.name("frequency").value(String.valueOf(this.getFrequency()));
		writer.name("graph");
		if (this.graph == null) writer.value(nullStr);
		else GraphUtil.writeGraph(GraphUtil.asDefaultGraph(this.graph), writer, false, false);
//		else GraphUtil.writeGraph(workspace, worksheet, GraphUtil.asDefaultGraph(this.graph), writer);
		writer.endObject();
	}
	
	public static Pattern readJson(String filename) throws IOException {

		File file = new File(filename);
		if (!file.exists()) {
			logger.error("cannot open the file " + filename);
		}
		
		FileInputStream in = new FileInputStream(file);
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	    try {
	    	return readModel(reader);
	    } catch (Exception e) {
	    	logger.error("error in reading the model from json!");
	    	e.printStackTrace();
	    	return null;
	    } finally {
	    	reader.close();
	    }
	}
	
	private static Pattern readModel(JsonReader reader) throws IOException {
		
		String id = null;
		String length = null;
		String frequency = null;
		DirectedWeightedMultigraph<Node, DefaultLink> graph = null;
		
		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("id") && reader.peek() != JsonToken.NULL) {
				id = reader.nextString();
			} else if (key.equals("length") && reader.peek() != JsonToken.NULL) {
				length = reader.nextString();
			} else if (key.equals("frequency") && reader.peek() != JsonToken.NULL) {
				frequency = reader.nextString();
			} else if (key.equals("graph") && reader.peek() != JsonToken.NULL) {
				graph = GraphUtil.readGraph(reader);
			} else {
				reader.skipValue();
			}
		}
    	reader.endObject();
    	
    	NodeIdFactory nodeIdFactory = new NodeIdFactory();
    	if (graph != null) {
    		for (Node n : graph.vertexSet()) {
    			if (n instanceof InternalNode)
    				nodeIdFactory.getNodeId(n.getUri());
    		}
    	}
    	Pattern p = new Pattern(id, 
    			Integer.valueOf(length), 
    			Integer.valueOf(frequency), 
    			GraphUtil.asLabeledGraph(graph),
    			nodeIdFactory);
    	
    	return p;
	}
	
	public String getPrintStr() {
		String s = "";
		s += "id: " + (this.id == null? "NULL" : this.id) + "\n";
		s += "size: " + this.length + "\n";
		s += "frequency: " + this.frequency + "\n";
		s += "types: ";
		if (this.types != null) { 
			for (String t : this.types) {
				s += t + ",";
			}
			s += "\n";
		}
		if (this.graph != null)
			s += GraphUtil.labeledGraphToString(graph);
		return s;
	}
	
	public static void main(String[] args) throws IOException {
		int length = 1;
		System.out.println("patterns with length " + length);
		File f = new File(Params.PATTERNS_OUTPUT_DIR);
		File f1 = new File(f.getAbsoluteFile() + "/" + length);
		File[] files = f1.listFiles();
		if (files != null) {
			for (File file : files) {
				Pattern p = Pattern.readJson(file.getAbsolutePath());
				p.setId("p" + length + "-" + p.getId());
				p.writeJson(file.getAbsolutePath());
			}
		}
		System.out.println("done.");
	}
}
