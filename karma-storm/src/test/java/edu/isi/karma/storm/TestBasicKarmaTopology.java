package edu.isi.karma.storm;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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

public class TestBasicKarmaTopology {

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
		SequenceFileBolt sequenceFileBolt = new SequenceFileBolt();
		
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
		Set<String> sources = new HashSet<String>();
		sources.add(source);
		KarmaReducerBolt reducerBolt = new KarmaReducerBolt(sources);
		builder.setBolt("karma-reducer-json", reducerBolt).fieldsGrouping("karma-generate-json", new Fields("id"));
		builder.setBolt("karma-output-json", sequenceFileBolt).shuffleGrouping("karma-reducer-json");
		Config config = new Config();
		config.put("input.path", "/tmp/loaded_data/simpleloader.seq");
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
