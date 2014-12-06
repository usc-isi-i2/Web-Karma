package edu.isi.karma.storm;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import edu.isi.karma.storm.bolt.ElasticsearchBolt;
import edu.isi.karma.storm.bolt.KarmaJoinBolt;
import edu.isi.karma.storm.spout.ElasticsearchSpout;
import edu.isi.karma.storm.strategy.JedisJoinStrategy;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

public class TestElasticsearchJoinJedisTopology {

	@Test
	public void testBasicTopology(){ 
		TopologyBuilder builder = new TopologyBuilder(); 
		builder.setSpout("karma-seq-spout", new ElasticsearchSpout());
		Map<String, String> basicKarmaBoltProperties = new HashMap<String, String>();
		basicKarmaBoltProperties.put("name", "Stormy");
		basicKarmaBoltProperties.put("karma.jedis.server", "karma-dig-service.cloudapp.net");
		basicKarmaBoltProperties.put("karma.jedis.port", "55299");
		basicKarmaBoltProperties.put("karma.context.atid", "uri");
		basicKarmaBoltProperties.put("karma.storm.mergepath", "featureObject,location");
		basicKarmaBoltProperties.put("karma.storm.reducer.field", "text");
		KarmaJoinBolt bolt = new KarmaJoinBolt(basicKarmaBoltProperties, new JedisJoinStrategy());
		builder.setBolt("karma-generate-json", bolt).shuffleGrouping("karma-seq-spout");
		Properties prop = new Properties();
		prop.setProperty("karma.elasticsearch.host", "localhost");
		prop.setProperty("karma.elasticsearch.port", "9300");
		prop.setProperty("karma.elasticsearch.doctype", "tweet");
		prop.setProperty("karma.elasticsearch.index", "twitter");
		prop.setProperty("karma.elasticsearch.docidfield", "docid");
		prop.setProperty("karma.elasticsearch.jsonfield", "json");
		ElasticsearchBolt elasticsearchBolt = new ElasticsearchBolt(prop);
		builder.setBolt("karma-output-json", elasticsearchBolt).fieldsGrouping("karma-generate-json", new Fields("id"));
		Config config = new Config();
		config.put("karma.elasticsearch.host", "localhost");
		config.put("karma.elasticsearch.port", "9300");
		config.put("karma.elasticsearch.doctype", "tweet");
		config.put("karma.elasticsearch.index", "twitter");
		config.put("karma.context.atid", "uri");
		config.setDebug(true);
		StormTopology topology = builder.createTopology(); 
		LocalCluster cluster = new LocalCluster(); 
		cluster.submitTopology("karma-basic-topology",
         config,
         topology); 
		Utils.sleep(600000);
		cluster.killTopology("karma-basic-topology");
	}
}
