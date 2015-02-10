package edu.isi.karma.modeling.steiner.topk;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;

public class BANKSfromMM extends TopKSteinertrees {
	
	private int iteratorCounter=0;
	

	public BANKSfromMM() {
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * implements a BANKS iterator
	 * @author kasneci
	 *
	 */
	class BANKSIterator{
		int id;
		Queue<SteinerNode> banksIterator;
		double distanceToSource;
		BANKSIterator(){
			id=iteratorCounter;
			banksIterator= new PriorityQueue<SteinerNode>(10, new Comparator<SteinerNode>(){
				public int compare(SteinerNode n1, SteinerNode n2){
					if(n1.distancesToSources[0]>n2.distancesToSources[0]) return 1;
					else if(n1.distancesToSources[0]>n2.distancesToSources[0])return -1;
					else return 0;
				}
			});
			distanceToSource=0;
			iteratorCounter++;
		}
		
		
		
	}
	
	/*
	 * priority queue of BANKS SSSP (single source shortest paths) 
	 * iterators ordered by distance to sources
	 */
	protected Queue<BANKSIterator> banksIterators;
	
	public BANKSfromMM(TreeSet<SteinerNode> terminals) throws Exception {
		super(terminals);
	
		banksIterators=new PriorityQueue<BANKSIterator>(terminals.size(), new Comparator<BANKSIterator>(){
			public int compare(BANKSIterator it1, BANKSIterator it2){
				if(it1.distanceToSource>it2.distanceToSource) return 1;
				else if (it1.distanceToSource<it2.distanceToSource) return -1;
				else return 0;
			}
		});
		
		
		for(int i=0; i<terminalNodes.size();i++){
			banksIterators.offer(new BANKSIterator());
		}
		
		
		
	}
	
	
	
	/**
	 * reconstructs a result tree
	 * @param ancestor the root reached by all of the iterators
	 * @throws Exception
	 */
	private void getApprTree(SteinerNode ancestor) throws Exception{
		TreeSet<SteinerNode> steinerNodes = new TreeSet<SteinerNode>();
		Map<String , SteinerNode> treeNodes= new HashMap<String, SteinerNode>();
		treeNodes.put(ancestor.name(),new SteinerNode(ancestor.name()));
		for(Map<String, SteinerNode> pnM:  visitedNodes){
			SteinerNode newNode=pnM.get(ancestor.name());
			while(newNode.predecessor!= null){
				SteinerNode preNode=new SteinerNode(newNode.predecessor.name());
				if(!treeNodes.containsKey(preNode.name())){
					preNode.addEdge(
						treeNodes.get(newNode.name()), 
						newNode.wasArg1,
						newNode.relationToPredecessor, 
						newNode.weightToPredecessor);
						
					treeNodes.put(preNode.name(), preNode);
				}
				newNode=newNode.predecessor;
			}
		}
//		clean(treeNodes);
		steinerNodes.addAll(treeNodes.values());
		steinerTree=new ApprSteinerTree(terminalNodes, steinerNodes);

		for (ApprSteinerTree st : addedSteinerTrees) {
			if (st.compareTo(steinerTree) == 0)
				return;
		}
		addedSteinerTrees.add(steinerTree);
		// add tree to the result queue
		int pos=0;
		for(ResultGraph g: resultQueue)
			if(g.getScore()<steinerTree.score)pos++;
		((LinkedList<ResultGraph>)resultQueue).add(pos,steinerTree.toResultGraph());
	}
	
	
	
	/**
	 * finds the top-k interconnections by exploiting the BANKS technique
	 * @param k number of interconnections to be retrieved
	 * @throws Exception
	 */
	public void buildConnectivityGraph(int k) throws Exception{
		
		//counting computed results
		int count=0;
		
		//nodes polled from queues
		List<Map<String, SteinerNode>> processedNodes= 
				new ArrayList<Map<String, SteinerNode>>();
		
		//mark nodes in the iterators as visited
		int j=0;
		for(Queue<SteinerNode> queue: iterators){
			//instatiate processedNodes for each iterator
			processedNodes.add(new HashMap<String, SteinerNode>());
			
			Object[] arr =banksIterators.toArray();
			for(SteinerNode n: queue){
				visitedNodes.get(j).put(n.name(),n);
				((BANKSIterator)arr[j]).banksIterator.offer(n);
			}
			j++;
		}
		
		
		while(count<k){
			
			//expanding iterators in turn
			while(true){
				BANKSIterator queue=banksIterators.poll();
				if (queue == null || count > k) return; //FIXME
				if(!queue.banksIterator.isEmpty()){
					SteinerNode n= (SteinerNode)queue.banksIterator.poll();
				
					//mark n as processed
					processedNodes.get(queue.id).put(n.name(), n);
					
					
					Queue<SteinerEdge> rs= new PriorityQueue<SteinerEdge>(10, new Comparator<SteinerEdge>(){
						public int compare(SteinerEdge e1, SteinerEdge e2){
							if(e1.weight()>e2.weight()) return 1;
							else if (e1.weight()<e2.weight()) return -1;
							else return 0;
						}
					});
					
					rs.addAll(graph.get(n));
					
					
					while(!rs.isEmpty()){
						
						accessedEdges++;
						SteinerNode newNode=null;
						
						// getting (creating) neighbor of n
						SteinerEdge e= rs.poll();
						
						if(!n.equals(e.sourceNode)){
							newNode= new SteinerNode(e.sourceNode.name());
							newNode.wasArg1=true;
						}
						else{
							newNode= new SteinerNode(e.sinkNode.name());
							newNode.wasArg1=false;
						}
						
						if(processedNodes.get(queue.id).containsKey(newNode.name())) continue;
						
						newNode.relationToPredecessor=e.getEdgeLabel();
						newNode.weightToPredecessor=e.getWeight();
						
						//check whether newNode has been visited
						SteinerNode v=visitedNodes.get(queue.id).get(newNode.name());
						if(v!=null){
							newNode=v;
							if(newNode.distancesToSources[0]>n.distancesToSources[0]+e.getWeight()){ //FIXME
								newNode.distancesToSources[0]=n.distancesToSources[0]+e.getWeight();
								newNode.predecessor= n;
								newNode.relationToPredecessor=e.getEdgeLabel();
								newNode.weightToPredecessor=e.getWeight();
								
								// reinsert the node into the queue;
									queue.banksIterator.remove(newNode);
									queue.banksIterator.offer(newNode);
								
							}	
						}
						
						//in case newNode has not been visited
						else
						{
							newNode.distancesToSources[0]=n.distancesToSources[0]+e.getWeight();
							newNode.predecessor= n;
							queue.banksIterator.offer(newNode);
							visitedNodes.get(queue.id).put(newNode.name(), newNode);
						}	
	
						if(isCommonAncestor(newNode)){
							
							count++;
//							if(count>k-1)break;
								//break;
						}	
					}
				}
				if (!queue.banksIterator.isEmpty()) {
					queue.distanceToSource=queue.banksIterator.peek().distancesToSources[0];
					banksIterators.offer(queue);
				}
			}
		}
	}
	
	public boolean isCommonAncestor(SteinerNode n)throws Exception{
		boolean isCAncestor= true;
		for(Map<String, SteinerNode> pnM: visitedNodes){
			if(!pnM.containsKey(n.name())) isCAncestor=false;
		}
	
		if(isCAncestor){
			
			getApprTree(n);
			return isCAncestor;
		}
		return isCAncestor;
	}

	@Override
	public void buildTaxonomicGraph() throws Exception {
		// TODO Auto-generated method stub
		buildConnectivityGraph(1);
	}

	@Override
	public Queue<ResultGraph> getTopKTrees(int k) throws Exception {
		// TODO Auto-generated method stub
		buildConnectivityGraph(k);
		return resultQueue;
	}
	
	
	public void getBestTree()throws Exception{
		buildConnectivityGraph(1);
	}
	

}
