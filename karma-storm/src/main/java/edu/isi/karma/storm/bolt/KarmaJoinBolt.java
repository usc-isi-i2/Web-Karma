package edu.isi.karma.storm.bolt;

import java.util.Iterator;
import java.util.Map;

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
import edu.isi.karma.storm.strategy.JoinStrategy;

public class KarmaJoinBolt extends BaseRichBolt {

	protected BaseKarma karma;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	@SuppressWarnings("rawtypes")
	private Map config;
	private String atId = "uri";
	private String mergePath;
	private String field;
	private JoinStrategy strategy;
	public KarmaJoinBolt(@SuppressWarnings("rawtypes") Map config, JoinStrategy strategy)
	{
		this.config = config;
		this.strategy = strategy;
	}

	@Override
	public void execute(Tuple tuple) {

		System.out.println("My name is: " + config.get("name"));
		String docid = "";
		if (tuple.contains("docid")) {
			docid = tuple.getStringByField("docid");
		}
		long start = System.currentTimeMillis();
		JSONObject objectToJoin = new JSONObject(tuple.getStringByField(field));
		String[] mergePath = this.mergePath.split(",");
		joinJSONObject(objectToJoin, mergePath, 0);
		outputCollector.emit(new Values(objectToJoin.getString(atId), docid, objectToJoin.toString()));
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
		atId = config.get("karma.context.atid").toString();
		mergePath = config.get("karma.storm.mergepath").toString();
		field = config.get("karma.storm.reducer.field").toString();
		strategy.config(config);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "docid", "json"));
	}
}
