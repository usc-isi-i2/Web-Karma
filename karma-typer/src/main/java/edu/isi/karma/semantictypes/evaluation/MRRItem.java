package edu.isi.karma.semantictypes.evaluation;

public class MRRItem {
	
	private double accuracy;
	private double mrr;
	
	public MRRItem(double accuracy, double mrr) {
		this.accuracy = accuracy;
		this.mrr = mrr;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public double getMrr() {
		return mrr;
	}
	
	
}
