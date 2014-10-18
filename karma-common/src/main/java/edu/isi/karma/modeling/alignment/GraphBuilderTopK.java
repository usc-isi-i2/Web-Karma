package edu.isi.karma.modeling.alignment;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.steiner.topk.BANKSfromMM;
import edu.isi.karma.modeling.steiner.topk.Fact;
import edu.isi.karma.modeling.steiner.topk.ResultGraph;
import edu.isi.karma.modeling.steiner.topk.SteinerEdge;
import edu.isi.karma.modeling.steiner.topk.SteinerNode;
import edu.isi.karma.modeling.steiner.topk.TopKSteinertrees;
import edu.isi.karma.rep.alignment.CompactObjectPropertyLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.util.EncodingDetector;

public class GraphBuilderTopK extends GraphBuilder {
	
	static Logger logger = LoggerFactory.getLogger(GraphBuilderTopK.class);

	private HashMap<SteinerNode, TreeSet<SteinerEdge>> topKGraph;
	private HashMap<String, SteinerNode> topKGraphNodes;

	public GraphBuilderTopK(OntologyManager ontologyManager, NodeIdFactory nodeIdFactory, boolean addThingNode) { 
		super(ontologyManager, nodeIdFactory, addThingNode);
		if (topKGraph == null) topKGraph = new HashMap<SteinerNode, TreeSet<SteinerEdge>>();
		if (topKGraphNodes == null) topKGraphNodes = new HashMap<String, SteinerNode>();
	}
	
	public GraphBuilderTopK(OntologyManager ontologyManager, DirectedWeightedMultigraph<Node, DefaultLink> graph) {
		super(ontologyManager, graph);
		if (topKGraph == null) topKGraph = new HashMap<SteinerNode, TreeSet<SteinerEdge>>();
		if (topKGraphNodes == null) topKGraphNodes = new HashMap<String, SteinerNode>();
	}
	
	public HashMap<SteinerNode, TreeSet<SteinerEdge>> getTopKGraph() {
		if (topKGraph == null)
			topKGraph = new HashMap<SteinerNode, TreeSet<SteinerEdge>>();
		return topKGraph;
	}

	public HashMap<String, SteinerNode> getTopKGraphNodes() {
		if (topKGraphNodes == null)
			topKGraphNodes = new HashMap<String, SteinerNode>();
		return topKGraphNodes;
	}

	public boolean addNode(Node node) {
		if (super.addNode(node)) {
			SteinerNode n = new SteinerNode(node.getId());
			getTopKGraphNodes().put(n.getNodeId(), n);
			getTopKGraph().put(n, new TreeSet<SteinerEdge>());
			return true;
		} else
			return false;
	}
	public boolean addLink(Node source, Node target, DefaultLink link) {
		if (super.addLink(source, target, link)) {
			SteinerNode n1 = new SteinerNode(source.getId());
			SteinerNode n2 = new SteinerNode(target.getId());
			SteinerEdge e = new SteinerEdge(n1, link.getId(), n2, (float)link.getWeight());
			getTopKGraph().get(n1).add(e);
			getTopKGraph().get(n2).add(e);
			return true;
		} else
			return false;
	}
	
	public boolean addLink(Node source, Node target, DefaultLink link, Double weight) {
		if (super.addLink(source, target, link, weight)) {
			SteinerNode n1 = new SteinerNode(source.getId());
			SteinerNode n2 = new SteinerNode(target.getId());
			SteinerEdge e = new SteinerEdge(n1, link.getId(), n2, (float)weight.doubleValue());
			getTopKGraph().get(n1).add(e);
			getTopKGraph().get(n2).add(e);
			return true;
		} else
			return false;
	}
	
	public boolean removeLink(DefaultLink link) {
		if (super.removeLink(link)) {
			SteinerNode n1 = new SteinerNode(link.getSource().getId());
			SteinerNode n2 = new SteinerNode(link.getTarget().getId());
			SteinerEdge e = new SteinerEdge(n1, link.getId(), n2, (float)link.getWeight());
			getTopKGraph().get(n1).remove(e);
			getTopKGraph().get(n2).remove(e);
			return true;
		} else
			return false;
	}
	
	public void changeLinkWeight(DefaultLink link, double weight) {
		super.changeLinkWeight(link, weight);
		SteinerNode n1 = new SteinerNode(link.getSource().getId());
		SteinerNode n2 = new SteinerNode(link.getTarget().getId());
		SteinerEdge e = new SteinerEdge(n1, link.getId(), n2, (float)weight);
		getTopKGraph().get(n1).remove(e);
		getTopKGraph().get(n2).remove(e);
		getTopKGraph().get(n1).add(e);
		getTopKGraph().get(n2).add(e);
	}
	
	public List<DirectedWeightedMultigraph<Node, LabeledLink>> getTopKSteinerTrees(Set<Node> steinerNodes, int k) 
			throws Exception {

		if (steinerNodes == null) {
			logger.error("no steiner node specified!");
			return null;
		}
		
//		for (Node n : steinerNodes)
//			System.out.println(n instanceof ColumnNode ? ((ColumnNode)n).getColumnName() : n.getId());
		
		TreeSet<SteinerNode> terminals= new TreeSet<SteinerNode>();
		for (Node n : steinerNodes) {
			terminals.add(new SteinerNode(n.getId()));
		}
		
//		DPBFfromMM N = new DPBFfromMM(terminals);
		BANKSfromMM N = new BANKSfromMM(terminals);
//		STARfromMM N = new STARfromMM(terminals);
		TopKSteinertrees.graph = this.getTopKGraph();
		TopKSteinertrees.nodes = this.getTopKGraphNodes();
		
		List<DirectedWeightedMultigraph<Node, LabeledLink>> results = new 
				LinkedList<DirectedWeightedMultigraph<Node, LabeledLink>>();
		
		N.getTopKTrees(k);
		DirectedWeightedMultigraph<Node, LabeledLink> processedTree = null;
		for(ResultGraph tree: N.getResultQueue()){
//			System.out.println(tree.getScore());
			processedTree = getLabeledSteinerTree(tree);
			if (processedTree != null) results.add(processedTree);
		}

		return results;
	}
	
	public DirectedWeightedMultigraph<Node, LabeledLink> getLabeledSteinerTree(ResultGraph initialTree) {
		WeightedMultigraph<Node, DefaultLink> tree =
				new WeightedMultigraph<Node, DefaultLink>(DefaultLink.class);
		
		HashSet<Node> visitedNodes = new HashSet<Node>();
		Node source, target;
		DefaultLink l; 
		double weight;
		for (Fact f : initialTree.getFacts()) { 
			source = this.getIdToNodeMap().get(f.source().name());
			target = this.getIdToNodeMap().get(f.destination().name());
			
			if (LinkIdFactory.getLinkUri(f.label().name).equals(Uris.DEFAULT_LINK_URI)) {
				String id = LinkIdFactory.getLinkId(Uris.DEFAULT_LINK_URI, source.getId(), target.getId());					
				l = new CompactObjectPropertyLink(id, ObjectPropertyType.None);
			}
			else l = this.getIdToLinkMap().get(f.label().name);
			weight = f.weight();
			if (!visitedNodes.contains(source)) {
				tree.addVertex(source);
				visitedNodes.add(source);
			}
			if (!visitedNodes.contains(target)) {
				tree.addVertex(target);
				visitedNodes.add(target);
			}
//			System.out.println(f.toString());
			tree.addEdge(source, target, l);
			tree.setEdgeWeight(l, weight);
		}
		
		TreePostProcess treePostProcess = new TreePostProcess(this, tree);
		return treePostProcess.getTree();

	}

	public static void main(String[] args) throws Exception {
		
		/** Check if any ontology needs to be preloaded **/
		String preloadedOntDir = "/Users/mohsen/karma/preloaded-ontologies/";
		File ontDir = new File(preloadedOntDir);
		OntologyManager mgr = null;
		if (ontDir.exists()) {
			File[] ontologies = ontDir.listFiles();
			mgr = new OntologyManager();
			for (File ontology: ontologies) {
				System.out.println(ontology.getName());
				if (ontology.getName().endsWith(".owl") || 
						ontology.getName().endsWith(".rdf") || 
						ontology.getName().endsWith(".n3") || 
						ontology.getName().endsWith(".ttl") || 
						ontology.getName().endsWith(".xml")) {
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try {
						String encoding = EncodingDetector.detect(ontology);
						mgr.doImport(ontology, encoding);
					} catch (Exception t) {
//						logger.error("Error loading ontology: " + ontology.getAbsolutePath(), t);
					}
				} else {
					logger.error ("the file: " + ontology.getAbsolutePath() + " does not have proper format: xml/rdf/n3/ttl/owl");
				}
			}
			// update the cache at the end when all files are added to the model
			mgr.updateCache();
		}
		
		GraphBuilderTopK gbtk = new GraphBuilderTopK(mgr, new NodeIdFactory(), false);
		
		Node n1 = new InternalNode("n1", new Label("http://erlangen-crm.org/current/E55_Type"));
		Node n2 = new InternalNode("n2", new Label("http://erlangen-crm.org/current/E70_Thing"));
		Node n3 = new InternalNode("n3", new Label("http://erlangen-crm.org/current/E39_Actor"));
		Node n4 = new InternalNode("n4", new Label("http://erlangen-crm.org/current/E74_Group"));

		gbtk.addNode(n1);
		gbtk.addNode(n2);
		gbtk.addNode(n3);
		gbtk.addNode(n4);

		ObjectPropertyLink e1 = new ObjectPropertyLink("e1", new Label("http://erlangen-crm.org/current/P104i_applies_to"), ObjectPropertyType.Direct);
		ObjectPropertyLink e2 = new ObjectPropertyLink("e2", new Label("http://erlangen-crm.org/current/P105i_has_right_on"), ObjectPropertyType.Direct);
		ObjectPropertyLink e3 = new ObjectPropertyLink("e3", new Label("http://erlangen-crm.org/current/P31_has_modified"), ObjectPropertyType.Direct);
		ObjectPropertyLink e4 = new ObjectPropertyLink("e4", new Label("http://erlangen-crm.org/current/P106i_forms_part_of"), ObjectPropertyType.Direct);
		ObjectPropertyLink e5 = new ObjectPropertyLink("e5", new Label("http://erlangen-crm.org/current/P92_brought_into_existence"), ObjectPropertyType.Direct);
		ObjectPropertyLink e6 = new ObjectPropertyLink("e6", new Label("http://erlangen-crm.org/current/P108_has_produced"), ObjectPropertyType.Direct);
		
		gbtk.addLink(n1, n2, e1, 0.6);
		gbtk.addLink(n1, n4, e2, 0.9);
		gbtk.addLink(n1, n3, e3, 0.2);
		gbtk.addLink(n2, n3, e4, 0.7);
		gbtk.addLink(n2, n4, e5, 0.4);
		gbtk.addLink(n4, n3, e6, 0.3);
		
		
		Set<Node> steinerNodes = new HashSet<Node>();
		steinerNodes.add(n2);
		steinerNodes.add(n3);
		
		List<DirectedWeightedMultigraph<Node, LabeledLink>> trees = gbtk.getTopKSteinerTrees(steinerNodes, 3);
		for (DirectedWeightedMultigraph<Node, LabeledLink> tree : trees) {
			System.out.println(GraphUtil.labeledGraphToString(tree));
		}

//		DPBFfromMM N = new DPBFfromMM(terminals);
//		N.graph = gbtk.getTopKGraph();
//		N.nodes = gbtk.getTopKGraphNodes();
//		
//		long startTime=System.currentTimeMillis();
//		N.getTopKTrees(3);
//		for(ResultGraph tree: N.getResultQueue()){
//			for (Fact f : tree.getFacts()) { 
//				System.out.println(f.toString());
//			}
//			System.out.println(tree.getScore());
//		}
//		long end= System.currentTimeMillis();
//		
//		System.out.println("done ...");

	}

}
