package edu.isi.karma.modeling.steiner.topk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * This class is part of the YAGO extractors (http://mpii.de/yago). It is licensed under the 
 * Creative Commons Attribution-Noncommercial-Share Alike 3.0 Unported License,
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/) 
 * by the YAGO team (http://mpii.de/yago). 
 * 
 * @author Maya Ramanath
 */
public class Graph<V, E extends Edge<V>> {
	
	protected HashMap<String, V> nodes;
	protected HashSet<E> edges;
	
	// Representing infinity
	public int INF = Integer.MAX_VALUE;
	
	public Graph () {
		nodes = new HashMap<>();
		edges = new HashSet<>();
	}
	
	public HashSet<E> edges () { return edges; }

	public HashSet<V> nodes () { return new HashSet<> (nodes.values()); }
	
	public void addNode (V v) {
		nodes.put(v.toString(), v);
	}

	public void addEdge (E e) {
		edges.add(e);
		if (!nodes.containsKey(e.source()))
			nodes.put(e.source().toString(), e.source());
		if (!nodes.containsKey(e.destination()))
			nodes.put(e.destination().toString(), e.destination());
	}

	public boolean loadGraph () 
	throws Exception {
		return false;
	}
	
	// NOTE: This gives the UNDIRECTED connectivity
	/*public IndexedMatrix<V> toConnectivityMatrix () {
		IndexedMatrix<V> matrix = new IndexedMatrix<V> (nodes.size(),nodes.size());
		// Initialize all values in matrix to infinity;
		for (int i = 0; i < nodes.size(); i++)
			for (int j = 0; j < nodes.size(); j++)
				matrix.setByInt (i, j, new Float(INF));
		
		Iterator<E> iter = edges.iterator();
		while (iter.hasNext()) {
			E e = iter.next();
  		Announce.debug ("Setting connectivities: " + e.source() + ", " + e.destination());			
			matrix.set(e.source(), e.destination(), new Float(1));
			matrix.set(e.destination(), e.source(), new Float(1));
		}
		return matrix;
	}*/
	
	public String toString () {
    if(edges.isEmpty()) return("<Empty graph>");
    StringBuilder b=new StringBuilder();
    Set<Edge> goodEdges = new TreeSet<>();
    Set<Edge> badEdges = new TreeSet<>();
    for(Edge e : edges) {
    	if(goodEdges.contains(e)){badEdges.add(e); continue;}
    	goodEdges.add(e);
    }
    goodEdges.removeAll(badEdges);
    for(Edge e : goodEdges) {
      b.append(e).append('\n');
    }
    b.setLength(b.length()-1);
		return b.toString();
	}
}
