package edu.isi.karma.research.modeling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.util.RandomGUID;

public class PatternGenerator {

	List<Pattern> basicPatterns;
	
	private HashMap<String,List<Connection>> possibleConnections;

	enum Direction {IN , OUT, BOTH}

	private class Connection {
		String linkUri;
		String nodeUri;
		Direction direction;
		public Connection(String linkUri, String nodeUri, Direction direction) {
			this.linkUri = linkUri;
			this.nodeUri = nodeUri;
			this.direction = direction;
		}
	}
	
	public PatternGenerator(String patternDirectoryPath) {
		this.basicPatterns = new LinkedList<Pattern>();
		this.possibleConnections = new HashMap<String,List<Connection>>();
		
		Map<String,Pattern> patternsLengthOne = PatternReader.importPatterns(patternDirectoryPath, 1);
		if (patternsLengthOne != null) {
			this.basicPatterns.addAll(patternsLengthOne.values());
		}
		
		this.computePossibleConnections(Direction.OUT);
	}
	
	public void computePossibleConnections(Direction direction) {

		for (Pattern p: this.basicPatterns) {
			
			DirectedWeightedMultigraph<Node, LabeledLink> g = p.getGraph();

			if (g == null)
				continue;

			if (g.edgeSet().size() == 0 || 
					g.edgeSet().size() > 1)
				continue;
			
			for (Node n : g.vertexSet()) {
				if (this.possibleConnections.get(n.getUri()) == null) {
					this.possibleConnections.put(n.getUri(), new LinkedList<Connection>());
				}
			}
		}
		
		for (String s : this.possibleConnections.keySet()) {

			for (Pattern p: this.basicPatterns) {
				
				DirectedWeightedMultigraph<Node, LabeledLink> g = p.getGraph();

				if (g == null)
					continue;

				if (g.edgeSet().size() == 0 || 
						g.edgeSet().size() > 1)
					continue;
				
				LabeledLink l = g.edgeSet().iterator().next();
				String sourceUri = l.getSource().getUri();
				String targetUri = l.getTarget().getUri();

				if (direction != Direction.IN && sourceUri.equalsIgnoreCase(s)) {
					Connection c = new Connection(l.getUri(), l.getTarget().getUri(), Direction.OUT);
					this.possibleConnections.get(s).add(c);
				} else if (direction != Direction.OUT && targetUri.equalsIgnoreCase(s)) {
					Connection c = new Connection(l.getUri(), l.getSource().getUri(), Direction.IN);
					this.possibleConnections.get(s).add(c);
				}
			}
		}

	}
	
	public Set<Node> getNodesWithUri(String uri, DirectedWeightedMultigraph<Node, LabeledLink> g) {
		Set<Node> nodes = new HashSet<Node>();
		if (uri == null || g == null || g.vertexSet().isEmpty())
			return nodes;
		for (Node n : g.vertexSet())
			if (n.getUri().equalsIgnoreCase(uri))
				nodes.add(n);
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	public List<Pattern> getPatterns(int length, int instanceLimit, boolean includeShorterPatterns) {
		
		System.out.println("length: " + length);
		if (length <= 0)
			return new LinkedList<Pattern>();
		else if (length == 1)
			return this.basicPatterns;
		else {
			List<Pattern> results = new LinkedList<Pattern>();
			List<Pattern> shorterPatterns = getPatterns(length-1, instanceLimit, includeShorterPatterns);
			if (includeShorterPatterns && shorterPatterns != null)
				results.addAll(shorterPatterns);
			for (Pattern p : shorterPatterns) {
				
				// try to connect patterns of length one to nodes of a pattern (join)
				DirectedWeightedMultigraph<Node, LabeledLink> g = p.getGraph();
				if (g == null)
					continue;
				
				Node source, target;
				for (Node n : g.vertexSet()) {
					
					List<Connection> connections = this.possibleConnections.get(n.getUri());
					if (connections == null || connections.size() == 0)
						continue;
					
					for (Connection c : connections) {

						NodeIdFactory nodeIdFactory = p.getNodeIdFactory().clone();
						if (nodeIdFactory.lastIndexOf(c.nodeUri) == instanceLimit)
							continue;
						
						String newNodeId = nodeIdFactory.getNodeId(c.nodeUri);
						Node newNode = new InternalNode(newNodeId, new Label(c.nodeUri));
						DirectedWeightedMultigraph<Node, LabeledLink> newG = 
								(DirectedWeightedMultigraph<Node, LabeledLink>)g.clone();
						newG.addVertex(newNode);
						
						if (c.direction == Direction.OUT) {
							source = n;
							target = newNode;
						} else {
							source = newNode;
							target = n;
						}
						String newLinkId = LinkIdFactory.getLinkId(c.linkUri, source.getId(), target.getId());
						LabeledLink newLink = new ObjectPropertyLink(newLinkId, new Label(c.linkUri), ObjectPropertyType.None);
						newG.addEdge(source, target, newLink);
						Pattern newP = new Pattern(new RandomGUID().toString(), length, 0, null, newG, nodeIdFactory);
						results.add(newP);

						// add links to existing nodes in the pattern
						Set<Node> nodesInPatternWithSameURI = this.getNodesWithUri(c.nodeUri, g);
						for (Node existingNode : nodesInPatternWithSameURI) {

							if (c.direction == Direction.OUT) {
								source = n;
								target = existingNode;
							} else {
								source = existingNode;
								target = n;
							}

							newLinkId = LinkIdFactory.getLinkId(c.linkUri, source.getId(), target.getId());
							newLink = new ObjectPropertyLink(newLinkId, new Label(c.linkUri), ObjectPropertyType.None);
							if (g.containsEdge(newLink))
								continue;
							
							newG = (DirectedWeightedMultigraph<Node, LabeledLink>)g.clone();
							newG.addEdge(source, target, newLink);
							newP = new Pattern(new RandomGUID().toString(), length, 0, null, newG, p.getNodeIdFactory().clone());
							results.add(newP);
						}
					}
				}
			}
			return results;
		}
	}
	
	public static void main(String[] args) {
		int length = 4;
		int instanceLimit = 2;
		boolean includeShorterPatterns = false;
		PatternGenerator pg = new PatternGenerator(Params.PATTERNS_DIR);
		List<Pattern> patterns = pg.getPatterns(length, instanceLimit, includeShorterPatterns);
		System.out.println("number of patterns: " + patterns.size());
		for (Pattern p : patterns) {
			if (p.getGraph() != null)
				if (!includeShorterPatterns && p.getGraph().edgeSet().size() != length) {
					System.out.println("************ There is a bug in the code!!! ************");
				}
				Node n1 = new InternalNode("http://erlangen-crm.org/current/E21_Person1", null);
				Node n2 = new InternalNode("http://erlangen-crm.org/current/E67_Birth1", null);
				Node n3 = new InternalNode("http://erlangen-crm.org/current/E69_Death1", null);
				Node n4 = new InternalNode("http://erlangen-crm.org/current/E52_Time-Span1", null);
				Node n5 = new InternalNode("http://erlangen-crm.org/current/E52_Time-Span2", null);
				
				if (p.getGraph().containsVertex(n1) &&
						p.getGraph().containsVertex(n2) &&
						p.getGraph().containsVertex(n3) &&
						p.getGraph().containsVertex(n4) &&
						p.getGraph().containsVertex(n5))
					System.out.println(GraphUtil.labeledGraphToString(p.getGraph()));
		}
		System.out.println("number of patterns: " + patterns.size());
	}
}
