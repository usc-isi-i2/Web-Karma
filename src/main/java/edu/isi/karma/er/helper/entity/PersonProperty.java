package edu.isi.karma.er.helper.entity;

import java.util.Vector;

public class PersonProperty {

	private String predicate;
	
	private Vector<String> value = null;
	
	public PersonProperty() {
		this.value = new Vector<String>();
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public Vector<String> getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value.add(value);
	}
	
	
}
