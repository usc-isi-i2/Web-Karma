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
	
	private HashMap<SteinerNode,SteinerNode> recurseNodeMap;
	private List<HashMap<SteinerNode,SteinerNode>> shortestNodeIndex;

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
					else if(n1.distancesToSources[0]<n2.distancesToSources[0])return -1;
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
		
		shortestNodeIndex = new ArrayList<HashMap<SteinerNode, SteinerNode>>();
		
		for(int i=0; i<terminalNodes.size();i++){
			banksIterators.offer(new BANKSIterator());
			shortestNodeIndex.add(new HashMap<SteinerNode,SteinerNode>());
		}
		
		recurseNodeMap = new HashMap<SteinerNode, SteinerNode>();
		
	}
	
	
	
	/**
	 * reconstructs a result tree
	 * @param ancestor the root reached by all of the iterators
	 * @throws Exception
	 */
	private void getApprTree(SteinerNode ancestor) throws Exception{
		TreeSet<SteinerNode> steinerNodes = new TreeSet<SteinerNode>();
		Map<String , SteinerNode> treeNodes= new HashMap<String, SteinerNode>();
		
		SteinerNode replacedNode = null;
		if (this.recurseNodeMap.containsKey(ancestor)) {
			replacedNode = this.recurseNodeMap.get(ancestor);
			treeNodes.put(replacedNode.name,new SteinerNode(replacedNode.name));
		}
		else
		{
			treeNodes.put(ancestor.name(),new SteinerNode(ancestor.name()));
		}
		
		String newNodeName;
		SteinerNode searchNode;
		for(int i = 0; i < visitedNodes.size(); i++){
			Map<String, SteinerNode> pnM = visitedNodes.get(i);
			if (pnM.get(ancestor.name()) == null && this.recurseNodeMap.containsKey(ancestor)) {
				searchNode = shortestNodeIndex.get(i).get(replacedNode);
			} else {
				searchNode = ancestor;
			}
			SteinerNode newNode=pnM.get(searchNode.name);
			newNodeName = newNode.name;
			if (this.recurseNodeMap.containsKey(newNode))
				newNodeName = this.recurseNodeMap.get(newNode).name;
			
			while(newNode.predecessor!= null){
				SteinerNode preNode=new SteinerNode(newNode.predecessor.name());
				if (this.recurseNodeMap.containsKey(preNode)) {
					preNode = new SteinerNode(this.recurseNodeMap.get(preNode).name);
				}
				if(!treeNodes.containsKey(preNode.name)){
						
					try {
						preNode.addEdge(
							treeNodes.get(newNodeName), 
							newNode.wasArg1,
							newNode.relationToPredecessor, 
							newNode.weightToPredecessor);
							
						treeNodes.put(preNode.name(), preNode);
					} catch (Exception e) {
						System.out.println("exception");
					}
				}
				newNode=newNode.predecessor;
				newNodeName = newNode.name;
				if (this.recurseNodeMap.containsKey(newNode))
					newNodeName = this.recurseNodeMap.get(newNode).name;
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
//		System.out.println(steinerTree.toString());
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
		List<Map<String, SteinerNode>> processedNodesHelper= 
				new ArrayList<Map<String, SteinerNode>>();

		
		List<HashMap<String, Integer>> recursesForNodesInQueue = 
				new ArrayList<HashMap<String, Integer>>();
		int maxrecurseCount = 100;
		
		//mark nodes in the iterators as visited
		int j=0;
		for(Queue<SteinerNode> queue: iterators){
			//instatiate processedNodes for each iterator
			processedNodes.add(new HashMap<String, SteinerNode>());
			processedNodesHelper.add(new HashMap<String, SteinerNode>());
			recursesForNodesInQueue.add(new HashMap<String, Integer>());
			
			Object[] arr =banksIterators.toArray();
			for(SteinerNode n: queue){
				visitedNodes.get(j).put(n.name(),n);
				shortestNodeIndex.get(j).put(n, n);

				((BANKSIterator)arr[j]).banksIterator.offer(n);
			}
			j++;
		}
		
		String newId;
		
		SteinerNode replacedNode;
		
		while(count<k){
			
			//expanding iterators in turn
			while(true){
//				for (BANKSIterator queue : banksIterators) {
//					System.out.println(queue.id + "-distance:" + queue.distanceToSource);
//				}
				BANKSIterator queue=banksIterators.poll();
				if (queue == null || count > k) return; //FIXME
				if(!queue.banksIterator.isEmpty()){
					SteinerNode n= (SteinerNode)queue.banksIterator.poll();
				
					//mark n as processed
					processedNodes.get(queue.id).put(n.name(), n);
					System.out.println("poll" + queue.id + ":" + queue.distanceToSource + "-->" + n.name);
					
					replacedNode = n;
					if (this.recurseNodeMap.containsKey(n)) {
						replacedNode = this.recurseNodeMap.get(n);
					}

					processedNodesHelper.get(queue.id).put(replacedNode.name(), replacedNode);

					if(isCommonAncestor(n, processedNodesHelper)){
						count++;
					}
					
					Queue<SteinerEdge> rs= new PriorityQueue<SteinerEdge>(10, new Comparator<SteinerEdge>(){
						public int compare(SteinerEdge e1, SteinerEdge e2){
							if(e1.weight()>e2.weight()) return 1;
							else if (e1.weight()<e2.weight()) return -1;
							else return 0;
						}
					});
											
					rs.addAll(graph.get(replacedNode));
					
					while(!rs.isEmpty()){
						
						accessedEdges++;
						SteinerNode newNode=null;
						
						// getting (creating) neighbor of n
						SteinerEdge e= rs.poll();
						
						if(!replacedNode.equals(e.sourceNode)){
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
							
							Integer recurseCount = recursesForNodesInQueue.get(queue.id).get(v.name);
							if (recurseCount == null) {
								recurseCount = 2;
							} else if (recurseCount < maxrecurseCount) {
								recurseCount = recurseCount + 1;
							} else {
								continue;
							}
							
							recursesForNodesInQueue.get(queue.id).put(v.name, recurseCount);

//							newNode=v;
//							if(newNode.distancesToSources[0]>n.distancesToSources[0]+e.getWeight()){ //FIXME
//								newNode.distancesToSources[0]=n.distancesToSources[0]+e.getWeight();
//								newNode.predecessor= n;
//								newNode.relationToPredecessor=e.getEdgeLabel();
//								newNode.weightToPredecessor=e.getWeight();
//								
//								// reinsert the node into the queue;
//									queue.banksIterator.remove(newNode);
//									queue.banksIterator.offer(newNode);
//								
//							}
													
							newId = queue.id + ":" + newNode.name + "///" + (recurseCount);							
							newNode.name = newId;
							newNode.distancesToSources[0]=n.distancesToSources[0]+e.getWeight();
							newNode.predecessor= n;
//							System.out.println(queue.banksIterator.peek().name);
//							System.out.println(queue.banksIterator.peek().distancesToSources[0]);
							queue.banksIterator.add(newNode);		
//							System.out.println(queue.banksIterator.peek().name);
//							System.out.println(queue.banksIterator.peek().distancesToSources[0]);
							recurseNodeMap.put(newNode, v);
							visitedNodes.get(queue.id).put(newNode.name(), newNode);
//							System.out.println("visited node: " + v.getNodeId());
//							System.out.println("offer" + queue.id + ":" + newNode.distancesToSources[0] + "-->" + newNode.name);

							if(newNode.distancesToSources[0] < shortestNodeIndex.get(queue.id).get(v).distancesToSources[0])
								shortestNodeIndex.get(queue.id).put(v, newNode);

						}
						
						//in case newNode has not been visited
						else
						{
							newNode.distancesToSources[0]=n.distancesToSources[0]+e.getWeight();
							newNode.predecessor= n;
							
							queue.banksIterator.add(newNode);
							visitedNodes.get(queue.id).put(newNode.name(), newNode);
							shortestNodeIndex.get(queue.id).put(newNode, newNode);
							System.out.println("offer" + queue.id + ":" + newNode.distancesToSources[0] + "-->" + newNode.name);
						}	
	
//						if(isCommonAncestor(newNode)){
//							count++;
//						}	
					}
				}
				if (!queue.banksIterator.isEmpty()) {
					queue.distanceToSource=queue.banksIterator.peek().distancesToSources[0];
					banksIterators.add(queue);
				}
			}
		}
	}
	
	public boolean isCommonAncestor(SteinerNode n)throws Exception{
		boolean isCAncestor= true;

		SteinerNode searchFor = n;
		if (this.recurseNodeMap.containsKey(n))
			searchFor = this.recurseNodeMap.get(n);

		for(Map<String, SteinerNode> pnM: visitedNodes){
			if(!pnM.containsKey(searchFor.name())) isCAncestor=false;
		}
	
		if(isCAncestor){
			
			getApprTree(n);
			return isCAncestor;
		}
		return isCAncestor;
	}
	
	public boolean isCommonAncestor(SteinerNode n, List<Map<String, SteinerNode>> processedNodes)throws Exception{
		boolean isCAncestor= true;

		SteinerNode searchFor = n;
		if (this.recurseNodeMap.containsKey(n))
			searchFor = this.recurseNodeMap.get(n);

		for(Map<String, SteinerNode> pnM: processedNodes){
			if(!pnM.containsKey(searchFor.name())) isCAncestor=false;
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
