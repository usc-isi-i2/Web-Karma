package edu.isi.karma.modeling.steiner.topk;


/**
 * This class is part of the YAGO extractors (http://mpii.de/yago). It is licensed under the 
 * Creative Commons Attribution-Noncommercial-Share Alike 3.0 Unported License,
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/) 
 * by the YAGO team (http://mpii.de/yago). 
 * 
 * This class represents a fact with a "target" node
 *  
 * @author Fabian M. Suchanek
 * */
public class DirectedFact extends Fact {

  /** Holds 1 or 2, depending on what is the target*/
  public int target;
  
  /** Constructs a directed fact, with the target being 1 or 2*/
  public DirectedFact(Entity e1, Entity e2, Relation r, Float w, int target) {
    super(e1, e2, r, w);
    this.target=target;
  }
  
  /** Returns the target */
  public Entity getTarget() {
    if(target==1) return(this.n1);
    else return(this.n2);
  }
 
  public String toString() {
    return super.toString()+" < "+getTarget();
  }
  
  public boolean equals(Object obj) {
    return super.equals(obj) && obj instanceof DirectedFact && ((DirectedFact)obj).target==target;
  }
  
  public int hashCode() {  
    return super.hashCode()^target;
  }
}