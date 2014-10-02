package edu.isi.karma.modeling.steiner.topk;

/**
 * This class is part of the YAGO extractors (http://mpii.de/yago). It is licensed under the 
 * Creative Commons Attribution-Noncommercial-Share Alike 3.0 Unported License,
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/) 
 * by the YAGO team (http://mpii.de/yago). 
 * 
 * Represents a weighted labeled edge
 * 
 * @author Maya Ramanath
 *
 */
public class WeightedLabeledEdge<T,L,W> extends LabeledEdge<T,L> {
	protected W w;

	public WeightedLabeledEdge (T n1, T n2, L l, W w) {
		super (n1, n2, l);
		this.w = w;
	}
	
	public String toString () {
		return (n1.toString() +
				"--" + label.toString() + 
				"," + w.toString() + 
				"-->" + n2.toString());
	}

	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof WeightedLabeledEdge)) return(false);
		WeightedLabeledEdge other=(WeightedLabeledEdge)obj;    
		return super.equals(obj) && other.w.equals(this.w);
	}

	public int hashCode() {  
		return super.hashCode()^w.hashCode();
	}

	public W weight () {
		return w;
	}

	public W getWeight () {
		return w;
	}

}
