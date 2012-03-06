package edu.isi.karma.rep.cleaning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;


public class RamblerValueCollection implements ValueCollection {
	private HashMap<String,String> data;
	public RamblerValueCollection(HashMap<String,String> data)
	{
		this.data = data;
	}
	public void setValue(String id, String val)
	{
		if(data.containsKey(id))
		{
			data.put(id, val);
		}
	}
	@Override
	public String getValue(String id) {
		// TODO Auto-generated method stub
		if(data.containsKey(id))
		{
			return data.get(id);
		}
		else
			return "";
	}

	@Override
	public Collection<String> getValues() {
		// TODO Auto-generated method stub
		return data.values();
	}

	@Override
	public Collection<String> getNodeIDs() {
		// TODO Auto-generated method stub
		return data.keySet();
	}

	@Override
	public JSONArray getJson() {
		// TODO Auto-generated method stub
		return null;
	}

}
