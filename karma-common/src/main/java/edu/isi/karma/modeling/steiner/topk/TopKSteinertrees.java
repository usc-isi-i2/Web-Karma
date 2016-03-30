package edu.isi.karma.modeling.steiner.topk;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;



/**
 * This class serves as a super class for different implementations of the STAR
 * algorithm.
 * 
 * @author kasneci
 *
 */

public abstract class TopKSteinertrees {
	
	/******* data structures and methods for main memory algorithms ************/
	
	//	graph which can be loaded  into main memory
	public static Map<SteinerNode, TreeSet<SteinerEdge>> graph;
	
	//nodes of the graph
	public static Map<String, SteinerNode> nodes;
	
	//maps node names to ids
	public static Map<String, Integer> nodeToId= new HashMap<>();

	
	
	/**
	 * writes the graph for BLINKS into a text file
	 * @param fileName name of the grph file
	 * @throws IOException
	 */
	public void writeGraphForBLINKS(String fileName) throws IOException{
		String folder ="d:\\DBLPgraph\\";
		new File(folder).mkdir();
		
		FileWriter fw= new FileWriter(folder+fileName+"BL.graph");
		fw.write(graph.size()+"\n");
		for(SteinerNode n: graph.keySet()){
			fw.write(nodeToId.get(n.name())+" "+"0.0 "+nodeToId.get(n.name())+" \n");
		}
		for(Map.Entry<SteinerNode, TreeSet<SteinerEdge>> steinerNodeTreeSetEntry : graph.entrySet()){
			for(SteinerEdge e: steinerNodeTreeSetEntry.getValue()){
				if(nodeToId.get(steinerNodeTreeSetEntry.getKey().name())==null) nodeToId.put(steinerNodeTreeSetEntry.getKey().name(),1);
				fw.write(nodeToId.get(steinerNodeTreeSetEntry.getKey().name())+" "
						+nodeToId.get(steinerNodeTreeSetEntry.getKey().getNeighborInEdge(e).name())+" "+e.weight()+"\n");
			}
		}
		fw.close();
		
		fw= new FileWriter(folder+fileName+"BL.key");
		for(SteinerNode n: graph.keySet()){
			fw.write(nodeToId.get(n.name())+"---"+nodeToId.get(n.name())+"---"+"1\n");
		}
		fw.close();
		fw= new FileWriter(folder+fileName+"BLStringToId.list");
		for(SteinerNode n: graph.keySet()){
			fw.write(nodeToId.get(n.name())+"---"+n.name()+"\n");
		}
		fw.close();
	}
	
	
	/**
	 * writes the graph for STAR into a text file
	 * @param fileName name of the graph file
	 * @throws IOException
	 */
	public void writeGraphForSTAR(String fileName) throws IOException{
//		String folder ="d:\\DBLPgraph\\";
		String folder ="/Users/mohsen/Documents/Academic/ISI/_GIT/Web-Karma/karma-research/";
		new File(folder).mkdir();
		
		FileWriter fw= new FileWriter(folder+fileName+"STAR.txt");
		TreeSet<SteinerEdge> ts = new TreeSet<>();
		for(Map.Entry<SteinerNode, TreeSet<SteinerEdge>> steinerNodeTreeSetEntry : graph.entrySet()){
			for(SteinerEdge e: steinerNodeTreeSetEntry.getValue()){
				if(ts.contains(e))continue;
				fw.write(e.getEdgeLabel()+" : "+e.weight()+" : "+ steinerNodeTreeSetEntry.getKey().name()+" : "+ steinerNodeTreeSetEntry.getKey().getNeighborInEdge(e).name()+" : "+e.sourceNode.equals(steinerNodeTreeSetEntry.getKey())+"\n");
				ts.add(e);
			}
		}
		System.out.println("#edges: "+ts.size());
		fw.close();
	}
	
	
	/**
	 * loads the graph from a text file into the main memory
	 * in case uniformWeights is true then all edges have weight 1,
	 * otherwise each edge has a random weight
	 * @throws IOException
	 */
	public void loadGraphFromFiles(boolean uniformWeights)throws IOException{
		graph= new HashMap<>();
		nodes = new HashMap<>();
		//FileReader fr = new FileReader("d:\\DBLPgraph\\IMDBSTAR.txt");
		FileReader fr = new FileReader("d:\\DBLPgraph\\dblpgraphNeighS.txt");
		//FileReader fr = new FileReader("d:\\DBLPgraph\\DBLPSTAR.txt");
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		int k=0;
		while(line!=null){
			k++;
			//if(k==30000) break;
			String[]arr = line.split(" : ");
			boolean isArg1=arr[4].equals("true");
			
			//D.p(isArg1);D.p();
			
			if(!nodes.containsKey(
					arr[2])){
				nodes.put(arr[2], new SteinerNode(arr[2]));
				graph.put(nodes.get(arr[2]), new TreeSet<SteinerEdge>());
			}
			if(nodes.get(arr[3])==null){
				nodes.put(arr[3], new SteinerNode(arr[3]));
				graph.put(nodes.get(arr[3]), new TreeSet<SteinerEdge>());
			}
			SteinerEdge e;
			if(!uniformWeights){
				Random r= new Random();
				arr[1]=String.valueOf(r.nextFloat());
			}
			if(isArg1) e= new SteinerEdge(nodes.get(arr[3]), arr[0].intern(), nodes.get(arr[2]) ,Float.valueOf(arr[1])); 
			else e= new SteinerEdge(nodes.get(arr[2]), arr[0].intern(), nodes.get(arr[3]),Float.valueOf(arr[1]));
				
				
			graph.get(nodes.get(arr[2])).add(e);
			graph.get(nodes.get(arr[3])).add(e);
			
			line=br.readLine();
			
		}
		System.out.println(k + " edges loaded...");
		br.close();
	}
	
	public void loadGraphFromFile(String filename)throws IOException{
		graph= new HashMap<>();
		nodes = new HashMap<>();
		//FileReader fr = new FileReader("d:\\DBLPgraph\\IMDBSTAR.txt");
		FileReader fr = new FileReader(filename);
		//FileReader fr = new FileReader("d:\\DBLPgraph\\DBLPSTAR.txt");
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		int k=0;
		while(line!=null){
			k++;
			//if(k==30000) break;
			String[]arr = line.split(" : ");
			boolean isArg1=arr[4].equals("true");
			
			//D.p(isArg1);D.p();
			
			if(!nodes.containsKey(
					arr[2])){
				nodes.put(arr[2], new SteinerNode(arr[2]));
				graph.put(nodes.get(arr[2]), new TreeSet<SteinerEdge>());
			}
			if(nodes.get(arr[3])==null){
				nodes.put(arr[3], new SteinerNode(arr[3]));
				graph.put(nodes.get(arr[3]), new TreeSet<SteinerEdge>());
			}
			SteinerEdge e;
			if(isArg1) e= new SteinerEdge(nodes.get(arr[3]), arr[0].intern(), nodes.get(arr[2]) ,Float.valueOf(arr[1])); 
			else e= new SteinerEdge(nodes.get(arr[2]), arr[0].intern(), nodes.get(arr[3]),Float.valueOf(arr[1]));
				
				
			graph.get(nodes.get(arr[2])).add(e);
			graph.get(nodes.get(arr[3])).add(e);
			
			line=br.readLine();
			
		}
		System.out.println(k + " edges loaded...");
		br.close();
	}
	
	
	protected Set<SteinerNode>terminalNodes;
	protected ApprSteinerTree steinerTree;
	protected List<Queue<SteinerNode>> iterators;
	protected List<HashMap<String, SteinerNode>>visitedNodes;
	protected Map<String, SteinerNode> forbiddenNodes;
	protected Map<String, SteinerNode> almostForbiddenNodes;
	protected Queue<ResultGraph> resultQueue; 
	/** Relations to exclude in interconnections */
	// added by mohsen
	protected List<ApprSteinerTree> addedSteinerTrees;

	
	int accessedEdges=0;
	

	
	public TopKSteinertrees() {
		// TODO Auto-generated constructor stub
		// added by mohsen
		this.addedSteinerTrees = new LinkedList<>();
	}
	
	
	
	
	/**
	 * 
	 * @param terminals the terminal nodes for which the Steiner trees are going to be constructed
	 * @throws Exception
	 */
	public TopKSteinertrees(TreeSet<SteinerNode> terminals)throws Exception {
		terminalNodes=terminals;
		iterators= new ArrayList<>(terminals.size());
		visitedNodes= new ArrayList<>();
		forbiddenNodes = new HashMap<>();
		almostForbiddenNodes = new HashMap<>();
		resultQueue= new LinkedList<>();
		
		// added by mohsen
		this.addedSteinerTrees = new LinkedList<>();

		//for each terminal set an iterator
		for(int i=0; i< terminals.size(); i++){
			iterators.add(new LinkedList<SteinerNode>());
			visitedNodes.add(new HashMap<String, SteinerNode>());
		}
		
		Iterator<SteinerNode> iteratorOfTerminals = terminalNodes.iterator();
	
		
		for(Queue<SteinerNode> queue: iterators){
			queue.offer(iteratorOfTerminals.next());
			//System.out.println(iteratorOfTerminals.next().getNodeId());
		}
	}
	
	
	
	/**
	 * This method reconstructs the first tree given the node (ancestor) that was reached
	 * by each of the iterators.
	 * @param ancestor the root node in a taxonomic tree
	 * @throws Exception 
	 */
	public void getTaxonomicTree(SteinerNode ancestor) throws Exception{
		TreeSet<SteinerNode> treeNodes = new TreeSet<>();
		Map<String , SteinerNode> taxonomicTreeNodes= new HashMap<>();
		taxonomicTreeNodes.put(ancestor.getNodeId(),new SteinerNode(ancestor.getNodeId()));
		for(Map<String, SteinerNode> pnM:  visitedNodes){
			SteinerNode newNode=pnM.get(ancestor.getNodeId());
			SteinerNode stNode=new SteinerNode(ancestor.getNodeId());
			while(newNode!= null){
				
				if (newNode.predecessor!= null){
					if(taxonomicTreeNodes.containsKey(stNode.getNodeId())){
						SteinerNode preNode=new SteinerNode(newNode.predecessor.getNodeId());
						if(!taxonomicTreeNodes.containsKey(preNode.getNodeId())){
							boolean isArg1=newNode.getEdgeToNode(newNode.predecessor).sourceNode.equals(stNode);
							preNode.addEdge(taxonomicTreeNodes.get(stNode.getNodeId()), 
									isArg1,newNode.getEdgeLabelToNode(newNode.predecessor),
									newNode.getEdgeToNode(newNode.predecessor).weight());
							taxonomicTreeNodes.put(preNode.getNodeId(), preNode);
						}
						stNode= new SteinerNode(newNode.predecessor.getNodeId());
					}
				}
				newNode=newNode.predecessor;
			}
		}
		clean(taxonomicTreeNodes);
		treeNodes.addAll(taxonomicTreeNodes.values());
		steinerTree=new ApprSteinerTree(terminalNodes, treeNodes);
	}
	
	/**
	 * cleans a result tree, that is, nodes that are no terminlas and have a degree of 1 are
	 * deleted from the tree since they just increase the weight of the tree...
	 * @param map
	 */
	protected void clean (Map<String, SteinerNode> map){
		LinkedList<SteinerNode> outliers = new LinkedList<>();
		for(SteinerNode n: map.values())
			if(!terminalNodes.contains(n)&& n.getDegree()==1)
				outliers.add(n);
		for(SteinerNode n: outliers)
			while(n.getDegree()<2 && !terminalNodes.contains(n)){
				SteinerNode node=n.getNeighbors().first();
				node.edges.remove(node.getEdgeToNode(n));
				map.remove(n.getNodeId());
				n=node;
			}
				
	}
	
	
	
	/**
	 * finds out whether all iterators hav visited the given node n
	 * @param n the node for which the check is being done
	 * @return true in case all iterators have visited the node n
	 * @throws Exception
	 */
	public boolean isCommonAncestor(SteinerNode n)throws Exception{
		boolean isCAncestor= true;
		for(Map<String, SteinerNode> pnM: visitedNodes){
			if(!pnM.containsKey(n.getNodeId())) isCAncestor=false;
		}
		
		if(isCAncestor){
			//n.print(); D.p(" Das ist es!!!");
			getTaxonomicTree(n);
			return isCAncestor;
		}
		return isCAncestor;
	}
	
	
	/**
	 * 
	 * @param v node which was reached by one of the iterators, note that
	 * there is a node with the same name as v in initialSet2
	 * @param visitedNodes nodes visited by the iterator by which v is reached
	 * @param initialSet1 one initial set of nodes resulting from the removal of a loosepath.
	 * The iterator that reaches v has started from this inital set.
	 * @param initialSet2 the other initial set of nodes resulting from the removal of a loosepath.
	 * @return a new tree which has a path that connects the two initial sets
	 */
	
	protected ApprSteinerTree getApprTreeFrom(SteinerNode v, Map<String, SteinerNode>visitedNodes, 
			Set<SteinerNode>initialSet1, Set<SteinerNode>initialSet2){
		
		v=visitedNodes.get(v.name());
		for(SteinerNode n: initialSet2){
			if(v.equals(n)){
				//v.print();
				n.addEdge(v.predecessor, !v.wasArg1, v.relationToPredecessor,v.weightToPredecessor);
				break;
			}
		}
		v=v.predecessor;
		Map<String, SteinerNode> resultSet= new HashMap<>();
		Map<String, SteinerNode> treeNodeSet= new HashMap<>();
		for(SteinerNode n: initialSet1){resultSet.put(n.name(), n);treeNodeSet.put(n.name(), new SteinerNode(n.name()));}
		for(SteinerNode n: initialSet2){resultSet.put(n.name(), n);treeNodeSet.put(n.name(), new SteinerNode(n.name()));}
		//follow the predecessors of v until a predecessor
		//is contained in initialSet1
		while(!initialSet1.contains(v)){
			SteinerNode newNode = v.predecessor;
			v.addEdge(v.predecessor, !v.wasArg1, v.relationToPredecessor, v.weightToPredecessor);
			resultSet.put(v.name(),v);
			treeNodeSet.put(v.name(), new SteinerNode(v.name()));
			v = newNode;
		}
		// make sure all edges are ok!
		TreeSet<SteinerEdge> copyOfEdges= new TreeSet<>();
		for(SteinerNode n: resultSet.values()){
			copyOfEdges.addAll(n.edges);
		}
		for(SteinerEdge e: copyOfEdges){
			treeNodeSet.get(e.sourceNode.name()).addEdge(
					treeNodeSet.get(e.sinkNode.name()), false, e.getEdgeLabel(), e.weight());
		}
		Set<SteinerNode> treeNodes= new TreeSet<>();
		treeNodes.addAll(treeNodeSet.values());
		//D.p(resultSet.values());
		return new ApprSteinerTree(terminalNodes, treeNodes);
		
	}
	
	/**
	 * 
	 * @param v node reached by the iterator that started from initialSet1
	 * @param visitedNodes1 nodes visited by the iterator that started from initialSet1
	 * @param visitedNodes2 nodes visited by the iterator that started from initialSet2
	 * @param initialSet1 one initial set of nodes resulting from the removal of a loosepath.
	 * The iterator that reaches v has started from this inital set.
	 * @param initialSet2 the other initial set of nodes resulting from the removal of a loosepath.
	 * @return a new tree which has a path that connects the two initial sets
	 */
	protected ApprSteinerTree getApprTreeFrom(SteinerNode v, Map<String, SteinerNode>visitedNodes1, 
			 Map<String, SteinerNode>visitedNodes2, Set<SteinerNode>initialSet1, 
			 Set<SteinerNode>initialSet2){
		Map<String, SteinerNode> resultSet= new HashMap<>();
		for(SteinerNode n: initialSet1)resultSet.put(n.name(), n);
		for(SteinerNode n: initialSet2)resultSet.put(n.name(), n);
		SteinerNode newNode= v.predecessor;
		SteinerNode n=visitedNodes2.get(v.name());
		n.addEdge(newNode, 
				!v.wasArg1, 
				v.relationToPredecessor, 
				v.weightToPredecessor);
		
		
		while(!initialSet2.contains(n)){
			SteinerNode m= n.predecessor;
			n.addEdge(m, !n.wasArg1, n.relationToPredecessor, n.weightToPredecessor);
			resultSet.put(n.name(),n);
			n = m;
		}
		
		while(!initialSet1.contains(newNode)){
			SteinerNode m= newNode.predecessor;
			newNode.addEdge(m, !newNode.wasArg1, newNode.relationToPredecessor, newNode.weightToPredecessor);
			resultSet.put(newNode.name(),newNode);
			newNode = m;
		}
		//make sure all edges are ok!
		for(SteinerNode node: resultSet.values()){
			TreeSet<SteinerEdge> copyOfEdges= new TreeSet<>();
			copyOfEdges.addAll(node.edges);
			for(SteinerEdge e: copyOfEdges){
				node.edges.remove(e);
				node.addEdge(new SteinerEdge(resultSet.get(e.sourceNode.name()), 
						e.getEdgeLabel(), resultSet.get(e.sinkNode.name()),e.weight()));
			}
		}
		Set<SteinerNode> treeNodes= new TreeSet<>();
		treeNodes.addAll(resultSet.values());
		//D.p(resultSet.values());
		return new ApprSteinerTree(terminalNodes, treeNodes);
	}
	
	/**
	 * This method constructs a first teaxonomic tree that contains all the 
	 * terminals.
	 * @throws Exception
	 */
	public abstract void buildTaxonomicGraph() throws Exception;
	
	/**
	 * 
	 * @param k number of steiner trees to be returned
	 * @return Queue contining top-k steiner trees
	 * @throws Exception
	 */
	public abstract Queue<ResultGraph> getTopKTrees(int k)throws Exception;




	public ApprSteinerTree getSteinerTree() {
		return steinerTree;
	}




	public Queue<ResultGraph> getResultQueue() {
		return resultQueue;
	}

}
