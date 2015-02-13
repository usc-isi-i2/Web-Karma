package edu.isi.karma.mapreduce.driver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.junit.Test;

public class TestAvroGenerationLocal {

	
	@Test
	public void test() throws Exception {
		Configuration conf = new Configuration();
	
		
		String [] jobArgs = {"-files", new File(getTestResource("people-model.ttl").toURI()).getAbsolutePath().toString(), "-archives", new File( getTestResource("sample_karma_user_home.zip").toURI()).getAbsolutePath(), "-libjars", System.getProperty("user.home") + "/.m2/repository/edu/isi/karma-offline/0.0.1-SNAPSHOT/karma-offline-0.0.1-SNAPSHOT-shaded.jar", new File(getTestResource("JSONAvroProcessorLocal.properties").toURI()).getAbsolutePath().toString()};
		int res = ToolRunner.run(conf, new JSONAvroProcessor(), jobArgs);
		assertEquals(0, res);
	}


	protected URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
