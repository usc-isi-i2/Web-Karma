package edu.isi.karma.storm;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.Config;
import backtype.storm.ILocalCluster;
import backtype.storm.Testing;
import backtype.storm.generated.StormTopology;
import backtype.storm.testing.CompleteTopologyParam;
import backtype.storm.testing.MkClusterParam;
import backtype.storm.testing.MockedSources;
import backtype.storm.testing.TestJob;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.storm.bolt.KarmaBolt;
import edu.isi.karma.storm.bolt.KarmaReducerBolt;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class TestBasicKarmaTopology {

	private static final Logger LOG = LoggerFactory
			.getLogger(TestBasicKarmaTopology.class);

	@Test
	public void testBasicTopology() {
		PythonRepository repo = new PythonRepository(false, ContextParametersRegistry.getInstance().getDefault().getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonRepositoryRegistry.getInstance().register(repo);
		MkClusterParam mkClusterParam = new MkClusterParam();
		Config daemonConf = new Config();
		daemonConf.put(Config.STORM_LOCAL_MODE_ZMQ, false);
		mkClusterParam.setDaemonConf(daemonConf);

		try {
			Testing.withSimulatedTimeLocalCluster(mkClusterParam,
					new TestJob() {

						@SuppressWarnings({ "rawtypes", "unchecked" })
						@Override
						public void run(ILocalCluster cluster) throws Exception {
							// TODO Auto-generated method stub
							TopologyBuilder builder = new TopologyBuilder(); 
							builder.setSpout("karma-json-spout", new BasicJSONTestSpout());
							Properties basicKarmaBoltProperties = new Properties();
							basicKarmaBoltProperties.setProperty("name", "Stormy");
							basicKarmaBoltProperties.setProperty("karma.input.type", "JSON");
							basicKarmaBoltProperties.setProperty("base.uri", "http://ex.com");
							String source = null; 
							try {
								source = new File(this.getClass().getClassLoader().getResource("people-model.ttl").toURI()).getAbsolutePath().toString();
								basicKarmaBoltProperties.setProperty("model.file", source);
							} catch (URISyntaxException e) {
								LOG.error("Unable to load model", e);
							}
							
							builder.setBolt("karma-generate-json", new KarmaBolt(basicKarmaBoltProperties, null)).shuffleGrouping("karma-json-spout");

							Set<String> sources = new HashSet<>();
							sources.add(source);
							KarmaReducerBolt reducerBolt = new KarmaReducerBolt(sources);
							builder.setBolt("karma-reducer-json", reducerBolt).fieldsGrouping("karma-generate-json", new Fields("id"));
							
							String inputs = IOUtils.toString(getTestResource("input/people.json"));
							JSONArray array = new JSONArray(inputs);
							List<Values> values = new LinkedList<>();
							for(int i = 0; i < array.length(); i++)
							{
								JSONObject obj = array.getJSONObject(i);
								values.add(new Values("a.txt",obj.toString()));
							}
							
							 MockedSources mockedSources = new MockedSources();
			                 mockedSources.addMockData("karma-json-spout", values.toArray(new Values[values.size()]));
			                    
							Config config = new Config();
							config.setDebug(true);
							
							StormTopology topology = builder.createTopology(); 
							CompleteTopologyParam completeTopologyParam = new CompleteTopologyParam();
		                    completeTopologyParam.setMockedSources(mockedSources);
		                    completeTopologyParam.setStormConf(config);
		                    Map results = Testing.completeTopology(cluster, topology, completeTopologyParam);
							ArrayList<String> karmaJsonSpoutResults = ( ArrayList<String>)results.get("karma-json-spout");
		                    Assert.assertEquals(7, karmaJsonSpoutResults.size() );
							ArrayList<String> karmaJsonReducerResults = ( ArrayList<String>)results.get("karma-reducer-json");
		                    Assert.assertEquals(7, karmaJsonReducerResults.size() );
							ArrayList<String> karmaBoltResults = ( ArrayList<String>)results.get("karma-generate-json");
		                    Assert.assertEquals(7, karmaBoltResults.size() );
						}
					});

		} catch (Exception ex) {
			if ((ex instanceof IOException) == false) {
				throw ex;
			}
		}

	}
	
	protected URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
