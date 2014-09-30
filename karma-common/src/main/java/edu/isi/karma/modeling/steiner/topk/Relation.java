package edu.isi.karma.modeling.steiner.topk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;


/**
 * This class is part of the YAGO extractors (http://mpii.de/yago). It is licensed under the 
 * Creative Commons Attribution-Noncommercial-Share Alike 3.0 Unported License,
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/) 
 * by the YAGO team (http://mpii.de/yago). 
 * 
 * This class represents a YAGO relation.<P>
 *
 * If a property of relations is added, all methods in the class Relation.java need to be adjusted!
 * RelationExtractor.java does not need to be adjusted.
 * Relations are represented as Java-objects in order to allow other Generators/Extractors to
 * access the information about relations.
 * 
 *  @author Fabian M. Suchanek
 *  
 *  TODO: I have relaxed some ranges (marked) because else we find too few facts
 *  */
public class Relation implements Comparable<Relation> {

  /** Holds the name of this relation*/
  public String name;

  public Relation(String name) {
    this.name = name;
  }

  /** Returns the relation name */
  public String toString() {
    return name();
  }

  /** Returns the relation name */
  public String name() {
    return this.name;
  }

  /** Tells whether two relations are identical*/
  public boolean equals(Object obj) {
    return (obj == this);
  }

  /** Compares two relations by name*/
  public int compareTo(Relation o) {
    return (name.compareTo(o.name()));
  }

  /** Returns the hash of the name*/
  public int hashCode() {
    return name.hashCode();
  }

}
