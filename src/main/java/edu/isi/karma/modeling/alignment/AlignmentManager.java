package edu.isi.karma.modeling.alignment;

import java.util.HashMap;

public class AlignmentManager {
	private static HashMap<String, Alignment> alignmentMap = null;
	private static AlignmentManager _InternalInstance = null;
	
	public static AlignmentManager Instance()
	{
		if (_InternalInstance == null)
		{
			_InternalInstance = new AlignmentManager();
			alignmentMap = new HashMap<String, Alignment>();
		}
		return _InternalInstance;
	}
	
	public void addAlignmentToMap(String key, Alignment alignment) {
		alignmentMap.put(key, alignment);
	}

	public Alignment getAlignment(String alignmentId) {
		return alignmentMap.get(alignmentId);
	}

	public void removeWorkspaceAlignments(String workspaceId) {
		for(String key:alignmentMap.keySet()) {
			if(key.startsWith(workspaceId+":")) {
				alignmentMap.remove(key);
			}
		}
	}
}
