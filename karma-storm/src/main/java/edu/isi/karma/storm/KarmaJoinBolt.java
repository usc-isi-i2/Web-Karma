package edu.isi.karma.storm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
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
import edu.isi.karma.mapreduce.driver.BaseKarma;

public class KarmaJoinBolt extends BaseRichBolt {

	private static Logger LOG = LoggerFactory.getLogger(KarmaJoinBolt.class);
	protected BaseKarma karma;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	private Properties config;
	private String atId = "uri";
	private String mergePath;
	private Map<String, JSONObject> joinTable = new HashMap<String, JSONObject>();
	public KarmaJoinBolt(Properties config)
	{
		this.config = config;
	}

	@Override
	public void execute(Tuple tuple) {

		System.out.println("My name is: " + config.getProperty("name"));
		long start = System.currentTimeMillis();
		JSONObject objectToJoin = new JSONObject(tuple.getStringByField("text"));
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
					JSONObject source = joinTable.get(target.get(atId).toString());
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
					JSONObject source = joinTable.get(val.toString());
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
		Configuration conf = new Configuration();
		String filePath = config.getProperty("karma.storm.join.source");
		atId = config.getProperty("karma.context.atid");
		mergePath = config.getProperty("karma.storm.mergepath");
		try {
			SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(new Path(filePath)));
			Text key = new Text();
			Text val = new Text();
			while(reader.next(key, val))
			{
				JSONObject obj = new JSONObject(val.toString());
				joinTable.put(obj.getString(atId), obj);
			}
			reader.close();		
		}catch(Exception e)
		{
			LOG.error("Cannot read source table", e);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "json"));
	}
}
