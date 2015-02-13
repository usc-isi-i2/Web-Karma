package edu.isi.karma.research.modeling;

import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;

public class Pattern {
	
	static Logger logger = LoggerFactory.getLogger(Pattern.class);

	private String id;
	private int size; // number of nodes
	private int frequency; // number of times this pattern appears in LOD
	private List<String> types;
	private DirectedWeightedMultigraph<Node, LabeledLink> graph;
	
	public Pattern(String id, 
			int size, 
			int frequency, 
			List<String> types,
			DirectedWeightedMultigraph<Node, LabeledLink> graph) {
		this.id = id;
		this.size = size;
		this.frequency = frequency;
		this.types = types;
		this.graph = graph;
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
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
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
	
	public String getPrintStr() {
		String s = "";
		s += "id: " + (this.id == null? "NULL" : this.id) + "\n";
		s += "size: " + this.size + "\n";
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
}
