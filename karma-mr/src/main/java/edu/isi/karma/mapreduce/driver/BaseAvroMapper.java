package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.avro.mapred.AvroKey;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.RDFGeneratorRequest;

public abstract class BaseAvroMapper extends Mapper<AvroKey<Text>, NullWritable, Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(BaseAvroMapper.class);

	protected BaseKarma karma;
	
	@Override
	public void setup(Context context) {

		karma = new BaseKarma();
		String inputTypeString = context.getConfiguration().get(
				"karma.input.type");
		String modelUri = context.getConfiguration().get("model.uri");
		String modelFile = context.getConfiguration().get("model.file");
		String baseURI = context.getConfiguration().get("base.uri");
		String contextURI = context.getConfiguration().get("context.uri");
		karma.setup(inputTypeString, modelUri, modelFile, baseURI, contextURI);
	
	}

	@Override
	public void map(AvroKey<Text> key, NullWritable value, Context context) throws IOException,
			InterruptedException {

		String contents = key.toString();
		String filename = "query";
		LOG.info(key.toString() + " started");
		StringWriter sw = new StringWriter();
		KR2RMLRDFWriter outWriter = configureRDFWriter(sw);
		try {
			RDFGeneratorRequest request = new RDFGeneratorRequest("model", filename);
			request.setDataType(karma.getInputType());
			request.setInputData(contents);
			request.setAddProvenance(false);
			request.addWriter(outWriter);
			karma.getGenerator().generateRDF(request);

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
