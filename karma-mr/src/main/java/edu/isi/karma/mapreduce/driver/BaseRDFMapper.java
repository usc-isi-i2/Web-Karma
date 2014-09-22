package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;

public abstract class BaseRDFMapper extends Mapper<Text, Text, Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(BaseRDFMapper.class);

	protected BaseKarma karma;
	
	@Override
	public void setup(Context context) {

		karma = new BaseKarma();
		String inputTypeString = context.getConfiguration().get(
				"karma.input.type");
		String modelUri = context.getConfiguration().get("model.uri");
		String modelFile = context.getConfiguration().get("model.file");
		String baseURI = context.getConfiguration().get("base.uri");
		karma.setup(inputTypeString, modelUri, modelFile, baseURI);
	
	}

	@Override
	public void map(Text key, Text value, Context context) throws IOException,
			InterruptedException {

		String filename = key.toString();
		String contents = value.toString();
		LOG.info(key.toString() + " started");
		StringWriter sw = new StringWriter();
		KR2RMLRDFWriter outWriter = configureRDFWriter(sw);
		try {
			karma.getGenerator().generateRDF("model", filename, contents, karma.getInputType(),
					false, outWriter);

			String results = sw.toString();
			if (!results.equals("[\n\n]\n")) {
				writeRDFToContext(context, results);
				
			}
		} catch (Exception e) {
			LOG.error("Unable to generate RDF: " + e.getMessage());
		}
		LOG.info(key.toString() + " finished");
	}

	protected abstract KR2RMLRDFWriter configureRDFWriter(StringWriter sw);

	protected abstract void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException;

}
