package edu.isi.karma.web.services.rdf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.commons.httpclient.NameValuePair;

public class TestRDFGeneratorServlet {

	public static void main(String[] args)  throws IOException, MalformedURLException, ProtocolException{
		
		HttpURLConnection urlCon;
		URL url = new URL("http://localhost:8080/rdf/metadata/images");
		String strJSON = "{\"metadata\":{\"GPSTimeStamp\":\"NOT_AVAILABLE\",\"ISOSpeedRatings\":\"100\",\"Orientation\":\"6\",\"Model\":\"GT-N7100\",\"WhiteBalance\":\"0\",\"GPSLongitude\":\"NOT_AVAILABLE\",\"ImageLength\":\"2448\",\"FocalLength\":\"3.7\",\"HasFaces\":\"1\",\"ImageName\":\"20140707_134558.jpg\",\"GPSDateStamp\":\"NOT_AVAILABLE\",\"Flash\":\"0\",\"DateTime\":\"2014:07:07 13:45:58\",\"NumberOfFaces\":\"1\",\"ExposureTime\":\"0.020\",\"GPSProcessingMethod\":\"NOT_AVAILABLE\",\"FNumber\":\"2.6\",\"ImageWidth\":\"3264\",\"GPSLatitude\":\"NOT_AVAILABLE\",\"GPSAltitudeRef\":\"-1\",\"Make\":\"SAMSUNG\",\"GPSAltitude\":\"-1.0\"}}";
		
		
		urlCon = (HttpURLConnection) url.openConnection();
		
		urlCon.setDoOutput(true); //its a POST request
		
		urlCon.setRequestMethod("POST"); //is not needed, but still...
		
		urlCon.setRequestProperty("Content-Type", "text/plain");
		
		
		//byte[] data = strJSON.getBytes("UTF-8");
		urlCon.setFixedLengthStreamingMode(strJSON.length());
		
		OutputStream os = urlCon.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		
		writer.write(strJSON);
		writer.flush();
		writer.close();
		os.close();
		
		urlCon.connect();
		
		
		System.out.print("run");
		

	}
	
	public static String QueryParams(NameValuePair params) throws UnsupportedEncodingException
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(URLEncoder.encode(params.getName(), "UTF-8"));
		sb.append("=");
		sb.append(URLEncoder.encode(params.getValue(), "UTF-8"));
		
		return sb.toString();
	}

}
