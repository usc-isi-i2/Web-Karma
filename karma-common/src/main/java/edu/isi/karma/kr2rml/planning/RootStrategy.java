package edu.isi.karma.kr2rml.planning;


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
		if(graph.getTriplesMapIds().isEmpty())
		{
			return null;
		}
		else
		{
			return graph.getTriplesMapIds().iterator().next();
		}
	}
}
