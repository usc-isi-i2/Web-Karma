package edu.isi.karma.research.modeling;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;

public class PatternGenerator {

	List<Pattern> basicPatterns;
	
	private HashMap<String,List<Connection>> possibleConnections;
	
	private String outputDir;

	private VirtuosoConnector virtuosoConnector;
	
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
	
	public PatternGenerator(String inputDir, String outputDir, VirtuosoConnector vc) {
		this.basicPatterns = new LinkedList<Pattern>();
		this.possibleConnections = new HashMap<String,List<Connection>>();
		this.outputDir = outputDir;
		this.virtuosoConnector = vc;
		
		Map<String,Pattern> patternsLengthOne = PatternReader.importPatterns(inputDir, 1);
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
	public List<Pattern> getPatterns(int length, 
			int instanceLimit, 
			boolean includeShorterPatterns, 
			List<Pattern> seeds,
			int seedLength) {

		List<Pattern> results = new LinkedList<Pattern>();
		if (length <= 0 || length < seedLength) {
			return results;
		} else if (length == seedLength) {
			return seeds;
		} else {
			List<Pattern> shorterPatterns = getPatterns(length-1, instanceLimit, includeShorterPatterns, seeds, seedLength);
			if (includeShorterPatterns && 
					(length-1) > seedLength && 
					shorterPatterns != null)
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
						Pattern newP = new Pattern(length, 0, newG, nodeIdFactory);
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
							newP = new Pattern(length, 0, newG, p.getNodeIdFactory().clone());
							results.add(newP);
						}
					}
				}
			}
			System.out.println("***** " + "patterns with length " + length + " *****");
			results = getValidPatterns(results);
			printPatterns(length, results);
			return results;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Pattern> getPatterns(int length, int instanceLimit, boolean includeShorterPatterns) {
		
		List<Pattern> results = new LinkedList<Pattern>();

		if (length <= 0) {
			return results;
		} else if (length == 1) {
			results = this.basicPatterns;
		} else {
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
						Pattern newP = new Pattern(length, 0, newG, nodeIdFactory);
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
							newP = new Pattern(length, 0, newG, p.getNodeIdFactory().clone());
							results.add(newP);
						}
					}
				}
			}
		}
		System.out.println("***** " + "patterns with length " + length + " *****");
		results = getValidPatterns(results);
		printPatterns(length, results);
		return results;
	}
	
	private void printPatterns(int length, List<Pattern> patterns) {
		File outRootDir = new File(outputDir);
		if (outRootDir.exists() && outRootDir.isDirectory()) {
			String outPath = outRootDir.getAbsolutePath() + "/" + length;
			File outDir = new File(outPath);
			if (outDir.exists()) {
				try {
					FileUtils.deleteDirectory(outDir);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			if (!outDir.mkdir()) {
				System.out.println("could not create the diretcory: " + outPath);
				return;
			} 
			String filename;
			for (Pattern p : patterns) {
				filename = outPath + "/" + p.getId() + ".json";
				try {
					p.writeJson(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private List<Pattern> getValidPatterns(List<Pattern> patterns) {
		List<Pattern> distinctPatterns, validPatterns;
		System.out.println("number of patterns before removing duplicates: " + patterns.size());
		distinctPatterns = removeDuplicates(patterns);
		System.out.println("number of patterns after removing duplicates: " + distinctPatterns.size());
		validPatterns = getPatternCount(distinctPatterns);
		System.out.println("number of existing patterns in the data: " + validPatterns.size());
		return validPatterns;
	}

	private List<Pattern> removeDuplicates(List<Pattern> patterns) {
		List<Pattern> results = new LinkedList<Pattern>();
		if (patterns == null || patterns.isEmpty())
			return results;
		
		HashSet<Integer> hashTable = new HashSet<Integer>();
		int hash;
		for (Pattern p :patterns) {
			hash = p.getLabel().hashCode();
			if (hashTable.contains(hash)) continue;
			hashTable.add(hash);
			results.add(p);
		}
//		Collections.sort(patterns, new PatternLabelComparator());
//		for (int i = 0; i < patterns.size(); i++) {
//			if (i > 0 && patterns.get(i).getLabel().equalsIgnoreCase(patterns.get(i-1).getLabel()))
//				continue;
//			results.add(patterns.get(i));
//		}
		return results;
	}

	private List<Pattern> getPatternCount(List<Pattern> patterns) {
		List<Pattern> results = new LinkedList<Pattern>();
		if (patterns == null || patterns.isEmpty())
			return results;
		
		if (this.virtuosoConnector == null)
			return results;
		
		VirtuosoManager vm = new VirtuosoManager(this.virtuosoConnector);
		
		RepositoryConnection con;
		try {
			con = vm.getConnection();
		} catch (RepositoryException e) {
			e.printStackTrace();
			return results;
		}
		
		int count;
		int patternNumber = 1;
		for (Pattern p : patterns) {
//			System.out.println(p.toSparql(this.virtuosoConnector.getGraphIRI()));
			count = vm.getPatternCount(con, p.toSparql(this.virtuosoConnector.getGraphIRI()));
			if (count != 0) {
				p.setFrequency(count);
				results.add(p);
			}
			System.out.println("pattern " + patternNumber++ + "/" + patterns.size() + 
					", length: " + p.getLength() + 
					", count: " + count);
			if (patternNumber % 5000 == 0) {
				System.out.println("getting new connection ...");
				try {
					vm.closeConnection(con);
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
				try {
					con = vm.getConnection();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			vm.closeConnection(con);
		} catch (RepositoryException e) {
			e.printStackTrace();
			return results;
		}
		
		return results;
	}
	
	private static void generatePatternsFromSeeds() throws IOException {

		int length = 5;
		int seedLength = 4;
		int instanceLimit = 2;
		boolean includeShorterPatterns = false;

		String instance = "fusionRepository.isi.edu";
		int port = 1300;  
		String username = "dba";
		String password = "dba";
		int queryTimeout = 1;

		VirtuosoConnector vc = new VirtuosoConnector(instance, port, username, password);
		vc.setQueryTimeout(queryTimeout);

		PatternGenerator pg = new PatternGenerator(Params.PATTERNS_INPUT_DIR, Params.PATTERNS_OUTPUT_DIR, vc);

		System.out.println("reading patterns with length " + seedLength);
		List<Pattern> seeds = new LinkedList<Pattern>();
		File f = new File(Params.PATTERNS_OUTPUT_DIR);
		for (int i = 0; i < length; i++) {
			File f1 = new File(f.getAbsoluteFile() + "/" + seedLength);
			File[] files = f1.listFiles();
			if (files != null)
			for (File f2 : files) {
				Pattern p = Pattern.readJson(f2.getAbsolutePath());
				seeds.add(p);
			}
		}
		System.out.println("finished reading patterns.");
		long start = System.currentTimeMillis();
		List<Pattern> patterns = pg.getPatterns(length, instanceLimit, includeShorterPatterns, seeds, seedLength);
		long patternGeneraionTime = System.currentTimeMillis();
		System.out.println("================================================================================");
		System.out.println("time to generate patterns: " + (patternGeneraionTime - start)/1000F);
		System.out.println("number of possible patterns: " + patterns.size());
		System.out.println("================================================================================");
		
	}
	
	private static void generatePatterns() {

		int length = 4;
		int instanceLimit = 2;
		boolean includeShorterPatterns = false;
		
		String instance = "fusionRepository.isi.edu";
		int port = 1300;  
		String username = "dba";
		String password = "dba";
		String baseGraph = "http://museum-crm-lod/";
		int queryTimeout = 1;
		String graphIRI;
		
		File f = new File(Params.SOURCE_DIR);
		File[] files = f.listFiles();
		String sourcename, filename;
		String patternInputDirStr, patternOutputDirStr;
//		int i = 4; {
		for (int i = 4; i < files.length; i++) {
			File file = files[i];
			filename = file.getName();
			System.out.println("processing " + filename + " ...");
			sourcename = filename.substring(0, filename.lastIndexOf("."));
			patternInputDirStr = Params.LOD_DIR + sourcename + "/" + Params.PATTERNS_INPUT_DIR;
			patternOutputDirStr = Params.LOD_DIR + sourcename + "/" + Params.PATTERNS_OUTPUT_DIR;
			File patternOutputDir = new File(patternOutputDirStr);
			if (!patternOutputDir.exists()) {
				patternOutputDir.mkdir();
			}
			
			graphIRI = baseGraph + sourcename;
			VirtuosoConnector vc = new VirtuosoConnector(instance, port, username, password, graphIRI);
			vc.setQueryTimeout(queryTimeout);
			PatternGenerator pg = new PatternGenerator(patternInputDirStr, patternOutputDirStr, vc);

			long start = System.currentTimeMillis();
			List<Pattern> patterns = pg.getPatterns(length, instanceLimit, includeShorterPatterns);
			long patternGeneraionTime = System.currentTimeMillis();
			System.out.println("================================================================================");
			System.out.println("time to generate patterns: " + (patternGeneraionTime - start)/1000F);
			System.out.println("number of possible patterns: " + patterns.size());
			System.out.println("================================================================================");

			System.out.println("done with " + filename + ".");

		}
		

		
	}
	
	public static void main(String[] args) throws IOException {

//		boolean generatePatternsFromSeeds = false;
//		
//		if (generatePatternsFromSeeds) generatePatternsFromSeeds();
//		else generatePatterns();
		
		System.out.println("done.");
	}
}
