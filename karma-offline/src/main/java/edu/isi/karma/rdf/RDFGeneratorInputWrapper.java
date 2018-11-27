package edu.isi.karma.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class RDFGeneratorInputWrapper {
	
	private InputStream is = null;
	private String data = null;
	private List<String> headers = null;
	private List<List<String>> values = null;
	private File inputFile;

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
	
	public RDFGeneratorInputWrapper(File inputFile) {
		this.inputFile = inputFile;
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

    public InputStream getInputAsStream() throws IOException {
		InputStream inputStream = null;
		if(inputFile != null)
		{
			inputStream = new FileInputStream(inputFile);
		}
		else if(data != null)
		{
			inputStream = IOUtils.toInputStream(data, Charset.forName("UTF-8"));
		}
		else if(is != null)
		{
			inputStream = is;
		}
		return inputStream;
    }
}
