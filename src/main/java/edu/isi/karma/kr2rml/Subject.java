package edu.isi.karma.kr2rml;

import java.util.List;

import edu.isi.karma.rep.Node;

public class Subject {

	List<Node> references;
	String uri;
	
	public Subject(List<Node> references, String uri)
	{
		this.references = references;
		this.uri = uri;
	}
	
	public String getURI()
	{
		return uri;
	}
	
	public List<Node> getReferences()
	{
		return references;
	}
}
