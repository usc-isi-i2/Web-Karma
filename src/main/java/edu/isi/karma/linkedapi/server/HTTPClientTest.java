package edu.isi.karma.linkedapi.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.CharEncoding;

import edu.isi.karma.model.serialization.MimeType;

public class HTTPClientTest {

	/**
	* Sends an HTTP GET request to a url
	*
	* @param endpoint - The URL of the server. (Example: " http://www.yahoo.com/search")
	* @param requestParameters - all the request parameters (Example: "param1=val1&param2=val2"). Note: This method will add the question mark (?) to the request - DO NOT add it yourself
	* @return - The response from the end point
	*/
	public static String sendGetRequest(String endpoint, String requestParameters)
	{
		String result = null;
		if (endpoint.startsWith("http://"))
		{
			// Send a GET request to the servlet
			try {
				// Construct data
				//StringBuffer data = new StringBuffer();
	 
				// Send data
				String urlStr = endpoint;
				if (requestParameters != null && requestParameters.length () > 0) {
					urlStr += "?" + requestParameters;
				}
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection ();
	 
				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
				rd.close();
				result = sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	
	/**
	* Reads data from the data reader and posts it to a server via POST request.
	* data - The data you want to send
	* endpoint - The server's address
	* output - writes the server's response to output
	* @throws Exception
	*/
	public static void postData(Reader data, URL endpoint, Writer output) throws Exception
	{
		HttpURLConnection urlc = null;
		try
		{
			urlc = (HttpURLConnection) endpoint.openConnection();
			try {
				urlc.setRequestMethod("POST");
			} catch (ProtocolException e) {
				throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
			}
		
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setUseCaches(false);
			urlc.setAllowUserInteraction(false);
//			urlc.setRequestProperty("Content-type", "text/xml; charset=" + "UTF-8");
			urlc.setRequestProperty("Content-Type", MimeType.APPLICATION_XML);
			urlc.addRequestProperty("format", "N3");
//			urlc.setRequestProperty("Content-Type", MimeType.APPLICATION_RDF_N3);			
	 
			OutputStream out = urlc.getOutputStream();
	 
			try {
				Writer writer = new OutputStreamWriter(out, "UTF-8");
				pipe(data, writer);
				writer.close();
			} catch (IOException e) {
				throw new Exception("IOException while posting data", e);
			} finally {
				if (out != null)
					out.close();
			}
	 
			InputStream in = urlc.getInputStream();
			try {
				Reader reader = new InputStreamReader(in);
				pipe(reader, output);
				reader.close();
			} catch (IOException e) {
				throw new Exception("IOException while reading response", e);
			} finally {
				if (in != null)
					in.close();
			}
	 
		} catch (IOException e) {
			throw new Exception("Connection error (is server running at " + endpoint + " ?): " + e);
		} finally {
			if (urlc != null)
			urlc.disconnect();
		}
	}
	 
	/**
	* Pipes everything from the reader to the writer via a buffer
	*/
	private static void pipe(Reader reader, Writer writer) throws IOException
	{
		char[] buf = new char[1024];
		int read = 0;
		while ((read = reader.read(buf)) >= 0)
		{
			writer.write(buf, 0, read);
		}
		writer.flush();
	}

	public static void postData2(String data, URL endpoint, Writer output) throws Exception
	{
		HttpURLConnection urlc = null;
		try
		{
			urlc = (HttpURLConnection) endpoint.openConnection();
			try {
				urlc.setRequestMethod("POST");
			} catch (ProtocolException e) {
				throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
			}
		
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setUseCaches(false);
			urlc.setAllowUserInteraction(false);
			urlc.setRequestProperty("Content-type", "text/xml; charset=" + CharEncoding.UTF_8);
//			urlc.setRequestProperty("Content-Type", MimeType.APPLICATION_RDF_XML);			
//			urlc.setRequestProperty("Content-Type", MimeType.APPLICATION_RDF_N3);			
	 
	        OutputStreamWriter wr = new OutputStreamWriter(urlc.getOutputStream(), CharEncoding.UTF_8);
	        wr.write(data);
	        wr.flush();
	        
	        // Get the response
	        BufferedReader rd = new BufferedReader(new InputStreamReader(urlc.getInputStream(), CharEncoding.UTF_8));
	        String line;
	        while ((line = rd.readLine()) != null) {
	            System.out.println(line);
	        }
	        wr.close();
	        rd.close();
	 
		} catch (IOException e) {
			throw new Exception("Connection error (is server running at " + endpoint + " ?): " + e);
		} finally {
			if (urlc != null)
			urlc.disconnect();
		}
	}
	 
	public static void main(String[] args) throws MalformedURLException, Exception {
		
		String xmlData = 
			"<?xml version=\"1.0\"?> \n" +
			"<rdf:RDF xmlns:geo=\"http://isi.edu/ontologies/geo/current#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"> \n" +
			"	<geo:Feature rdf:about=\"http://www.geonames.org/5112085\"> \n" +
			"		<geo:description>first feature</geo:description> \n" +
			"		<geo:lat>40.78343</geo:lat> \n" +
			"		<geo:long>-73.96625</geo:long> \n" +
			"	</geo:Feature> \n" +
			"	<geo:Feature rdf:about=\"http://www.geonames.org/5125771\"> \n" +
			"		<geo:description>second feature</geo:description> \n" +
			"		<geo:lat>40.71012</geo:lat> \n" +
			"		<geo:long>-73.90078</geo:long> \n" +
			"	</geo:Feature> \n" +
			"</rdf:RDF>";
		
//		String n3Data = 
//				"@prefix geo:     <http://isi.edu/ontologies/geo/current#> .\n" + 
//				"@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
//
//				"<http://www.geonames.org/5112085> rdf:type geo:Feature.\n" +
//				"<http://www.geonames.org/5112085> geo:description \"first feature\".\n" +
//				"<http://www.geonames.org/5112085> geo:lat \"40.78343\".\n" +
//				"<http://www.geonames.org/5112085> geo:long \"-73.96625\".\n" +
//
//				"<http://www.geonames.org/5125771> rdf:type geo:Feature.\n" +
//				"<http://www.geonames.org/5125771> geo:description \"second feature\".\n" +
//				"<http://www.geonames.org/5125771> geo:lat \"40.71012\".\n" +
//				"<http://www.geonames.org/5125771> geo:long \"-73.90078\".\n";
		
		InputStream is = new ByteArrayInputStream(xmlData.getBytes());
		InputStreamReader in= new InputStreamReader(is);
		BufferedReader bin= new BufferedReader(in);

		String endpoint = "http://localhost:8080/karma/services?id=CDA81BE4-DD77-E0D3-D033-FC771B2F4800&format=N3";
		Writer writer = new OutputStreamWriter(System.out);//, "UTF-8");
		postData(bin , new URL(endpoint), writer);
//		postData( new FileReader("C:\\Users\\mohsen\\Desktop\\karma\\input.rdf"), new URL(endpoint), writer);
//		postData2( new String(xmlData.getBytes(CharEncoding.UTF_8)), new URL(endpoint), writer);
	}
}
