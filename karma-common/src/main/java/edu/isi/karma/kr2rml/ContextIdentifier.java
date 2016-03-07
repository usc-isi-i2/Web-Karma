package edu.isi.karma.kr2rml;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class ContextIdentifier {
	private String name;
	private URL location;
	private String content;
	
	public ContextIdentifier(String name, URL location, String content)
	{
		this.name = name;
		this.location = location;
		this.content = content;
	}
	
	public ContextIdentifier(String name, URL location)
	{
		this.name = name;
		this.location = location;
		this.content = null;
	}
	
	public ContextIdentifier(JSONObject id) throws MalformedURLException, JSONException
	{
		this.name = id.getString("name");
		this.location = new URL(id.getString("location"));
		this.content = id.getString("content");
		if(this.content.equals("null"))
			this.content = null;
	}
	
	public String getName() {
		return name;
	}
	
	public URL getLocation() {
		return location;
	}
	
	public String getContent() {
		return content;
	}
	
	public JSONObject toJSON()
	{
		JSONObject id = new JSONObject();
		id.put("name", name);
		id.put("location", location.toString());
		id.put("content", content);
		return id;
	}
}
