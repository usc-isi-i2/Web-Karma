package edu.isi.karma.config;

import java.util.concurrent.ConcurrentHashMap;

public class ModelingConfigurationRegistry {
	private static ModelingConfigurationRegistry singleton = new ModelingConfigurationRegistry();

	private final ConcurrentHashMap<String, ModelingConfiguration> contextIdToModelingConfiguration = new ConcurrentHashMap<>();

	public static ModelingConfigurationRegistry getInstance() {
		return singleton;
	}

	public ModelingConfiguration register(String contextId) {
		if(!contextIdToModelingConfiguration.containsKey(contextId))
		{
			ModelingConfiguration modelingConfiguration = new ModelingConfiguration(contextId);
			modelingConfiguration.load();
			contextIdToModelingConfiguration.putIfAbsent(contextId, modelingConfiguration);
		}
		return contextIdToModelingConfiguration.get(contextId);
	}

	public ModelingConfiguration getModelingConfiguration(String contextId) {
		return register(contextId);
	}
	
	public void deregister(String contextId) {
		contextIdToModelingConfiguration.remove(contextId);
	}
}
