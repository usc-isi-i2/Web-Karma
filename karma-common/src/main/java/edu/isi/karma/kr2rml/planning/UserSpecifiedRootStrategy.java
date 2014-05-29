package edu.isi.karma.kr2rml.planning;


public class UserSpecifiedRootStrategy extends RootStrategy {

	private String rootTriplesMapId;

	public UserSpecifiedRootStrategy(String rootTriplesMapId)
	{
		super(new RootStrategy());
		this.rootTriplesMapId = rootTriplesMapId;
	}
	public UserSpecifiedRootStrategy(String rootTriplesMapId,
			RootStrategy backupStrategy) {
		super(backupStrategy);
		this.rootTriplesMapId = rootTriplesMapId;
	}

	@Override
	public String findRoot(TriplesMapGraph graph) {
		if(graph.getTriplesMapIds().contains(rootTriplesMapId))
			return rootTriplesMapId;
		return this.backupStrategy.findRoot(graph);
	}

}
