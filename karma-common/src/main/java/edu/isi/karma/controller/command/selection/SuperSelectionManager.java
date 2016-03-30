package edu.isi.karma.controller.command.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.webserver.KarmaException;

public class SuperSelectionManager {
	public static final String DEFAULT_SELECTION_NAME = "DEFAULT_SELECTION";
	public static final String DEFAULT_SELECTION_TEST_NAME = "DEFAULT_TEST";
	private Map<String, SuperSelection> selectionMapping = new HashMap<>();
	public static final SuperSelection DEFAULT_SELECTION = new SuperSelection(DEFAULT_SELECTION_NAME);
	public SuperSelectionManager() {
		selectionMapping.put(DEFAULT_SELECTION_NAME, DEFAULT_SELECTION);
		selectionMapping.put(DEFAULT_SELECTION_TEST_NAME, new SuperSelection(DEFAULT_SELECTION_TEST_NAME));
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
		return new ArrayList<>(selectionMapping.values());
	}
 }
