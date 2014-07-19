package edu.isi.karma.controller.command.transformation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;

public class PyTransformConsolidator extends CommandConsolidator {

	@Override
	public List<Command> consolidateCommand(List<Command> commands, Workspace workspace) {
		List<Command> refinedCommands = new ArrayList<Command>();
		//
		for (Command command : commands) {
			if (command instanceof SubmitEditPythonTransformationCommand) {
				Iterator<Command> itr = refinedCommands.iterator();
				boolean flag = true;
				boolean found = false;
				while(itr.hasNext()) {
					Command tmp = itr.next();
					if (tmp.getOutputColumns().equals(command.getOutputColumns()) && tmp instanceof SubmitPythonTransformationCommand && !(tmp instanceof SubmitEditPythonTransformationCommand)) {
//						System.out.println("May Consolidate");
						SubmitPythonTransformationCommand py = (SubmitPythonTransformationCommand)tmp;
						SubmitPythonTransformationCommand edit = (SubmitPythonTransformationCommand)command;
						JSONArray inputJSON = new JSONArray(py.getInputParameterJson());
						HistoryJsonUtil.setArgumentValue("transformationCode", edit.getTransformationCode(), inputJSON);
						py.setInputParameterJson(inputJSON.toString());
						py.setTransformationCode(edit.getTransformationCode());
						flag = false;
//						System.out.println(py.getInputParameterJson());
						try {
							py.doIt(workspace);
						} catch (CommandException e) {
							// TODO Auto-generated catch block
						}
						found = true;
						//PlaceHolder
					}
					if (tmp.getOutputColumns().equals(command.getOutputColumns()) && tmp instanceof SubmitEditPythonTransformationCommand && command instanceof SubmitEditPythonTransformationCommand) {
//						System.out.println("Here");
						found = true;
						itr.remove();
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

}
