package edu.isi.karma.storm.strategy;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONObject;

public interface JoinStrategy extends Serializable{
	public JSONObject get(String uri);
	public void prepare(@SuppressWarnings("rawtypes") Map globalConfig);
}
