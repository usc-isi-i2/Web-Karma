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
/**
 * 
 */
package edu.isi.karma.controller.command;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import edu.isi.karma.rep.Entity;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;

/**
 * Abstract class for all commands.
 * 
 * @author szekely
 * 
 */
public abstract class Command extends Entity implements ICommand
{

	/**
	 * Has this command been executed already?
	 */
	private boolean isExecuted = false;

	/**
	 * Flag that should be unset if you don't want this command instance to be
	 * written into the history
	 */
	private boolean saveInHistory = true;

	/**
	 * Flag to tell if the command history should be written after this command
	 * has been executed
	 */
	private boolean writeWorksheetHistoryAfterCommandExecutes = true;

	private boolean appendToHistory = false;
	protected Set<String> inputColumns = new HashSet<String>();
	protected Set<String> outputColumns = new HashSet<String>();
	/**
	 * List of tags for the command
	 */
	private List<CommandTag> tags = new ArrayList<CommandTag>();

	private String inputParameterJson;
	
	private boolean isExecutedInBatch = false;
	
	protected Command(String id) {
		super(id);
	}

	@Override
	public boolean isExecuted() {
		return isExecuted;
	}

	@Override
	public void setExecuted(boolean isExecuted) {
		this.isExecuted = isExecuted;
	}

	public boolean isExecutedInBatch() {
		return isExecutedInBatch;
	}

	public void setExecutedInBatch(boolean isExecuted) {
		this.isExecutedInBatch = isExecuted;
	}
	
	@Override
	public boolean isSavedInHistory() {
		return saveInHistory;
	}

	@Override
	public void saveInHistory(boolean flag) {
		this.saveInHistory = flag;
	}

	@Override
	public void writeWorksheetHistoryAfterCommandExecutes(boolean flag) {
		this.writeWorksheetHistoryAfterCommandExecutes = flag;
	}

	@Override
	public boolean writeWorksheetHistoryAfterCommandExecutes() {
		return this.writeWorksheetHistoryAfterCommandExecutes;
	}

	/**
	 * @param prefix
	 * @param pw
	 * @param vWorkspace
	 * @param historyType
	 *            of the lists where the command is, either undo or redo.
	 */
	@Override
	public void generateJson(String prefix, PrintWriter pw,
	                         VWorkspace vWorkspace, HistoryType historyType) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref + JSONUtil.json(JsonKeys.commandId, getId()));
		pw.println(newPref + JSONUtil.json(JsonKeys.title, getTitle()));
		pw.println(newPref
				+ JSONUtil.json(JsonKeys.description, getDescription()));
		pw.println(newPref
				+ JSONUtil.json(JsonKeys.historyType, historyType.name()));
		pw.println(newPref
				+ JSONUtil.jsonLast(JsonKeys.commandType, getCommandType()
						.name()));
		pw.println(prefix + "}");
	}

	@Override
	public boolean hasTag(CommandTag tag) {
		return tags.contains(tag);
	}

	@Override
	public void addTag(CommandTag tag) {
		tags.add(tag);
	}

	@Override
	public String getInputParameterJson() {
		return inputParameterJson;
	}

	@Override
	public void setInputParameterJson(String inputParamJson) {
		this.inputParameterJson = inputParamJson;
	}

	@Override
	public List<CommandTag> getTags() {
		return tags;
	}

	@Override
	public boolean appendToHistory() {
		return appendToHistory;
	}

	@Override
	public void setAppendToHistory(boolean appendToHistory) {
		this.appendToHistory = appendToHistory;
	}

	// /////////////////////////////////////////////////////////////////////////////
	//
	// Methods to help with logging and error reporting.
	//
	// /////////////////////////////////////////////////////////////////////////////

	protected void logCommand(Logger logger, Workspace workspace) {
		try {
			logger.info("Executing command:\n"
					+ getArgsJSON(workspace).toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Pedro
	 * 
	 * We should have an abstraction for Commands that operate on worksheets,
	 * and this method should go there.
	 */
	protected String formatWorsheetId(Workspace workspace, String worksheetId) {
		return worksheetId + " ("
				+ workspace.getWorksheet(worksheetId).getTitle() + ")";
	}

	/*
	 * Pedro
	 * 
	 * Return an HNodeId in a nice format for printing on command logs.
	 */
	protected String formatHNodeId(Workspace workspace, String hNodeId) {
		return hNodeId + " (" + workspace.getFactory().getColumnName(hNodeId)
				+ ")";
	}

	/*
	 * Pedro
	 * 
	 * Meant to be overriden, but would need to define for all commands. We are
	 * going to need a nicer way to record the inputs of commands to reason
	 * about them, so perhaps this will also be easier and nicer to do.
	 */
	protected JSONObject getArgsJSON(Workspace workspace) {
		return new JSONObject();
	}
	
	public Set<String> getInputColumns() {
		return inputColumns;
	}
	
	public Set<String> getOutputColumns() {
		return outputColumns;
	}
	
	public void setInputColumns(Set<String> inputColumns) {
		this.inputColumns = inputColumns;
	}
	
	public void setOutputColumns(Set<String> outputColumns) {
		this.outputColumns = outputColumns;
	}
}
