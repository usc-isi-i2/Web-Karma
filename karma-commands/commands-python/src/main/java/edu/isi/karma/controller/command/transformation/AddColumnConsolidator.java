package edu.isi.karma.controller.command.transformation;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.command.worksheet.AddColumnCommand;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;

public class AddColumnConsolidator extends CommandConsolidator {

	@Override
	public Pair<ICommand, Object> consolidateCommand(List<ICommand> commands,
			ICommand newCommand, Workspace workspace) {
		if (newCommand instanceof SubmitPythonTransformationCommand) {
            String model = newCommand.getModel();
            
            Iterator<ICommand> itr = commands.iterator();
            while(itr.hasNext()) {
                ICommand tmp = itr.next();
                if (((Command)tmp).getOutputColumns().equals(((Command)newCommand).getOutputColumns())
                    && tmp.getModel().equals(model)
                        && (tmp instanceof AddColumnCommand)) {
                	SubmitPythonTransformationCommand py = ((SubmitPythonTransformationCommand)newCommand);
                	JSONArray inputJSON = new JSONArray(py.getInputParameterJson());
                	
                	JSONArray tmpInputJSON = new JSONArray(tmp.getInputParameterJson());
        			String hNodeId = HistoryJsonUtil.getStringValue("hNodeId", tmpInputJSON);
        			
					HistoryJsonUtil.setArgumentValue("hNodeId", hNodeId, inputJSON);
					py.setInputParameterJson(inputJSON.toString());
                	
                    return new ImmutablePair<>(tmp, (Object)py);
                }
            }
        }
        return null;
	}

}
