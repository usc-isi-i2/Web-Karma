package edu.isi.karma.kr2rml;

public class SteinerTreeRootStrategy extends RootStrategy {

	public SteinerTreeRootStrategy()
	{
		super();
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
