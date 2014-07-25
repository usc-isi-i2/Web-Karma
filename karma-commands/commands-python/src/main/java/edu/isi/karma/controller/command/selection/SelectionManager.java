package edu.isi.karma.controller.command.selection;

import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class SelectionManager {
	private Map<String, Selection> selectionMapping = new HashMap<String, Selection>();
	private Selection currentSelection = null;
	private Workspace workspace;
	private String worksheetId;
	
	public SelectionManager(Workspace workspace, String worksheetId) {
		this.workspace = workspace;
		this.worksheetId = worksheetId;
	}
	public Selection defineSelection(String name) {
		Selection t = new Selection(workspace, worksheetId);
		selectionMapping.put(name, t);
		return t;
	}
	
	public void renameSelection(String oldName, String newName) throws KarmaException {
		Selection t = selectionMapping.get(oldName);
		if (t != null) {
			selectionMapping.remove(oldName);
			selectionMapping.put(newName, t);
		}
		else
			throw new KarmaException("Name not Found");
	}
	
	public void setCurrentSelection(String name) throws KarmaException {
		Selection t = selectionMapping.get(name);
		if (t != null) {
			currentSelection = t;
		}
		else
			throw new KarmaException("Name not Found");
	}
	
	public Selection getCurrentSelection() {
		return currentSelection;
	}
 }
