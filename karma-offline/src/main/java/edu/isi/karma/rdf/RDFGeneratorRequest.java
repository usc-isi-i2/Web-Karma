package edu.isi.karma.rdf;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.planning.RootStrategy;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;

public class RDFGeneratorRequest {

	private RootStrategy strategy;
	private int maxNumLines;
	private File inputFile;
	private String inputData;
	private InputStream inputStream;
	private InputType dataType;
	private boolean addProvenance;
	private List<KR2RMLRDFWriter> writers = new LinkedList<KR2RMLRDFWriter>();
	private String modelName;
	private String sourceName;
	public RDFGeneratorRequest(String modelName, String sourceName)
	{
	
		this.modelName = modelName;
		this.sourceName = sourceName;
		this.addProvenance = false;
		this.maxNumLines = -1;
		this.strategy = null;
		this.inputFile = null;
		this.inputStream = null;
		
	}
	
	public boolean isValidRequest()
	{
		return inputFile != null || inputData != null || inputStream != null;
	}

	public RootStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(RootStrategy strategy) {
		this.strategy = strategy;
	}

	public int getMaxNumLines() {
		return maxNumLines;
	}

	public void setMaxNumLines(int maxNumLines) {
		this.maxNumLines = maxNumLines;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public String getInputData() {
		return inputData;
	}

	public void setInputData(String inputData) {
		this.inputData = inputData;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public InputType getDataType() {
		return dataType;
	}

	public void setDataType(InputType dataType) {
		this.dataType = dataType;
	}

	public boolean isAddProvenance() {
		return addProvenance;
	}

	public void setAddProvenance(boolean addProvenance) {
		this.addProvenance = addProvenance;
	}

	public List<KR2RMLRDFWriter> getWriters() {
		return writers;
	}

	public void addWriter(KR2RMLRDFWriter writer) {
		this.writers.add(writer);
	}

	public String getModelName() {
		return modelName;
	}

	public String getSourceName() {
		return sourceName;
	}
	
}
