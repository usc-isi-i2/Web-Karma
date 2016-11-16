package edu.isi.karma.web.services.rdf;

import edu.isi.karma.rdf.GenericRDFGenerator.InputType;

public class FormParameters {
	
	public static final String SPARQL_ENDPOINT = "SparqlEndPoint";
	public static final String GRAPH_URI = "GraphURI";
	public static final String R2RML_URL = "R2rmlURI";
	public static final String DATA_URL = "DataURL";
	public static final String RAW_DATA = "RawData";
	public static final String USERNAME = "UserName";
	public static final String PASSWORD = "Password";
	public static final String TRIPLE_STORE = "TripleStore";
	public static final String TRIPLE_STORE_SESAME = "Sesame";
	public static final String TRIPLE_STORE_VIRTUOSO = "Virtuoso";
	public static final String OVERWRITE = "Overwrite"; 
	public static final String CONTENT_TYPE = "ContentType";
	public static final String CONTENT_TYPE_CSV = InputType.CSV.toString();
	public static final String CONTENT_TYPE_JSON = InputType.JSON.toString();
	public static final String CONTENT_TYPE_XML = InputType.XML.toString();
	public static final String CONTENT_TYPE_EXCEL = InputType.EXCEL.toString();
	public static final String CONTENT_TYPE_JL = InputType.JL.toString();
	public static final String CONTEXT_URL="ContextURL";
	public static final String RDF_GENERATION_ROOT="RDFGenerationRoot";
	public static final String RDF_GENERATION_SELECTION="RDFGenerationSelection";
	public static final String BASE_URI="BaseURI";
	
	
	//Need one of these for every edu.isi.karma.rdf.InputProperties.InputProperty;
	public static final String MAX_NUM_LINES = "MaxNumLines";
	public static final String ENCODING = "Encoding";
	public static final String DATA_START_INDEX = "DataStartIndex";
	public static final String HEADER_START_INDEX = "HeaderStartIndex";
	public static final String COLUMN_DELIMITER = "ColumnDelimiter";
	public static final String TEXT_QUALIFIER = "TextQualifier";
	public static final String WORKSHEET_INDEX = "WorksheetIndex";
	
	public static final String REFRESH_MODEL = "RefreshModel";
}
