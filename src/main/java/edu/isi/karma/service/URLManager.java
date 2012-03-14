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
package edu.isi.karma.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

public class URLManager {

	static Logger logger = Logger.getLogger(URLManager.class);

	public URLManager() {
		
	}

	public static List<URL> getURLsFromJSON(String requestURLsJSONArray) throws JSONException, MalformedURLException {

		List<URL> urls = new ArrayList<URL>();
		JSONArray jsonArray = new JSONArray(requestURLsJSONArray);
		
		String firstEndpoint = "";
		for (int i = 0; i < jsonArray.length(); i++) {
			URL url = new URL(jsonArray.getString(i).trim());
			if (i == 0) firstEndpoint = getEndPoint(url);
			
			// only urls with the same endpoints will be added to the list.
			if (firstEndpoint.equalsIgnoreCase(getEndPoint(url)))
				urls.add(url);
		}
		
		return urls;
	}
	
	public static String getEndPoint(URL url) {
		String endPoint = url.getHost() + url.getPath();
		return endPoint;
	}
	
	private static boolean verifyParamExtraction(URL url, List<Param> paramList) {
		String query = "";
		for (Param p:paramList) {
			query += p.getName().trim();
			query += "=";
			query += p.getValue().trim();
			query += "&";
		}
		if (query.endsWith("&"))
			query = query.substring(0, query.length()-1);
		
		if (query.equalsIgnoreCase(url.getQuery()))
			return true;
		
		return false;
	}
	
	public static List<Param> getQueryParams(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);
		return getQueryParams(url);
	}	    	

	public static List<Param> getQueryParams(URL url) {
	    try {
	    	
	    	String urlString = url.toString();
	    	
	        Map<String, List<String>> params = new HashMap<String, List<String>>();
	        
	        List<Param> paramList = new ArrayList<Param>();
	        
	        String[] urlParts = urlString.split("\\?");
	        if (urlParts.length > 1) {
	            String query = urlParts[1];
	            for (String param : query.split("&")) {
	                try {
		            	String[] pair = param.split("=");
		                String key = URLDecoder.decode(pair[0], "UTF-8");
		                String value = "";
		                if (pair.length > 1) {
		                	for (int i = 1; i < pair.length; i++) {
		                		if (i != 1) value += "=";
		                		value += URLDecoder.decode(pair[i], "UTF-8");
		                	}
		                }
	
		                List<String> values = params.get(key);
		                if (values == null) {
		                    values = new ArrayList<String>();
		                    params.put(key, values);
		                }
		                values.add(value);
		                
		    	    	// remember that we can have multiple values for a single parameter
		    	    	// example: /path/to/my/resource?param1=value1&param1=value2&param1=value3
		    	    	// we are currently ignoring these types of urls
		    	    	
		                if (values.size() > 0) {
			                Param p = new Param();
			                p.setName(key);
			                p.setValue(values.get(0));
			                paramList.add(p);
		                }
	                } catch (Exception e) {
	                	//e.printStackTrace();
	                }
	            }
	        }
	        
	        if (!verifyParamExtraction(url, paramList)) {
	        	logger.error("parameters have not been extracted successfully.");
	        	return null;
	        }

	        return paramList;
	        
	    } catch (Exception ex) {
	        throw new AssertionError(ex);
	    }
	}

	/**
	 * 
	 * @param urlList
	 * @return a list of parameter names
	 * @throws MalformedURLException 
	 */
	public static List<String>  extractParameterNames(List<String> urlList) throws MalformedURLException {
		List<String> paramsNameList = new ArrayList<String>();

		Param p = null;
		String key = null;
		for (int i = 0; i < urlList.size(); i++) {
			//System.out.println(urlList.get(i));
			List<Param> paramList = getQueryParams(urlList.get(i));
			for (int j = 0; j < paramList.size(); j++) {
				p = paramList.get(j);
				key = p.getName();
				//FIXME
				if (key.equalsIgnoreCase("parameters"))
					continue;
				if (key.trim().length() > 0 && paramsNameList.indexOf(key) == -1) {
					paramsNameList.add(key);
				}

			}
		}
		return paramsNameList;
	}
	
	
	public static List<List<String>>  paramValuesInTable(List<String> urlList, List<String> paramNames) throws MalformedURLException {
		List<List<String>> allParams = new ArrayList<List<String>>();

		Param p = null;
		String key = null;
		int index = -1;
		
		for (int i = 0; i < urlList.size(); i++) {
			//System.out.println(urlList.get(i));
			allParams.add(new ArrayList<String>());

			List<Param> paramList = getQueryParams(urlList.get(i));
			
			for (int j = 0; j < paramNames.size(); j++)
				allParams.get(i).add("");
			
			for (int j = 0; j < paramList.size(); j++) {
				p = paramList.get(j);
				key = p.getName();
				index = paramNames.indexOf(key);
				if (p.getValue() != null && p.getValue().trim().length() > 0)
					if (index != -1)
						allParams.get(i).set(index, p.getValue().trim());
			}
		}
		return allParams;
	}
	
	public static List<List<String>>  paramValuesInSets(List<String> urlList, List<String> paramNames) throws MalformedURLException {
		List<List<String>> allParams = new ArrayList<List<String>>();

		Param p = null;
		String key = null;
		int index = -1;
		
		for (int j = 0; j < paramNames.size(); j++) {
			allParams.add(new ArrayList<String>());
			
			// Add empty value to each list
			allParams.get(j).add("");
		}
		
		for (int i = 0; i < urlList.size(); i++) {
			//System.out.println(urlList.get(i));
			//allParams.add(new ArrayList<String>());

			List<Param> paramList = getQueryParams(urlList.get(i));
			
			for (int j = 0; j < paramList.size(); j++) {
				p = paramList.get(j);
				key = p.getName();
				index = paramNames.indexOf(key);
				if (p.getValue() != null && p.getValue().trim().length() > 0)
					if (index != -1)
						if (allParams.get(index).indexOf(p.getValue().trim()) == -1)
							allParams.get(index).add(p.getValue().trim());
			}
		}
		return allParams;
	}
	
	public static void printParamsTabular(List<String> paramNames, List<List<String>> paramValuesInTable) {
	
		try {
			
			for (int i = 0; i < paramValuesInTable.size(); i++) {
				//System.out.println(urlList.get(i));
				System.out.println("******************************************");
				for (int j = 0; j < paramValuesInTable.get(i).size(); j++) {
					System.out.write(paramNames.get(j).getBytes());
					System.out.write(":\t".getBytes());
					System.out.write(paramValuesInTable.get(i).get(j).getBytes());
					System.out.write("\n".getBytes());
				}
				System.out.write("\n".getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String printParamsCSV(List<String> urlList, List<String> paramNames, List<List<String>> paramValues) {
		String csv = "";
		
		try {
			
			
			for (int i = 0; i < paramNames.size(); i++) {
				if (i != 0)
					csv += ",";
				csv += "\"" + paramNames.get(i) + "\"";
			}
			csv += "\n";

			for (int i = 0; i < paramValues.size(); i++) {
				for (int j = 0; j < paramValues.get(i).size(); j++) {
					if (j != 0)
						csv += ",";
					csv += "\"" + paramValues.get(i).get(j) + "\"";
				}
				csv += "\n";
			}

			return csv;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static void printParamsSets(List<String> paramNames, List<List<String>> paramValuesInSets) {
		
		for (int i = 0; i < paramValuesInSets.size(); i++) {
			System.out.println(paramNames.get(i));
			
			for (int j = 0; j < paramValuesInSets.get(i).size(); j++) {
				System.out.println(paramValuesInSets.get(i).get(j).trim());
			}
			
			System.out.println("******************");
		}

	}
	
	public static String createApiLink(String apiEndPoint, List<String> paramNames, List<String> paramValues) {
		String invocationURL = apiEndPoint.trim();
			
		if (!invocationURL.endsWith("?"))
			invocationURL += "?";

		String value = "";
		for (int i = 0; i < paramNames.size(); i++) {
			value = paramValues.get(i).trim();
			
			if (value == null || value.length() == 0 || value.equalsIgnoreCase(null))
				continue;
			if (!invocationURL.endsWith("?"))
				invocationURL += "&";
			
			invocationURL += paramNames.get(i);
			invocationURL += "=";
			String refinedValue = paramValues.get(i).replaceAll("\\ ", "\\+");
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
		
	public static void main(String[] args) {

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
//		List<String> paramNames = extractParameterNames(urlList);
//		List<List<String>> paramValuesInTable = paramValuesInTable(urlList, paramNames);
//		List<List<String>> paramValuesInSets = paramValuesInSets(urlList, paramNames);
//		List<List<String>> crossProductTable = Permutation.permuteAll(paramValuesInSets);
//
////		printParamsCSV(urlList, paramNames, paramValuesInTable, pathPrefix + serviceName +  ".csv");
////		printParamsTabular(paramNames, paramValuesInTable);
////		printParamsSets(paramNames, paramValuesInSets);
////		printParamsTabular(paramNames, crossProductTable);
//		
//		printParamsCSV(urlList, paramNames, crossProductTable, pathPrefix + serviceName +  "-cross.csv");
//		
//		String url = "";
//		String output = "";
////		for (int i = 0; i < 10; i++) {
//		System.out.println(crossProductTable.size());
//		for (int i = 0; i < crossProductTable.size(); i++) {
//			url = createApiLink(apiEndPoint, paramNames, crossProductTable.get(i));
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
