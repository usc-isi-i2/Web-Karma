package edu.isi.karma.modeling.steiner.topk;

/**
 * This class is part of the YAGO extractors (http://mpii.de/yago). It is licensed under the 
 * Creative Commons Attribution-Noncommercial-Share Alike 3.0 Unported License,
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/) 
 * by the YAGO team (http://mpii.de/yago). 
 * 
 * This class represents an Entity
 * 
 * @author Fabian M. Suchanek and Maya Ramanath
 *
 */
public class Entity implements Comparable<Entity> {
	/** Holds the name of the entity*/
	protected String name;

	public Entity(String n) {
		this.name=n;
	}

	
	public String name () { return name; }

	/** Compares by name*/
	public boolean equals (Object o){
		if(!(o instanceof Entity)) return(false);
		return (((Entity)o).name.equals(name)); 
	}

	/** Compares by name*/
	public int compareTo (Entity e) {
		return(this.name.compareTo(e.name));
	}

	/** Hashes by name*/
	public int hashCode() {  
		return name.hashCode();
	}

	/** Returns name*/
	public String toString () { return name; }

}
