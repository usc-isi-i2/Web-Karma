package edu.isi.karma.storm.bolt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import edu.isi.karma.storm.function.JSONToMerge;

public class KarmaReducerBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	protected Map<String, JSONToMerge> allJsonToMerge;
	protected Set<String> models;
	protected Boolean outputId = false;
	public KarmaReducerBolt(Set<String> models)
	{
		this.models = models;
	}
	
	public KarmaReducerBolt(Set<String> models, boolean outputId)
	{
		this.models = models;
		this.outputId = outputId;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map globalConf, TopologyContext context,
			OutputCollector collector) {
		allJsonToMerge = new HashMap<>();
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
			String mergedJson;
			try {
				mergedJson = jsonToMerge.merge();
			} catch (ParseException e) {
				//TODO handle this properly
				mergedJson = "";
			}
			if(outputId)
			{
				collector.emit(new Values(id, mergedJson));
			}
			else
			{
				collector.emit(new Values(mergedJson));
			}
			List<Tuple> tuplesToAck = jsonToMerge.getTuplesToAck();
			for(Tuple tuple : tuplesToAck)
			{
				collector.ack(tuple);
			}
			
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		if(outputId)
		{
			declarer.declare(new Fields("id", "json"));
		}
		else
		{
			declarer.declare(new Fields("json"));
		}
		
	}

}
