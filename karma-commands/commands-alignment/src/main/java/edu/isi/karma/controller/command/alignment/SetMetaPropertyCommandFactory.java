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

package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class SetMetaPropertyCommandFactory extends JSONInputCommandFactory {

	public enum METAPROPERTY_NAME {
		isUriOfClass, isSubclassOfClass, isSpecializationForEdge
	}
	
	enum Arguments {
		worksheetId, hNodeId, metaPropertyName, 
		metaPropertyId, metaPropertyUri, trainAndShowUpdates, rdfLiteralType, 
		selectionName
	}
	
	enum ArgumentsOld {
		metaPropertyValue
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String rdfLiteralType = request.getParameter(Arguments.rdfLiteralType.name());
		
		METAPROPERTY_NAME prop = METAPROPERTY_NAME.valueOf(request.getParameter(Arguments.metaPropertyName.name()));
		String propUri = request.getParameter(Arguments.metaPropertyUri.name());
		String propId = request.getParameter(Arguments.metaPropertyId.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		
		if(propUri == null && propId == null) {
			propUri = request.getParameter(ArgumentsOld.metaPropertyValue.name());
			propId = propUri;
		}
			
		return new SetMetaPropertyCommand(getNewId(workspace), worksheetId, hNodeId, 
				prop, propUri, propId, true, rdfLiteralType, 
				selectionName);
	}

	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		METAPROPERTY_NAME prop = METAPROPERTY_NAME.valueOf(HistoryJsonUtil.getStringValue(Arguments.metaPropertyName.name(), inputJson));
		String propUri, propId;
		if(HistoryJsonUtil.valueExits(Arguments.metaPropertyUri.name(), inputJson)) {
			propUri = HistoryJsonUtil.getStringValue(Arguments.metaPropertyUri.name(), inputJson);
			propId = HistoryJsonUtil.getStringValue(Arguments.metaPropertyId.name(), inputJson);
		} else {
			propUri = HistoryJsonUtil.getStringValue(ArgumentsOld.metaPropertyValue.name(), inputJson);
			propId = propUri;
		}
		String rdfLiteralType = HistoryJsonUtil.getStringValue(Arguments.rdfLiteralType.name(), inputJson);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		SetMetaPropertyCommand comm = new SetMetaPropertyCommand(getNewId(workspace), worksheetId, 
				hNodeId, prop, propUri, propId, true, rdfLiteralType, 
				selectionName);
		
		// Change the train flag, so that it does not train while reading from history
		HistoryJsonUtil.setArgumentValue(Arguments.trainAndShowUpdates.name(), false, inputJson);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}
	
	public Command createCommand(Workspace workspace, String hNodeId, String worksheetId, String metaPropertyName, 
			String propUri, String propId, String rdfLiteralType, String selectionId) {
		METAPROPERTY_NAME prop = METAPROPERTY_NAME.valueOf(metaPropertyName);
		SetMetaPropertyCommand comm = new SetMetaPropertyCommand(getNewId(workspace), worksheetId, 
				hNodeId, prop, propUri, propId, true, rdfLiteralType, selectionId);
		return comm;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return SetMetaPropertyCommand.class;
	}
}
