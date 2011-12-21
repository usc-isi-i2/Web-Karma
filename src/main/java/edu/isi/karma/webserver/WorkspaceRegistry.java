package edu.isi.karma.webserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to map Workspace Ids to ExecutionController objects
 * 
 * @author szekely
 * 
 */
public class WorkspaceRegistry {

	private static WorkspaceRegistry singleton = new WorkspaceRegistry();

	private final Map<String, ExecutionController> workspaceId2ExecutionController = new HashMap<String, ExecutionController>();

	public static WorkspaceRegistry getInstance() {
		return singleton;
	}

	public void register(ExecutionController executionController) {
		workspaceId2ExecutionController.put(executionController.getvWorkspace()
				.getWorkspace().getId(), executionController);
	}

	public ExecutionController getExecutionController(String workspaceId) {
		return workspaceId2ExecutionController.get(workspaceId);
	}
	
	public void deregister(String workspaceId) {
		workspaceId2ExecutionController.remove(workspaceId);
	}
}
