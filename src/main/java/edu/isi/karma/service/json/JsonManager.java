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
package edu.isi.karma.service.json;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.Table;

public class JsonManager {

    public static void main(String... args) throws Exception {
        
    	String json1 = 
            "{"
                + "'title': 'Computing and Information systems',"
                + "'id' : [{'p1' : 'v1'}, {'p2' : 'v2'}],"
                + "'children' : 'true',"
                + "'groups' : [{"
                    + "'title' : 'Level one CIS',"
                    + "'id' : 2,"
                    + "'children' : 'true',"
                    + "'groups' : [{"
                        + "'title' : 'Intro To Computing and Internet',"
                        + "'id' : [{'p3' : 'v3'}, {'p4' : 'v4'}],"
//                        + "'id' : 3,"
                        + "'children': 'false',"
                        + "'groups':[]"
                    + "}]" 
                + "}]"
            + "}";
        
    	String json2 = 
        	"{" 
        	+ "'weatherObservation': {"
        	+ "'clouds': 'scattered clouds',"
        	+ "'weatherCondition': 'n/a',"
        	+ "'observation': 'LESO 311830Z 24005KT 210V280 4000 -TSRA SCT012 SCT022CB BKN060 22/20 Q1009',"
        	+ "'windDirection': 240,"
        	+ "'ICAO': 'LESO',"
        	+ "'elevation': 8,"
        	+ "'countryCode': 'ES',"
        	+ "'lng': -1.8,"
        	+ "'temperature': '22',"
        	+ "'dewPoint': '20',"
        	+ "'windSpeed': '05',"
        	+ "'humidity': 88,"
        	+ "'stationName': 'San Sebastian / Fuenterrabia',"
        	+ "'datetime': '2011-08-31 18:30:00',"
        	+ "'lat': 43.35,"
        	+ "'hectoPascAltimeter': 1009"
        	+ "},"
        	+ "'node2': {"
        	+ "'node2-1-key': 'node2-1-value',"
        	+ "'node2-2-key': 'node2-2-value'"
        	+ "}"
        	+ "}";

    	String json3 = "{'postalCodes':[{'adminCode3':'UK','adminName2':'Surrey County','adminName3':'Tandridge District','adminCode2':'43','distance':'0.48844','adminCode1':'ENG','postalCode':'CR6 9QN','countryCode':'GB','lng':0.0070237530613011506,'placeName':'Chelsham and Farleigh','lat':51.30010642380937,'adminName1':'England'},{'adminCode3':'UK','adminName2':'Surrey County','adminName3':'Tandridge District','adminCode2':'43','distance':'1.06201','adminCode1':'ENG','postalCode':'CR6 9QP','countryCode':'GB','lng':-0.0026705499321176274,'placeName':'Titsey','lat':51.29059577159804,'adminName1':'England'},{'adminCode3':'UK','adminName2':'Surrey County','adminName3':'Tandridge District','adminCode2':'43','distance':'1.1316','adminCode1':'ENG','postalCode':'CR6 9QG','countryCode':'GB','lng':-0.011357770951524116,'placeName':'Chelsham and Farleigh','lat':51.307290694391604,'adminName1':'England'},{'adminCode3':'UK','adminName2':'Surrey County','adminName3':'Tandridge District','adminCode2':'43','distance':'1.13857','adminCode1':'ENG','postalCode':'CR6 9QJ','countryCode':'GB','lng':-0.015818287144742343,'placeName':'Chelsham and Farleigh','lat':51.29734822895096,'adminName1':'England'},{'adminCode3':'AF','adminName3':'Bromley London Boro','adminCode2':'00','distance':'1.26682','adminCode1':'ENG','postalCode':'TN16 3PD','countryCode':'GB','lng':0.01783733164101557,'placeName':'Biggin Hill Ward','lat':51.30233067295212,'adminName1':'England'}]}";
        
    	String json4 = "{'data':{" 
        				+ "'current_condition':["
        				+ "{"
        				+ "'cloudcover':'0',"
        				+ "'humidity':'62',"
        				+ "'observation_time':'11:17 PM',"
        				+ "'precipMM':'0.0',"
        				+ "'pressure':'1013',"
        				+ "'temp_C':'23',"
        				+ "'temp_F':'74',"
        				+ "'visibility':'16',"
        				+ "'weatherCode':'113',"
        				+ "'weatherDesc':["
        				+ "{"
        				+ "'value':'Sunny'"
            			+ "}"
            			+ "],"
            			+ "'weatherIconUrl':["
            			+ "{"
            			+ "'value':'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0001_sunny.png'"
            			+ "}"
            			+ "],"
            			+ "'winddir16Point':'W',"
            			+ "'winddirDegree':'280',"
            			+ "'windspeedKmph':'6',"
            			+ "'windspeedMiles':'4'"
    					+ "}],"
        				
    					+ "'weather':["
        				+ "{"
        				+ "'cloudcover':'0',"
        				+ "'humidity':'62',"
        				+ "'observation_time':'11:17 PM',"
        				+ "'precipMM':'0.0',"
        				+ "'pressure':'1013',"
        				+ "'temp_C':'23',"
        				+ "'temp_F':'74',"
        				+ "'visibility':'16',"
        				+ "'weatherCode':'113',"
        				+ "'weatherDesc':["
        				+ "{"
        				+ "'value':'Sunny'"
            			+ "}"
            			+ "],"
            			+ "'weatherIconUrl':["
            			+ "{"
            			+ "'value':'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0001_sunny.png'"
            			+ "}"
            			+ "],"
            			+ "'winddir16Point':'W',"
            			+ "'winddirDegree':'280',"
            			+ "'windspeedKmph':'6',"
            			+ "'windspeedMiles':'4'"
    					+ "},"
        				+ "{"
        				+ "'cloudcover':'0',"
        				+ "'humidity':'62',"
        				+ "'observation_time':'11:17 PM',"
        				+ "'precipMM':'0.0',"
        				+ "'pressure':'1013',"
        				+ "'temp_C':'23',"
        				+ "'temp_F':'74',"
        				+ "'visibility':'16',"
        				+ "'weatherCode':'113',"
        				+ "'weatherDesc':["
        				+ "{"
        				+ "'value':'Sunny'"
            			+ "}"
            			+ "],"
            			+ "'weatherIconUrl':["
            			+ "{"
            			+ "'value':'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0001_sunny.png'"
            			+ "}"
            			+ "],"
            			+ "'winddir16Point':'W',"
            			+ "'winddirDegree':'280',"
            			+ "'windspeedKmph':'6',"
            			+ "'windspeedMiles':'4'"
    					+ "}]"
    					+ "}"
    					+ "}";

    	String json5 = "{'data': { 'current_condition': [ {'cloudcover': '75', 'humidity': '83', 'observation_time': '02:56 AM', 'precipMM': '0.0', 'pressure': '1013', 'temp_C': '18', 'temp_F': '64', 'visibility': '16', 'weatherCode': '116',  'weatherDesc': [ {'value': 'Partly Cloudy' } ],  'weatherIconUrl': [ {'value': 'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0004_black_low_cloud.png' } ], 'winddir16Point': 'N', 'winddirDegree': '0', 'windspeedKmph': '0', 'windspeedMiles': '0' } ],  'request': [ {'query': 'Los Angeles, United States Of America', 'type': 'City' } ],  'weather': [ {'date': '2011-09-19', 'precipMM': '0.0', 'tempMaxC': '27', 'tempMaxF': '80', 'tempMinC': '18', 'tempMinF': '64', 'weatherCode': '113',  'weatherDesc': [ {'value': 'Sunny' } ],  'weatherIconUrl': [ {'value': 'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0001_sunny.png' } ], 'winddir16Point': 'WSW', 'winddirDegree': '239', 'winddirection': 'WSW', 'windspeedKmph': '14', 'windspeedMiles': '9' }, {'date': '2011-09-20', 'precipMM': '0.0', 'tempMaxC': '29', 'tempMaxF': '84', 'tempMinC': '17', 'tempMinF': '62', 'weatherCode': '113',  'weatherDesc': [ {'value': 'Sunny' } ],  'weatherIconUrl': [ {'value': 'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0001_sunny.png' } ], 'winddir16Point': 'WSW', 'winddirDegree': '237', 'winddirection': 'WSW', 'windspeedKmph': '14', 'windspeedMiles': '9' }, {'date': '2011-09-21', 'precipMM': '0.0', 'tempMaxC': '30', 'tempMaxF': '85', 'tempMinC': '18', 'tempMinF': '64', 'weatherCode': '113',  'weatherDesc': [ {'value': 'Sunny' } ],  'weatherIconUrl': [ {'value': 'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0001_sunny.png' } ], 'winddir16Point': 'SW', 'winddirDegree': '231', 'winddirection': 'SW', 'windspeedKmph': '14', 'windspeedMiles': '9' }, {'date': '2011-09-22', 'precipMM': '0.0', 'tempMaxC': '31', 'tempMaxF': '88', 'tempMinC': '19', 'tempMinF': '66', 'weatherCode': '113',  'weatherDesc': [ {'value': 'Sunny' } ],  'weatherIconUrl': [ {'value': 'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0001_sunny.png' } ], 'winddir16Point': 'SW', 'winddirDegree': '218', 'winddirection': 'SW', 'windspeedKmph': '12', 'windspeedMiles': '7' }, {'date': '2011-09-23', 'precipMM': '0.0', 'tempMaxC': '34', 'tempMaxF': '92', 'tempMinC': '22', 'tempMinF': '71', 'weatherCode': '113',  'weatherDesc': [ {'value': 'Sunny' } ],  'weatherIconUrl': [ {'value': 'http:\\/\\/www.worldweatheronline.com\\/images\\/wsymbols01_png_64\\/wsymbol_0001_sunny.png' } ], 'winddir16Point': 'SSW', 'winddirDegree': '206', 'winddirection': 'SSW', 'windspeedKmph': '17', 'windspeedMiles': '11' } ] }}";
    		
    	// Guava library
//    	String text = Files.toString(new File("/Users/mohsen/Desktop/temp/karma issues/Aditya/jsonData.txt"), Charsets.UTF_8);
    	
    	String json6 = readFile(new File("/Users/mohsen/Desktop/temp/karma issues/Aditya/jsonData.txt"));

    	List<String> columns = new ArrayList<String>();
        List<List<String>> values = new ArrayList<List<String>>();

        System.out.println(json1.replace("'", "\""));
        System.out.println("===================================================================================");
        getJsonFlat(json1, columns, values);
        System.out.println("===================================================================================");
        
        System.out.println(json2.replace("'", "\""));
        System.out.println("===================================================================================");
        getJsonFlat(json2, columns, values);
        System.out.println("===================================================================================");
        
        System.out.println(json3.replace("'", "\""));
        System.out.println("===================================================================================");
        getJsonFlat(json3, columns, values);
        System.out.println("===================================================================================");
        
        System.out.println(json4.replace("'", "\""));
        System.out.println("===================================================================================");
        getJsonFlat(json4, columns, values);
        System.out.println("===================================================================================");
        
        System.out.println(json5.replace("'", "\""));
        System.out.println("===================================================================================");
        getJsonFlat(json5, columns, values);
        System.out.println("===================================================================================");

        System.out.println(json6.replace("'", "\""));
        System.out.println("===================================================================================");
        getJsonFlat(json6, columns, values);
        System.out.println("===================================================================================");

        System.out.println();
        System.out.println("*************************************");
        System.out.println("Finished.");

    }
    
    public static String readFile(File file) throws IOException {
    	InputStream in = new FileInputStream(file);
    	byte[] b  = new byte[(int)file.length()];
    	int len = b.length;
    	int total = 0;

    	while (total < len) {
    	  int result = in.read(b, total, len - total);
    	  if (result == -1) {
    	    break;
    	  }
    	  total += result;
    	}
    	in.close();
    	return new String(b);
    }
    
    public static Element getJsonElements(String json) {
        
    	JsonElement  jse = new JsonParser().parse(json);
        
        Element rootElement = new Element();
        rootElement.setKey("");
        rootElement.setValue(new ArrayValue());
        rootElement.setValueType(ValueType.ARRAY);
        recursiveParse(jse, rootElement);
        //rootElement.print(rootElement, 0);
        
        return rootElement;
    }
    
//    private static void getLeavesPath(Element element, String prefix, String separator, int depth) {
//        
//    	if (element.getValueType() == ValueType.SINGLE) {
//    		element.setFullPath(element.getFullPath() + separator + element.getLocalName(depth));
//    	} else {
//    		int count = 0;
//    		for (int i = 0; i < ((ArrayValue)element.getValue()).getElements().size(); i++) {
//    			Element e = ((ArrayValue)element.getValue()).getElements().get(i);
//    			if (e.getValueType() == ValueType.SINGLE) {
//    				prefix = prefix + separator + e.getLocalName(depth);
//    				count ++;
//    			}
//    		}
//    		if (count == ((ArrayValue)element.getValue()).getElements().size() ||
//    				((ArrayValue)element.getValue()).getElements().size() == 0)
//    			element.setFullPath(prefix.substring(1));
//    		for (int i = 0; i < ((ArrayValue)element.getValue()).getElements().size(); i++) {
//    			Element e = ((ArrayValue)element.getValue()).getElements().get(i);
//    			if (e.getValueType() == ValueType.ARRAY)
//    				getLeavesPath(e, prefix, separator, depth + 1);
//    		}
//
//    	}
//    	
//    }
    
//    private static String getColumnName(String part) {
//    	part = part.substring(0, part.indexOf("v="));
//    	part = part.substring(0, part.length() - 1);
//    	return part;
//    }
    
//    private static List<String> getValues(String path) {
//    	List<String> result = new ArrayList<String>();
//    	
//		String[] parts = path.split("\\|");
//
//		String temp = "";
//		for (int i = 0; i < parts.length; i++) {
//			if (parts[i].trim().length() == 0)
//				continue;
//	    	temp = parts[i].substring(parts[i].indexOf("v=") + 2);
//			result.add(temp.trim());
//		}
//		return result;
//    }
    
//    private static List<String> getColumns(String path) {
//    	List<String> result = new ArrayList<String>();
//    	
//		String[] parts = path.split("\\|");
//
//		String temp = "";
//		for (int i = 0; i < parts.length; i++) {
//			if (parts[i].trim().length() == 0)
//				continue;
//			temp = parts[i].substring(0, parts[i].indexOf("v="));
//			temp = temp.substring(0, temp.length() - 1);
//			result.add(temp.trim());
//		}
//		return result;
//    }
//    
    public static void union(List<List<String>> srcColumns, List<List<List<String>>> srcValues, 
    		List<String> columns, List<List<String>> values) {
    	
		String colName = "";
		for (int i = 0; i < srcColumns.size(); i++) {
			for (int j = 0; j < srcColumns.get(i).size(); j++)
			{
				colName = srcColumns.get(i).get(j).toString();
				if (columns.indexOf(colName) == -1)
					columns.add(colName);
			}
		}
		
		List<String> rawNames = null;
		List<String> rawValues = null;
		String singleValue = null;
		for (int i = 0; i < srcColumns.size(); i++) {
			rawNames = srcColumns.get(i);
			for (int j = 0; j < srcValues.get(i).size(); j++) {
				
				List<String> populatedValues = new ArrayList<String>();
				rawValues = srcValues.get(i).get(j);
				
				for (int k = 0; k < columns.size(); k++) {
					int index = rawNames.indexOf(columns.get(k).toString());
					if (index == -1)
						singleValue = null;
					else
						singleValue = rawValues.get(index);
					populatedValues.add(singleValue);
				}
				
				values.add(populatedValues);
			}
			
		}
    }
    
    public static void union(List<List<String>> srcColumns, List<List<String>> srcTypes, List<List<List<String>>> srcValues, 
    		List<String> columns, List<String> types, List<List<String>> values) {
    	
		String colName = "";
		for (int i = 0; i < srcColumns.size(); i++) {
			for (int j = 0; j < srcColumns.get(i).size(); j++)
			{
				colName = srcColumns.get(i).get(j).toString();
				if (columns.indexOf(colName) == -1) {
					columns.add(colName);
					types.add(srcTypes.get(i).get(j));
				}
			}
		}
		
		List<String> rawNames = null;
		List<String> rawValues = null;
		String singleValue = null;
		for (int i = 0; i < srcColumns.size(); i++) {
			rawNames = srcColumns.get(i);
			for (int j = 0; j < srcValues.get(i).size(); j++) {
				
				List<String> populatedValues = new ArrayList<String>();
				rawValues = srcValues.get(i).get(j);
				
				for (int k = 0; k < columns.size(); k++) {
					int index = rawNames.indexOf(columns.get(k).toString());
					if (index == -1)
//						singleValue = null;
						singleValue = "";
					else
						singleValue = rawValues.get(index);
					populatedValues.add(singleValue);
				}
				
				values.add(populatedValues);
			}
			
		}
    }
    
    public static void getJsonFlat(String json, List<String> columns, List<List<String>> values) {
    	
    	Element element = JsonManager.getJsonElements(json);
    	element.updateHeaders();
    	Table t = element.getFlatTable();

    	if (columns == null)
    		columns = new ArrayList<String>();
    	
    	if (values == null)
    		values = new ArrayList<List<String>>();
    	
    	if (t.getColumnsCount() > 0) {
	    	for (Attribute att : t.getHeaders())
	    		columns.add(att.getName());
	    	
	    	for (List<String> v : t.getValues()) 
	    		if (v != null)
	    			values.add(v);
    	}
    	
//    	t.print();
    	
//    	element.moveUpOneValueElements();
//    	
//    	
//    	columns.clear();
//    	values.clear();
//    	
//    	getLeavesPath(element, "", "|", 0);
//    	
//    	List<String> fullPaths = new ArrayList<String>();
//    	
//    	element.computeFullPaths(fullPaths);
//    	    	
//    	List<List<String>> srcColumns = new ArrayList<List<String>>();
//    	List<List<List<String>>> srcValues = new ArrayList<List<List<String>>>();
//    	
//		for (int i = 0; i < fullPaths.size(); i++) {
//			srcColumns.add(getColumns(fullPaths.get(i)));
//			
//			List<List<String>> onePathValues = new ArrayList<List<String>>();
//			onePathValues.add(getValues(fullPaths.get(i)));
//			srcValues.add(onePathValues);
//		}
//		
//		union(srcColumns, srcValues, columns, values);
//		
//		int[] sameColumnName = new int[columns.size()];
//		for (int i = 0; i < columns.size(); i++) 
//			sameColumnName[i] = 1;
//		
//		String name = "";
//		for (int i = 0; i < columns.size(); i++) {
//			name = columns.get(i);
//			name = name.substring(name.indexOf("k=")+2);
//			int index = columns.indexOf(name);
//			if (index != -1) {
//				sameColumnName[index] ++;
//				name += sameColumnName[index];
//			}
//			columns.set(i, name);
//		}
    	
    }
    
	public static String jsonToCSV(String json) {
		return jsonToCSV(json, null, null, null);
	}

	public static String jsonToCSV(String json, Character separator, Character quotechar, Character endlinechar) {
		
        List<String> columns = new ArrayList<String>();
        List<List<String>> values = new ArrayList<List<String>>();
        
        getJsonFlat(json, columns, values);
        
		String csv = "";
		if (separator == null) separator = ',';
		if (quotechar == null) quotechar = '"';
		if (endlinechar == null) endlinechar = '\n';
		
		try {
			
			if (columns != null && columns.size() > 0) {
				for (int i = 0; i < columns.size(); i++) {
					if (i != 0)
						csv += separator.charValue();
					csv += quotechar + columns.get(i) + quotechar;
				}
				csv += endlinechar;
			} else
				System.out.println("Json does not have any header.");

			
			if (values != null && values.size() > 0) {
				for (int i = 0; i < values.size(); i++) {
					for (int j = 0; j < values.get(i).size(); j++) {
						if (j != 0)
							csv += separator;
						csv += quotechar + values.get(i).get(j) + quotechar;
					}
					csv += endlinechar;
				}
			} else
				System.out.println("Json does not have any value.");

			return csv;
			
		} catch (Exception e) {
			System.out.println("Error in generating CSV from the Json.");
			e.printStackTrace();
			return null;
		}

	}
    
    public static String convertXML2JSON(String xmlContent) {
    	try {
            XMLSerializer xmlSerializer = new XMLSerializer(); 
            JSON json = xmlSerializer.read( xmlContent ); 
            return json.toString();
//    		JSONObject jsonObj =  XML.toJSONObject(xmlContent);
//	        return jsonObj.toString();
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    		return null;
    	}
        
    }

    private static void recursiveParse(JsonElement jse, Element element) {
        if (jse.isJsonObject()) {
        	JsonObject j = jse.getAsJsonObject();
        	Set<Entry<String, JsonElement>> set = j.entrySet();
//        	System.out.println(set.size());
            Iterator<Entry<String, JsonElement>> iter = set.iterator();
            while (iter.hasNext()) {
            	Entry<String, JsonElement> entry = iter.next();
            	
            	Element e = new Element();
            	e.setKey(entry.getKey());
//            	System.out.println("create " + e.getKey());
            	e.setValue(new ArrayValue());
            	e.setValueType(ValueType.ARRAY);
            	
            	recursiveParse(entry.getValue(), e);
            	
//            	System.out.println("element " + element.getKey());
            	e.setParent(element);
            	((ArrayValue)element.getValue()).getElements().add(e);
//            	System.out.println("e " + e.getKey());

            }
        } else if (jse.isJsonArray()) {
        	JsonArray j = jse.getAsJsonArray();
        	Iterator<JsonElement> iter = j.iterator();
        	while (iter.hasNext()) {
            	
        		Element e = new Element();
            	e.setKey("");
            	e.setValue(new ArrayValue());
            	e.setValueType(ValueType.ARRAY);
        		recursiveParse(iter.next(), e);
        		e.setParent(element);
        		((ArrayValue)element.getValue()).getElements().add(e);
        	}
        } else if (jse.isJsonPrimitive()) {
        	element.setValueType(ValueType.SINGLE);
        	element.setValue(new SingleValue(jse.toString()));
//        	System.out.println(jse.getAsString());
        }
        
    }
    

}

