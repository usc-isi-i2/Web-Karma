package edu.isi.karma.controller.command.transformation;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.rep.Workspace;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PyTransformConsolidator extends CommandConsolidator {

	@Override
	public List<ICommand> consolidateCommand(List<ICommand> commands, Workspace workspace) {
		List<ICommand> refinedCommands = new ArrayList<>();
		//
		for (ICommand command : commands) {
			if (command instanceof SubmitEditPythonTransformationCommand) {
				Iterator<ICommand> itr = refinedCommands.iterator();
				boolean flag = true;
				boolean found = false;
				String model = command.getModel();
				
				while(itr.hasNext()) {
					ICommand tmp = itr.next();
					if (((Command)tmp).getOutputColumns().equals(((Command)command).getOutputColumns())
							&& tmp instanceof SubmitPythonTransformationCommand 
							&& !(tmp instanceof SubmitEditPythonTransformationCommand)
							&& model.equals(tmp.getModel())) {
							SubmitPythonTransformationCommand py = (SubmitPythonTransformationCommand)tmp;
							SubmitPythonTransformationCommand edit = (SubmitPythonTransformationCommand)command;
							JSONArray inputJSON = new JSONArray(py.getInputParameterJson());
							HistoryJsonUtil.setArgumentValue("transformationCode", edit.getTransformationCode(), inputJSON);
							py.setInputParameterJson(inputJSON.toString());
							py.setTransformationCode(edit.getTransformationCode());
							flag = false;
							try {
								py.doIt(workspace);
							} catch (CommandException e) {
								// TODO Auto-generated catch block
							}
						found = true;
					}
					if (((Command)tmp).getOutputColumns().equals(((Command)command).getOutputColumns())
							&& tmp instanceof SubmitEditPythonTransformationCommand 
							&& command instanceof SubmitEditPythonTransformationCommand
							&& model.equals(tmp.getModel())) {
						found = true;
						if(model.equals(tmp.getModel())) {
							itr.remove();
						}
					}
				}
				if (flag && found)
					refinedCommands.add(command);
			}
			else
				refinedCommands.add(command);
		}
		return refinedCommands;
	}

	@Override
	public Pair<ICommand, JSONArray> consolidateCommand(List<ICommand> commands, ICommand newCommand, Workspace workspace) {
		if (newCommand instanceof SubmitEditPythonTransformationCommand) {
			Iterator<ICommand> itr = commands.iterator();
			String model = newCommand.getModel();
			while(itr.hasNext()) {
				ICommand tmp = itr.next();
				if (((Command)tmp).getOutputColumns().equals(((Command)newCommand).getOutputColumns())
						&& tmp instanceof SubmitPythonTransformationCommand
						&& !(tmp instanceof SubmitEditPythonTransformationCommand)
						&& model.equals(tmp.getModel())) {
						SubmitPythonTransformationCommand py = (SubmitPythonTransformationCommand)tmp;
						SubmitPythonTransformationCommand edit = (SubmitPythonTransformationCommand)newCommand;
						JSONArray inputJSON = new JSONArray(py.getInputParameterJson());
						JSONArray oldInputJSON = new JSONArray(py.getInputParameterJson());
						HistoryJsonUtil.setArgumentValue("transformationCode", edit.getTransformationCode(), inputJSON);
						py.setInputParameterJson(inputJSON.toString());
						py.setTransformationCode(edit.getTransformationCode());
						return new ImmutablePair<>(tmp, oldInputJSON);
				}
			}
		}
		return null;
	}

}
