package edu.isi.karma.storm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import edu.isi.karma.mapreduce.driver.BaseKarma;

public class KarmaJoinBolt extends BaseRichBolt {

	protected BaseKarma karma;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	private Properties config;
	private String atId = "uri";
	private String mergePath;
	private String field;
	private JoinStrategy strategy;
	public KarmaJoinBolt(Properties config, JoinStrategy strategy)
	{
		this.config = config;
		this.strategy = strategy;
	}

	@Override
	public void execute(Tuple tuple) {

		System.out.println("My name is: " + config.getProperty("name"));
		long start = System.currentTimeMillis();
		JSONObject objectToJoin = new JSONObject(tuple.getStringByField(field));
		String[] mergePath = this.mergePath.split(",");
		joinJSONObject(objectToJoin, mergePath, 0);
		outputCollector.emit(new Values(objectToJoin.getString(atId), objectToJoin.toString()));
		System.out.println("id: "+ tuple.getStringByField("id") + " " + (System.currentTimeMillis() - start));
	}

	private void joinJSONObject(JSONObject obj, String[] mergePath, int level) {
		if (obj.has(mergePath[level])) {
			Object val = obj.get(mergePath[level]);
			if (level == mergePath.length - 1) {
				if (val instanceof JSONObject) {
					JSONObject target = (JSONObject)val;
					JSONObject source = strategy.get(target.get(atId).toString());
					if (source != null) {
						@SuppressWarnings("rawtypes")
						Iterator itr = source.keys();
						while (itr.hasNext()) {
							String key = itr.next().toString();
							Object value = source.get(key);
							target.put(key, value);
						}
					}
				}
				if (val instanceof String) {
					JSONObject source = strategy.get(val.toString());
					if (source != null) {
						obj.put(mergePath[level], source);
					}
				}
			}
			else {
				if (val instanceof JSONObject) {
					joinJSONObject((JSONObject)val, mergePath, level + 1);
				}
				if (val instanceof JSONArray) {
					JSONArray array = (JSONArray)val;
					for (int i = 0; i < array.length(); i++) {
						try {
							JSONObject object = array.getJSONObject(i);
							joinJSONObject(object, mergePath, level + 1);
						} catch(Exception e)
						{}
					}
				}
			}
		}
	}

	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map configMap, TopologyContext arg1, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
		String filePath = config.getProperty("karma.storm.join.source");
		atId = config.getProperty("karma.context.atid");
		mergePath = config.getProperty("karma.storm.mergepath");
		field = config.getProperty("karma.storm.reducer.field");
		Map<String, String> map = new HashMap<String, String>();
		map.put("karma.storm.join.source", filePath);
		map.put("karma.context.atid", atId);
		if (config.getProperty("karma.jedis.server") != null) {
			map.put("karma.jedis.server", config.getProperty("karma.jedis.server"));
		}
		if (config.getProperty("karma.jedis.port") != null) {
			map.put("karma.jedis.port", config.getProperty("karma.jedis.port"));
		}
		if (config.getProperty("karma.jedis.auth") != null) {
			map.put("karma.jedis.auth", config.getProperty("karma.jedis.auth"));
		}
		strategy.config(map);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "json"));
	}
}
