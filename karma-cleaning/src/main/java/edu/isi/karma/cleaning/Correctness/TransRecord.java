package edu.isi.karma.cleaning.Correctness;

public class TransRecord {
	public String Id = "";
	public String org = "";
	public String tar = "";
	public String label = "";
	public String correct = "f";
	public double[] features = null;
	public TransRecord(String Id, String org, String tar, String lab, double[] feats)
	{
		this.Id = Id;
		this.org = org;
		this.tar = tar;
		this.label = lab;
		this.features = feats;
	}
}
