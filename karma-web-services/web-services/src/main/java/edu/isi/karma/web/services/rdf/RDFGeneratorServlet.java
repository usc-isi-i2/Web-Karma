package edu.isi.karma.web.services.rdf;

/*
 * changed by raj
 * merged the publish-es web service into web-service rdf
 * Now there is only one web-service which has all options
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ContextGenerator;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.N3KR2RMLRDFWriter;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rdf.GenericRDFGenerator;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.rdf.RDFGeneratorRequest;
import edu.isi.karma.util.HTTPUtil.HTTP_HEADERS;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

@Path("/")
public class RDFGeneratorServlet extends Application{

	private static final int MODEL_CACHE_SIZE = 20;
	private static Logger logger = LoggerFactory
			.getLogger(RDFGeneratorServlet.class);
	private static LRUMap modelCache = new LRUMap(MODEL_CACHE_SIZE);
	
	private static String webAppPath = null;
	private static final int retry = 10;
	private int bulksize = 100;
	private int sleepTime = 100;
	private ServletContext context;
	
	public RDFGeneratorServlet(@Context ServletContext context){
	
		this.context = context;
		try {
			initialization(context,null);
		} catch (KarmaException ke) {
			logger.error("KarmaException: " + ke.getMessage());
		}
		String bulksize = context.getInitParameter("ESBulkSize");
		if(bulksize != null)
			this.bulksize = Integer.parseInt(bulksize);
		String sleep = context.getInitParameter("ESUploadInterval");
		if(sleep != null)
			this.sleepTime = Integer.parseInt(sleep);
	}
	
	
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/rdf/r2rml/rdf")
	public String RDF(MultivaluedMap<String, String> formParams) {
		try {
			logger.info("Path - r2rml/rdf . Generate and return RDF as String");
			return getRDF(formParams);
		} catch (Exception e) {
			logger.error("Error generating RDF", e);
			return "Exception: " + e.getMessage();
		}

	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/rdf/r2rml/json")
	public String JSON(MultivaluedMap<String, String> formParams) {
		try {
			logger.info("Path - r2rml/json . Generate and return JSON ld as String");
			return getJSON(context,formParams);
		} catch (Exception e) {
			logger.error("Error generating JSON", e);
			return "Exception: " + e.getMessage();
		}

	}

	/**
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 * @throws KarmaException
	 */

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/rdf/r2rml/clearCache")
	public Response clearCache(MultivaluedMap<String, String> formParams) {
		modelCache.clear();
		return Response.status(200).entity("Success").build();
	}

	/**
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 * @throws KarmaException
	 */

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/rdf/r2rml/sparql")
	public Response saveToTriplestore(MultivaluedMap<String, String> formParams) {
		try {
			logger.info("Path - r2rml/sparql. Store RDF to triplestore and return the Response");

			logger.info("Generating RDF for: "
					+ formParams.getFirst(FormParameters.RAW_DATA));

			String strRDF = getRDF(formParams);
			if (strRDF != null) {
				int responseCode = PublishRDFToTripleStore(formParams, strRDF);

				// TODO Make it better
				if (responseCode == 200 || responseCode == 201)
					return Response.status(responseCode).entity("Success")
							.build();
				else
					return Response.status(responseCode)
							.entity("Failure: Check logs for more information")
							.build();
			}

			return Response.status(401)
					.entity("Failure: Check logs for more information").build();

		} catch (Exception e) {
			logger.error("Exception : " + e.getMessage());
			return Response.status(401)
					.entity("Exception : " + e.getMessage()).build();
		}

	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/rdf/r2rml/rdf/sparql")
	public String saveAndReturnRDF(MultivaluedMap<String, String> formParams) {
		try {
			logger.info("Path - r2rml/rdf/sparql. Store RDF to triplestore and return the Response");

			String strRDF = getRDF(formParams);

			if (strRDF != null) {
				int responseCode = PublishRDFToTripleStore(formParams, strRDF);

				// TODO Make it better
				if (responseCode == 200 || responseCode == 201)
					logger.info("Successfully completed");
				else
					logger.error("There was an error while publishing to Triplestore");

			}

			return strRDF;
		} catch (Exception e) {
			logger.error("Exception : " + e.getMessage());
			return "Exception : " + e.getMessage();
		}
	}
	
	// service for pushing data into ES using given r2rml model
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/publish/es/data")
	public String publishFromData(MultivaluedMap<String, String> formParams) {
		try {
			logger.info("Path - es/json . Generate jsonld and publish to ES");
			ElasticSearchConfig esConfig = ElasticSearchConfig.parse(context, formParams);
			String jsonld = getJSON(context,formParams);
			if(jsonld != null)
				return publishES(jsonld, esConfig);
		} catch (Exception e) {
			logger.error("Error generating JSON", e);
			return "Exception: " + e.getMessage();
		}
		return null;
	}
	
	@POST
	@Path("/publish/es/data")
	@Consumes(MediaType.APPLICATION_JSON)
	public String publishFromData(JSONObject json) {
		try {
			logger.info("Path - es/json . Generate jsonld from multipart and publish to ES");
			ElasticSearchConfig esConfig = ElasticSearchConfig.parse(context, null);
			String jsonld = getJSON(context, null);
			if(jsonld != null)
				return publishES(jsonld, esConfig);
		} catch (Exception e) {
			logger.error("Error generating JSON", e);
			return "Exception: " + e.getMessage();
		}
		return null;
	}
	
	// service that pushes jsonld to ES directly 
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/publish/es/jsonld")
	public String publishFromJsonLD(MultivaluedMap<String, String> formParams) {
		try {
			logger.info("Path - es/jsonld . Publish JSONLD to ES");
			ElasticSearchConfig esConfig = ElasticSearchConfig.parse(context, formParams);
			InputStream is = null;
			if (formParams != null && formParams.containsKey(FormParameters.DATA_URL)
					&& formParams.getFirst(FormParameters.DATA_URL).trim() != "")
				is = (new URL(formParams.getFirst(FormParameters.DATA_URL)).openStream());
			else if(formParams != null && formParams.containsKey(FormParameters.RAW_DATA)
					&& formParams.getFirst(FormParameters.RAW_DATA).trim() != "")
				is =(IOUtils.toInputStream(formParams.getFirst(FormParameters.RAW_DATA)));
			String jsonld = IOUtils.toString(is);
			if(jsonld != null)
				return publishES(jsonld, esConfig);
		} catch (Exception e) {
			logger.error("Error generating JSON", e);
			return "Exception: " + e.getMessage();
		}
		return null;
	}
	/*
	 * If URL is provided, data will be fetched from the URL, else raw Data in
	 * JSON, CSV or XML should be provided
	 */

	private String getRDF(MultivaluedMap<String, String> formParams)
			throws JSONException, MalformedURLException, KarmaException,
			IOException {
//
//		boolean refreshModel = false;
//		if (formParams.containsKey(FormParameters.REFRESH_MODEL)
//				&& formParams.getFirst(FormParameters.REFRESH_MODEL)
//						.equalsIgnoreCase("true"))
//			refreshModel = true;

		InputStream is = null;
		if (formParams.containsKey(FormParameters.DATA_URL)
				&& formParams.getFirst(FormParameters.DATA_URL).trim() != "")
			is = new URL(formParams.getFirst(FormParameters.DATA_URL)).openStream();
		else if(formParams.containsKey(FormParameters.RAW_DATA)
				&& formParams.getFirst(FormParameters.RAW_DATA).trim() != "")
			is = IOUtils.toInputStream(formParams.getFirst(FormParameters.RAW_DATA));
		
		
		if(is != null) {
			String r2rmlURI = formParams.getFirst(FormParameters.R2RML_URL);
			String dataType = formParams.getFirst(FormParameters.CONTENT_TYPE);
			
			logger.info(r2rmlURI);
			logger.info(dataType);

			GenericRDFGenerator gRDFGen = new GenericRDFGenerator(null);

			R2RMLMappingIdentifier rmlID = new R2RMLMappingIdentifier(r2rmlURI,
					new URL(r2rmlURI));
			gRDFGen.addModel(rmlID);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			URIFormatter uriFormatter = new URIFormatter();
			KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);

			String sourceName = r2rmlURI;
			RDFGeneratorRequest request = generateRDFRequest(null,rmlID.getName(), sourceName, is, formParams, outWriter);
			gRDFGen.generateRDF(request);

			return sw.toString();
		}

		return null;
	}
	
	
	private HttpPost getHttpPost(ElasticSearchConfig esConfig) {
		return new HttpPost(esConfig.getProtocol()+"://" + esConfig.getHostname() + 
				":" + esConfig.getPort() + "/" + esConfig.getIndex() + "/_bulk");
	}
	
	private CloseableHttpClient getHttpClient(ElasticSearchConfig esConfig) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		if(esConfig.getProtocol().equalsIgnoreCase("https")) {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} else if(esConfig.getProtocol().equalsIgnoreCase("http"))
			return HttpClients.createDefault();
		return null;
		
	}
	
	
	private String getJSON(ServletContext context, MultivaluedMap<String, String> formParams) throws JSONException, MalformedURLException, KarmaException, IOException{
		
		InputStream is = null;
		URL urlContext = null;
		String jsonContext = null;
		InputStream isContext = null;
		String rdfGenerationSelection=null;
		String baseUri=null;
		
		if (formParams !=null && formParams.containsKey(FormParameters.DATA_URL) && formParams.getFirst(FormParameters.DATA_URL).trim() != "")
			is = new URL(formParams.getFirst(FormParameters.DATA_URL)).openStream();
		else if(formParams != null && formParams.containsKey(FormParameters.RAW_DATA) && formParams.getFirst(FormParameters.RAW_DATA).trim() != "")
			is = IOUtils.toInputStream(formParams.getFirst(FormParameters.RAW_DATA));
		
		if (formParams != null && formParams.containsKey(FormParameters.CONTEXT_URL) && formParams.getFirst(FormParameters.CONTEXT_URL).trim() != ""){
			urlContext = new URL(formParams.getFirst(FormParameters.CONTEXT_URL));
			isContext = urlContext.openStream();
			jsonContext = IOUtils.toString(isContext);
		} 
		
		
		if (formParams != null && formParams.containsKey(FormParameters.RDF_GENERATION_SELECTION) && formParams.getFirst(FormParameters.RDF_GENERATION_SELECTION).trim() != ""){
			rdfGenerationSelection = formParams.getFirst(FormParameters.RDF_GENERATION_SELECTION).trim();
		}
		
		if (formParams != null &&formParams.containsKey(FormParameters.BASE_URI) && formParams.getFirst(FormParameters.BASE_URI).trim() != ""){
			baseUri = formParams.getFirst(FormParameters.BASE_URI).trim();
		}
		
		if(is != null) {
			
			String r2rmlURI = null;
			
			if(formParams != null && formParams.containsKey(FormParameters.R2RML_URL))
				r2rmlURI = formParams.getFirst(FormParameters.R2RML_URL);
			else
				r2rmlURI = context.getInitParameter(FormParameters.R2RML_URL);
			
			
			String r2rmlFileName = new File(r2rmlURI).getName();
	        String contextFileName = null;
	       
	        if(urlContext == null){
	        	
	        	jsonContext = GenerateContext(r2rmlURI);
	        	contextFileName = r2rmlFileName.substring(0,r2rmlFileName.length()-4) + "_context.json";
	        	urlContext = writeContext(contextFileName, jsonContext);
	        }
	       
			GenericRDFGenerator rdfGen = new GenericRDFGenerator(rdfGenerationSelection);

			// Add the models in;
			R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
					"generic-model", new URL(r2rmlURI)); 
			rdfGen.addModel(modelIdentifier);

			//logger.info("Loading json file: " + jsonContext);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			JSONTokener token = new JSONTokener(IOUtils.toInputStream(jsonContext)); 

			ContextIdentifier contextId = new ContextIdentifier("generic-context", urlContext);
			JSONKR2RMLRDFWriter writer =null;
			if (baseUri != null)
				 writer = new JSONKR2RMLRDFWriter(pw,baseUri);
			else
				writer = new JSONKR2RMLRDFWriter(pw);
			
			writer.setGlobalContext(new JSONObject(token), contextId); 
			RDFGeneratorRequest request = generateRDFRequest(context,"generic-model", "Karma-Web-Services", is, formParams, writer);
			rdfGen.generateRDF(request);
			String rdf = sw.toString();
			
			sw.close();
			pw.close();
			is.close();
			if(isContext != null)
				isContext.close();
			
			return rdf;
		}
		
		return null;
	}
	
	private String publishES(String jsonld, ElasticSearchConfig esConfig) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		CloseableHttpClient httpClient = getHttpClient(esConfig);
		HttpPost httpPost = getHttpPost(esConfig);

		String bulkFormat = null;
		StringBuilder sb = new StringBuilder();

		logger.info("Got JSONLD, now pushing to ES");
		JSONArray jsonArray = null;
		if(jsonld.startsWith("["))
			jsonArray = new JSONArray(jsonld);
		else {
			JSONObject jObj = new JSONObject(jsonld);
			jsonArray = new JSONArray();
			jsonArray.put(jObj);
		}
		logger.info("FInished de-serializing JSON-LD");

		long counter = 0;
		Exception postException = null;
		String index = esConfig.getIndex();
		String type = esConfig.getType();

		for(int k=0; k<jsonArray.length(); k++) {
			JSONObject jObj = jsonArray.getJSONObject(k);


			String id = null;

			if(jObj.has("uri"))
			{
				id = jObj.getString("uri");
			}

			if(id != null)
			{
				bulkFormat = "{\"index\":{\"_index\":\"" + index+ "\",\"_type\":\""+ type +"\",\"_id\":\""+id+"\"}}";
			}
			else
			{
				bulkFormat = "{\"index\":{\"_index\":\"" + index+ "\",\"_type\":\""+ type +"\"}}";
			}
			sb.append(bulkFormat);
			sb.append(System.getProperty("line.separator"));
			sb.append(jObj.toString());
			sb.append(System.getProperty("line.separator"));
			counter++;
			if (counter % bulksize == 0) {
				int i = 0;
				Exception ex = null;
				while (i < retry) {
					try {
						StringEntity entity = new StringEntity(sb.toString(),"UTF-8");
						entity.setContentType("application/json");
						httpPost.setEntity(entity);
						httpClient.execute(httpPost);
						httpClient.close();
						System.out.println(counter + " processed");
						break;
					}catch(Exception e) {
						ex = e;
						logger.error("Error", e);
						i++;
					}
				}
				if (i > 0) {
					logger.error("Exception occurred!", ex);
					postException = ex;
					break;
				}
				httpClient = getHttpClient(esConfig);
				httpPost = getHttpPost(esConfig);
				sb = new StringBuilder();
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
				}
			}
		}
		try {
			StringEntity entity = new StringEntity(sb.toString(),"UTF-8");
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			httpClient.execute(httpPost);
			httpClient.close();
		} catch(Exception e) {
			postException = e;
		}

		if (postException != null) {
			logger.error("Exception occurred!", postException);
			return "{\"result\": {\"code\": \"0\", \"message\": \"" + postException.getMessage() + "\"}}";
		}
		return "{\"result\": {\"code\": \"1\", \"message\": \"success\"}}";
	}

	/*
	 * pulls all the python files from specified github url and creates a karma home
	 */
	private void createKarmaHome(String url, String path) throws ClientProtocolException, IOException{
		CloseableHttpClient client = HttpClientBuilder.create().build();
		if(!url.contains("karma")){
			url = url + "/contents";
		}
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		
		String response_contents = EntityUtils.toString(response.getEntity());
		JSONArray jsonArray = new JSONArray(response_contents);
		
		for(int i=0;i<jsonArray.length();i++){
			JSONObject object = jsonArray.getJSONObject(i);
			if(object.getString("name").equals("karma")){
				createKarmaHome(url+"/karma",path);
			}
			if(object.getString("name").equals("python")){
				createKarmaHome(url+"/python", path);
			}
			else if(url.contains("python")){
				String fileName = object.getString("name");
				String download_url = object.getString("download_url");
				request = new HttpGet(download_url);
				String file_contents = EntityUtils.toString(client.execute(request).getEntity());
				File pythonScript = new File(path+ "/python/" + fileName);
				pythonScript.getParentFile().mkdirs();
				FileUtils.writeStringToFile(pythonScript, file_contents.trim());
			}
		}
	}

	
	private RDFGeneratorRequest generateRDFRequest(ServletContext context, String modelName, String sourceName, InputStream is, MultivaluedMap<String, String> formParams, KR2RMLRDFWriter writer) throws KarmaException {
		RDFGeneratorRequest request = new RDFGeneratorRequest(modelName, sourceName);
		request.addWriter(writer);
		request.setInputStream(is);

		String dataType = null; 
		if ( formParams != null && formParams.containsKey(FormParameters.CONTENT_TYPE))
			dataType = formParams.getFirst(FormParameters.CONTENT_TYPE);
		request.setDataType(InputType.valueOf(dataType));
		request.setAddProvenance(false);

		if (formParams != null && formParams.containsKey(FormParameters.MAX_NUM_LINES))
			request.setMaxNumLines(Integer.parseInt(formParams.getFirst(FormParameters.MAX_NUM_LINES)));
		else
			request.setMaxNumLines(-1);

		if (formParams != null && formParams.containsKey(FormParameters.ENCODING))
			request.setEncoding(formParams.getFirst(FormParameters.ENCODING));
		if (formParams != null && formParams.containsKey(FormParameters.COLUMN_DELIMITER))
			request.setDelimiter(formParams.getFirst(FormParameters.COLUMN_DELIMITER));
		if (formParams != null && formParams.containsKey(FormParameters.TEXT_QUALIFIER))
			request.setTextQualifier(formParams.getFirst(FormParameters.TEXT_QUALIFIER));
		if (formParams != null && formParams.containsKey(FormParameters.DATA_START_INDEX))
			request.setDataStartIndex(Integer.parseInt(formParams.getFirst(FormParameters.DATA_START_INDEX)));
		if (formParams != null && formParams.containsKey(FormParameters.HEADER_START_INDEX))
			request.setHeaderStartIndex(Integer.parseInt(formParams.getFirst(FormParameters.HEADER_START_INDEX)));
		if (formParams != null && formParams.containsKey(FormParameters.WORKSHEET_INDEX))
			request.setWorksheetIndex(Integer.parseInt(formParams.getFirst(FormParameters.WORKSHEET_INDEX)));
		if (formParams != null && formParams.containsKey(FormParameters.RDF_GENERATION_ROOT) && formParams.getFirst(FormParameters.RDF_GENERATION_ROOT).trim() != ""){
			request.setStrategy(new UserSpecifiedRootStrategy(formParams.getFirst(FormParameters.RDF_GENERATION_ROOT).trim()));
		}
		// checks if remote karma repo url is there, if so downloads python files from that url and sets that directory as new karma home.
		if (formParams != null && formParams.containsKey(FormParameters.REMOTE_KARMA_HOME_REPO_URL) && formParams.getFirst(FormParameters.REMOTE_KARMA_HOME_REPO_URL).trim() != ""){
			try {
				
				String url = formParams.getFirst(FormParameters.REMOTE_KARMA_HOME_REPO_URL);
				String remoteKarmaURL = "https://api.github.com/repos/"+url.substring(new String("https://github.com/").length()); 
				String localKarmaHome = (String) modelCache.get(remoteKarmaURL);
				
				ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(localKarmaHome);
				String userKarmaHome = contextParameters.getKarmaHome();
				String karmas_dir = new File(userKarmaHome).getParentFile().getAbsolutePath() + "/karmas/";
				new File(karmas_dir).mkdirs();
				// if clearcache parameter is true it will delete old karma folder for the URL and download all files again.
				if(formParams.containsKey(FormParameters.CLEAR_CACHE)){
						// delete existing karma home
						if(localKarmaHome != null){
							FileUtils.deleteDirectory(new File(localKarmaHome));
							createKarmaHome(remoteKarmaURL, karmas_dir);
						}else{
							localKarmaHome = karmas_dir + remoteKarmaURL.hashCode();
							createKarmaHome(remoteKarmaURL, karmas_dir);
							modelCache.put(remoteKarmaURL, localKarmaHome);
						}
				}
				else{
					if(localKarmaHome == null){
						localKarmaHome = karmas_dir + remoteKarmaURL.hashCode();
						createKarmaHome(remoteKarmaURL, localKarmaHome);
						modelCache.put(remoteKarmaURL, localKarmaHome);
					}
				}
				initialization(context,localKarmaHome);
				contextParameters = ContextParametersRegistry.getInstance().getContextParameters(localKarmaHome);
				request.setContextParameters(contextParameters);
			} catch (ClientProtocolException e) {
				logger.error("Error in downloading github repo");
			} catch (IOException e) {
				logger.error("Error in downloading the github repo");
			}
			
		}
		
		return request;
	}

	private int PublishRDFToTripleStore(
			MultivaluedMap<String, String> formParams, String strRDF)
			throws ClientProtocolException, IOException, KarmaException {
		String tripleStoreURL = getTripleStoreURL(formParams);

		Boolean overWrite = Boolean.parseBoolean(formParams
				.getFirst(FormParameters.OVERWRITE));
		int responseCode;

		logger.info("Publishing RDF to TripleStore: " + tripleStoreURL);

		switch (formParams.getFirst(FormParameters.TRIPLE_STORE)) {
		case FormParameters.TRIPLE_STORE_VIRTUOSO:
			URL url = new URL(tripleStoreURL);

			HttpHost httpHost = new HttpHost(url.getHost(), url.getPort(),
					url.getProtocol());

			HttpPost httpPost = new HttpPost(tripleStoreURL);

			httpPost.setEntity(new StringEntity(strRDF));

			if (overWrite) // Manually delete everything if overWrite is set to
							// true for Virtuoso
				invokeHTTPDeleteWithAuth(httpHost, tripleStoreURL,
						formParams.getFirst(FormParameters.USERNAME),
						formParams.getFirst(FormParameters.PASSWORD));

			responseCode = invokeHTTPRequestWithAuth(httpHost, httpPost,
					MediaType.APPLICATION_XML, null,
					formParams.getFirst(FormParameters.USERNAME),
					formParams.getFirst(FormParameters.PASSWORD));

			break;

		case FormParameters.TRIPLE_STORE_SESAME:
			TripleStoreUtil tsu = new TripleStoreUtil();

			// TODO Find purpose of Graph URI and replace it with formParams
			String baseURI = "http://isiimagefinder/";

			boolean success = tsu.saveToStoreFromString(strRDF, tripleStoreURL,
					formParams.getFirst(FormParameters.GRAPH_URI), overWrite,
					baseURI);
			responseCode = (success) ? 200 : 503; // HTTP OK or Error
			break;
		default:
			responseCode = 404;
			break;
		}

		return responseCode;
	}

	private String getTripleStoreURL(MultivaluedMap<String, String> formParams) {
		StringBuilder sbTSURL = new StringBuilder();

		if (formParams.getFirst(FormParameters.SPARQL_ENDPOINT) != null
				|| formParams.getFirst(FormParameters.SPARQL_ENDPOINT).trim() != "")
			sbTSURL.append(formParams.getFirst(FormParameters.SPARQL_ENDPOINT));

		if (formParams.getFirst(FormParameters.TRIPLE_STORE).equals(
				FormParameters.TRIPLE_STORE_VIRTUOSO))
			if (formParams.getFirst(FormParameters.GRAPH_URI) != null
					|| formParams.getFirst(FormParameters.GRAPH_URI).trim() != "") {
				sbTSURL.append("?graph-uri=");
				sbTSURL.append(formParams.getFirst(FormParameters.GRAPH_URI));
			}

		return sbTSURL.toString();

	}

	public String GenerateContext(String  r2rmlURI) throws MalformedURLException, IOException {
		Model model = ModelFactory.createDefaultModel();
        InputStream s = new URL(r2rmlURI).openStream(); //get the r2rml from URI
        model.read(s, null, "TURTLE");
        JSONObject top = new ContextGenerator(model, true).generateContext();
        return top.toString();
		
	}
	
	private URL writeContext(String filename, String jsonContext) throws IOException {
        if(webAppPath == null) {
        	
        	String classFolder = this.getClass().getResource(".").toString();
        	int idx = classFolder.indexOf("/target");
        	StringBuilder base = new StringBuilder();
        	base.append(classFolder.substring(0,  idx));
        	if(base.toString().startsWith("file:")){
        		String path = base.substring(5); 
        		base.setLength(0);
        		base.append(path);
        	}
        	webAppPath = base.append("/src/main/webapp").toString();
        }
        
		File contextFile = new File(webAppPath+ "/" + filename);
        if(!contextFile.exists()){
        	contextFile.createNewFile();
        }
        FileWriter fw = new FileWriter(contextFile);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(jsonContext);
        bw.close();
        
        return new URL("http://"+InetAddress.getLocalHost().getHostAddress() + ":8080/"+filename);
	}
	
	public static void initContextParameters(ServletContext ctx, ServletContextParameterMap contextParameters)
	{
		Enumeration<?> params = ctx.getInitParameterNames();
		ArrayList<String> validParams = new ArrayList<String>();
		for (ContextParameter param : ContextParameter.values()) {
			validParams.add(param.name());
		}
		while (params.hasMoreElements()) {
			String param = params.nextElement().toString();
			if (validParams.contains(param)) {
				ContextParameter mapParam = ContextParameter.valueOf(param);
				String value = ctx.getInitParameter(param);
				contextParameters.setParameterValue(mapParam, value);
			}
		}

		String contextPath = ctx.getRealPath("/"); //File.separator was not working in Windows. / works
		contextParameters.setParameterValue(ContextParameter.WEBAPP_PATH, contextPath);
	}
	

	private static void initialization(ServletContext context,String karmaHome) throws KarmaException {
		 
		ContextParametersRegistry contextParametersRegistry = ContextParametersRegistry.getInstance();
		ServletContextParameterMap contextParameters = contextParametersRegistry.registerByKarmaHome(karmaHome);
		initContextParameters(context, contextParameters);
		UpdateContainer uc = new UpdateContainer();
		KarmaMetadataManager userMetadataManager = new KarmaMetadataManager(contextParameters);
		userMetadataManager.register(new UserPreferencesMetadata(contextParameters), uc);
		userMetadataManager.register(new UserConfigMetadata(contextParameters), uc);
		userMetadataManager.register(new PythonTransformationMetadata(contextParameters), uc);

		PythonRepository pythonRepository = new PythonRepository(false, contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonRepositoryRegistry.getInstance().register(pythonRepository);

		SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
		
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().register(contextParameters.getId());
		modelingConfiguration.setLearnerEnabled(false); // disable automatic													// learning
	}

	private int invokeHTTPRequestWithAuth(HttpHost httpHost, HttpPost httpPost,
			String contentType, String acceptContentType, String userName,
			String password) throws ClientProtocolException, IOException {

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

		digestScheme.overrideParamter("realm", "SPARQL"); // Virtuoso specific
		// digestScheme.overrideParamter("nonce", new Nonc);

		authCache.put(httpHost, digestScheme);

		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

		// Execute the request
		HttpResponse response = httpClient.execute(httpHost, httpPost,
				localcontext);

		return response.getStatusLine().getStatusCode();
	}

	private int invokeHTTPDeleteWithAuth(HttpHost httpHost, String url,
			String userName, String password) throws ClientProtocolException,
			IOException {
		HttpDelete httpDelete = new HttpDelete(url);

		DefaultHttpClient httpClient = new DefaultHttpClient();

		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(httpHost.getHostName(), httpHost.getPort()),
				new UsernamePasswordCredentials(userName, password));

		AuthCache authCache = new BasicAuthCache();
		DigestScheme digestScheme = new DigestScheme();

		digestScheme.overrideParamter("realm", "SPARQL"); // Virtuoso specific
		// digestScheme.overrideParamter("nonce", new Nonc);

		authCache.put(httpHost, digestScheme);

		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

		// Execute the request
		HttpResponse response = httpClient.execute(httpHost, httpDelete,
				localcontext);

		logger.info(Integer.toString(response.getStatusLine().getStatusCode()));

		return response.getStatusLine().getStatusCode();
	}
}
