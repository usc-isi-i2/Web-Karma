package edu.isi.karma.controller.command;

import java.io.PrintWriter;
import java.util.List;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.IEntity;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

/**
 * Class ICommand
 *
 * @since 01/23/2014
 */
public interface ICommand extends IEntity
{
	/**
	 * @return the internal name of this command. Used to communicate between
	 *         the server and the browser.
	 */
	String getCommandName();

	/**
	 * @return the label shown in the user interface.
	 */
	String getTitle();

	/**
	 * @return a description of what the command does or did, if it was
	 *         executed.
	 */
	String getDescription();

	/**
	 * @return the type of this command.
	 */
	CommandType getCommandType();

	UpdateContainer doIt(Workspace workspace)
			throws CommandException;

	UpdateContainer undoIt(Workspace workspace);

	boolean isExecuted();

	void setExecuted(boolean isExecuted);

	boolean isSavedInHistory();

	void saveInHistory(boolean flag);

	void generateJson(String prefix, PrintWriter pw,
	                  VWorkspace vWorkspace, HistoryType historyType);

	boolean hasTag(CommandTag tag);

	void addTag(CommandTag tag);

	String getInputParameterJson();

	void setInputParameterJson(String inputParamJson);

	List<CommandTag> getTags();

	CommandTag getTagFromPriority();

	String getModel();
	
	enum HistoryType {
		undo, redo, normal, lastRun
	}

	enum JsonKeys {
		commandId, title, description, commandType, historyType
	}

	enum CommandTag {
		Modeling, Transformation, Selection, SemanticType, Import, Other, IgnoreInBatch
	}
}
