package edu.isi.karma.storm.bolt;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import edu.isi.karma.storm.strategy.JoinStrategy;

public class KarmaJoinBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger LOG = LoggerFactory.getLogger(KarmaJoinBolt.class);
	
	private OutputCollector outputCollector;
	@SuppressWarnings("rawtypes")
	private Map localConfig;
	private String atId = "uri";
	private String mergePath;
	private String joinObjectField;
	private JoinStrategy strategy;
	public KarmaJoinBolt(@SuppressWarnings("rawtypes") Map localConfig, JoinStrategy joinStrategy)
	{
		this.localConfig = localConfig;
		this.strategy = joinStrategy;
	}

	@Override
	public void execute(Tuple tuple) {

		long start = System.currentTimeMillis();
		JSONObject objectToJoin = new JSONObject(tuple.getStringByField(joinObjectField));
		String[] mergePath = this.mergePath.split(",");
		joinJSONObject(objectToJoin, mergePath, 0);
		outputCollector.emit(new Values(objectToJoin.getString(atId), objectToJoin.toString()));
		outputCollector.ack(tuple);
		LOG.debug("id: "+ tuple.getStringByField("id") + " " + (System.currentTimeMillis() - start));
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
						{
							LOG.error("Unable to join JSON object", e.getMessage());
						}
					}
				}
			}
		}
	}

	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map globalConfig, TopologyContext arg1, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
		atId = localConfig.get("karma.context.atid").toString();
		mergePath = localConfig.get("karma.storm.mergepath").toString();
		joinObjectField = localConfig.get("karma.storm.join.object.field").toString();
		strategy.prepare(localConfig);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "json"));
	}
}
