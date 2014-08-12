package edu.isi.karma.mapreduce.driver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
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
		File modelFile = new File(getTestResource("people-model.ttl").toURI());
		File tempModelFile = File.createTempFile("people-model", "ttl");
		FileUtils.copyFile(modelFile, tempModelFile);
		tempModelFile.deleteOnExit();
		
		Properties p = new Properties();
		p.load(getTestResource("SimpleProcessor.properties").openStream());
		p.setProperty("model.uri", tempModelFile.toURI().toString());
		File tempPropertiesFile= File.createTempFile(TestSimpleProcessor.class.getName(), "properties");
		FileOutputStream fos = new FileOutputStream(tempPropertiesFile);
		p.store(fos, "");
		fos.close();
		tempPropertiesFile.deleteOnExit();
		String [] jobArgs = {"-archives", new File( getTestResource("sample_karma_user_home.zip").toURI()).getAbsolutePath(), "-libjars", System.getProperty("user.home") + "/.m2/repository/edu/isi/karma-offline/0.0.1-SNAPSHOT/karma-offline-0.0.1-SNAPSHOT-shaded.jar", tempPropertiesFile.getAbsolutePath()}; 
		res = ToolRunner.run(conf, new SimpleProcessor(), jobArgs);
		assertEquals(0, res);
	}

	protected URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
