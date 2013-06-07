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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.sources.InvocationManager;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class ImportServiceCommand extends Command {
	private static Logger logger = LoggerFactory.getLogger(ImportServiceCommand.class);

	private String serviceUrl;
	private String worksheetName;
	private boolean includeInputAttributes;

	public enum PreferencesKeys {
		ServiceUrl, WorksheetName
	}

	protected ImportServiceCommand(String id, String ServiceUrl, String worksheetName, 
			boolean includeInputAttributes){
		super(id);
		this.serviceUrl=ServiceUrl;
		this.worksheetName = worksheetName;
		this.includeInputAttributes = includeInputAttributes;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Import Service";
	}

	@Override
	public String getDescription() {
		if (serviceUrl.length() > 50)
			return serviceUrl.substring(0, 50);
		else
			return serviceUrl;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		//save the preferences 
		savePreferences(vWorkspace);

		UpdateContainer c = new UpdateContainer();
		Workspace ws = vWorkspace.getWorkspace();
		
		List<String> urls = new ArrayList<String>();
		urls.add(serviceUrl);
		List<String> ids = new ArrayList<String>();
		ids.add("1"); 
		try{
			InvocationManager invocatioManager = new InvocationManager(null, ids, urls);
			String json = invocatioManager.getServiceJson(includeInputAttributes);
//			System.out.println(json);
			JsonImport imp = new JsonImport(json, worksheetName, ws);
			
			Worksheet wsht = imp.generateWorksheet();
			vWorkspace.addAllWorksheets();
			
			
			c.add(new WorksheetListUpdate(vWorkspace.getVWorksheetList()));
			VWorksheet vw = vWorkspace.getVWorksheet(wsht.getId());
			vw.update(c);
			
			return c;
		} catch (Exception e) {
			logger.error("Error occured while creating worksheet from web-service: " + serviceUrl);
			return new UpdateContainer(new ErrorUpdate("Error creating worksheet from web-service"));
		}
	}

	private void savePreferences(VWorkspace vWorkspace){
		try{
			JSONObject prefObject = new JSONObject();
			prefObject.put(PreferencesKeys.ServiceUrl.name(), serviceUrl);
			prefObject.put(PreferencesKeys.WorksheetName.name(), worksheetName);
			vWorkspace.getPreferences().setCommandPreferences(
					"ImportServiceCommandPreferences", prefObject);
			
			/*
			System.out.println("I Saved .....");
			ViewPreferences prefs = vWorkspace.getPreferences();
			JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishDatabaseCommandPreferences");
			System.out.println("I Saved ....."+prefObject1);
			 */
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
