package edu.isi.karma.modeling;

public class NameSet {

	private String ns;
	private String localName;

	public NameSet(String localName) {
		this.ns = "";
		this.localName = localName;
	}

	public NameSet(String ns, String localName) {
		this.ns = ns;
		this.localName = localName;
	}

	public String getNs() {
		return ns;
	}

	public String getLocalName() {
		return localName;
	}
	
	public String getLabel() {
		return (ns + localName);
	}


	
}
