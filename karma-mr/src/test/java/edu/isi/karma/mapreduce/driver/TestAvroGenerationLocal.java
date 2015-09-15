package edu.isi.karma.mapreduce.driver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.junit.Test;

public class TestAvroGenerationLocal {

	
	@Test
	public void test() throws Exception {
		if(!new File("/tmp/avro_data").exists())
		{
			new File("/tmp/avro_data").mkdir();
			Files.copy(FileSystems.getDefault().getPath(new File(getTestResource("data/avro/people.avro").toURI()).getAbsolutePath()), FileSystems.getDefault().getPath("/tmp/avro_data/people.avro"));
			
		}
		if(!new File("/tmp/avro_data_output").exists())
		{
			Configuration conf = new Configuration();
		
			
			String [] jobArgs = {"-files", new File(getTestResource("people-model.ttl").toURI()).getAbsolutePath().toString(),"-libjars", System.getProperty("user.home") + "/.m2/repository/edu/isi/karma-offline/0.0.1-SNAPSHOT/karma-offline-0.0.1-SNAPSHOT-shaded.jar", new File(getTestResource("JSONAvroProcessorLocal.properties").toURI()).getAbsolutePath().toString()};
			int res = ToolRunner.run(conf, new JSONAvroProcessor(), jobArgs);
			assertEquals(0, res);
		}
	}


	protected URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
