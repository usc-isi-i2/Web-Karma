/**
 * 
 */
package edu.isi.karma.modeling.research.graphmatching.util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author riesen
 * 
 */
public class TreeNode implements Comparable<TreeNode> {

	/** nodes of g1 are mapped to...*/
	private int[] matching;

	/** nodes of g2 are mapped to...*/
	private int[] inverseMatching;
	
	/** the current cost of this partial solution*/
	private double cost;

	/** the cost function defines the cost of individual node/edge operations*/
	private CostFunction cf;

	/** the adjacency-matrix of the graphs */
	private Edge[][] a1;
	private Edge[][] a2;

	/** the original graphs */
	private Graph originalGraph1;
	private Graph originalGraph2;
	
	/** the graphs where the processed nodes are removed */
	private Graph unusedNodes1;
	private Graph unusedNodes2;

	/** weighting factor for edge operations
	 * = 0.5 if undirected edges are used (1.0 otherwise)
	 * */
	private double factor;
	
		
	/**
	 * constructor for the initial empty solution
	 * @param g2
	 * @param g1
	 * @param cf
	 * @param factor
	 */
	public TreeNode(Graph g1, Graph g2, CostFunction cf, double factor) {
		this.unusedNodes1 = g1;
		this.unusedNodes2 = g2;
		this.originalGraph1 = g1;
		this.originalGraph2 = g2;
		this.a1 = g1.getAdjacenyMatrix();
		this.a2 = g2.getAdjacenyMatrix();
		this.cost = 0;
		this.cf = cf;
		this.matching = new int[g1.size()];
		this.inverseMatching = new int[g2.size()];
		for (int i = 0; i < this.matching.length; i++) {
			this.matching[i] = -1;
		}
		for (int i = 0; i < this.inverseMatching.length; i++) {
			this.inverseMatching[i] = -1;
		}
		this.factor = factor;

	}

	/**
	 * copy constructor in order to generate successors 
	 * of treenode @param o
	 */
	public TreeNode(TreeNode o) {
		this.unusedNodes1 = (Graph) o.getUnusedNodes1().clone();
		this.unusedNodes2 = (Graph) o.getUnusedNodes2().clone();
		this.cost = o.getCost();
		this.cf = o.getCf();
		this.matching =o.matching.clone();
		this.inverseMatching = o.inverseMatching.clone();
		this.a1 = o.a1;
		this.a2 = o.a2;
		this.originalGraph1 = o.originalGraph1;
		this.originalGraph2 = o.originalGraph2;
		this.factor = o.factor;

	}


	/**
	 * @return a list of successsors of this treenode (extended solutions to 
	 * *this* solution)
	 * @param factor = 0.5 if edges are undirected (since there are edges are in both directions)
	 */
	public LinkedList<TreeNode> generateSuccessors() {
		// list with successors
		LinkedList<TreeNode> successors = new LinkedList<TreeNode>();
		
		// all nodes of g2 are processed, the remaining nodes of g1 are deleted
		if (this.unusedNodes2.isEmpty()) {
			TreeNode tn = new TreeNode(this);
			int n = tn.unusedNodes1.size();
			int e = 0;
			Iterator<Node> nodeIter = tn.unusedNodes1.iterator();
			while (nodeIter.hasNext()) {
				Node node = nodeIter.next();
				int i = tn.originalGraph1.indexOf(node);
				// find number of edges adjacent to node i
				e += this.getNumberOfAdjacentEdges(tn.matching,a1,i);
				tn.matching[i] = -2; // -2 = deletion
			}
			
			tn.addCost(n * this.cf.getNodeCosts());
			tn.addCost(e * this.cf.getEdgeCosts() * factor);
			tn.unusedNodes1.clear();
			successors.add(tn);
			
		} else { // there are still nodes in g2 but no nodes in g1, the nodes of
					// g2 are inserted
			if (this.unusedNodes1.isEmpty()) {
				TreeNode tn = new TreeNode(this);
				int n = tn.unusedNodes2.size();
				int e = 0;
				Iterator<Node> nodeIter = tn.unusedNodes2.iterator();
				while (nodeIter.hasNext()) {
					Node node = nodeIter.next();
					int i = tn.originalGraph2.indexOf(node);
					// find number of edges adjacent to node i
					e += this.getNumberOfAdjacentEdges(tn.inverseMatching,a2,i);
					tn.inverseMatching[i] = -2; // -2 = insertion
				}
				tn.addCost(n * this.cf.getNodeCosts());
				tn.addCost(e * this.cf.getEdgeCosts() * factor);
				tn.unusedNodes2.clear();
				successors.add(tn);
				
			} else { // there are nodes in both g1 and g2
				for (int i = 0; i < this.unusedNodes2.size(); i++) {
					TreeNode tn = new TreeNode(this);
					Node start = tn.unusedNodes1.remove();
					Node end = tn.unusedNodes2.remove(i);
					tn.addCost(this.cf.getCost(start, end));
					int startIndex = tn.originalGraph1.indexOf(start);
					int endIndex = tn.originalGraph2.indexOf(end);
					tn.matching[startIndex] = endIndex;
					tn.inverseMatching[endIndex] = startIndex;
					// edge processing
					this.processEdges(tn, start, startIndex, end, endIndex);					
					successors.add(tn);
				}
				// deletion of a node from g_1 is also a valid successor
				TreeNode tn = new TreeNode(this);
				Node deleted = tn.unusedNodes1.remove();
				int i = tn.originalGraph1.indexOf(deleted);
				tn.matching[i] = -2; // deletion
				tn.addCost(this.cf.getNodeCosts());
				// find number of edges adjacent to node i
				int e = this.getNumberOfAdjacentEdges(tn.matching, a1, i);
				tn.addCost(this.cf.getEdgeCosts() * e
						* factor);
				successors.add(tn);
			}
		}
		return successors;
	}

	
	/**
	 * TODO needs massive refactoring!
	 * @param tn
	 * @param start
	 * @param startIndex
	 * @param end
	 * @param endIndex
	 */
	private void processEdges(TreeNode tn, Node start, int startIndex, Node end, int endIndex) {
		for (int e = 0; e < tn.a1[startIndex].length; e++) {
			if (tn.a1[startIndex][e] != null) { // there is an edge between start and start2
				Edge edge = tn.a1[startIndex][e];
				int start2Index = e;
				if (tn.matching[start2Index] != -1) { // other end has been handled
					int end2Index = tn.matching[start2Index];
					if (end2Index >= 0) {
						if (tn.a2[endIndex][end2Index] != null) {
							Edge edge2 = tn.a2[endIndex][end2Index];
							tn.addCost(this.cf.getCost(edge, edge2)
									* factor);
						} else {
							tn.addCost(this.cf.getEdgeCosts() * factor);
						}
					} else { // deletion
						tn.addCost(this.cf.getEdgeCosts() * factor);
					}
				}
			} else { // there is no edge between start and start2
				int start2Index = e;
				if (tn.matching[start2Index] != -1) { // other "end" has been handled
					int end2Index = tn.matching[start2Index];
					if (end2Index >= 0) {
						if (tn.a2[endIndex][end2Index] != null) {
							tn.addCost(this.cf.getEdgeCosts()* factor);
						}
					}
				}
			}
			// DUPLICATED CODE REFACTOR
			if (tn.a1[e][startIndex] != null) {
				Edge edge = tn.a1[e][startIndex];
				int start2Index = e;
				if (tn.matching[start2Index] != -1) { // other end has been handled
					int end2Index = tn.matching[start2Index];
					if (end2Index >= 0) {
						if (tn.a2[endIndex][end2Index] != null) {
							Edge edge2 = tn.a2[endIndex][end2Index];
							tn.addCost(this.cf.getCost(edge, edge2)
									* factor);
						} else {
							tn.addCost(this.cf.getEdgeCosts() * factor);
						}
					} else { // deletion
						tn.addCost(this.cf.getEdgeCosts() * factor);
					}
				}
			} else {
				int start2Index = e;
				if (tn.matching[start2Index] != -1) { // other "end" has been handled
					int end2Index = tn.matching[start2Index];
					if (end2Index >= 0) {
						if (tn.a2[endIndex][end2Index] != null) {
							tn.addCost(this.cf.getEdgeCosts()* factor);
						}
					}
				}
			}
		}
	}

	/**
	 * @return number of adjacent edges of node with index @param i
	 * NOTE: only edges (i,j) are counted if
	 * j-th node hae been processed (deleted or substituted)
	 */
	private int getNumberOfAdjacentEdges(int[] m, Edge[][] a, int i) {
		int e= 0;
		for (int j = 0; j < a[i].length; j++){
			if (m[j]!=-1){ // count edges only if other end has been processed
				if (a[i][j]!=null){
					e += 1;
				}
				if (a[j][i]!=null){
					e += 1;
				}
			}
		}
		return e;
	}

	/**
	 * adds @param c
	 * to the current solution cost
	 */
	private void addCost(double c) {
		this.cost += c;
	}

	/** 
	 * tree nodes are ordererd according to their past cost 
	 * in the open list:
	 * NOTE THAT CURRENTLY NO HEURISTIC IS IMPLEMENTED FOR ESTIMATING THE FUTURE COSTS
	 */
	public int compareTo(TreeNode other) {
		if ((this.getCost() - other.getCost())<0){
			return -1;
		} 
		if ((this.getCost() - other.getCost())>0){
			return 1;
		} 
		// we implement the open list as a TreeSet which does not allow 
		// two equal objects. That is, if two treenodes have equal cost, only one 
		// of them would be added to open, which would not be desirable
		return 1;
	}
	
	/**
	 * @return true if all nodes are used in the current solution
	 */
	public boolean allNodesUsed() {
		if (unusedNodes1.isEmpty() && unusedNodes2.isEmpty()) {
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * some getters and setters
	 */
	
	public Graph getUnusedNodes1() {
		return unusedNodes1;
	}

	public Graph getUnusedNodes2() {
		return unusedNodes2;
	}
	
	public double getCost() {
		return this.cost;
	}

	public CostFunction getCf() {
		return cf;
	}


	


}
