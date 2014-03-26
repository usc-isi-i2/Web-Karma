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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;

public class SetMetaPropertyCommandFactory extends JSONInputCommandFactory {

	public enum METAPROPERTY_NAME {
		isUriOfClass, isSubclassOfClass, isSpecializationForEdge
	}
	
	enum Arguments {
		worksheetId, hNodeId, metaPropertyName, metaPropertyValue, trainAndShowUpdates, rdfLiteralType
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String rdfLiteralType = request.getParameter(Arguments.rdfLiteralType.name());
		
		METAPROPERTY_NAME prop = METAPROPERTY_NAME.valueOf(request.getParameter(Arguments.metaPropertyName.name()));
		String propValue = request.getParameter(Arguments.metaPropertyValue.name());
		return new SetMetaPropertyCommand(getNewId(workspace), worksheetId, hNodeId, 
				prop, propValue, true, rdfLiteralType);
	}

	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		METAPROPERTY_NAME prop = METAPROPERTY_NAME.valueOf(HistoryJsonUtil.getStringValue(Arguments.metaPropertyName.name(), inputJson));
		String propValue = HistoryJsonUtil.getStringValue(Arguments.metaPropertyValue.name(), inputJson);
		String rdfLiteralType = HistoryJsonUtil.getStringValue(Arguments.rdfLiteralType.name(), inputJson);
		SetMetaPropertyCommand comm = new SetMetaPropertyCommand(getNewId(workspace), worksheetId, 
				hNodeId, prop, propValue, true, rdfLiteralType);
		
		// Change the train flag, so that it does not train while reading from history
		HistoryJsonUtil.setArgumentValue(Arguments.trainAndShowUpdates.name(), false, inputJson);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return SetMetaPropertyCommand.class;
	}
}
