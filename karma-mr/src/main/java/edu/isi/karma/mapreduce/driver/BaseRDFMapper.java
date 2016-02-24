package edu.isi.karma.mapreduce.driver;

import edu.isi.karma.rdf.BaseRDFImpl;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public abstract class BaseRDFMapper extends Mapper<Writable, Text, Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(BaseRDFMapper.class);
	protected BaseRDFImpl process;
	@Override
	public void setup(Context context) throws IOException {
		Properties properties = new Properties();
		for (Map.Entry<String, String> entry : context.getConfiguration()) {
			properties.setProperty(entry.getKey(), entry.getValue());
		}
		process.setup(properties);
	}

	@Override
	public void map(Writable key, Text value, Context context) throws IOException,
			InterruptedException {
		String results = process.mapResult(key.toString(), value.toString());
		if (results != null && !results.equals("[\n\n]\n")) {

			writeRDFToContext(context, results);

		}
		else
		{
			LOG.info("RDF is empty! ");
		}
	}

	protected abstract void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException;


}