package edu.isi.karma.modeling.alignment.learner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Confidence {

	private List<Double> values;
	
	public Confidence() {
		this.values = new ArrayList<>();
	}
	
	public Confidence(Confidence confidence) {
		this.values = new ArrayList<>(confidence.getValues());
	}

	public List<Double> getValues() {
		return Collections.unmodifiableList(this.values);
	}

	public void addValue(Double value) {
		if (value != null) {
			this.values.add(value);
		}
	}
	
	public double getConfidenceValue() {
		
		double sum = 0.0;
//		double mult = 1.0;
		int count = 0;
		for (Double d : this.values) {
			if (d != null) {
				count ++;
				sum += d.doubleValue();
//				mult *= d == 0.0? 0.1 : d.doubleValue();
			}
		}
//		return mult;
		return sum / (double)count;
	}

}
