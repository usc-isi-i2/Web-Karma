package edu.isi.karma.rdf;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.kr2rml.planning.RootStrategy;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.rdf.InputProperties.InputProperty;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class RDFGeneratorRequest {

	private RootStrategy strategy;
	private File inputFile;
	private String inputData;
	protected InputProperties inputProperties
	;
	private InputStream inputStream;
	private InputType dataType;
	private boolean addProvenance;
	private List<KR2RMLRDFWriter> writers = new LinkedList<>();
	private String modelName;
	private String sourceName;
	private String contextName;
	private List<String> tripleMapToKill;
	private List<String> tripleMapToStop;
	private List<String> POMToKill;
	private ServletContextParameterMap contextParameters;
	public RDFGeneratorRequest(String modelName, String sourceName)
	{

		this.modelName = modelName;
		this.sourceName = sourceName;
		this.addProvenance = false;
		this.inputProperties = new InputProperties();
		this.strategy = null;
		this.inputFile = null;
		this.inputStream = null;
		this.contextName = null;
		tripleMapToKill = new ArrayList<>();
		tripleMapToStop = new ArrayList<>();
		POMToKill = new ArrayList<>();
		contextParameters = null;
	}

	public void setTripleMapToKill(List<String> tripleMapToKill) {
		if (tripleMapToKill != null) {
			this.tripleMapToKill = tripleMapToKill;
		}
	}

	public List<String> getTripleMapToKill() {
		return tripleMapToKill;
	}
	
	public void setTripleMapToStop(List<String> tripleMapToStop) {
		if (tripleMapToStop != null) {
			this.tripleMapToStop = tripleMapToStop;
		}
	}

	public List<String> getTripleMapToStop() {
		return tripleMapToStop;
	}
	
	public void setPOMToKill(List<String> POMToKill) {
		if (POMToKill != null) {
			this.POMToKill = POMToKill;
		}
	}

	public List<String> getPOMToKill() {
		return POMToKill;
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

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public void setMaxNumLines(int maxNumLines) {
		this.inputProperties.set(InputProperty.MAX_NUM_LINES, maxNumLines);
	}

	public void setDelimiter(String delim) {
		if(delim == null)
			delim = ",";
		this.inputProperties.set(InputProperty.DELIMITER, delim);
	}
	
	public void setDataStartIndex(int idx) {
		this.inputProperties.set(InputProperty.DATA_START_INDEX, idx);
	}
	
	public void setHeaderStartIndex(int idx) {
		this.inputProperties.set(InputProperty.HEADER_START_INDEX, idx);
	}
	
	public void setTextQualifier(String qualifier) {
		if(qualifier == null)
			qualifier = "\"";
		this.inputProperties.set(InputProperty.TEXT_QUALIFIER, qualifier);
	}
	
	public void setEncoding(String encoding) {
		this.inputProperties.set(InputProperty.ENCODING, encoding);
	}
	
	public void setWorksheetIndex(int index) {
		this.inputProperties.set(InputProperty.WORKSHEET_INDEX, index);
	}

	public InputProperties getInputTypeProperties() {
		return this.inputProperties;
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

	public void addWriters(Collection<KR2RMLRDFWriter> writers) {
		this.writers.addAll(writers);
	}

	public String getModelName() {
		return modelName;
	}

	public String getSourceName() {
		return sourceName;
	}
	
	public void setContextParameters(ServletContextParameterMap contextParameters)
	{
		this.contextParameters = contextParameters;
	}
	
	public ServletContextParameterMap getContextParameters()
	{
		return this.contextParameters;
	}

}
