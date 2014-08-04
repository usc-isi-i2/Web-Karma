package edu.isi.karma.web.services.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.map.LRUMap;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.hsqldb.lib.StringInputStream;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.N3KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rdf.GenericRDFGenerator;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.util.HTTPUtil.HTTP_HEADERS;
import edu.isi.karma.webserver.KarmaException;


@Path("/r2rml") 
public class RDFGeneratorServlet{
	
	private static final int MODEL_CACHE_SIZE = 20;
	private static Logger logger = LoggerFactory.getLogger(RDFGeneratorServlet.class);
	private static LRUMap modelCache = new LRUMap(MODEL_CACHE_SIZE);
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/rdf")
	public String RDF(MultivaluedMap<String, String> formParams) 
	{
		try
		{
			logger.info("Path - r2rml/rdf . Generate and return RDF as String");
			return getRDF(formParams);
		}
		catch(JSONException je)
		{
			return "JSONException: " + je.getMessage();
		}
		catch(KarmaException ke)
		{
			return "KarmaException: " + ke.getMessage();
		}
		catch(IOException ioe)
		{
			return "IOException: " + ioe.getMessage();
		}
		catch(Exception e)
		{
			return "Exception: " + e.getMessage();
		}
		
	}

	/**
	 * 
	 * @throws ClientProtocolException 
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws KarmaException */
	
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/clearCache")
	public Response clearCache(MultivaluedMap<String, String> formParams) {
		modelCache.clear();
		return Response.status(200).entity("Success").build();
	}
	
	/**
	 * 
	 * @throws ClientProtocolException 
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws KarmaException */
	
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/sparql")
	public Response saveToTriplestore(MultivaluedMap<String, String> formParams)
	{
		try
		{
			logger.info("Path - r2rml/sparql. Store RDF to triplestore and return the Response");

			logger.info("Generating RDF for: " + formParams.getFirst(FormParameters.RAW_DATA));
			
			String strRDF = getRDF(formParams);
			if(strRDF != null)
			{
				int responseCode = PublishRDFToTripleStore(formParams, strRDF);
				
				
				//TODO Make it better
				if(responseCode == 200 || responseCode == 201)
					return Response.status(responseCode).entity("Success").build();
				else
					return Response.status(responseCode).entity("Failure: Check logs for more information").build();
			}
			
			return Response.status(401).entity("Failure: Check logs for more information").build();
			
		}
		catch(ClientProtocolException cpe)
		{
			logger.error("ClientProtocolException : " + cpe.getMessage());
			return Response.status(401).entity("ClientProtocolException : " + cpe.getMessage()).build();
		}
		catch(IOException ioe)
		{
			logger.error("IOException : " + ioe.getMessage());
			return Response.status(401).entity("IOException : " + ioe.getMessage()).build();
		}
		catch(KarmaException ke)
		{
			logger.error("KarmaException : " + ke.getMessage());
			return Response.status(401).entity("KarmaException : " + ke.getMessage()).build();
		}
		catch(JSONException je)
		{
			logger.error("JSONException : " + je.getMessage());
			return Response.status(401).entity("JSONException : " + je.getMessage()).build();
		}
		catch(Exception e)
		{
			logger.error("Exception : " + e.getMessage());
			return Response.status(401).entity("Exceptionß : " + e.getMessage()).build();
		}
		

	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/rdf/sparql")
	public String saveAndReturnRDF(MultivaluedMap<String, String> formParams)
	{
		try
		{
			logger.info("Path - r2rml/rdf/sparql. Store RDF to triplestore and return the Response");
			

			String strRDF = getRDF(formParams);
			
			if(strRDF != null)
			{
				int responseCode = PublishRDFToTripleStore(formParams, strRDF);
				
				//TODO Make it better
				if(responseCode == 200 || responseCode == 201)
					logger.info("Successfully completed");
				else
					logger.error("There was an error while publishing to Triplestore");
				
			}
			
			return strRDF;
		}
		catch(ClientProtocolException cpe)
		{
			logger.error("ClientProtocolException : " + cpe.getMessage());
			return "ClientProtocolException : " + cpe.getMessage();
		}
		catch(IOException ioe)
		{
			logger.error("IOException : " + ioe.getMessage());
			return "IOException : " + ioe.getMessage();
		}
		catch(KarmaException ke)
		{
			logger.error("KarmaException : " + ke.getMessage());
			return "KarmaException : " + ke.getMessage();
		}
		catch(JSONException je)
		{
			logger.error("JSONException : " + je.getMessage());
			return "JSONException : " + je.getMessage();
		}
		catch(Exception e)
		{
			logger.error("Exception : " + e.getMessage());
			return "Exception : " + e.getMessage();
		}
	}
	
	/*
	 * If URL is provided, data will be fetched from the URL, else raw Data in JSON, CSV or XML should be provided
	 */
	
	private String getRDF(MultivaluedMap<String, String> formParams) throws JSONException, MalformedURLException, KarmaException, IOException
	{
		
		boolean refreshModel = false;
		String sRefreshModel = formParams.getFirst(FormParameters.REFRESH_MODEL);
		if(sRefreshModel != null && sRefreshModel.equalsIgnoreCase("true"))
			refreshModel = true;
		
		if(formParams.getFirst(FormParameters.DATA_URL) != null || formParams.getFirst(FormParameters.DATA_URL).trim() != "")
			return GenerateRDF(new URL(formParams.getFirst(FormParameters.DATA_URL)).openStream(),
									   formParams.getFirst(FormParameters.R2RML_URL),
									   formParams.getFirst(FormParameters.CONTENT_TYPE),
									   refreshModel);
		
		if(formParams.getFirst(FormParameters.RAW_DATA) != null || formParams.getFirst(FormParameters.RAW_DATA).trim() != "")
			return GenerateRDF(formParams.getFirst(FormParameters.RAW_DATA), 
							   formParams.getFirst(FormParameters.R2RML_URL),
					    	   formParams.getFirst(FormParameters.CONTENT_TYPE),
					    	   refreshModel);
		
		return null;
	}
	
	private int PublishRDFToTripleStore(MultivaluedMap<String, String> formParams, String strRDF) throws ClientProtocolException, IOException, KarmaException
	{
		String tripleStoreURL = getTripleStoreURL(formParams);
		
		Boolean overWrite = Boolean.parseBoolean(formParams.getFirst(FormParameters.OVERWRITE));
		int responseCode;
		
		logger.info("Publishing RDF to TripleStore: " + tripleStoreURL);
		
		switch (formParams.getFirst(FormParameters.TRIPLE_STORE))
		{
			case FormParameters.TRIPLE_STORE_VIRTUOSO : URL url = new URL(tripleStoreURL);
														
														HttpHost httpHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
			
														HttpPost httpPost = new HttpPost(tripleStoreURL);
			
														httpPost.setEntity(new StringEntity(strRDF));
														
														if(overWrite) //Manually delete everything if overWrite is set to true for Virtuoso
															invokeHTTPDeleteWithAuth(httpHost,tripleStoreURL,formParams.getFirst(FormParameters.USERNAME), 
																									formParams.getFirst(FormParameters.PASSWORD));
			
														responseCode = invokeHTTPRequestWithAuth(httpHost, httpPost, MediaType.APPLICATION_XML, 
																									null, formParams.getFirst(FormParameters.USERNAME), 
																									formParams.getFirst(FormParameters.PASSWORD));
														
														break;
											
			case FormParameters.TRIPLE_STORE_SESAME : TripleStoreUtil tsu = new TripleStoreUtil();
			
													  //TODO Find purpose of Graph URI and replace it with formParams
													  String baseURI = "http://isiimagefinder/";
													  
													  boolean success = tsu.saveToStoreFromString(strRDF, tripleStoreURL, formParams.getFirst(FormParameters.GRAPH_URI), overWrite, baseURI);
													  responseCode = (success) ? 200 : 503; //HTTP OK or Error
													  break;
			default : responseCode = 404 ;
						   break;
		}
		
		return responseCode;
	}
	
	
	private String getTripleStoreURL(MultivaluedMap<String, String> formParams)
	{
		StringBuilder sbTSURL = new StringBuilder();
		
		if(formParams.getFirst(FormParameters.SPARQL_ENDPOINT) != null || formParams.getFirst(FormParameters.SPARQL_ENDPOINT).trim() != "")
			sbTSURL.append(formParams.getFirst(FormParameters.SPARQL_ENDPOINT));
		
		if(formParams.getFirst(FormParameters.TRIPLE_STORE).equals(FormParameters.TRIPLE_STORE_VIRTUOSO))
			if(formParams.getFirst(FormParameters.GRAPH_URI) != null || formParams.getFirst(FormParameters.GRAPH_URI).trim() != "")
			{
				sbTSURL.append("?graph-uri=");
				sbTSURL.append(formParams.getFirst(FormParameters.GRAPH_URI));
			}
		
		return sbTSURL.toString();   
				
	}

	private String GenerateRDF(InputStream dataStream, String r2rmlURI, String dataType, boolean refreshR2RML) throws KarmaException, JSONException, IOException
	{		
			initialization();

			
	        GenericRDFGenerator gRDFGen = new GenericRDFGenerator();
			
			R2RMLMappingIdentifier rmlID = new R2RMLMappingIdentifier(r2rmlURI,
					new URL(r2rmlURI));
			gRDFGen.addModel(rmlID);
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			URIFormatter uriFormatter = new URIFormatter();
			KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
			

			WorksheetR2RMLJenaModelParser modelParser = getModel(rmlID, refreshR2RML);
			
			String sourceName = r2rmlURI;
			gRDFGen.generateRDF(modelParser, sourceName, dataStream, InputType.JSON, -1, false, outWriter);
			
			return sw.toString();
		
	}
	

	private String GenerateRDF(String rawData, String r2rmlURI, String dataType, boolean refreshModel) throws KarmaException, JSONException, IOException
	{
		return GenerateRDF(new StringInputStream(rawData), r2rmlURI, dataType, refreshModel);
	}

	static {
		try {
			initialization();
		} catch (KarmaException ke) {
			logger.error("KarmaException: " + ke.getMessage());
		}
	}
	private static void initialization() throws KarmaException {
		UpdateContainer uc = new UpdateContainer();
		KarmaMetadataManager userMetadataManager = new KarmaMetadataManager();
		userMetadataManager.register(new UserPreferencesMetadata(), uc);
		userMetadataManager.register(new UserConfigMetadata(), uc);
		userMetadataManager.register(new PythonTransformationMetadata(), uc);

		SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
		
		ModelingConfiguration.setLearnerEnabled(false); // disable automatic learning
	}
	

	private WorksheetR2RMLJenaModelParser getModel(R2RMLMappingIdentifier modelIdentifier, boolean refreshR2RML) throws JSONException, KarmaException {
		WorksheetR2RMLJenaModelParser modelParser = null;
		if(refreshR2RML == false) {
			modelParser = (WorksheetR2RMLJenaModelParser) modelCache.get(modelIdentifier.getName());
		}
		if(modelParser == null) {
			modelParser = new WorksheetR2RMLJenaModelParser(modelIdentifier);
			modelCache.put(modelIdentifier.getName(), modelParser);
		}
		return modelParser;
	}
	

	private int invokeHTTPRequestWithAuth(HttpHost httpHost, HttpPost httpPost, String contentType, 
			String acceptContentType, String userName, String password) throws ClientProtocolException, IOException 
	{
	
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		if (acceptContentType != null && !acceptContentType.isEmpty()) {
			httpPost.setHeader(HTTP_HEADERS.Accept.name(), acceptContentType);	
		}
		if (contentType != null && !contentType.isEmpty()) {
			httpPost.setHeader("Content-Type", contentType);
		}
		
		
		
		httpClient.getCredentialsProvider().setCredentials(
									 new AuthScope(httpHost.getHostName(), httpHost.getPort()), 
									 new UsernamePasswordCredentials(userName, password));
		
		AuthCache authCache = new BasicAuthCache();
		DigestScheme digestScheme = new DigestScheme();
		
		digestScheme.overrideParamter("realm", "SPARQL"); //Virtuoso specific
		//digestScheme.overrideParamter("nonce", new Nonc);
		
		authCache.put(httpHost, digestScheme);
		
		BasicHttpContext localcontext = new BasicHttpContext();
	    localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

	    // Execute the request
		HttpResponse response = httpClient.execute(httpHost, httpPost, localcontext);
		
		return response.getStatusLine().getStatusCode();
	}
	
	private int invokeHTTPDeleteWithAuth(HttpHost httpHost, String url, String userName, String password) throws ClientProtocolException, IOException
	{
		HttpDelete httpDelete = new HttpDelete(url);
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		httpClient.getCredentialsProvider().setCredentials(
									 new AuthScope(httpHost.getHostName(), httpHost.getPort()), 
									 new UsernamePasswordCredentials(userName, password));
		
		AuthCache authCache = new BasicAuthCache();
		DigestScheme digestScheme = new DigestScheme();
		
		digestScheme.overrideParamter("realm", "SPARQL"); //Virtuoso specific
		//digestScheme.overrideParamter("nonce", new Nonc);
		
		authCache.put(httpHost, digestScheme);
		
		BasicHttpContext localcontext = new BasicHttpContext();
	    localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

		
		
		
		// Execute the request
		HttpResponse response = httpClient.execute(httpHost, httpDelete, localcontext);
		
		logger.info(Integer.toString(response.getStatusLine().getStatusCode()));
		
		return response.getStatusLine().getStatusCode();
	}

}
