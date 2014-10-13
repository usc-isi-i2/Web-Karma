package edu.isi.karma.modeling.steiner.topk;

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

public class STARfromMM extends TopKSteinertrees{
	
	
	public STARfromMM() {
		// TODO Auto-generated constructor stub
	}
	
	public STARfromMM(TreeSet<SteinerNode> terminals) throws Exception {
		super(terminals);
		
	}
	
	
	/**
	 * builds the taxonomic graph...
	 * exploits only means, type, and subClassOf edges
	 */
	public void buildTaxonomicGraph() throws Exception{
		boolean commonAncestorFound=false;
		while(!commonAncestorFound){
			int i=0; 
			for(Queue<SteinerNode> queue: iterators){
				if(!queue.isEmpty()){
					SteinerNode n= queue.poll();
					visitedNodes.get(i).put(n.getNodeId(), n);
					
					Iterator <SteinerEdge> rs = graph.get(n).iterator();
						while(rs.hasNext()){
							SteinerNode newNode = null;
							SteinerEdge e= rs.next();
							accessedEdges++;
							if(e.sourceNode.equals(n)){
								newNode= new SteinerNode(e.getSinkNode().name());
								if(visitedNodes.get(i).containsKey(newNode.name())) continue;
								newNode.addEdge(n, true, e.getEdgeLabel(),e.getWeight());
							}
							else{
								newNode=new SteinerNode(e.getSourceNode().name());
								if(visitedNodes.get(i).containsKey(newNode.name())) continue;
								newNode.addEdge(n, false, e.getEdgeLabel(),e.getWeight());
								
							}
							//newNode.print();
							newNode.setPredecessor(n);
							visitedNodes.get(i).put(newNode.getNodeId(), newNode);
							
							if(isCommonAncestor(newNode)){
								commonAncestorFound=true;
								break;
							}
							queue.offer(newNode);
						}
					
				}
				if(commonAncestorFound)break;
				
				i=(i+1)%iterators.size();
				
			}
		}
	}
	
	
	
	public Queue<ResultGraph> getTopKTrees(int k)throws Exception{
		buildTaxonomicGraph();
		//cache all previous results
		//then there is no need to recompute them from scratch
		((LinkedList<ResultGraph>)resultQueue).add(steinerTree.toResultGraph());
		while (improveTree(steinerTree)){
			((LinkedList<ResultGraph>)resultQueue).addFirst(steinerTree.toResultGraph());
			if(resultQueue.size()==k)((LinkedList<ResultGraph>)resultQueue).removeLast();
			//if(steinerTree.score<2*terminalNodes.size()-3)break; //wellknown stopping heuristic
																//on sparse graphs
		}
		ApprSteinerTree tOld= steinerTree.clone();
		int countEquality=0;
		
		while(k>resultQueue.size()){
			forbiddenNodes.putAll(almostForbiddenNodes);
			
			Queue<SteinerSubTree> lpQueue= new PriorityQueue<SteinerSubTree>();
			lpQueue.addAll(steinerTree.getLoosePaths());
			int j=1; //loose path counter
			
			if(countEquality>=100)break;
			while(!lpQueue.isEmpty()){
				LoosePath lp=null;
				
				// take another loose path for improvement
				if(j<=lpQueue.size())
					for(int l=0; l<j; l++ )
						lp= (LoosePath)lpQueue.poll();
				
				// take longest loose path again 
				else{j=1; lp= (LoosePath)lpQueue.poll();}
				//lp.score=lp.score+0.4; 
				ApprSteinerTree tNew = replaceForTopK(steinerTree.clone(), lp);
				
				
				steinerTree=tNew;
				
				if(tOld.score==tNew.score){
					countEquality++;
				}
				if(countEquality>=100) break;
				if(tOld.compareTo(tNew)!=0){
					int i=0;
					for(ResultGraph t: resultQueue){
						if(t.getScore()<steinerTree.getScore()) i++;
						else break;
					}
					countEquality=0;
					((LinkedList<ResultGraph>)resultQueue).add(i, steinerTree.toResultGraph());
					tOld= steinerTree.clone();
				}
				
				forbiddenNodes.putAll(almostForbiddenNodes);
				almostForbiddenNodes= new HashMap<String, SteinerNode>();
				if(resultQueue.size()>=k)break;
				lpQueue=new PriorityQueue<SteinerSubTree>();
				lpQueue.addAll(steinerTree.getLoosePaths());
				j++;
			}
		}
		return resultQueue;
	}
	
	/**
	 * ...improves the current tree t
	 * @param t current Steiner tree
	 * @return improved Steiner tree
	 * @throws Exception
	 */
	public boolean improveTree(ApprSteinerTree t) throws Exception{
		Queue<LoosePath> lpQueue= new PriorityQueue<LoosePath>(1, new Comparator<LoosePath>(){
			public int compare(LoosePath lp1, LoosePath lp2){
				if(lp1.score>lp2.score) return -1;
				if(lp1.score<lp2.score) return -2;
				else return 0;
			}
		});
		lpQueue.addAll(t.getLoosePaths());
		
		boolean improved= false;
		while(!lpQueue.isEmpty()){
			LoosePath lp= (LoosePath)lpQueue.poll();
			ApprSteinerTree tNew = replace(t.clone(), lp);
			if(tNew.score<t.score){
				t=tNew;
				improved=true;
				break;
			}
		}
		return improved;
	}
	
	/**
	 * This method replaces the LoosePath lp with a new shorter path when possible
	 * @param t current Steiner tree
	 * @param lp the loose path to be replaced
	 * @return a smaller Steiner tree
	 * @throws Exception
	 */
	protected ApprSteinerTree replace(ApprSteinerTree t, LoosePath lp) throws Exception{
		List<Set<SteinerNode>> partition=t.getNodeSetPartitioning(lp);
		return findTreeWithHeuristicShortestPath(t, partition.get(0), partition.get(1), lp);
	}
	
	/**
	 * This method replaces the LoosePath lp with a new shorter path when possible.
	 * It uses a very fast heuristic for the top-k case.
	 * @param t current Steiner tree
	 * @param lp the loose path to be replaced
	 * @return a smaller Steiner tree
	 * @throws Exception
	 */
	protected ApprSteinerTree replaceForTopK(ApprSteinerTree t, LoosePath lp) throws Exception{
		List<Set<SteinerNode>> partition=t.getNodeSetPartitioning(lp);
		return findTreeShortestPathForTopK(t, partition.get(0), partition.get(1), lp);
	}
	
	/**
	 * This maethod finds the shortest path between the two subtrees of t which 
	 * result from removing the loose path lp from the current tree t
	 * @param t the current tree
	 * @param set1 first set of nodes resulting from removing the loose path lp from t
	 * @param set2 other set of nodes resulting from removing the loose path lp from t
	 * @param lp the loosepath to be replaced
	 * @return a smaller Steiner tree
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected ApprSteinerTree findTreeWithHeuristicShortestPath(ApprSteinerTree t, Set<SteinerNode> set1, 
			Set<SteinerNode> set2, LoosePath lp)throws Exception{
		//	copies of set1 and set2 (copies of initial sets)
		
		List<Set<SteinerNode>> partition=t.clone().getNodeSetPartitioning(lp);
		
		Set<SteinerNode>[]initialSets= new TreeSet[2];
		initialSets[0]=set1; initialSets[1]=set2;
		
		Queue<SteinerNode>[] queueArray =new Queue[2];
		
		Map<String,SteinerNode>[]visitedNodes = new HashMap[2];
		visitedNodes[0]= new HashMap<String, SteinerNode>(); 
		visitedNodes[1]= new HashMap<String, SteinerNode>();
		for(SteinerNode node: set1) visitedNodes[0].put(node.name(), node);
		for(SteinerNode node: set2)visitedNodes[1].put(node.name(), node);
		
		Map<String,SteinerNode>[]processedNodes = new HashMap[2];
		processedNodes[0]= new HashMap<String, SteinerNode>(); 
		processedNodes[1]= new HashMap<String, SteinerNode>();
		
		Queue<SteinerNode> q1= new PriorityQueue<SteinerNode>(10, new Comparator<SteinerNode>(){
			public int compare(SteinerNode node1, SteinerNode node2){
				if(node1.distancesToSources[0]>node2.distancesToSources[0]) return 1;
				else if (node1.distancesToSources[0]<node2.distancesToSources[0]) return -1;
				else return 0;
			}
			
		});
		q1.addAll(set1);
		
		Queue<SteinerNode> q2= new PriorityQueue<SteinerNode>(10, new Comparator<SteinerNode>(){
			public int compare(SteinerNode node1, SteinerNode node2){
				if(node1.distancesToSources[1]>node2.distancesToSources[1]) return 1;
				else if (node1.distancesToSources[1]<node2.distancesToSources[1]) return -1;
				else return 0;
			}
		});
		q2.addAll(set2);
		queueArray[0]=q1;
		queueArray[1]=q2;
		
		int current =0;
		int other =1;
		
		//instantiating v...
		SteinerNode v = queueArray[current].peek(); 
		while(!q1.isEmpty()&&!q2.isEmpty()&& !initialSets[other].contains(v)){
			
			if(queueArray[other].size()<queueArray[current].size()){
				int help = current;
				current = other;
				other = help;
			}
			v = queueArray[current].poll(); 
			accessedEdges++;
			if(initialSets[other].contains(v))break;
			//in case v is a forbidden node don't consider it
			if(forbiddenNodes.containsKey(v.name())) continue;
			//in case distance of v from the source is greater than the
			//weight of lp don't consider v
			if(v.distancesToSources[current]>lp.score)continue;
			
			visitedNodes[current].put(v.name(), v);
			processedNodes[current].put(v.name(), v);
			
			Iterator<SteinerEdge> rs=graph.get(v).iterator();
			
			while (rs.hasNext()){
				
				SteinerNode n=null;
				
				
				SteinerEdge e= rs.next();
				//check whether v is arg1 or arg2
				if(!v.equals(e.sinkNode)) {
					n = new SteinerNode(e.sinkNode.name());
					n.wasArg1=false;
					
				}
				else {
					n = new SteinerNode(e.sourceNode.name());
					n.wasArg1=true;
				}
				
				//in case n is fobidden don't consider it
				if(forbiddenNodes.containsKey(n.name())) continue;
				
				// in case n has been processed (polled) don't consider it
				if(processedNodes[current].containsKey(n.name()))continue;
				
				n.relationToPredecessor=e.getEdgeLabel();
				n.weightToPredecessor=e.getWeight();
				
				//check whether n has been visited
				SteinerNode newNode = visitedNodes[current].get(n.name());
				if(newNode!=null){ 
					n=newNode;
					if(n.distancesToSources[current]>v.distancesToSources[current]+e.getWeight()){
						n.distancesToSources[current]=v.distancesToSources[current]+e.getWeight();
						n.predecessor= v;
						n.relationToPredecessor=e.getEdgeLabel();
						n.weightToPredecessor=e.getWeight();
					}	
				}
				
				// in case n has not been visited
				else{
					n.distancesToSources[current]=v.distancesToSources[current]+e.getWeight();
					n.predecessor= v;
					queueArray[current].offer(n);
					visitedNodes[current].put(n.name(), n);
				}
				
			}
		}
		
		//rebuild the tree starting from v
		if((q1.isEmpty()||q2.isEmpty())&& !initialSets[other].contains(v))return steinerTree;
		
		if(almostForbiddenNodes.isEmpty())
			almostForbiddenNodes.put(v.name(), v);
		return steinerTree = getApprTreeFrom(v, visitedNodes[current], 
			partition.get(current), partition.get(other));
	
	}
	
	/**
	 * This method applies a very fast heuristic to compute the shortest 
	 * path between the two subtrees of t which 
	 * result from removing the loose path lp from the current tree t. 
	 * It is used only in the top-k case.
	 * @param t the current tree
	 * @param set1 first set of nodes resulting from removing the loose path lp from t
	 * @param set2 other set of nodes resulting from removing the loose path lp from t
	 * @param lp the loosepath to be replaced
	 * @return a smaller Steiner tree
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected ApprSteinerTree findTreeShortestPathForTopK(ApprSteinerTree t, Set<SteinerNode> set1, 
			Set<SteinerNode> set2, LoosePath lp)throws Exception{
		//	copies of set1 and set2 (copies of initial sets)
		
		List<Set<SteinerNode>> partition=t.clone().getNodeSetPartitioning(lp);
		
		Set<SteinerNode>[]initialSets= new TreeSet[2];
		initialSets[0]=set1; initialSets[1]=set2;
		
		Queue<SteinerNode>[] queueArray =new Queue[2];
		
		Map<String,SteinerNode>[]visitedNodes = new HashMap[2];
		visitedNodes[0]= new HashMap<String, SteinerNode>(); 
		visitedNodes[1]= new HashMap<String, SteinerNode>();
		for(SteinerNode node: set1) visitedNodes[0].put(node.name(), node);
		for(SteinerNode node: set2)visitedNodes[1].put(node.name(), node);
		
		Map<String,SteinerNode>[]processedNodes = new HashMap[2];
		processedNodes[0]= new HashMap<String, SteinerNode>(); 
		processedNodes[1]= new HashMap<String, SteinerNode>();
		
		Queue<SteinerNode> q1= new PriorityQueue<SteinerNode>(10, new Comparator<SteinerNode>(){
			public int compare(SteinerNode node1, SteinerNode node2){
				if(node1.distancesToSources[0]>node2.distancesToSources[0]) return 1;
				else if (node1.distancesToSources[0]<node2.distancesToSources[0]) return -1;
				else return 0;
			}
			
		});
		q1.addAll(set1);
		
		Queue<SteinerNode> q2= new PriorityQueue<SteinerNode>(10, new Comparator<SteinerNode>(){
			public int compare(SteinerNode node1, SteinerNode node2){
				if(node1.distancesToSources[1]>node2.distancesToSources[1]) return 1;
				else if (node1.distancesToSources[1]<node2.distancesToSources[1]) return -1;
				else return 0;
			}
		});
		q2.addAll(set2);
		queueArray[0]=q1;
		queueArray[1]=q2;
		
		int current =0;
		int other =1;
		
		//instantiating v...
		SteinerNode v = queueArray[current].peek(); 
		while(!q1.isEmpty()&&!q2.isEmpty()&& !initialSets[other].contains(v)){
			
			if(queueArray[other].size()<queueArray[current].size()){
				int help = current;
				current = other;
				other = help;
			}
			v = queueArray[current].poll(); 
			accessedEdges++;
			if(initialSets[other].contains(v))break;
			//in case v is a forbidden node don't consider it
			if(forbiddenNodes.containsKey(v.name())) continue;
			//in case distance of v from the source is greater than the
			//weight of lp don't consider v
			if(v.distancesToSources[current]>lp.score)continue;
			
			visitedNodes[current].put(v.name(), v);
			processedNodes[current].put(v.name(), v);
			
			Iterator<SteinerEdge> rs=graph.get(v).iterator();
			
			while (rs.hasNext()){
				
				SteinerNode n=null;
				
				
				SteinerEdge e= rs.next();
				//check whether v is arg1 or arg2
				if(!v.equals(e.sinkNode)) {
					n = new SteinerNode(e.sinkNode.name());
					n.wasArg1=false;
					
				}
				else {
					n = new SteinerNode(e.sourceNode.name());
					n.wasArg1=true;
				}
				
				//in case n is fobidden don't consider it
				if(forbiddenNodes.containsKey(n.name())) continue;
				
				// in case n has been processed (polled) don't consider it
				if(processedNodes[current].containsKey(n.name()))continue;
				
				n.relationToPredecessor=e.getEdgeLabel();
				n.weightToPredecessor=e.getWeight();
				
				//check whether n has been visited
				SteinerNode newNode = visitedNodes[current].get(n.name());
				if(newNode!=null){ 
					n=newNode;
					if(n.distancesToSources[current]>v.distancesToSources[current]+e.getWeight()){
						n.distancesToSources[current]=v.distancesToSources[current]+e.getWeight();
						n.predecessor= v;
						n.relationToPredecessor=e.getEdgeLabel();
						n.weightToPredecessor=e.getWeight();
					}	
				}
				
				// in case n has not been visited
				else{
					n.distancesToSources[current]=v.distancesToSources[current]+e.getWeight();
					n.predecessor= v;
					queueArray[current].offer(n);
					visitedNodes[current].put(n.name(), n);
				}
				
				// rebuild the tree starting from n
				// early pruning
				// this can be done since the edges are odered ascending by weight
				if(visitedNodes[other].containsKey(n.name())){
					almostForbiddenNodes.put(n.name(), n);
					if(partition.get(current).contains(n)) return steinerTree = 
						getApprTreeFrom(n, visitedNodes[other], 
							partition.get(other), 
							partition.get(current));
					else if(partition.get(other).contains(n)) return steinerTree = 
						getApprTreeFrom(n, visitedNodes[current], 
						partition.get(current), partition.get(other));
					else return steinerTree = getApprTreeFrom(n, visitedNodes[current], visitedNodes[other], partition.get(current), partition.get(other)); 
				}
				
			}
		}
		//rebuild the tree starting from v
		if(q1.isEmpty()||q2.isEmpty())return steinerTree;
		if(partition.get(other).contains(v)){
			almostForbiddenNodes.put(v.name(), v);
			return steinerTree = getApprTreeFrom(v, visitedNodes[current], 
					partition.get(current), partition.get(other));
		}
		else if(partition.get(current).contains(v)){
			almostForbiddenNodes.put(v.name(), v);
			return steinerTree = getApprTreeFrom(v, visitedNodes[other], 
						partition.get(other), partition.get(current));
		}
		else{
			almostForbiddenNodes.put(v.name(), v);
			return steinerTree = getApprTreeFrom(v, visitedNodes[current], visitedNodes[other], partition.get(current), partition.get(other)); 
		}
	}
	
	public void getBestTree()throws Exception{
		getTopKTrees(1);
	}

}
