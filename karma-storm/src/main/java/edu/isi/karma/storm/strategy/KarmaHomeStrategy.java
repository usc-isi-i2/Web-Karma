package edu.isi.karma.storm.strategy;

import java.io.Serializable;
import java.util.Map;

public class KarmaHomeStrategy implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileDistributionStrategy fileDistributionStrategy;

	public KarmaHomeStrategy(FileDistributionStrategy fileDistributionStrategy)
	{
		this.fileDistributionStrategy = fileDistributionStrategy;
	}
	
	public void prepare(@SuppressWarnings("rawtypes")  Map globalConfig)
	{
		fileDistributionStrategy.prepare(globalConfig);
	}
	
	public String getKarmaHomeDirectory()
	{
		return fileDistributionStrategy.getPath();
	}

	public void cleanup() {
		this.fileDistributionStrategy.cleanup();
		
	}
}
