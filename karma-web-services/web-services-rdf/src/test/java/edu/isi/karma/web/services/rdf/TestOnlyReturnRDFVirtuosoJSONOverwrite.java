package edu.isi.karma.web.services.rdf;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TestOnlyReturnRDFVirtuosoJSONOverwrite {

	public static void main(String[] args) {
		ClientConfig clientConfig = new DefaultClientConfig();
		Client client = Client.create(clientConfig);
		
		WebResource webRes = client.resource(UriBuilder.fromUri("http://localhost:8080/rdf/r2rml/rdf").build());
		
		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		
		formParams.add(FormParameters.SPARQL_ENDPOINT, "http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/");
		formParams.add(FormParameters.GRAPH_URI, "http://fusion-sqid.isi.edu:8890/image-metadata");
		formParams.add(FormParameters.TRIPLE_STORE, FormParameters.TRIPLE_STORE_VIRTUOSO);
		formParams.add(FormParameters.OVERWRITE, "True");
		formParams.add(FormParameters.R2RML_URL, "/Users/d3admin/Documents/WSP1WS2-metadata.json-model.ttl");
		//formParams.add("DataURL", "");
		formParams.add(FormParameters.RAW_DATA, "{\"metadata\":{\"GPSTimeStamp\":\"NOT_AVAILABLE\",\"ISOSpeedRatings\":\"100\",\"Orientation\":\"6\",\"Model\":\"GT-N7100\",\"WhiteBalance\":\"0\",\"GPSLongitude\":\"NOT_AVAILABLE\",\"ImageLength\":\"2448\",\"FocalLength\":\"3.7\",\"HasFaces\":\"1\",\"ImageName\":\"20140707_134558.jpg\",\"GPSDateStamp\":\"NOT_AVAILABLE\",\"Flash\":\"0\",\"DateTime\":\"2014:07:07 13:45:58\",\"NumberOfFaces\":\"1\",\"ExposureTime\":\"0.020\",\"GPSProcessingMethod\":\"NOT_AVAILABLE\",\"FNumber\":\"2.6\",\"ImageWidth\":\"3264\",\"GPSLatitude\":\"NOT_AVAILABLE\",\"GPSAltitudeRef\":\"-1\",\"Make\":\"SAMSUNG\",\"GPSAltitude\":\"-1.0\"}}");
		formParams.add(FormParameters.USERNAME, "finimg");
		formParams.add(FormParameters.PASSWORD, "isi");
		formParams.add(FormParameters.CONTENT_TYPE, FormParameters.CONTENT_TYPE_JSON);
		
		String response = webRes.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class, formParams);
		
		System.out.print(response);

	}

}
