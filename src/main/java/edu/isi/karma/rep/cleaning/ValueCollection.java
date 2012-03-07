package edu.isi.karma.rep.cleaning;

import java.util.Collection;

import org.json.JSONArray;

public interface ValueCollection {
	public abstract String getValue(String id);
	public abstract Collection<String> getValues();
	public abstract Collection<String> getNodeIDs();
	public abstract void setValue(String id, String val);
	public abstract JSONArray getJson();
}
