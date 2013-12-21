package edu.isi.karma.kr2rml;

import java.net.URL;

public class R2RMLMappingIdentifier {

	private String name;
	private URL location;
	
	public R2RMLMappingIdentifier(String name, URL location)
	{
		this.name = name;
		this.location = location;
	}
	
	public String getName() {
		return name;
	}
	public URL getLocation() {
		return location;
	}
	
}
