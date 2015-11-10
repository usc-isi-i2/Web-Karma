package edu.isi.karma.cleaning.correctness;

import java.util.HashMap;

public class Instance {
	public String ID; //instance ID
	//the predication resutls of each checker on this instance
	public HashMap<String, Double> labeles = new HashMap<String,Double>();
	//the ground truth label
	public Double label;
	public double weight = 1.0;
}
