package edu.isi.karma.storm.strategy;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class JedisJoinStrategy implements JoinStrategy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private static Logger LOG = LoggerFactory.getLogger(JedisJoinStrategy.class);
	private Jedis jedis;
	@Override
	public JSONObject get(String uri) {
		String join = jedis.get(uri);
		if (join != null) {
			return new JSONObject(join);
		}
		else {
			return new JSONObject();
		}
	}

	@Override
	public void config(@SuppressWarnings("rawtypes") Map configMap) {
		String server = configMap.get("karma.jedis.server").toString();
		int port = Integer.parseInt(configMap.get("karma.jedis.port").toString());
		String auth = configMap.get("karma.jedis.auth").toString();
		try {
			jedis = new Jedis(server, port);
			jedis.auth(auth);
		}catch(Exception e)
		{
			LOG.error("Cannot read source table", e);
		}

	}

}
