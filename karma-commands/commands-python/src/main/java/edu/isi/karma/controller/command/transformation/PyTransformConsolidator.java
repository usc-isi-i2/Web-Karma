package edu.isi.karma.controller.command.transformation;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;

public class PyTransformConsolidator extends CommandConsolidator {

	@Override
	public Pair<ICommand, Object> consolidateCommand(List<ICommand> commands, ICommand newCommand, Workspace workspace) {
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
						return new ImmutablePair<>(tmp, (Object)oldInputJSON);
				}
			}
		}
		return null;
	}

}
