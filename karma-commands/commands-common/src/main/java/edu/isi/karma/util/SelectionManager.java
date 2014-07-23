package edu.isi.karma.util;

import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.webserver.KarmaException;

public class SelectionManager {
	private Map<String, Selection> selectionMapping = new HashMap<String, Selection>();
	private Selection currentSelection = null;
	public Selection defineSelection(String name) {
		Selection t = new Selection();
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
