package edu.isi.karma.kr2rml.planning;

import java.util.Set;

import edu.isi.karma.kr2rml.template.ColumnTemplateTerm;

public class WorksheetDepthRootStrategy extends RootStrategy {

	public WorksheetDepthRootStrategy()
	{
		super();
	}
	public WorksheetDepthRootStrategy(
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
