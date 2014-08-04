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
	public static final String REFRESH_MODEL = "RefreshModel";
}
