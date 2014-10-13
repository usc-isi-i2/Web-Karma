package edu.isi.karma.storm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class KarmaReducerBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	protected Map<String, JSONToMerge> allJsonToMerge;
	protected Set<String> models;
	
	public KarmaReducerBolt(Set<String> models)
	{
		this.models = models;
	}
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		allJsonToMerge = new HashMap<String, JSONToMerge>();
		this.collector = collector;
		
	}

	@Override
	public void execute(Tuple input) {
		String id = input.getStringByField("id");
		if(!allJsonToMerge.containsKey(input.getValueByField("id")))
		{
			allJsonToMerge.put(id, new JSONToMerge(models.size()));
		}
		JSONToMerge jsonToMerge = allJsonToMerge.get(id);
		jsonToMerge.addJSON(input, input.getStringByField("model"), input.getStringByField("json"));
		if(jsonToMerge.isReadyToMerge())
		{
			String mergedJson = jsonToMerge.merge();
			collector.emit(new Values(id, mergedJson));
			List<Tuple> tuplesToAck = jsonToMerge.getTuplesToAck();
			for(Tuple tuple : tuplesToAck)
			{
				collector.ack(tuple);
			}
			
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("id", "json"));
		
	}

}
