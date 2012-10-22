package edu.isi.karma.er.compare.impl;

import edu.isi.karma.er.compare.NumberComparator;

public class NumberEqualityComparatorImpl implements NumberComparator {

	private double min, max, delta;
	
	public NumberEqualityComparatorImpl(double min, double max, double delta) {
		this.min = min;
		this.max = max;
		this.delta = delta;
	}
	
	public double getSimilarity(Number a, Number b) {
		if (a.intValue() == b.intValue()) 
			return 1;
		return 0; 
	}

}
