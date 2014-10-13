package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.webserver.KarmaException;

public class SuperSelectionManager {
	private Map<String, SuperSelection> selectionMapping = new HashMap<String, SuperSelection>();
	public static final SuperSelection DEFAULT_SELECTION = new SuperSelection("DEFAULT_SELECTION");
	public SuperSelectionManager() {
		selectionMapping.put("DEFAULT_SELECTION", DEFAULT_SELECTION);
		selectionMapping.put("DEFAULT_TEST", new SuperSelection("DEFAULT_TEST"));
	}
	
	public SuperSelection defineSelection(String name) {
		SuperSelection t = new SuperSelection(name);
		selectionMapping.put(name, t);
		return t;
	}
	
	public SuperSelection defineSelection(String name, SuperSelection sel) {
		selectionMapping.put(name, sel);
		return sel;
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
	
	public boolean hasSelection(String name) {
		return selectionMapping.containsKey(name);
	}
	
	public SuperSelection getSuperSelection(String name) {
		if (name == null)
			return DEFAULT_SELECTION;
		SuperSelection sel = selectionMapping.get(name);
		return sel == null ? DEFAULT_SELECTION : sel;
	}
	
	public List<SuperSelection> getAllDefinedSelection() {
		return new ArrayList<SuperSelection>(selectionMapping.values());
	}
 }
