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

import org.json.JSONObject;
import org.json.XML;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.Table;

public class JsonManager {

	private JsonManager() {
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

    public static boolean parse(String json) {
        try {
        	new JsonParser().parse(json);
        	return true;
        } catch (Exception e) {
        	return false;
        }
    }
    
    public static Element getJsonElements(String json) {

    	JsonElement  jse = null;
    	try {
    		jse = new JsonParser().parse(json);
    	} catch (Exception e) {
    		return null;
    	}
        
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
				colName = srcColumns.get(i).get(j);
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
				
				List<String> populatedValues = new ArrayList<>();
				rawValues = srcValues.get(i).get(j);
				
				for (int k = 0; k < columns.size(); k++) {
					int index = rawNames.indexOf(columns.get(k));
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
				colName = srcColumns.get(i).get(j);
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
				
				List<String> populatedValues = new ArrayList<>();
				rawValues = srcValues.get(i).get(j);
				
				for (int k = 0; k < columns.size(); k++) {
					int index = rawNames.indexOf(columns.get(k));
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
    		columns = new ArrayList<>();
    	
    	if (values == null)
    		values = new ArrayList<>();
    	
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
		
        List<String> columns = new ArrayList<>();
        List<List<String>> values = new ArrayList<>();
        
        getJsonFlat(json, columns, values);
        
		String csv = "";
		if (separator == null) separator = ',';
		if (quotechar == null) quotechar = '"';
		if (endlinechar == null) endlinechar = '\n';
		
		try {
			
			if (columns != null && !columns.isEmpty()) {
				for (int i = 0; i < columns.size(); i++) {
					if (i != 0)
						csv += separator.charValue();
					csv += quotechar + columns.get(i) + quotechar;
				}
				csv += endlinechar;
			} else
				System.out.println("Json does not have any header.");

			
			if (values != null && !values.isEmpty()) {
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
//            XMLSerializer xmlSerializer = new XMLSerializer();
//            JSON json = xmlSerializer.read( xmlContent );
//            return json.toString();
    		JSONObject jsonObj =  XML.toJSONObject(xmlContent);
	        return jsonObj.toString();
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

