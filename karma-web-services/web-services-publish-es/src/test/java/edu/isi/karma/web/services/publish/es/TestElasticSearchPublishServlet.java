package edu.isi.karma.web.services.publish.es;

import java.net.URL;

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

public class TestElasticSearchPublishServlet extends JerseyTest {

	public TestElasticSearchPublishServlet() throws Exception {
		super();
	}
	
	@Override
    public WebAppDescriptor configure() {
		return new WebAppDescriptor.Builder()
	    	.initParam(WebComponent.RESOURCE_CONFIG_CLASS,
	              ClassNamesResourceConfig.class.getName())
	        .initParam(
	              ClassNamesResourceConfig.PROPERTY_CLASSNAMES,
	              ElasticSearchPublishServlet.class.getName()) //Add more classnames Class1;Class2;Class3
	        .build();
    }
 
    @Override
    public TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

//	@Test
//	public void testPublishJSON() {
//		
//		WebResource webRes = resource().path("data");
//		
//		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
//		
//		formParams.add(FormParameters.CONTEXT_URL, getTestResource("webpage-context.json").toString());
//		formParams.add(FormParameters.R2RML_URL,
//				getTestResource("webpage-model.ttl").toString());
//		formParams
//				.add(FormParameters.DATA_URL,
//						getTestResource("webpage-sample.json").toString());
//		formParams.add(FormParameters.CONTENT_TYPE, FormParameters.CONTENT_TYPE_JSON);
//
//		
//		formParams.add(FormParameters.ES_INDEX, "dig-sotera");
//		formParams.add(FormParameters.ES_TYPE, "WebPage");
//		formParams.add(FormParameters.ES_HOSTNAME, "localhost");
//		formParams.add(FormParameters.ES_PORT, "9200");
//		formParams.add(FormParameters.ES_PROTOCOL, "http");
//		formParams.add(FormParameters.CONTEXT_ROOT, "http://schema.org/WebPage1");
//		
//
//		String response = webRes.type(MediaType.APPLICATION_FORM_URLENCODED)
//				.post(String.class, formParams);;
//		System.out.println(response);
//	}
	
	private URL getTestResource(String name) {
		return getClass().getClassLoader().getResource(name);
	}

}
