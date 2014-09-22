package edu.isi.karma.storm;

import java.io.File;
import java.net.URISyntaxException;

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

import com.ibm.icu.util.TimeUnit;

public class TestBasicKarmaTopology {

	@Test
	public void testBasicTopology(){ 
		TopologyBuilder builder = new TopologyBuilder(); 
		builder.setSpout("karma-seq-spout", new KarmaSequenceFileSpout()); 
		builder.setBolt("karma-generate-json", new KarmaBolt()).shuffleGrouping("karma-seq-spout").setMaxTaskParallelism(1);
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
		builder.setBolt("karma-output-json", sequenceFileBolt).fieldsGrouping("karma-generate-json", new Fields("id"));
		Config config = new Config();
		config.put("karma.input.type", "JSON");
		config.put("input.path", "/tmp/loaded_data/simpleloader.seq");
		try {
			config.put("model.file", new File(this.getClass().getClassLoader().getResource("people-model.ttl").toURI()).getAbsolutePath().toString());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
