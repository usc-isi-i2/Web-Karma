package edu.isi.karma.kr2rml;

public class ShortHandURI {
	private final String prefix;
	private final String uri;
	
	public ShortHandURI(String prefix, String uri) {
		this.prefix = prefix;
		this.uri = uri;
	}

	public String toString() {
		if (prefix != null) 
			return prefix + ":" + uri;
		else
			return uri;
	}

}
