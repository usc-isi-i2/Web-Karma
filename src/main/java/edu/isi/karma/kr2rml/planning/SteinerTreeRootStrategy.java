package edu.isi.karma.kr2rml.planning;


public class SteinerTreeRootStrategy extends RootStrategy {

	public SteinerTreeRootStrategy()
	{
		super(new RootStrategy());
	}
	public SteinerTreeRootStrategy(
			RootStrategy backupStrategy) {
		super(backupStrategy);
	}

	@Override
	public String findRoot(TriplesMapGraph graph) {
		for(String triplesMapId :graph.getTriplesMapIds())
		{
			if(graph.getTriplesMap(triplesMapId).getSubject().isSteinerTreeRootNode())
			{
				return triplesMapId;
			}
		}
		return this.backupStrategy.findRoot(graph);
	}

}
