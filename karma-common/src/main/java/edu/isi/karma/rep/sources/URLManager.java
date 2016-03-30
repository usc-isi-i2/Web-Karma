/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.rep.sources;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.webserver.KarmaException;

public class URLManager {

	static Logger logger = LoggerFactory.getLogger(URLManager.class);

	private URLManager() {
		
	}

	public static List<URL> getURLsFromJSON(String requestURLsJSONArray) throws JSONException, MalformedURLException {

		List<URL> urls = new ArrayList<>();
		JSONArray jsonArray = new JSONArray(requestURLsJSONArray);
		
//		String firstEndpoint = "";
		for (int i = 0; i < jsonArray.length(); i++) {
			URL url = new URL(jsonArray.getString(i).trim());
//			if (i == 0) firstEndpoint = getEndPoint(url);
			
			// only urls with the same endpoints will be added to the list.
//			if (firstEndpoint.equalsIgnoreCase(getEndPoint(url)))
			urls.add(url);
		}
		
		return urls;
	}
	
	public static List<URL> getURLsFromStrings(List<String> requestURLStrings) 
	throws MalformedURLException, KarmaException {

		List<URL> urls = new ArrayList<>();
		
		String firstEndpoint = "";
		for (int i = 0; i < requestURLStrings.size(); i++) {
			String urlStr = requestURLStrings.get(i).trim();
			urlStr = urlStr.replaceAll(Pattern.quote(" "), "%20");
			URL url = new URL(urlStr);
			if (i == 0) firstEndpoint = getEndPoint(url);
			
			// only urls with the same endpoints will be added to the list.
			if (!firstEndpoint.equalsIgnoreCase(getEndPoint(url)))
				throw new KarmaException("To model a service, all request examples should have the same endpoint.");
			urls.add(url);
		}
		
		return urls;
	}
	
	/**
	 * Example: "http://www.test.com/getVideos?user=demo" returns "http://www.test.com/getVideos"
	 * Example: "http://www.test.com/getVideos/" returns "http://www.test.com/getVideos"
	 * @attribute url
	 * @return
	 */
	public static String getEndPoint(URL url) {
		String endPoint = url.getHost() + url.getPath();
		if (endPoint.endsWith("/"))
			endPoint = endPoint.substring(0, endPoint.length() - 1);
		return endPoint;
	}
	
	/**
	 * Example: "http://www.test.com/getVideos?user=demo" returns "http://www.test.com"
	 * Example: "http://www.test.com/getVideos/" returns "http://www.test.com/getVideos/"
	 * @attribute url
	 * @return
	 */
	public static String getServiceAddress(URL url) {
		String address = url.getProtocol() + "://" + url.getHost() + url.getPath();
		int index = address.lastIndexOf("/");
		if (index != -1) {
			address = address.substring(0, index);
		}
		address += "/";
		
		return address;
	}
	
	/**
	 * Example: "http://www.test.com/getVideos?user=demo" returns "getVideos"
	 * Example: "http://www.test.com/getVideos/" returns "unknown"
	 * @attribute url
	 * @return
	 */
	public static String getOperationName(URL url) {
		String operationName = url.getPath();
		if (operationName.indexOf("/") != -1) 
			operationName = operationName.substring(operationName.lastIndexOf("/") + 1, operationName.length());
		if (operationName.trim().length() == 0)
			operationName = "";
		return operationName;
	}		
	
	/**
	 * Example: "http://www.test.com/getVideos?user=demo" returns "user=demo"
	 * Example: "http://www.test.com/getVideos/" returns ""
	 * @attribute url
	 * @return
	 */
	public static String getOperationAddress(URL url) {
		String address = "";

		address += getOperationName(url);
		
		if (url.getQuery() != null)
			address += "?" + url.getQuery().trim();
		
		return address;
	}

	private static boolean verifyAttributeExtraction(URL url, List<Attribute> attributeList) throws UnsupportedEncodingException {
		if (url.getQuery() == null && (attributeList == null || attributeList.isEmpty()))
			return true;
		
		if (url.getQuery() != null && url.getQuery().trim().length() == 0 && 
				(attributeList == null || attributeList.isEmpty()))
			return true;
		
		String originalQuery = URLDecoder.decode(url.getQuery(), "UTF-8");
		
		String query = "";
		for (Attribute p:attributeList) {
			query += p.getName().trim();
			query += "=";
			query += p.getValue().trim();
			query += "&";
		}
		if (query.endsWith("&"))
			query = query.substring(0, query.length()-1);
		
		if (query.equalsIgnoreCase(originalQuery))
			return true;
		
		return false;
	}
	
	public static List<Attribute> getQueryAttributes(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);
		return getQueryAttributes(url);
	}	    	

	public static List<Attribute> getQueryAttributes(URL url) {
	    try {
	    	
	    	String urlString = url.toString();
	        Map<String, List<String>> attributes = new HashMap<>();
	        List<Attribute> attributeList = new ArrayList<>();
	        HashMap<String, Integer> attributeNameCounter = new HashMap<>();
	        String attributeName;
	        String attributeId;
	        String attributeValue;
	        
	        String[] urlParts = urlString.split("\\?");
	        if (urlParts.length > 1) {
	            String query = urlParts[1];
	            for (String attribute : query.split("&")) {
	                try {
		            	String[] pair = attribute.split("=");
		                String key = URLDecoder.decode(pair[0], "UTF-8");
		                String value = "";
		                if (pair.length > 1) {
		                	for (int i = 1; i < pair.length; i++) {
		                		if (i != 1) value += "=";
		                		value += URLDecoder.decode(pair[i], "UTF-8");
		                	}
		                }
	
		                List<String> values = attributes.get(key);
		                if (values == null) {
		                    values = new ArrayList<>();
		                    attributes.put(key, values);
		                }
		                values.add(value);
		                
		    	    	// remember that we can have multiple values for a single attributeeter
		    	    	// example: /path/to/my/resource?attribute1=value1&attribute1=value2&attribute1=value3
		    	    	// we are currently ignoring these types of urls
		    	    	
		                if (!values.isEmpty()) {
			                attributeName = key;
		                	attributeId = getId(attributeName, attributeNameCounter); 
			                attributeValue = values.get(0);
			                Attribute p = new Attribute(attributeId, attributeName, IOType.INPUT, attributeValue);
			                attributeList.add(p);
			                logger.debug(p.getInfo());
		                }
	                } catch (Exception e) {
	                	//e.printStackTrace();
	                }
	            }
	        }
	        
	        try {
		        if (!verifyAttributeExtraction(url, attributeList)) {
		        	logger.error("Attributes have not been extracted successfully.");
		        }
	        } catch (UnsupportedEncodingException e) {
	        	logger.warn("There might be an error in extracting the attribute names from the URL.");
	        }
	        
	        logger.debug("Attributes extracted successfully from " + url.toString());
	        return attributeList;
	        
	    } catch (Exception ex) {
	        throw new AssertionError(ex);
	    }
	}

	private static String getId(String name, HashMap<String, Integer> nameCounter) {
		if (nameCounter == null)
			return null;

		Integer count = nameCounter.get(name);
		if (count == null) {
			nameCounter.put(name, 1);
			return Attribute.INPUT_PREFIX + name;
		} else {
			nameCounter.put(name, count.intValue() + 1);
			return (Attribute.INPUT_PREFIX + name + "_" + String.valueOf(count.intValue()));
		}
	}
	
	/**
	 * 
	 * @attribute urlList
	 * @return a list of attributes names
	 * @throws MalformedURLException 
	 */
	public static List<String>  extractAttributeNames(List<String> urlList) throws MalformedURLException {
		List<String> attributesNameList = new ArrayList<>();

		Attribute p;
		String key;
		for (int i = 0; i < urlList.size(); i++) {
			//System.out.println(urlList.get(i));
			List<Attribute> attributeList = getQueryAttributes(urlList.get(i));
			for (int j = 0; j < attributeList.size(); j++) {
				p = attributeList.get(j);
				key = p.getName();
				if (key.trim().length() > 0 && attributesNameList.indexOf(key) == -1) {
					attributesNameList.add(key);
				}

			}
		}
		return attributesNameList;
	}
	
	
	public static List<List<String>>  attributeValuesInTable(List<String> urlList, List<String> attributeNames) throws MalformedURLException {
		List<List<String>> allAttributes = new ArrayList<>();

		Attribute p;
		String key;
		int index;
		
		for (int i = 0; i < urlList.size(); i++) {
			//System.out.println(urlList.get(i));
			allAttributes.add(new ArrayList<String>());

			List<Attribute> attributeList = getQueryAttributes(urlList.get(i));
			
			for (int j = 0; j < attributeNames.size(); j++)
				allAttributes.get(i).add("");
			
			for (int j = 0; j < attributeList.size(); j++) {
				p = attributeList.get(j);
				key = p.getName();
				index = attributeNames.indexOf(key);
				if (p.getValue() != null && p.getValue().trim().length() > 0)
					if (index != -1)
						allAttributes.get(i).set(index, p.getValue().trim());
			}
		}
		return allAttributes;
	}
	
	public static List<List<String>>  attributeValuesInSets(List<String> urlList, List<String> attributeNames) throws MalformedURLException {
		List<List<String>> allAttributes = new ArrayList<>();

		Attribute p;
		String key;
		int index;
		
		for (int j = 0; j < attributeNames.size(); j++) {
			allAttributes.add(new ArrayList<String>());
			
			// Add empty value to each list
			allAttributes.get(j).add("");
		}
		
		for (int i = 0; i < urlList.size(); i++) {
			//System.out.println(urlList.get(i));
			//allAttributes.add(new ArrayList<String>());

			List<Attribute> attributeList = getQueryAttributes(urlList.get(i));
			
			for (int j = 0; j < attributeList.size(); j++) {
				p = attributeList.get(j);
				key = p.getName();
				index = attributeNames.indexOf(key);
				if (p.getValue() != null && p.getValue().trim().length() > 0)
					if (index != -1)
						if (allAttributes.get(index).indexOf(p.getValue().trim()) == -1)
							allAttributes.get(index).add(p.getValue().trim());
			}
		}
		return allAttributes;
	}
	
	public static void printAttributesTabular(List<String> attributeNames, List<List<String>> attributeValuesInTable) {
	
		try {
			
			for (int i = 0; i < attributeValuesInTable.size(); i++) {
				//System.out.println(urlList.get(i));
				System.out.println("******************************************");
				for (int j = 0; j < attributeValuesInTable.get(i).size(); j++) {
					System.out.write(attributeNames.get(j).getBytes());
					System.out.write(":\t".getBytes());
					System.out.write(attributeValuesInTable.get(i).get(j).getBytes());
					System.out.write("\n".getBytes());
				}
				System.out.write("\n".getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String printAttributesCSV(List<String> urlList, List<String> attributeNames, List<List<String>> attributeValues) {
		String csv = "";
		
		try {
			
			
			for (int i = 0; i < attributeNames.size(); i++) {
				if (i != 0)
					csv += ",";
				csv += "\"" + attributeNames.get(i) + "\"";
			}
			csv += "\n";

			for (int i = 0; i < attributeValues.size(); i++) {
				for (int j = 0; j < attributeValues.get(i).size(); j++) {
					if (j != 0)
						csv += ",";
					csv += "\"" + attributeValues.get(i).get(j) + "\"";
				}
				csv += "\n";
			}

			return csv;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static void printAttributesSets(List<String> attributeNames, List<List<String>> attributeValuesInSets) {
		
		for (int i = 0; i < attributeValuesInSets.size(); i++) {
			System.out.println(attributeNames.get(i));
			
			for (int j = 0; j < attributeValuesInSets.get(i).size(); j++) {
				System.out.println(attributeValuesInSets.get(i).get(j).trim());
			}
			
			System.out.println("******************");
		}

	}
	
	public static String createApiLink(String apiEndPoint, List<String> attributeNames, List<String> attributeValues) {
		String invocationURL = apiEndPoint.trim();
			
		if (!invocationURL.endsWith("?"))
			invocationURL += "?";

		String value;
		for (int i = 0; i < attributeNames.size(); i++) {
			value = attributeValues.get(i).trim();
			
			if (value == null || value.length() == 0 || value.equalsIgnoreCase(null))
				continue;
			if (!invocationURL.endsWith("?"))
				invocationURL += "&";
			
			invocationURL += attributeNames.get(i);
			invocationURL += "=";
			String refinedValue = attributeValues.get(i).replaceAll("\\ ", "\\+");
			invocationURL += refinedValue;
		}
		
//		System.out.println(invocationURL);
		
		return invocationURL;
		
	}
	

	
//	private static boolean isValidOutput(String str) {
//		return true;
//	}
	
//	public static ArrayList<String> pullLinks(String text) {
//		//Pull all links from the body for easy retrieval
//		ArrayList links = new ArrayList();
//		 
//		String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
//		Pattern p = Pattern.compile(regex);
//		Matcher m = p.matcher(text);
//		while(m.find()) {
//			String urlStr = m.group();
//			if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
//				urlStr = urlStr.substring(1, urlStr.length() - 1);
//			}
//			links.add(urlStr);
//		}
//		return links;
//	}
		
	public static void main(String[] args) throws MalformedURLException, UnsupportedEncodingException {

//		URL url = new URL("http://www.test.com/getVideos?user=demo");
//		URL url = new URL("http://www.test.com/?test=1");
		URL url = new URL("http://localhost:8080/SpatialReferenceSystemService?srid=2163&geometry=POINT%20(2236208.82887498%2093460.8811236587)");
		System.out.println(getEndPoint(url));
		System.out.println(getServiceAddress(url));
		System.out.println(getOperationName(url));
		System.out.println(getOperationAddress(url));
		List<Attribute> attributeList = getQueryAttributes(url);
		for (Attribute att : attributeList) {
			System.out.println(att.getName());
		}
		System.out.println(verifyAttributeExtraction(url, attributeList));
		
//		String s = "srid={p1}&geometry=POINT (2236208.82887498 93460.8811236587)";
//		String r = "POINT (2236208.82887498 93460.8811236587)";
//		s = s.replaceFirst(Pattern.quote(r), "test");
//		System.out.println(s);
		
//		String s = URLDecoder.decode(url.toString(), "UTF-8");
//		System.out.println(s);
//		String s2 = URLEncoder.encode(s, "UTF-8");
//		System.out.println(s2);
//		URL url2 = new URL(s2);
//		System.out.println(url2.toString());
		
		
		
//		String serviceName = "";
//		String apiEndPoint = "";
		
//		apiEndPoint = "http://gdata.youtube.com/feeds/api/videos?";
//		serviceName = "youtube-search";
		
//		apiEndPoint = "http://maps.googleapis.com/maps/api/geocode/json?";
//		serviceName = "google-geocode";

//		apiEndPoint = "http://api.twitter.com/1/statuses/update";
//		serviceName = "twitter-status-update";			
			
//		apiEndPoint = "http://api.flickr.com/services/rest/";
//		serviceName = "flickr";			

//		apiEndPoint = "http://api.yelp.com/v2/search";
//		serviceName = "yelp-search";
		
//		apiEndPoint = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
//		serviceName = "pubmed-esearch";
		
//		apiEndPoint = "http://en.wikipedia.org/w/api.php?";
//		serviceName = "wikipedia";

//		apiEndPoint = "http://dev.virtualearth.net/REST/v1/Locations?";
//		serviceName = "bing-map-location";
		
//		apiEndPoint = "http://services.digg.com/1.0/endpoint?";
//		serviceName = "digg-ver-1.0-100";
		
//		apiEndPoint = "http://api.nytimes.com/svc/search/v1/article?";
//		serviceName = "nytimes-serach";

//		apiEndPoint = "http://www.zillow.com/webservice/GetSearchResults.htm?";
//		serviceName = "zillow-find-property";

//		apiEndPoint = "http://api.nytimes.com/svc/search/v1/article?";
//		serviceName = "nytimes-serach";
		
//		apiEndPoint = "http://sandbox.api.shopping.com/publisher/3.0/rest/generalsearch?";
//		serviceName = "shopping-search";
				
//		apiEndPoint = "http://www.worldweatheronline.com/feed/weather.ashx";
//		serviceName = "world-weather-online";	
		
//		apiEndPoint = "http://answers.yahooapis.com/AnswersService/V1/getByUser";
//		serviceName = "yahoo-answers-getbyuser";	

//		apiEndPoint = "http://api.bitly.com/v3/shorten";
//		serviceName = "bitly-shortern";
		

		// Without Authentication 
		
//		apiEndPoint = "http://api.geonames.org/countryCode?";
//		serviceName = "geonames-countryCode";	
		
//		apiEndPoint = "http://api.geonames.org/countryInfo?";
//		serviceName = "geonames-countryInfo";
		
//		apiEndPoint = "http://api.geonames.org/findNearbyPostalCodes?";
//		serviceName = "geonames-findNearbyPostalCodes";
		
//		apiEndPoint = "http://api.wikilocation.org/articles";
//		serviceName = "wiki-location-articles";	
		
//		apiEndPoint = "http://api.geonames.org/findNearby?";
//		serviceName = "geoname-nearby";
		
//		apiEndPoint = "http://weather.yahooapis.com/forecastrss";
//		serviceName = "yahoo-weather";			
					
//		apiEndPoint = "http://search.yahooapis.com/WebSearchService/V1/webSearch";
//		serviceName = "yahoo-search-100";
		
//		apiEndPoint = "http://www.indeed.com/jobs";
//		serviceName = "indeed-job-search";
		

//		generateURLFile(apiEndPoint.trim(), serviceName.trim());
		
//		List<String> urlList = getUrlListFromFile(pathPrefix + serviceName + ".txt");
//
//		List<String> attributeNames = extractAttributeeterNames(urlList);
//		List<List<String>> attributeValuesInTable = attributeValuesInTable(urlList, attributeNames);
//		List<List<String>> attributeValuesInSets = attributeValuesInSets(urlList, attributeNames);
//		List<List<String>> crossProductTable = Permutation.permuteAll(attributeValuesInSets);
//
////		printAttributesCSV(urlList, attributeNames, attributeValuesInTable, pathPrefix + serviceName +  ".csv");
////		printAttributesTabular(attributeNames, attributeValuesInTable);
////		printAttributesSets(attributeNames, attributeValuesInSets);
////		printAttributesTabular(attributeNames, crossProductTable);
//		
//		printAttributesCSV(urlList, attributeNames, crossProductTable, pathPrefix + serviceName +  "-cross.csv");
//		
//		String url = "";
//		String output = "";
////		for (int i = 0; i < 10; i++) {
//		System.out.println(crossProductTable.size());
//		for (int i = 0; i < crossProductTable.size(); i++) {
//			url = createApiLink(apiEndPoint, attributeNames, crossProductTable.get(i));
//			System.out.println(url);
//			//url = "http://maps.googleapis.com/maps/api/geocode/json?address=1600 Amphitheatre Parkway, Mountain View, CA&sensor=true";
////			url = "http://weather.yahooapis.com/forecastrss?w=location&u=c&p=000";
////			output = invokeAPI(url);
////			System.out.println("**************\n**************\n**************\n");
////			System.out.println(output);
////			System.out.println("**************\n**************\n**************\n");
//		}
		
	}
}
