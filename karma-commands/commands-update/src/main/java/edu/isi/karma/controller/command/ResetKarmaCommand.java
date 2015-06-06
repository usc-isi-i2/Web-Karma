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

package edu.isi.karma.controller.command;

import java.io.File;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ResetKarmaCommand extends Command {
	private final boolean forgetSemanticTypes;
	private final boolean forgetModels;
	private final boolean forgetAlignment;
	
	private static Logger logger = LoggerFactory.getLogger(ResetKarmaCommand.class);
	
	protected ResetKarmaCommand(String id, String model, boolean forgetSemanticTypes, boolean forgetModels, boolean forgetAlignment) {
		super(id, model);
		this.forgetSemanticTypes = forgetSemanticTypes;
		this.forgetModels = forgetModels;
		this.forgetAlignment = forgetAlignment;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Reset Karma";
	}

	@Override
	public String getDescription() {
		String desc = "";
		int numSelected = 0;
		
		if(forgetSemanticTypes)  numSelected++;
		if(forgetModels) numSelected++;
		if(forgetAlignment) numSelected++;
		
		int curSep = 0;
		if(forgetSemanticTypes) {
			desc = "Semantic Types";
			curSep ++;
		}
		if(forgetModels) {
			if(curSep > 0) {
				if(curSep < numSelected-1) {
					desc += ", ";
				} else {
					desc += " and ";
				}
			}
				
			desc += "Models";
			curSep++;
		}
		
		if(forgetAlignment) {
			if(curSep > 0) {
				if(curSep < numSelected-1) {
					desc += ", ";
				} else {
					desc += " and ";
				}
			}
				
			desc += "Alignment";
			curSep++;
		}
		
		return desc;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		UpdateContainer c = new UpdateContainer();
		if (forgetSemanticTypes) {
			boolean deletTypes = workspace.getSemanticTypeModelHandler().removeAllLabels();
			if (!deletTypes)
				c.add(new ErrorUpdate("Error occured while removing semantic types."));
		}
		
		if (forgetModels) {
			File dir = new File(contextParameters.getParameterValue(ContextParameter.R2RML_USER_DIR));
			if (!dir.exists() || !dir.isDirectory()) {
				logger.error("Directory not found where the model histories are stored.");
				c.add(new ErrorUpdate("Error occured while removing Model Histories: Directory was not found"));
			} else {
			
				File[] workspaceFiles = dir.listFiles();
				if (workspaceFiles != null && workspaceFiles.length != 0) {
					for (File file: workspaceFiles) {
						file.delete();
					}
				}
			}
		}
			
		if(forgetAlignment) {
			File dir = new File(contextParameters.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY));
			if (!dir.exists() || !dir.isDirectory()) {
				logger.error("Directory not found where the Alignment is stored.");
				c.add(new ErrorUpdate("Error occured while removing Alignment: Directory was not found"));
			} else {
			
				File[] workspaceFiles = dir.listFiles();
				if (workspaceFiles != null && workspaceFiles.length != 0) {
					for (File file: workspaceFiles) {
						file.delete();
					}
				}
			}
		}
		
		c.add(new AbstractUpdate(){
				
	
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject obj = new JSONObject();
					try {
						obj.put(GenericJsonKeys.updateType.name(), "ResetKarmaCommandUpdate");
						pw.println(obj.toString());
					} catch (JSONException e) {
						logger.error("Unable to generate Json", e);
					}
					
				}
			});
		
		
		
		c.add(new InfoUpdate("Reset complete"));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
