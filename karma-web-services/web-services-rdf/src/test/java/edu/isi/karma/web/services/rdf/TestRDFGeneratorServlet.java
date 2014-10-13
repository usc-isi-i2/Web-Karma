package edu.isi.karma.web.services.rdf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.container.servlet.WebComponent;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;

public class TestRDFGeneratorServlet extends JerseyTest {

	public TestRDFGeneratorServlet() throws Exception {
		super();
	}
	
	@Override
    public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder()
	    	.initParam(WebComponent.RESOURCE_CONFIG_CLASS,
	              ClassNamesResourceConfig.class.getName())
	        .initParam(
	              ClassNamesResourceConfig.PROPERTY_CLASSNAMES,
	              RDFGeneratorServlet.class.getName()) //Add more classnames Class1;Class2;Class3
	        .build();
    }
 
    @Override
    public TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

	@Test
	public void testR2RMLRDF() {
		
		WebResource webRes = resource().path("r2rml/rdf");

		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		formParams.add(FormParameters.R2RML_URL,
				getTestResource("metadata.json-model.ttl").toString());
		formParams
				.add(FormParameters.RAW_DATA,
						"{\"metadata\":{\"GPSTimeStamp\":\"NOT_AVAILABLE\",\"ISOSpeedRatings\":\"100\",\"Orientation\":\"6\",\"Model\":\"GT-N7100\",\"WhiteBalance\":\"0\",\"GPSLongitude\":\"NOT_AVAILABLE\",\"ImageLength\":\"2448\",\"FocalLength\":\"3.7\",\"HasFaces\":\"1\",\"ImageName\":\"20140707_134558.jpg\",\"GPSDateStamp\":\"NOT_AVAILABLE\",\"Flash\":\"0\",\"DateTime\":\"2014:07:07 13:45:58\",\"NumberOfFaces\":\"1\",\"ExposureTime\":\"0.020\",\"GPSProcessingMethod\":\"NOT_AVAILABLE\",\"FNumber\":\"2.6\",\"ImageWidth\":\"3264\",\"GPSLatitude\":\"NOT_AVAILABLE\",\"GPSAltitudeRef\":\"-1\",\"Make\":\"SAMSUNG\",\"GPSAltitude\":\"-1.0\"}}");
		
		formParams.add(FormParameters.CONTENT_TYPE, FormParameters.CONTENT_TYPE_JSON);
		String response = webRes.type(MediaType.APPLICATION_FORM_URLENCODED)
				.post(String.class, formParams);
		System.out.print(response);
		String sampleTriple = "<20140707_134558.jpg> <http://www.semanticdesktop.org/ontologies/2007/05/10/nexif#make> \"SAMSUNG\" .";
		int idx = response.indexOf(sampleTriple);
		assert(idx != -1);
		
		String[] lines = response.split(System.getProperty("line.separator"));
		assertEquals(17, lines.length);
	}
	
	@Test
	public void testR2RMLRDFVirtuoso() throws IOException,
			MalformedURLException, ProtocolException {
		URL url = new URL("http://fusion-sqid.isi.edu:8890");
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(10000);
			InputStreamReader reader = new InputStreamReader(connection.getInputStream());
			reader.read();
		}catch(Exception e) {
			return;
		}
		WebResource webRes = resource().path("r2rml/rdf/sparql");

		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();

		formParams.add(FormParameters.SPARQL_ENDPOINT,
				"http://fusion-sqid.isi.edu:8890/sparql-graph-crud-auth/");
		formParams.add(FormParameters.GRAPH_URI,
				"http://fusion-sqid.isi.edu:8890/image-metadata");
		formParams.add(FormParameters.TRIPLE_STORE,
				FormParameters.TRIPLE_STORE_VIRTUOSO);
		formParams.add(FormParameters.OVERWRITE, "True");
		formParams.add(FormParameters.R2RML_URL,
				getTestResource("metadata.json-model.ttl").toString());
		// formParams.add("DataURL", "");
		formParams
				.add(FormParameters.RAW_DATA,
						"{\"metadata\":{\"GPSTimeStamp\":\"NOT_AVAILABLE\",\"ISOSpeedRatings\":\"100\",\"Orientation\":\"6\",\"Model\":\"GT-N7100\",\"WhiteBalance\":\"0\",\"GPSLongitude\":\"NOT_AVAILABLE\",\"ImageLength\":\"2448\",\"FocalLength\":\"3.7\",\"HasFaces\":\"1\",\"ImageName\":\"20140707_134558.jpg\",\"GPSDateStamp\":\"NOT_AVAILABLE\",\"Flash\":\"0\",\"DateTime\":\"2014:07:07 13:45:58\",\"NumberOfFaces\":\"1\",\"ExposureTime\":\"0.020\",\"GPSProcessingMethod\":\"NOT_AVAILABLE\",\"FNumber\":\"2.6\",\"ImageWidth\":\"3264\",\"GPSLatitude\":\"NOT_AVAILABLE\",\"GPSAltitudeRef\":\"-1\",\"Make\":\"SAMSUNG\",\"GPSAltitude\":\"-1.0\"}}");
		formParams.add(FormParameters.CONTENT_TYPE, FormParameters.CONTENT_TYPE_JSON);
		formParams.add(FormParameters.USERNAME, "finimg");
		formParams.add(FormParameters.PASSWORD, "isi");

		String response = webRes.type(MediaType.APPLICATION_FORM_URLENCODED)
				.post(String.class, formParams);
		System.out.print(response);
		String sampleTriple = "<20140707_134558.jpg> <http://www.semanticdesktop.org/ontologies/2007/05/10/nexif#make> \"SAMSUNG\" .";
		int idx = response.indexOf(sampleTriple);
		assert(idx != -1);
		
		String[] lines = response.split(System.getProperty("line.separator"));
		assertEquals(17, lines.length);
	}

	@Test
	public void testR2RMLRDFSesame() {
		//TODO: Add testcase for Sesame
	}


	private URL getTestResource(String name) {
		return getClass().getClassLoader().getResource(name);
	}

}
