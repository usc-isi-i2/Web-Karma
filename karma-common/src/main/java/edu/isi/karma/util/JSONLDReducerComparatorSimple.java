package edu.isi.karma.util;

import java.util.Comparator;

import org.json.simple.JSONObject;

public class JSONLDReducerComparatorSimple implements Comparator<Object>
{

	public JSONLDReducerComparatorSimple()
	{

	}
	@Override
	public int compare(Object o1, Object o2) {
		if(o1 instanceof String && o2 instanceof String)
		{
			return ((String)o1).compareToIgnoreCase((String)o2);
		}
		else if(o1 instanceof JSONObject && o2 instanceof String)
		{
			JSONObject t = (JSONObject)o1;
			if (t.containsKey("uri")) {
				return ((String)t.get("uri")).compareToIgnoreCase((String)o2);
			}
			else if (t.containsKey("@id")) {
				return ((String)t.get("@id")).compareToIgnoreCase((String)o2);
			}

			else {
				return t.toString().compareToIgnoreCase((String)o2);
			}
		}
		else if(o1 instanceof String && o2 instanceof JSONObject)
		{
			JSONObject t2 = (JSONObject)o2;
			if (t2.containsKey("uri")) {
				return (((String)o1).compareToIgnoreCase(((String)t2.get("uri"))));
			}
			else if (t2.containsKey("@id")) {
				return (((String)o1).compareToIgnoreCase(((String)t2.get("@id"))));
			}
			else {
				return o1.toString().compareToIgnoreCase(t2.toString());
			}
		}
		else if(o1 instanceof JSONObject && o2 instanceof JSONObject)
		{
			JSONObject t1 = (JSONObject)o1;
			JSONObject t2 = (JSONObject)o2;
			if (t1.containsKey("uri") && t2.containsKey("uri")) {
				return ((String)t1.get("uri")).compareTo(((String)t2.get("uri")));
			}
			else if (t1.containsKey("@id") && t2.containsKey("@id")) {
				return ((String)t1.get("@id")).compareTo(((String)t2.get("@id")));
			}
			else {
				return t1.toString().compareToIgnoreCase(t2.toString());
			}
		}
		return 0;
	}

}