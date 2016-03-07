package edu.isi.karma.storm.bolt;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

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
import edu.isi.karma.storm.strategy.KarmaHomeStrategy;

public class KarmaBolt extends BaseRichBolt {

	private static Logger LOG = LoggerFactory.getLogger(KarmaBolt.class);
	protected BaseKarma karma;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	@SuppressWarnings("rawtypes")
	private Map localConfig;
	private KarmaHomeStrategy karmaHomeStrategy;

	@SuppressWarnings("rawtypes")
	public KarmaBolt(Map localConfig, KarmaHomeStrategy karmaHomeStrategy)
	{
		this.localConfig = localConfig;
		this.karmaHomeStrategy = karmaHomeStrategy;
	}
	
	@Override
	public void execute(Tuple tuple) {
		
		long start = System.currentTimeMillis();
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
		LOG.debug("id: "+ tuple.getStringByField("id") + " " + (System.currentTimeMillis() - start));
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
	public void prepare(Map globalConfig, TopologyContext arg1, OutputCollector outputCollector) {
		this.outputCollector = outputCollector;
		String karmaHomeDirectory = null;
		if(karmaHomeStrategy != null){

			karmaHomeStrategy.prepare(globalConfig);
			karmaHomeDirectory = karmaHomeStrategy.getKarmaHomeDirectory();	
		}
		karma = new BaseKarma();
		karma.setup(karmaHomeDirectory, 
				(String)localConfig.get("karma.input.type"), 
				(String)localConfig.get("model.uri"), (String)localConfig.get("model.file"), (String)localConfig.get("model.content"),
				(String)localConfig.get("base.uri"), 
				(String)localConfig.get("context.uri"), (String)localConfig.get("context.content"),
				(String)localConfig.get("rdf.generation.root"), (String)localConfig.get("rdf.generation.selection"));
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFields) {
		outputFields.declare(new Fields("id", "json", "model"));
		
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		this.karmaHomeStrategy.cleanup();
	}
}
