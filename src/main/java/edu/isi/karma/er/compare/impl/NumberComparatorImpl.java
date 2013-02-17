package edu.isi.karma.er.compare.impl;

import edu.isi.karma.er.compare.NumberComparator;

public class NumberComparatorImpl implements NumberComparator {

	double delta;
	double min;
	double max;
	
	/**
	 * Constructor
	 * @param delta the tolerance for number comparing
	 */
	public NumberComparatorImpl(double min, double max, double delta) {
		this.delta = delta;
		this.min = min;
		this.max = max;
	}
	
	
	/**
	 * Calculate the similarity between 2 numbers.
	 * @param a  srcNumber
	 * @param b  dstNumber
	 * @return y = f(x) = exp(-x) if x <= delta, else return 0 ;
	 */
	public double getSimilarity(Number a, Number b) {
		
		double numV = a.doubleValue(), numW = b.doubleValue();
		double diff = Math.abs(numV - numW);
		if (diff <= delta) {
			// return Math.exp(-1 * (diff * 4.6 / 100));  
			// return (1- diff * 0.1 / delta);
			return 1- (diff * 1.0 /delta);
		} else {
			return 0;
		}
		
	}
}
