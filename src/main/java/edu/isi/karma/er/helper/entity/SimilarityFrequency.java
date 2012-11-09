package edu.isi.karma.er.helper.entity;

public class SimilarityFrequency {

	private double similarity;
	
	private double frequency;

	public SimilarityFrequency(double sim, double freq) {
		this.similarity = sim;
		this.frequency = freq;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	

}
