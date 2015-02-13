package edu.isi.karma.storm.strategy;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;

public interface LoadStrategy extends Serializable {
	public boolean loadNext(MutablePair<String, String> next);
	public void prepare(@SuppressWarnings("rawtypes") Map globalConfig);
}
