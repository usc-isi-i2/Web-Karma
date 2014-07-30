package edu.isi.karma.mapreduce.driver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapreduce.v2.MiniMRYarnCluster;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSimpleProcessor {

	protected static MiniDFSCluster dfsCluster;
	protected static MiniMRYarnCluster cluster;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		Configuration dfsConf = new Configuration();
		dfsCluster = new MiniDFSCluster.Builder(dfsConf).build();
		 System.setProperty("hadoop.log.dir", "/tmp/logs");
		 YarnConfiguration clusterConf = new YarnConfiguration();
		 clusterConf.addResource(dfsConf);
		 clusterConf.set("yarn.nodemanager.vmem-check-enabled", "false");
		 clusterConf.set("yarn.nodemanager.pmem-check-enabled", "false");
		cluster = new MiniMRYarnCluster("simpleprocessor");
		cluster.init(clusterConf);
		cluster.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if(cluster != null)
		{
			cluster.stop();
			cluster.close();
		}
	}

	@Test
	public void test() throws Exception {
		Configuration conf = cluster.getConfig();
		String[] args = {new File( getTestResource("SimpleLoader.properties").toURI()).getAbsolutePath()};
		int res = ToolRunner.run(conf, new SimpleLoader(),args);
		assertEquals(0, res);
		String [] jobArgs = {"-archives", "/Users/jason/karma.zip", "-libjars", "/Users/jason/projects/Web-Karma/karma-offline/target/karma-offline-0.0.1-SNAPSHOT-shaded.jar", new File( getTestResource("SimpleProcessor.properties").toURI()).getAbsolutePath()}; 
		res = ToolRunner.run(conf, new SimpleProcessor(), jobArgs);
		assertEquals(0, res);
	}

	protected URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
