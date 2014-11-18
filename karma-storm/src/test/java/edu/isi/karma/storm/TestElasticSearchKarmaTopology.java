package edu.isi.karma.storm;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.elasticsearch.storm.EsBolt;
import org.junit.Test;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

public class TestElasticSearchKarmaTopology {

	@Test
	public void testBasicTopology(){ 
		TopologyBuilder builder = new TopologyBuilder(); 
		builder.setSpout("karma-seq-spout", new KarmaSequenceFileSpout());
		Properties basicKarmaBoltProperties = new Properties();
		basicKarmaBoltProperties.setProperty("name", "Stormy");
		basicKarmaBoltProperties.setProperty("karma.input.type", "JSON");
		basicKarmaBoltProperties.setProperty("base.uri", "http://ex.com");
		String source = null; 
		try {
			source = new File(this.getClass().getClassLoader().getResource("people-model.ttl").toURI()).getAbsolutePath().toString();
			basicKarmaBoltProperties.setProperty("model.file", source);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		builder.setBolt("karma-generate-json", new KarmaBolt(basicKarmaBoltProperties)).shuffleGrouping("karma-seq-spout");
		
		
		DefaultFileNameFormat fileNameFormat = new DefaultFileNameFormat();
		fileNameFormat.withExtension(".seq");
		fileNameFormat.withPath("/tmp/storm");
		fileNameFormat.withPrefix("karma");
		Set<String> sources = new HashSet<String>();
		sources.add(source);
		KarmaReducerBolt reducerBolt = new KarmaReducerBolt(sources, false);
		builder.setBolt("karma-reducer-json", reducerBolt).fieldsGrouping("karma-generate-json", new Fields("id"));
		Map conf = new HashMap();
		conf.put("es.input.json", "true");
		conf.put("es.index.auto.create", "true");
		conf.put("es.storm.bolt.flush.entries.size", 1);
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 10);
		conf.put("es.storm.bolt.write.ack", "true");
		conf.put("es.storm.bolt.tick.tuple.flush", "true");
		conf.put("es.nodes", "localhost");
		conf.put("es.port", "9200");
		EsBolt elasticSearchOutput = new EsBolt("nerp/people", conf);
		
		builder.setBolt("karma-elasticsearch-json", elasticSearchOutput).shuffleGrouping("karma-reducer-json");
		Config config = new Config();
		config.put("input.path", "/tmp/loaded_data/simpleloader.seq");
		config.setDebug(true);
		StormTopology topology = builder.createTopology(); 
		LocalCluster cluster = new LocalCluster(); 
		cluster.submitTopology("karma-basic-topology",
         config,
         topology); 
		Utils.sleep(35000);
		//cluster.killTopology("karma-basic-topology");
		cluster.shutdown();
	}
}
