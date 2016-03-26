package edu.isi.karma.modeling.steiner.topk;


/**
 * This class is part of the YAGO extractors (http://mpii.de/yago). It is licensed under the 
 * Creative Commons Attribution-Noncommercial-Share Alike 3.0 Unported License,
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/) 
 * by the YAGO team (http://mpii.de/yago). 
 * 
 * Represents a Fact 
 * 
 * @author Fabian M. Suchanek
 * */
public class Fact extends WeightedLabeledEdge<Entity, Relation, Float> implements Comparable<Fact> {
	
	/**  For the construction of a new fact */
	public Fact (Entity e1, Entity e2, Relation r, Float w) {
		super (e1, e2, r, w);
	}
	
  public int compareTo(Fact o) {
    if(n1.compareTo(o.n1)!=0) return(n1.compareTo(o.n1));
    if(n2.compareTo(o.n2)!=0) return(n2.compareTo(o.n2));    
    return label.compareTo(o.label);
  }
  
  public boolean equals(Object obj){
	  return this.compareTo((Fact)obj)==0;
  }

}
