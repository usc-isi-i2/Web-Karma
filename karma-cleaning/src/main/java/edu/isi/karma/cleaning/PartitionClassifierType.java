package edu.isi.karma.cleaning;

public interface PartitionClassifierType {
	//public void addTrainingData(String value, double[] val ,String label);
	public void addTrainingData(String value,String label);
	public String learnClassifer();
	public void addTrainingData(String text,double[] values, String label);
	public String getLabel(String value);
	public void init(String[] vocb);
}
