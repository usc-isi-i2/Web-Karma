package edu.isi.karma.controller.command.selection;

import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.webserver.KarmaException;

public class SuperSelectionManager {
	private Map<String, SuperSelection> selectionMapping = new HashMap<String, SuperSelection>();
	private SuperSelection currentSelection = null;
	public SuperSelectionManager() {
		selectionMapping.put("DEFAULT_SELECTION", new SuperSelection(null));
	}
	
	public SuperSelection defineSelection(String name) {
		SuperSelection t = new SuperSelection();
		selectionMapping.put(name, t);
		return t;
	}
	
	public boolean removeSelection(String name) {
		return (selectionMapping.remove(name) != null);
	}
	
	public void renameSelection(String oldName, String newName) throws KarmaException {
		SuperSelection t = selectionMapping.get(oldName);
		if (t != null) {
			selectionMapping.remove(oldName);
			selectionMapping.put(newName, t);
		}
		else
			throw new KarmaException("Name not Found");
	}
	
	public void setCurrentSuperSelection(String name) throws KarmaException {
		SuperSelection t = selectionMapping.get(name);
		if (t != null) {
			currentSelection = t;
		}
		else
			throw new KarmaException("Name not Found");
	}
	
	public SuperSelection getCurrentSuperSelection() {
		return currentSelection;
	}
 }
