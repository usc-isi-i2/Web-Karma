package edu.isi.karma.rdf;

import java.io.InputStream;
import java.util.List;

public class RDFGeneratorInputWrapper {
	
	private InputStream is = null;
	private String data = null;
	private List<String> headers = null;
	private List<List<String>> values = null;

	public RDFGeneratorInputWrapper(InputStream is) {
		this.is=is;
	}
	
	public RDFGeneratorInputWrapper(String data) {
		this.data = data;
	}
	
	public RDFGeneratorInputWrapper(List<String> headers, List<List<String>> values) {
		this.headers = headers;
		this.values = values;
	}

	public String getData() {
		return data;
	}
	public List<String> getHeaders() {
		return headers;
	}
	public List<List<String>> getValues() {
		return values;
	}
	public InputStream getIs() {
		return is;
	}

}
