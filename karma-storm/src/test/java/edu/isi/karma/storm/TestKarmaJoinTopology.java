package edu.isi.karma.storm;

import java.util.Properties;

import org.apache.storm.hdfs.bolt.SequenceFileBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.junit.Test;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

public class TestKarmaJoinTopology {

	@Test
	public void testBasicTopology(){ 
		TopologyBuilder builder = new TopologyBuilder(); 
		builder.setSpout("karma-seq-spout", new KarmaSequenceFileSpout());
		Properties basicKarmaBoltProperties = new Properties();
		basicKarmaBoltProperties.setProperty("name", "Stormy");
		basicKarmaBoltProperties.setProperty("karma.storm.join.source", "/Users/chengyey/exchange.seq");
		basicKarmaBoltProperties.setProperty("karma.context.atid", "uri");
		basicKarmaBoltProperties.setProperty("karma.storm.mergepath", "hasFeatureCollection,phonenumber_feature,featureObject,location");
		KarmaJoinBolt bolt = new KarmaJoinBolt(basicKarmaBoltProperties);
		builder.setBolt("karma-generate-json", bolt).shuffleGrouping("karma-seq-spout");
		SequenceFileBolt sequenceFileBolt = new SequenceFileBolt();
		builder.setBolt("karma-output-json", sequenceFileBolt).fieldsGrouping("karma-generate-json", new Fields("id"));
		KarmaSequenceFormat sequenceFormat = new KarmaSequenceFormat("id", "json");
		sequenceFileBolt.withSequenceFormat(sequenceFormat);
		DefaultFileNameFormat fileNameFormat = new DefaultFileNameFormat();
		fileNameFormat.withExtension(".seq");
		fileNameFormat.withPath("/tmp/storm");
		fileNameFormat.withPrefix("karma");
		sequenceFileBolt.withFileNameFormat(fileNameFormat);
		sequenceFileBolt.withFsUrl("file:///");
		sequenceFileBolt.withSyncPolicy(new CountSyncPolicy(1));
		sequenceFileBolt.withRotationPolicy(new FileSizeRotationPolicy(1, Units.KB));
		Config config = new Config();
		config.put("input.path", "/Users/chengyey/phone_small.seq");
		config.setDebug(true);
		StormTopology topology = builder.createTopology(); 
		LocalCluster cluster = new LocalCluster(); 
		cluster.submitTopology("karma-basic-topology",
         config,
         topology); 
		Utils.sleep(60000);
		cluster.killTopology("karma-basic-topology");
	}
}
