/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.rep;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.isi.karma.modeling.alignment.AlignmentManager;


/**
 * @author szekely
 *
 */
public class WorkspaceManager {
	
	private AtomicInteger nextId = new AtomicInteger(1);
	
	private final Map<String, Workspace> workspaces = new ConcurrentHashMap<String, Workspace>();
	
	private static WorkspaceManager singleton = new WorkspaceManager();
	
	public static WorkspaceManager getInstance() {
		return singleton;
	}
	
	public static WorkspaceManager _getNewInstance() {
		return new WorkspaceManager();
	}
	
	public Workspace createWorkspace() {
		String id = getNewId("WSP");
		Workspace wsp = new Workspace(id);
		workspaces.put(id, wsp);
		return wsp;
	}
	
	public Workspace createWorkspaceWithPreferencesId(String preferenceId) {
		String id = getNewId("WSP");
		Workspace wsp = new Workspace(id, preferenceId);
		workspaces.put(id, wsp);
		return wsp;
	}
	
	public String getNewId(String prefix) {
		return prefix + (nextId.getAndIncrement());
	}
	
	public void removeWorkspace(String workspaceId) {
		workspaces.remove(workspaceId);
		AlignmentManager.Instance().removeWorkspaceAlignments(workspaceId);
	}
	
	public Workspace getWorkspace(String workspaceId) {
		return workspaces.get(workspaceId);
	}
}
 