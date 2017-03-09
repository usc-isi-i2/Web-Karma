package edu.isi.karma.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONLDUtilSimple {
	private static JSONLDReducerComparatorSimple comparator;
	
	static {
		comparator =  new JSONLDReducerComparatorSimple();
	}

	private JSONLDUtilSimple() {
	}
	
	public static JSONObject mergeJSONObjects(String left, String right, Map<String, String> provenanceProperties) throws ParseException
	{

		JSONParser parser = new JSONParser();
		return mergeJSONObjects((JSONObject)parser.parse(left), (JSONObject)parser.parse(right), provenanceProperties);
	}
	
	public static JSONObject mergeJSONObjects(Iterator<String> iterator) throws ParseException {
		return mergeJSONObjects(iterator, new HashMap<String, String>());
	}

	public static JSONObject mergeJSONObjects(Iterator<String> iterator, Map<String, String> provenanceProperties) throws ParseException {

		JSONParser parser = new JSONParser(); 
		JSONObject accumulatorObject = new JSONObject();
		
		while(iterator.hasNext())
		{

			String value = iterator.next();
			JSONObject object = (JSONObject)parser.parse(value);
			accumulatorObject = mergeJSONObjects(accumulatorObject, object, provenanceProperties);
		}
		
		return accumulatorObject;
	}

	public static JSONObject mergeJSONObjects(JSONObject left, JSONObject right, Map<String, String> provenanceProperties)
	{
		mergeJSONObjectsInner(left, right, provenanceProperties);
		return left;
	}
	
	private static boolean mergeJSONObjectsInner(JSONObject left, JSONObject right, Map<String, String> provenanceProperties)
	{
		boolean rightAdded = false;
		ArrayList<String> names = new ArrayList<>(right.keySet());
		
		//Move provenance properties to the end of the list
		for(String provProp: provenanceProperties.keySet()) {
			if(names.contains(provProp)) {
				names.remove(provProp);
				names.add(provProp);
			}
		}
		for(Object rawName : names)
		{
			String name = (String) rawName;
			if(!left.containsKey(name))
			{
				left.put(name, right.get(name));
				rightAdded = true;
			}
			else
			{
				Object leftObject = left.get(name);
				Object rightObject = right.get(name);
				if(leftObject instanceof JSONArray)
				{
					if(rightObject instanceof JSONArray)
					{
						rightAdded = rightAdded | mergeArrays(left, name, rightAdded, (JSONArray) leftObject, (JSONArray) rightObject, provenanceProperties);
					}
					else
					{
						JSONArray newRightArray = new JSONArray();
						newRightArray.add(rightObject);
						rightAdded = rightAdded | mergeArrays(left, name, rightAdded, (JSONArray) leftObject, newRightArray, provenanceProperties);
					}

				}
				else
				{
					if(rightObject instanceof JSONArray)
					{
						JSONArray newLeftArray = new JSONArray();
						newLeftArray.add(leftObject);
						rightAdded = rightAdded | mergeArrays(left, name, rightAdded, newLeftArray, (JSONArray)rightObject, provenanceProperties);
					}
					else
					{
						JSONArray newLeftArray = new JSONArray();
						JSONArray newRightArray = new JSONArray();
						newLeftArray.add(leftObject);
						newRightArray.add(rightObject);
						rightAdded = rightAdded | mergeArrays(left, name, rightAdded, newLeftArray, newRightArray, provenanceProperties);
					}
				}
			}
		}
		return rightAdded;
	}
	
	protected static boolean mergeArrays(JSONObject left, String name, boolean newAdded,
			JSONArray leftArray, JSONArray rightArray, Map<String, String> provenanceProperties) {
		LinkedList<Object> newArrayBuilder = new LinkedList<Object>();
		int leftIndex = 0;
		int rightIndex = 0;
		boolean rightAdded = false;
		boolean provenanceDateProperty = false;
		
		//If it is a provenance property and no new information is added, we do not need to add the property
		if(provenanceProperties.containsKey(name) && !newAdded) {
			String provType = provenanceProperties.get(name);
			if(provType == "date") {
				//For dates, we would like to min, max date
				provenanceDateProperty = true;
			} else {
				return false;
			}
		}
		
		while(leftIndex < leftArray.size() && rightIndex < rightArray.size() )
		{
			int result = comparator.compare(leftArray.get(leftIndex),rightArray.get(rightIndex));
			if(result < 0)
			{
				newArrayBuilder.add(leftArray.get(leftIndex++));
			}
			else if (result == 0)
			{
				Object tempLeft = leftArray.get(leftIndex++);
				Object tempRight = rightArray.get(rightIndex++);
				ObjectRightAdded mergedResultAdded = mergeStringsAndJSONObjects(
						tempLeft, tempRight, provenanceProperties);
				Object mergedResult = mergedResultAdded.object;
				rightAdded = rightAdded | mergedResultAdded.rightAdded;
				newArrayBuilder.add(mergedResult);
			}
			else
			{
				newArrayBuilder.add(rightArray.get(rightIndex++));
				rightAdded = true;
			}
		}
		while(leftIndex < leftArray.size())
		{
			newArrayBuilder.add(leftArray.get(leftIndex++));
		}
		while(rightIndex < rightArray.size())
		{
			newArrayBuilder.add(rightArray.get(rightIndex++));
			rightAdded = true;
		}
		if(newArrayBuilder.size() > 1 || name.equals("a"))
		{
			JSONArray newArray = new JSONArray();
			
			if(!provenanceDateProperty) {
				newArray.addAll(newArrayBuilder);
			} else {
				String min = null, max = null;
				for(Object obj : newArrayBuilder) {
					String s = obj.toString();
					if(min == null || s.compareTo(min) < 0) 
						min = s;
					if(max == null || s.compareTo(max) > 0)
						max = s;
				}
				newArray.add(min);
				if(min != max)
					newArray.add(max);
				
			}
			left.put(name, newArray);
		}
		else if(newArrayBuilder.size() == 1)
		{
			left.put(name, newArrayBuilder.get(0));
		}
		return rightAdded;
	}
	
	private static ObjectRightAdded mergeStringsAndJSONObjects(Object tempLeft,
			Object tempRight, Map<String, String> provenanceProperties) {
		Object mergedResult = null;
		boolean rightAdded = false;
		if(tempLeft instanceof String && tempRight instanceof String)
		{
			mergedResult = tempLeft;
		}
		else if(tempLeft instanceof JSONObject && tempRight instanceof String)
		{
			mergedResult = tempLeft;
		}
		else if(tempLeft instanceof String && tempRight instanceof JSONObject)
		{
			mergedResult = tempRight;
			rightAdded = true;
		}
		else if(tempLeft instanceof JSONObject && tempRight instanceof JSONObject)
		{
			rightAdded = rightAdded |  mergeJSONObjectsInner((JSONObject)tempLeft, (JSONObject)tempRight, provenanceProperties);
		}
		else {
			if (tempLeft instanceof String) {
				mergedResult = tempRight.toString();
				rightAdded = true;
			}
			else {
				mergedResult = tempLeft.toString();
			}
		}
		return new ObjectRightAdded(mergedResult, rightAdded);
	}
	
	public static void main(String[] args) throws ParseException {
		HashMap<String, String> provProperties = new HashMap<String, String>();
		provProperties.put("source", "string");
		provProperties.put("publisher", "string");
		provProperties.put("dateRecorded", "date");
		
		JSONParser parser = new JSONParser();
		String json_line = "{\"a\": [\"Vulnerability\"], \"dateRecorded\": [\"2017-02-10T12:13:51\",\"2017-02-09T18:13:51\"], \"description\": \"java/org/apache/catalina/authenticator/FormAuthenticator.java in the form authentication feature in Apache Tomcat 6.0.21 through 6.0.36 and 7.x before 7.0.33 does not properly handle the relationships between authentication requirements and sessions, which allows remote attackers to inject a request into a session by sending this request during completion of the login form, a variant of a session fixation attack.\", \"publisher\": \"hg-cve\", \"uri\": \"http://effect.isi.edu/data/vulnerability/CVE-2013-2067\", \"source\": \"hg-cve-2A7DF54A\", \"hasCVSS\": \"http://effect.isi.edu/data/vulnerability/CVE-2013-2067/scoring\", \"vulnerabilityOf\": [\"cpe:/a:apache:tomcat:7.0.18\", \"cpe:/a:apache:tomcat:6.0.33\", \"cpe:/a:apache:tomcat:7.0.0:beta\", \"cpe:/a:apache:tomcat:7.0.19\", \"cpe:/a:apache:tomcat:6.0.31\", \"cpe:/a:apache:tomcat:7.0.30\", \"cpe:/a:apache:tomcat:6.0.32\", \"cpe:/a:apache:tomcat:7.0.14\", \"cpe:/a:apache:tomcat:7.0.32\", \"cpe:/a:apache:tomcat:7.0.15\", \"cpe:/a:apache:tomcat:6.0.30\", \"cpe:/a:apache:tomcat:7.0.16\", \"cpe:/a:apache:tomcat:7.0.17\", \"cpe:/a:apache:tomcat:7.0.10\", \"cpe:/a:apache:tomcat:7.0.0\", \"cpe:/a:apache:tomcat:7.0.11\", \"cpe:/a:apache:tomcat:7.0.12\", \"cpe:/a:apache:tomcat:7.0.13\", \"cpe:/a:apache:tomcat:7.0.4\", \"cpe:/a:apache:tomcat:7.0.3\", \"cpe:/a:apache:tomcat:6.0.35\", \"cpe:/a:apache:tomcat:7.0.2\", \"cpe:/a:apache:tomcat:6.0.36\", \"cpe:/a:apache:tomcat:7.0.1\", \"cpe:/a:apache:tomcat:7.0.7\", \"cpe:/a:apache:tomcat:7.0.8\", \"cpe:/a:apache:tomcat:7.0.5\", \"cpe:/a:apache:tomcat:7.0.6\", \"cpe:/a:apache:tomcat:7.0.9\", \"cpe:/a:apache:tomcat:7.0.4:beta\", \"cpe:/a:apache:tomcat:6.0.21\", \"cpe:/a:apache:tomcat:7.0.28\", \"cpe:/a:apache:tomcat:7.0.25\", \"cpe:/a:apache:tomcat:6.0.28\", \"cpe:/a:apache:tomcat:7.0.23\", \"cpe:/a:apache:tomcat:6.0.29\", \"cpe:/a:apache:tomcat:7.0.21\", \"cpe:/a:apache:tomcat:7.0.22\", \"cpe:/a:apache:tomcat:6.0.24\", \"cpe:/a:apache:tomcat:7.0.20\", \"cpe:/a:apache:tomcat:6.0.26\", \"cpe:/a:apache:tomcat:6.0.27\", \"cpe:/a:apache:tomcat:7.0.2:beta\"], \"name\": [\"CVE-2013-2067\"]}";
		JSONObject json1 = (JSONObject)parser.parse(json_line);
		JSONObject json2 = (JSONObject)parser.parse(json_line);
		
		//Change the dateRecorded for json2
		json2.put("dateRecorded", "2017-02-09T16:13:51");
		json2.put("source", "hg-cve-FFFFFFFF");
		JSONObject result = mergeJSONObjects(json1, json2, provProperties);
		
//		["2017-02-09T16:13:51","2017-02-10T12:13:51"]
//		hg-cve-2A7DF54A
		System.out.println(result.get("dateRecorded"));
		System.out.println(result.get("source"));
		
		//Add a new field to json2. The source should get added
		json2.put("tempField", "test");
		result = mergeJSONObjects(json1, json2, provProperties);
//		["2017-02-09T16:13:51","2017-02-10T12:13:51"]
//		["hg-cve-2A7DF54A","hg-cve-FFFFFFFF"]
		System.out.println(result.get("dateRecorded"));
		System.out.println(result.get("source"));
	}
	
	private static class ObjectRightAdded {
		public Object object;
		public boolean rightAdded;
		public ObjectRightAdded(Object object, boolean rightAdded) {
			this.object = object;
			this.rightAdded = rightAdded;
		}
	}
}
