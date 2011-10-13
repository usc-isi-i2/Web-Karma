package edu.isi.karma.util;

import org.json.JSONException;
import org.json.JSONWriter;

public interface Jsonizable {
	public void write(JSONWriter writer) throws JSONException;
}
