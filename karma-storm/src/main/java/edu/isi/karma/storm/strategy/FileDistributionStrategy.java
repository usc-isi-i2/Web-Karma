package edu.isi.karma.storm.strategy;

import java.io.Serializable;
import java.util.Map;

public interface FileDistributionStrategy extends Serializable{

	void prepare(@SuppressWarnings("rawtypes")  Map globalConfig);

	String getPath();
	
	void cleanup();
}
