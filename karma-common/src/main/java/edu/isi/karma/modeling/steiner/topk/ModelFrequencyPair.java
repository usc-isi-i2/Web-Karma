package edu.isi.karma.modeling.steiner.topk;

public class ModelFrequencyPair implements Comparable<ModelFrequencyPair> {

	private String id;
	private int frequency;
	public ModelFrequencyPair(String id, int frequency) {
		this.id = id;
		this.frequency = frequency;
	}
	
	public String getId() {
		return id;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Override
	public int compareTo(ModelFrequencyPair o) {
		// TODO Auto-generated method stub
		return Integer.compare(o.frequency, this.frequency);
	}
	
	
}
