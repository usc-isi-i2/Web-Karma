package edu.isi.karma.storm.topology;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.hdfs.bolt.SequenceFileBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import edu.isi.karma.storm.bolt.KarmaJoinBolt;
import edu.isi.karma.storm.function.KarmaSequenceFormat;
import edu.isi.karma.storm.spout.KarmaSequenceFileSpout;
import edu.isi.karma.storm.strategy.InMemoryJoinStrategy;

public class KarmaJoinTopology {

	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
		TopologyBuilder builder = new TopologyBuilder(); 
		builder.setSpout("karma-seq-spout", new KarmaSequenceFileSpout());
		Map<String, String> basicKarmaBoltProperties = new HashMap<String, String>();
		basicKarmaBoltProperties.put("name", "Stormy");
		basicKarmaBoltProperties.put("karma.storm.join.source", "/user/frank/storm/input/exchange.seq");
		basicKarmaBoltProperties.put("karma.context.atid", "uri");
		basicKarmaBoltProperties.put("karma.storm.mergepath", "hasFeatureCollection,phonenumber_feature,featureObject,location");
		basicKarmaBoltProperties.put("karma.storm.reducer.field", "text");
		KarmaJoinBolt bolt = new KarmaJoinBolt(basicKarmaBoltProperties, new InMemoryJoinStrategy());
		builder.setBolt("karma-generate-json", bolt).shuffleGrouping("karma-seq-spout");
		SequenceFileBolt sequenceFileBolt = new SequenceFileBolt();
		builder.setBolt("karma-output-json", sequenceFileBolt).fieldsGrouping("karma-generate-json", new Fields("id"));
		KarmaSequenceFormat sequenceFormat = new KarmaSequenceFormat("id", "json");
		sequenceFileBolt.withSequenceFormat(sequenceFormat);
		DefaultFileNameFormat fileNameFormat = new DefaultFileNameFormat();
		fileNameFormat.withExtension(".seq");
		fileNameFormat.withPath("/user/frank/storm/output");
		fileNameFormat.withPrefix("karma");
		sequenceFileBolt.withFileNameFormat(fileNameFormat);
		sequenceFileBolt.withFsUrl("hdfs://karma-dig-1.hdp.azure.karma.isi.edu:8020");
		sequenceFileBolt.withSyncPolicy(new CountSyncPolicy(1));
		sequenceFileBolt.withRotationPolicy(new FileSizeRotationPolicy(1, Units.MB));
		Config config = new Config();
		config.put("input.path", "/user/frank/storm/input/phone_small.seq");
		config.setDebug(true);
		StormTopology topology = builder.createTopology(); 
		StormSubmitter.submitTopology("karma-basic-topology",
         config,
         topology); 
	}

}
