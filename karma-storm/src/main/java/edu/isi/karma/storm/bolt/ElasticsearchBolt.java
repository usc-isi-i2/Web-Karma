package edu.isi.karma.storm.bolt;

import java.util.Map;
import java.util.Properties;

import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
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

public class ElasticsearchBolt extends BaseRichBolt {

	private static Logger LOG = LoggerFactory.getLogger(ElasticsearchBolt.class);
	protected BaseKarma karma;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	private Properties config;
	private Client client;

	public ElasticsearchBolt(Properties config)
	{
		this.config = config;
	}
	
	@Override
	public void execute(Tuple tuple) {
		String docType = config.getProperty("karma.elasticsearch.doctype");
		String index = config.getProperty("karma.elasticsearch.index");
		String docIdField = config.getProperty("karma.elasticsearch.docidfield");
		String jsonField = config.getProperty("karma.elasticsearch.jsonfield");
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.index(index);
		updateRequest.type(docType);
		updateRequest.id(tuple.getStringByField(docIdField));
		updateRequest.doc(tuple.getStringByField(jsonField));
		try {
			client.update(updateRequest).get();
		} catch (Exception e) {
			LOG.error("Elasticsearch update fail!", e);
		}
		outputCollector.emit(new Values(tuple.getStringByField("id"), tuple.getStringByField(docIdField), tuple.getStringByField(jsonField)));
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map configMap, TopologyContext arg1, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
		String host = config.getProperty("karma.elasticsearch.host");
		int port = Integer.parseInt(config.getProperty("karma.elasticsearch.port"));
		client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(host, port));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "docid", "json"));	
	}
}
