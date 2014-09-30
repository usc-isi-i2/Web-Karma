package edu.isi.karma.storm;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class KarmaSequenceFileSpout extends BaseRichSpout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector outputCollector;
	private SequenceFile.Reader reader;
	@Override
	public void nextTuple() {
		Text key = new Text();
		Text val = new Text();
		
		
		try {
			while(reader.next(key, val))
			{
				outputCollector.emit(new Values(key.toString(), val.toString()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void open(Map configMap, TopologyContext arg1, SpoutOutputCollector outputCollector) {
		this.outputCollector = outputCollector;
		
		Configuration conf = new Configuration();
		conf.setIfUnset("fs.default.name", "file:///");
		Path inputPath = new Path((String)configMap.get("input.path"));
		try {
			this.reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(inputPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "text"));
		
	}

}
