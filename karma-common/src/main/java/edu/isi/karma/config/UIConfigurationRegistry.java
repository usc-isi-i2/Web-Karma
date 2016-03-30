package edu.isi.karma.config;

import java.util.concurrent.ConcurrentHashMap;

public class UIConfigurationRegistry {
	private static UIConfigurationRegistry singleton = new UIConfigurationRegistry();

	private final ConcurrentHashMap<String, UIConfiguration> contextIdToUIConfiguration = new ConcurrentHashMap<>();

	public static UIConfigurationRegistry getInstance() {
		return singleton;
	}

	public UIConfiguration register(String contextId)
	{
		if(!contextIdToUIConfiguration.containsKey(contextId))
		{
			UIConfiguration uiConfiguration = new UIConfiguration(contextId);
			uiConfiguration.loadConfig();
			contextIdToUIConfiguration.putIfAbsent(contextId, uiConfiguration);
		}
		return contextIdToUIConfiguration.get(contextId);
	}

	public UIConfiguration getUIConfiguration(String contextId) {
		return register(contextId);
	}
	
	public void deregister(String contextId) {
		contextIdToUIConfiguration.remove(contextId);
	}
}
