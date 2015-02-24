package edu.isi.karma.storm;

import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class BasicJSONTestSpout extends BaseRichSpout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3248860630442380844L;
	
	/** OutputCollector */
    SpoutOutputCollector      collector;
    
	@Override
	public void nextTuple() {
		collector.emit(new Values(""), new Values(""));
		
	}

	@Override
	public void open(@SuppressWarnings("rawtypes") Map arg0, TopologyContext arg1, SpoutOutputCollector collector) {
		this.collector = collector;
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("id", "text"));
		
	}

}
