package edu.isi.karma.util;

import java.util.Comparator;

import org.json.JSONObject;

public class JSONLDReducerComparator implements Comparator<Object>
{

	public JSONLDReducerComparator()
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
			if (t.has("uri")) {
				return t.getString("uri").compareToIgnoreCase((String)o2);
			}
			else if (t.has("@id")) {
				return t.getString("@id").compareToIgnoreCase((String)o2);
			}

			else {
				return t.toString().compareToIgnoreCase((String)o2);
			}
		}
		else if(o1 instanceof String && o2 instanceof JSONObject)
		{
			JSONObject t2 = (JSONObject)o2;
			if (t2.has("uri")) {
				return (((String)o1).compareToIgnoreCase(t2.getString("uri")));
			}
			else if (t2.has("@id")) {
				return (((String)o1).compareToIgnoreCase(t2.getString("@id")));
			}
			else {
				return o1.toString().compareToIgnoreCase(t2.toString());
			}
		}
		else if(o1 instanceof JSONObject && o2 instanceof JSONObject)
		{
			JSONObject t1 = (JSONObject)o1;
			JSONObject t2 = (JSONObject)o2;
			if (t1.has("uri") && t2.has("uri")) {
				return t1.getString("uri").compareTo(t2.getString("uri"));
			}
			else if (t1.has("@id") && t2.has("@id")) {
				return t1.getString("@id").compareTo(t2.getString("@id"));
			}
			else {
				return t1.toString().compareToIgnoreCase(t2.toString());
			}
		}
		return 0;
	}

}