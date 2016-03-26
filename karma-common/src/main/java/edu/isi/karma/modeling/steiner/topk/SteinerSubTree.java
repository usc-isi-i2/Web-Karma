package edu.isi.karma.modeling.steiner.topk;


import java.util.Set;
import java.util.TreeSet;


/**
 * This class serves as an abstract class for Steiner subtrees. It comes with a 
 * score and a set of Steiner nodes
 * @author kasneci
 *
 */
public abstract class SteinerSubTree implements Comparable<SteinerSubTree>, Cloneable {

	protected double score;
	private Set<SteinerNode> nodes;
	
	
	public SteinerSubTree() {
		nodes= new TreeSet<>();
		score=0;
	}
	
	public SteinerSubTree(Set<SteinerNode> nodes){
		this.nodes= nodes;
		// standard function
		setScoreWithFunction(null);
	}
	
	public double getScore(){
		return score;
	}
	
	/**
	 * 
	 * @return all edges of this subtree
	 */
	public Set<SteinerEdge> getEdges(){
		Set<SteinerEdge> stEdges = new TreeSet<>();
		for(SteinerNode n: getNodes())
			for(SteinerEdge e: n.edges)
				stEdges.add(e);
		return stEdges;
	}
	
	public Set<SteinerNode> getNodes(){
		return nodes;
	}
	
	/**
	 * in case f is null simple summation over edge weights is done
	 * @param f function to be used for the scoring, e.g. an instance of SteinerScoringFunction...
	 */
	public void setScoreWithFunction(SteinerScoringFunction f){
		// summation of edge weights is the standard function
		if(f==null){
			double weight=0.0;
			for(SteinerEdge e: this.getEdges()){
				weight+=e.weight();
				//D.p(e.edgeLabel.weight);
			}
			score=weight;
		}
		else score=f.score(this);
	}
	

	public int compareTo(SteinerSubTree p){
		String thisEdgeString = "";
		String otherEdgesString = "";
//		if (p.score - this.score < 0.0005)
//			return 0;
		for(SteinerEdge e: p.getEdges()) otherEdgesString=otherEdgesString+e.toString();
		for(SteinerEdge e: this.getEdges()) thisEdgeString= thisEdgeString+e.toString();
		return thisEdgeString.compareTo(otherEdgesString);
	}
	
	public boolean equals(Object p){
		return (this.compareTo((SteinerSubTree)p)==0);
	}
	
	
	public String toString () {
		String path="";
		for(SteinerEdge e: getEdges()){
			path = path+e.toString()+"\n";
		}
		return path;
	}
	
	
	/**
	 * makes a result graph out of this steiner subtree
	 * @return the result graph representing this steiner subtree
	 */
	public ResultGraph toResultGraph(){
		ResultGraph g= new ResultGraph();
		for(Fact f: this.getEdges()){
			g.addEdge(f);
		}
		try {
			g.setScore((Double)score);
		}catch(Exception e){}
		return g;
	}
	
	public abstract SteinerSubTree clone();

}
