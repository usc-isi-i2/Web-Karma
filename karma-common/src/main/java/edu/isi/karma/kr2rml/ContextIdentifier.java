package edu.isi.karma.kr2rml;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class ContextIdentifier {
	private String name;
	private URL location;
	public ContextIdentifier(String name, URL location)
	{
		this.name = name;
		this.location = location;
	}
	
	public ContextIdentifier(JSONObject id) throws MalformedURLException, JSONException
	{
		this.name = id.getString("name");
		this.location = new URL(id.getString("location"));
	}
	
	public String getName() {
		return name;
	}
	public URL getLocation() {
		return location;
	}
	
	public JSONObject toJSON()
	{
		JSONObject id = new JSONObject();
		id.put("name", name);
		id.put("location", location.toString());
		return id;
	}
}
