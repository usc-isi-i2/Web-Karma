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
/**
 * 
 */
package edu.isi.karma.view;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Workspace;


/**
 * @author szekely
 * 
 */
public class VWorkspace {

	private final Workspace workspace;

	private final ViewFactory viewFactory = new ViewFactory();

	private final ViewPreferences preferences;
	
	private final String preferencesId;

	public VWorkspace(Workspace workspace) {
		super();
		this.workspace = workspace;
		preferences = new ViewPreferences(workspace.getId(), workspace.getContextId());
		preferencesId=workspace.getId();
	}
	
	public VWorkspace(Workspace workspace, String workspacePreferencesId) {
		super();
		this.workspace = workspace;
		preferences = new ViewPreferences(workspacePreferencesId, workspace.getContextId());
		preferencesId = workspacePreferencesId;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public RepFactory getRepFactory() {
		return getWorkspace().getFactory();
	}
	
	public ViewFactory getViewFactory() {
		return viewFactory;
	}

	public ViewPreferences getPreferences() {
		return preferences;
	}
	public String getPreferencesId() {
		return preferencesId;
	}

	public void createVWorksheetsForAllWorksheets() {
		viewFactory.addWorksheets(workspace.getWorksheets(), this);
	}
}
