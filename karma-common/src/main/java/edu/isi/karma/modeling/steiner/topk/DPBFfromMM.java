package edu.isi.karma.modeling.steiner.topk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;


/**
 * This class is an implementation of the dynamic programming Steiner Tree algorithm 
 * proposed by Ding et al. (ICDE 2007 ). It retrieves the optimal Steiner tree for
 * a given set of terminals, and can also be applied to retrieve top-k results where
 * only the first result is optimal (e.g. the second tree retrieved by the algorithm
 * may be only an approximation of the second best Steiner tree).
 * 
 * @author kasneci
 *
 */
public class DPBFfromMM extends TopKSteinertrees {
	
	public DPBFfromMM() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * instantiation with the received terminals
	 * @param terminals terminal nodes
	 * @throws Exception
	 */
	public DPBFfromMM(TreeSet<SteinerNode> terminals)throws Exception{
		super(terminals);
	}
	
	/**
	 * For efficiency issues...
	 * This class implements a main memory friendly :-) version
	 * of (Steiner) tree.
	 * @author kasneci
	 *
	 */
	class Tree{
		double score;
		Set<SteinerEdge> edges;
		Set<SteinerNode> terminals;
		SteinerNode root;
		
		public Tree() {
			score=0;
			edges= new TreeSet<SteinerEdge>();
			terminals=new TreeSet<SteinerNode>();
			// TODO Auto-generated constructor stub
		}
		
	}
	
	
	/**
	 * constructs an ApprSteinerTree from a given Tree object
	 * @param t Tree object
	 * @return ApprSteinerTree
	 */
	protected ApprSteinerTree getApprTree(Tree t){
		Map<String, SteinerNode> nodes= new HashMap<String, SteinerNode>();
		for(SteinerEdge e: t.edges){
			if(!nodes.containsKey(e.sourceNode.name()))
				nodes.put(e.sourceNode.name(), new SteinerNode(e.sourceNode.name()));
			if(!nodes.containsKey(e.sinkNode.name()))
				nodes.put(e.sinkNode.name(), new SteinerNode(e.sinkNode.name()));
			nodes.get(e.sinkNode.name()).addEdge(nodes.get(e.sourceNode.name()), 
					true, e.getEdgeLabel(), e.weight());
			
		}
		clean(nodes);
		Set<SteinerNode> treeNodes= new TreeSet<SteinerNode>();
		treeNodes.addAll(nodes.values());
		return new ApprSteinerTree(terminalNodes, treeNodes);
	}
	
	
	/**
	 * tells whether two sets of SteinerNodes are equal
	 * @param set1
	 * @param set2
	 * @return true if set1 and set2 kontain the same nodes, false otherwise
	 */
	private boolean areEqual(TreeSet<SteinerNode> set1, TreeSet<SteinerNode> set2){
		String set1String= "", set2String="";
		
		for(SteinerNode n: set1){
			set1String=set1String+n.name();
		}
		for(SteinerNode n: set2){
			set2String=set2String+n.name();
		}
		return (set1String.equals(set2String));
	}
	
	/**
	 * creates an id from a given root node and terminalnodes
	 * @param rootNode
	 * @param terminals
	 * @return String id
	 */
	private String getStringId(SteinerNode rootNode, Set<SteinerNode> terminals){
		String str = rootNode.name()+"*";
		for(SteinerNode n:terminals)
			str=str+n.name();
		return str;
	}

	
	/**
	 * constructs a list of all possible subsets of a given set of terminal
	 * nodes
	 * @param terminals set of terminals
	 * @return powerset of the given terminals
	 */
	private List<Set<SteinerNode>> powerSet(List<SteinerNode> terminals){
		int powNum = (int)Math.pow(2,terminals.size());
		//D.p(terminals);
		List<Set<SteinerNode>> powSetOfTerminals = new LinkedList<Set<SteinerNode>>();
	
		for(int i=0; i<powNum; i++){
			
			TreeSet<SteinerNode> terminalSet = new TreeSet<SteinerNode>();
			String binString=Integer.toBinaryString(i);
			
			for(int j=0; j<binString.toCharArray().length; j++){
				if(binString.charAt(j)=='1'){
					terminalSet.add(terminals.get(j));
				}
			}
			powSetOfTerminals.add(terminalSet);
		}
		return powSetOfTerminals;
	}
	
	
	@Override
	public void buildTaxonomicGraph() throws Exception {
		// TODO Auto-generated method stub

	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Queue<ResultGraph> getTopKTrees(int k) throws Exception {
		
		// priority queue that stores the candidate trees
		Queue<Tree> candidateTrees = new PriorityQueue<Tree>(10, 
				new Comparator<Tree>(){
			public int compare(Tree t1, Tree t2){
				if(t1.score==t2.score) return 0;
				else if(t1.score<t2.score)return -1;
				else return 1;
			}
		});
		
		// maps the root-terminal id of a tree to its tree
		Map<String, Tree> rootsToTrees = new HashMap<String, Tree>();
		
		for(SteinerNode n: terminalNodes){
			
			//terminals for the new tree
			Tree t= new Tree();
			t.terminals.add(n);
			t.root=n;
			
			// put tree into the queue
			candidateTrees.offer(t);
			String treeId= getStringId(t.root, t.terminals);
			rootsToTrees.put(treeId, t);
		}
		
		while(!candidateTrees.isEmpty()){
			Tree t=candidateTrees.poll();
			
			// when a tree contains all terminals it can be added to the results
			if(areEqual((TreeSet)t.terminals,(TreeSet)terminalNodes)){
				
				ApprSteinerTree steinerTree = getApprTree(t);
				boolean isNewTree = true;
				for (ApprSteinerTree st : addedSteinerTrees) {
					if (st.compareTo(steinerTree) == 0) {
						isNewTree = false;
						break;
					}
				}
				if (isNewTree) {
					resultQueue.add(steinerTree.toResultGraph());
					addedSteinerTrees.add(steinerTree);
				}

//				resultQueue.add(getApprTree(t).toResultGraph());
				//D.p(getApprTree(t));
			}
			
			// breaking condition
			if(resultQueue.size()==k) return resultQueue;
			
			Iterator<SteinerEdge> rs= graph.get(t.root).iterator();
			
			while(rs.hasNext()){
				
				SteinerEdge e= rs.next();
				accessedEdges++;
				
				Tree t1=rootsToTrees.get(getStringId(t.root.getNeighborInEdge(e), t.terminals));
				if(t1!=null){
					// check whether a new tree with a new root needs to be constructed
					if(t.score+e.weight()<t1.score){
						Tree tNew=new Tree();
						tNew.terminals.addAll(t.terminals);
						tNew.edges.addAll(t.edges);
						tNew.edges.add(e);
						tNew.root=t1.root;
						tNew.score=t.score+e.weight();
						
						// put it into the queue of candidate trees
						candidateTrees.offer(tNew);
						
						// remember it for the given root and the given terminals
						rootsToTrees.put(getStringId(tNew.root, tNew.terminals), tNew);
					}
				}
				
				// construct a new tree if its root-terminal id is not contained in rootsToTrees
				else{
					Tree tNew=new Tree();
					tNew.terminals.addAll(t.terminals);
					tNew.edges.addAll(t.edges);
					tNew.edges.add(e);
					tNew.root=t.root.getNeighborInEdge(e);
					tNew.score=t.score+e.weight();
					
					// put it into the queue of candidate trees
					candidateTrees.offer(tNew);
					
					// remember it for the given root and the given terminals
					rootsToTrees.put(getStringId(tNew.root, tNew.terminals), tNew);
				}
		
				// get the terminals of t
				Set<SteinerNode> currentTerminals = new TreeSet<SteinerNode>();
				currentTerminals.addAll(t.terminals);
				
				//D.p(currentTerminals);
				
				// get all other terminals (that are not contained in t)
				List<SteinerNode> remainingTerminals= new ArrayList<SteinerNode>();
				for(SteinerNode n: terminalNodes)
					if(!currentTerminals.contains(n))
						remainingTerminals.add(n);
				
				// for each subset of the remaining terminals
				for(Set<SteinerNode> terminalSet : powerSet(remainingTerminals)){
					
					Set<SteinerNode> unionOfTerminalSets= new TreeSet<SteinerNode>();
					unionOfTerminalSets.addAll(terminalSet);
					unionOfTerminalSets.addAll(currentTerminals);
					
					
					Tree t2=rootsToTrees.get(getStringId(t.root, currentTerminals));
					Tree t3=rootsToTrees.get(getStringId(t.root, terminalSet));
					Tree t4=rootsToTrees.get(getStringId(t.root, unionOfTerminalSets));
					
					if(t2!=null && t3!=null){ 
						
						if(t4!=null){
							// check whether new tree needs to be constructed
							if(t2.score+t3.score<t4.score){
								
								Tree tNew= new Tree();
								tNew.terminals.addAll(t2.terminals);
								tNew.terminals.addAll(t3.terminals);
								tNew.edges.addAll(t2.edges);
								tNew.edges.addAll(t3.edges);
								tNew.root=t.root;
								tNew.score=t2.score+t3.score;
								
								// put it into the queue of candidate trees
								candidateTrees.offer(tNew);
								
								// remember it for the given root and the given terminals
								rootsToTrees.put(getStringId(tNew.root, tNew.terminals), tNew);
							}
						}
						
						// construct a new tree
						else {
							Tree tNew= new Tree();
							tNew.terminals.addAll(t2.terminals);
							tNew.terminals.addAll(t3.terminals);
							tNew.edges.addAll(t2.edges);
							tNew.edges.addAll(t3.edges);
							tNew.root=t.root;
							tNew.score=t2.score+t3.score;
							
							// put it into the queue of candidate trees
							candidateTrees.offer(tNew);
							
							// remember it for the given root and the given terminals
							rootsToTrees.put(getStringId(tNew.root, tNew.terminals), tNew);
						}
					}
				}
			}
		}
		// TODO Auto-generated method stub
		return resultQueue;
	}
	
	public void getBestTree()throws Exception{
		getTopKTrees(1);
	}

}
