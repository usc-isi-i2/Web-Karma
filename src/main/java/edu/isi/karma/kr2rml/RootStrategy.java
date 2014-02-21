package edu.isi.karma.kr2rml;

public class RootStrategy {

	protected RootStrategy backupStrategy;
	public RootStrategy()
	{
		
	}
	public RootStrategy(RootStrategy backupStrategy)
	{
		this.backupStrategy = backupStrategy;
	}
	public String findRoot(TriplesMapGraph graph){
		return null;
	}
}
