package edu.isi.karma.storm.bolt;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.BaseKarma;
import edu.isi.karma.rdf.RDFGeneratorRequest;

public class KarmaBolt extends BaseRichBolt {

	private static Logger LOG = LoggerFactory.getLogger(KarmaBolt.class);
	protected BaseKarma karma;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	private Properties config;

	public KarmaBolt(Properties config)
	{
		this.config = config;
	}
	
	@Override
	public void execute(Tuple tuple) {
		
		System.out.println("My name is: " + config.getProperty("name"));
		long start = System.currentTimeMillis();
		System.out.println("id: "+tuple.getStringByField("id"));
		StringWriter sw = new StringWriter();
		KR2RMLRDFWriter outWriter = configureRDFWriter(sw);
		try {
			RDFGeneratorRequest request = new RDFGeneratorRequest("model", tuple.getStringByField("id"));
			request.addWriter(outWriter);
			request.setInputData(tuple.getStringByField("text"));
			request.setDataType(karma.getInputType());
			karma.getGenerator().generateRDF(request);
			String results = sw.toString();
			if (!results.equals("[\n\n]\n")) {
				writeRDF(tuple, results);
			}
		} catch (Exception e) {
			LOG.error("Unable to generate RDF: " + e.getMessage());
			
		}
		finally{
			outputCollector.ack(tuple);
		}
		System.out.println("id: "+ tuple.getStringByField("id") + " " + (System.currentTimeMillis() - start));
	}

	protected KR2RMLRDFWriter configureRDFWriter(StringWriter sw) {
		PrintWriter pw = new PrintWriter(sw);
		KR2RMLRDFWriter outWriter = new JSONKR2RMLRDFWriter(pw, karma.getBaseURI());
		return outWriter;
	}

	protected void writeRDF(Tuple tuple, String results)
			throws IOException, InterruptedException {
		JSONArray generatedObjects = new JSONArray(results);
		for(int i = 0; i < generatedObjects.length(); i++)
		{
			outputCollector.emit(tuple, new Values(generatedObjects.getJSONObject(i).getString("@id"), generatedObjects.getJSONObject(i).toString(), karma.getModel().toString() ));
			
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map configMap, TopologyContext arg1, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
		karma = new BaseKarma();
		karma.setup(config.getProperty("karma.input.type"), config.getProperty("model.uri"), config.getProperty("model.file"), 
				config.getProperty("base.uri"), config.getProperty("context.uri"), 
				config.getProperty("rdf.generation.root"), config.getProperty("rdf.generation.selection"));
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "json", "model"));
		
	}
}
