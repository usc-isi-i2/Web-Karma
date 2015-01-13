package edu.isi.karma.storm.spout;

import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ElasticsearchSpout extends BaseRichSpout{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector outputCollector;
	private SearchResponse response;
	private Client client;
	private String atId;
	
	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		this.outputCollector = collector;
		String host = conf.get("karma.elasticsearch.host").toString();
		int port = Integer.parseInt(conf.get("karma.elasticsearch.port").toString());
		String docType = conf.get("karma.elasticsearch.doctype").toString();
		String index = conf.get("karma.elasticsearch.index").toString();
		atId = conf.get("karma.context.atid").toString();
		client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(host, port));
		response = client.prepareSearch(index).
				setTypes(docType).
				setScroll(new TimeValue(60000)).
				setSize(1000).
				execute().			
				actionGet();
	}

	@Override
	public void nextTuple() {
		while (true) {
		    for (SearchHit hit : response.getHits()) {
		    	JSONObject obj = new JSONObject(hit.getSourceAsString());	    	
		    	outputCollector.emit(new Values(obj.getString(atId), hit.getId(), hit.getSourceAsString()));
		    }
		    response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();	   
		    if (response.getHits().getHits().length == 0) {
		        break;
		    }
		}
		client.close();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("id", "docid", "text"));	
	}

}
