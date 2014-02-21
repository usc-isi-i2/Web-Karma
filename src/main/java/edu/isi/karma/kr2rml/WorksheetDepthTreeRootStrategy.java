package edu.isi.karma.kr2rml;

import java.util.Set;

public class WorksheetDepthTreeRootStrategy extends RootStrategy {

	public WorksheetDepthTreeRootStrategy()
	{
		super();
	}
	public WorksheetDepthTreeRootStrategy(
			RootStrategy backupStrategy) {
		super(backupStrategy);
	}
	
	@Override
	public String findRoot(TriplesMapGraph graph) {
		Set<String> triplesMapIds = graph.getTriplesMapIds();
		int minDepth = Integer.MAX_VALUE;
		String root = null;
		for(String triplesMapId : triplesMapIds)
		{
			TriplesMap map = graph.getTriplesMap(triplesMapId);
			int depth = ColumnTemplateTerm.calculateMaximumColumnPathLength(map.getSubject().getTemplate().getAllColumnNameTermElements());
			if(depth < minDepth)
			{
				minDepth = depth;
				root = triplesMapId;
			}
		}
		if(root == null)
		{
			return backupStrategy.findRoot(graph);
		}
		return root;
	}

}
