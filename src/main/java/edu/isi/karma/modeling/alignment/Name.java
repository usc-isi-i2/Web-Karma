package edu.isi.karma.modeling.alignment;

public class Name {

	private String uri;
	private String ns;
	private String prefix;
	
	public Name(String uri, String ns, String prefix) {
		this.uri = uri;
		this.ns = ns;
		this.prefix = prefix;
	}

	public Name(Name n) {
		this.uri = n.uri;
		this.ns = n.ns;
		this.prefix = n.prefix;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}


	public void setNs(String ns) {
		this.ns = ns;
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	public String getUri() {
		return uri;
	}

	public String getNs() {
		return ns;
	}

	public String getPrefix() {
		return prefix;
	}
	
	
}
