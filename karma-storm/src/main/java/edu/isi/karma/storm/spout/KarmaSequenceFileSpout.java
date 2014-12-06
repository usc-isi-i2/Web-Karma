package edu.isi.karma.storm.spout;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOG = LoggerFactory.getLogger(KarmaSequenceFileSpout.class);
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
			LOG.error("Unable to emit new tuple", e);
		}
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map configMap, TopologyContext arg1, SpoutOutputCollector outputCollector) {
		this.outputCollector = outputCollector;
		
		Configuration conf = new Configuration();
		conf.setIfUnset("fs.default.name", "file:///");
		Path inputPath = new Path((String)configMap.get("input.path"));
		try {
			this.reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(inputPath));
		} catch (IOException e) {
			LOG.error("Unable to open sequence file", e);
		}
		
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "text"));
		
	}

}
