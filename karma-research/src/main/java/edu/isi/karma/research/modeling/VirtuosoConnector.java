package edu.isi.karma.research.modeling;

public class VirtuosoConnector {

	private String instance; // = "fusionRepository.isi.edu";
	private int port; // = 1200;  
	private String username; // = "dba";
	private String password; // = "dba";
	private String graphIRI;
	private Integer queryTimeout; // seconds
	
	public VirtuosoConnector(String instance, int port, String username, String password) {
		this.instance = instance; // "fusionRepository.isi.edu"
		this.port = port; //1200
		this.username = username ; //dba
		this.password = password; //dba
		this.graphIRI = null;
		this.queryTimeout = null;
	}

	public VirtuosoConnector(String instance, int port, String username, String password, String graphIRI) {
		this.instance = instance; // "fusionRepository.isi.edu"
		this.port = port; //1200
		this.username = username ; //dba
		this.password = password; //dba
		this.graphIRI = graphIRI;
		this.queryTimeout = null;
	}

	public String getInstance() {
		return instance;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getGraphIRI() {
		return graphIRI;
	}

	public Integer getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(Integer queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	
	
}
