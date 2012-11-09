package edu.isi.karma.cleaning;

public interface PartitionClassifierType {
	public void addTrainingData(String value,String label);
	public String learnClassifer();
	public String getLabel(String value);
}
