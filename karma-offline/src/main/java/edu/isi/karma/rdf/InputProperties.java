package edu.isi.karma.rdf;

import java.util.HashMap;

public class InputProperties {

	public enum InputProperty {
		MAX_NUM_LINES,
		ENCODING,
		DELIMITER,
		TEXT_QUALIFIER,
		HEADER_START_INDEX,
		DATA_START_INDEX,
		WORKSHEET_INDEX
		
	}
	private HashMap<InputProperty, Object> properties;
	
	public InputProperties() {
		this.properties = new HashMap<>();
	}
	
	public void set(InputProperty name, Object value) {
		this.properties.put(name, value);
	}
	
	public Object get(InputProperty name) {
		return this.properties.get(name);
	}
}
