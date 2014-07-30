package edu.isi.karma.web.services.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
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
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.N3KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
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


@Path("/metadata") 
public class RDFGeneratorServlet{
	
	private static Logger logger = LoggerFactory.getLogger(RDFGeneratorServlet.class);
	private final String CONTENT_TYPE_XML = "application/xml";

	/**
	 * 
	 * @throws ClientProtocolException 
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws KarmaException */
	
	
	@POST
	@Consumes("text/plain")
	@Path("/images")
	public Response postRDF(String metadataJSON) throws ClientProtocolException, IOException, JSONException, KarmaException
	{
		try
		{
			logger.info("Calling the web service");
	
			logger.info("Generating RDF");
			String strRDF = GenerateRDF(metadataJSON);
	
			logger.info("Publishing to Virtuoso");
			HttpHost httpHost = new HttpHost("fusion-sqid.isi.edu", 8890, "http");
			//TODO - pass graphURI as parameter
			String graphURI = "http://fusion-sqid.isi.edu:8890/image-metadata"; //Context in Sesame
			
			HttpPost httpPost = new HttpPost("http://" + httpHost.getHostName() + ":" + httpHost.getPort() +
											"/sparql-graph-crud-auth?graph-uri=" + graphURI);
			
			httpPost.setEntity(new StringEntity(strRDF));
			String userName = "finimg"; //Virtuoso User, should have appropriate roles
			String password = "isi"; //Has to be better way for this
			
			logger.info("RDF Published to Virtuoso");
			
			int responseCode = invokeHTTPRequestWithAuth(httpHost, httpPost, CONTENT_TYPE_XML, null, userName, password);
			//int responseCode = invokeHTTPDeleteWithAuth(httpHost,"http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth?graph-uri=http://fusion-sqid.isi.edu:8890/image-metadata", userName, password);
			return Response.status(responseCode).entity("Success").build();
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
		

		/* 
		 *Store to Virtuoso - Geospatial capability, the commented code below will store the rdf to Sesame 
		 * 
		 * 
		 * TripleStoreUtil tsu = new TripleStoreUtil();
		String tripleStoreURL = "http://localhost:3000/openrdf-sesame/repositories/karma_data";

		String context = "http://image-metadata.com";
		Boolean replace = true;
		String baseURI = "http://isiimagefinder/";

		logger.info("Publishing rdf to triplestore:" + "http://localhost:3000/openrdf-sesame/repositories/karma_data");
		boolean success = tsu.saveToStoreFromString(sw.toString(), tripleStoreURL, context, replace, baseURI);*/


		///if(success)
		//	return Response.status(201).entity(metadataJSON).build(); //successfully created
		//else
		//	return Response.status(503).entity("OOPS it did not work").build(); //Something went wrong
		//TODO set better return codes in case of error

		//return Response.status(200).entity(output).build();

	}
	
	private String GenerateRDF(String metadataJSON) throws KarmaException, JSONException, IOException
	{

			logger.info("Parse and model JSON:" + metadataJSON);
			
			UpdateContainer uc = new UpdateContainer();
			KarmaMetadataManager userMetadataManager = new KarmaMetadataManager();
			userMetadataManager.register(new UserPreferencesMetadata(), uc);
			userMetadataManager.register(new UserConfigMetadata(), uc);
			userMetadataManager.register(new PythonTransformationMetadata(), uc);
	
	        SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
	        
	        ModelingConfiguration.setLearnerEnabled(false); // disable automatic learning
			
	        GenericRDFGenerator gRDFGen = new GenericRDFGenerator();
			
	        //R2RML Mapping file - has to be a local file for now
			R2RMLMappingIdentifier rmlID = new R2RMLMappingIdentifier("rdf-model",
					new File("/Users/d3admin/Documents/WSP1WS2-metadata.json-model.ttl").toURI().toURL());
			
			gRDFGen.addModel(rmlID);
			
			//String filename = "C:\\metadata.json";
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			URIFormatter uriFormatter = new URIFormatter();
			KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
			
			//gRDFGen.generateRDF("rdf-model", new File(filename), InputType.JSON, false, outWriter);
			logger.info("Generating RDF");
			gRDFGen.generateRDF("rdf-model","PHONE",metadataJSON , InputType.JSON, false, outWriter);
			
			return sw.toString();
		
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
		
		//logger.info(Integer.toString(response.getStatusLine().getStatusCode()));
		
		// Parse the response and store it in a String
		/*HttpEntity entity = response.getEntity();
		StringBuilder responseString = new StringBuilder();
		if (entity != null) {
			BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
			
			String line = buf.readLine();
			while(line != null) {
				responseString.append(line);
				responseString.append('\n');
				line = buf.readLine();
			}
		}*/
		
		//logger.info("Response: " +responseString.toString());
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
