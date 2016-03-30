package edu.isi.karma.webserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkspaceKarmaHomeRegistry {
	private static WorkspaceKarmaHomeRegistry singleton = new WorkspaceKarmaHomeRegistry();

	private final Map<String, String> workspaceToKarmaHome = new ConcurrentHashMap<>();

	public static WorkspaceKarmaHomeRegistry getInstance() {
		return singleton;
	}

	public void register(String workspaceId, String karmaHome) {
		if(workspaceId == null)
		{
			//TODO error
		}
		workspaceToKarmaHome.put(workspaceId, karmaHome);
	}

	public String getKarmaHome(String workspaceId) {
		if(workspaceId == null)
		{
			ServletContextParameterMap contextParameters  =ContextParametersRegistry.getInstance().getContextParameters(null);
			return contextParameters.getKarmaHome();
		}
		return workspaceToKarmaHome.get(workspaceId);
	}
	
	public void deregister(String workspaceId) {
		workspaceToKarmaHome.remove(workspaceId);
	}
}
