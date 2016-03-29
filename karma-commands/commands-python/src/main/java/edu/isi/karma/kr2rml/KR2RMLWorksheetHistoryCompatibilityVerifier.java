package edu.isi.karma.kr2rml;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.command.transformation.PythonTransformationCommand;
import edu.isi.karma.controller.history.CommandHistory.HistoryArguments;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.transformation.tokenizer.PythonTransformationAsURIValidator;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class KR2RMLWorksheetHistoryCompatibilityVerifier {

	private static Logger logger = LoggerFactory
			.getLogger(KR2RMLWorksheetHistoryCompatibilityVerifier.class);

	private KR2RMLWorksheetHistoryCompatibilityVerifier() {
	}

	public static boolean verify(Workspace workspace, String historyJsonStr) {
		boolean isR2RMLCompatible = true;
		if(null != historyJsonStr && !historyJsonStr.isEmpty())
		{
			JSONArray commands = new JSONArray(historyJsonStr);
			isR2RMLCompatible = verify(workspace,
					commands);
		}
		return isR2RMLCompatible;
	}
	public static boolean verify(
			Workspace workspace,  JSONArray commands) {
		boolean isR2RMLCompatible = true;
		if(null == commands || commands.length() == 0)
		{
			return isR2RMLCompatible;
		}
		
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspace.getId());
		HashMap<String, CommandFactory> commandFactoryMap = ctrl.getCommandFactoryMap();
		
		for(int i = 0; i < commands.length(); i++)
		{
			JSONObject commObject = commands.getJSONObject(i);
			JSONArray tags = commObject.getJSONArray(HistoryArguments.tags.name());
			if(null == tags || tags.length() == 0)
			{
				continue;
			}
			for(int j = 0; j < tags.length(); j++)
			{
				String tag = tags.getString(j);
				if(null != tag && tag.equals(CommandTag.Transformation.name()))
				{
					JSONArray inputParamArr = (JSONArray) commObject.get(HistoryArguments.inputParameters.name());
					CommandFactory cf = commandFactoryMap.get(commObject.get(HistoryArguments.commandName.name()));
					if(cf != null && cf instanceof JSONInputCommandFactory) {

						JSONInputCommandFactory scf = (JSONInputCommandFactory)cf;
						Command comm = null;
						try {
							String model = Command.NEW_MODEL;
							if(commObject.has(HistoryArguments.model.name()))
								model = commObject.getString(HistoryArguments.model.name());
							comm = scf.createCommand(inputParamArr, model, workspace);
						} catch (JSONException | KarmaException e) {
							logger.error("Unable to parse command from worksheet history");
						}
						if(comm != null){
							if(comm instanceof PythonTransformationCommand)
							{
								PythonTransformationCommand pyTransform = (PythonTransformationCommand) comm;
								String transformationCode = pyTransform.getTransformationCode();
								PythonTransformationAsURIValidator validator = new PythonTransformationAsURIValidator();
								isR2RMLCompatible = isR2RMLCompatible && validator.validate(transformationCode);
								
							}
							else
							{
								isR2RMLCompatible = false;
							}
						}
					}
				}
					
			}
			if(!isR2RMLCompatible)
			{
			 break;	
			}
		}
		return isR2RMLCompatible;
	}
	
}
