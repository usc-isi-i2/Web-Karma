package edu.isi.karma.storm;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONObject;

public interface JoinStrategy extends Serializable{
	public JSONObject get(String uri);
	public void config(@SuppressWarnings("rawtypes") Map configMap);
}
