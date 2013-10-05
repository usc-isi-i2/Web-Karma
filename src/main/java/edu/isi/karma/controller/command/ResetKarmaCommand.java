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
import java.io.FilenameFilter;
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
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ResetKarmaCommand extends Command {
	private final boolean forgetSemanticTypes;
	private final boolean forgetModels;
	
	private static Logger logger = LoggerFactory.getLogger(ResetKarmaCommand.class);
	
	protected ResetKarmaCommand(String id, boolean forgetSemanticTypes, boolean forgetModels) {
		super(id);
		this.forgetSemanticTypes = forgetSemanticTypes;
		this.forgetModels = forgetModels;
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
		if (forgetSemanticTypes && forgetModels)
			return "Semantic Types and Models";
		else if (forgetModels)
			return "Models";
		else if(forgetSemanticTypes)
			return "Semantic Types";
		else
			return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		if (forgetSemanticTypes) {
			boolean deletTypes = workspace.getCrfModelHandler().removeAllLabels();
			if (!deletTypes && forgetModels)
				return new UpdateContainer(new ErrorUpdate("Error occured while removing semantic types. Models have also not been reset."));
			else if (!deletTypes)
				return new UpdateContainer(new ErrorUpdate("Error occured while removing semantic types."));
		}
		
		if (forgetModels) {
			c.add(new AbstractUpdate(){
				
			@Override
			public void applyUpdate(VWorkspace vWorkspace){
			/** Delete the model history files **/
			final String vwsPrefId = vWorkspace.getPreferencesId();
			File historyDir = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + "publish/History/");
			if (!historyDir.exists() || !historyDir.isDirectory()) {
				logger.error("Directory not found where the model histories are stored.");
				/*if (forgetSemanticTypes)
					return new UpdateContainer(new ErrorUpdate("Error occured while removing model histories." +
							" Learned Semantic types have been reset."));
				return new UpdateContainer(new ErrorUpdate("Error occured while removing model histories."));*/
				//TODO return error messages
				return;
			}
			
			File[] workspaceFiles = historyDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					// Remove the workspace name in front of it
					// If it has been removed and it starts with _, return true
					return (name.replaceAll(vwsPrefId, "").startsWith("_"));
				}
			});
			if (workspaceFiles != null && workspaceFiles.length != 0) {
				for (File file: workspaceFiles) {
					file.delete();
				}
			}
			}

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
		}
		c.add(new InfoUpdate("Reset complete"));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
