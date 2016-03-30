package edu.isi.karma.webserver;

import java.util.concurrent.ConcurrentHashMap;


public class ContextParametersRegistry {
	private static ContextParametersRegistry singleton = new ContextParametersRegistry();

	private final ConcurrentHashMap<String, ServletContextParameterMap> karmaHomeToContextParameters = new ConcurrentHashMap<>();

	public static ContextParametersRegistry getInstance() {
		return singleton;
	}

	private String defaultKarmaId = null;
	
	public synchronized ServletContextParameterMap registerByKarmaHome(String karmaHome)
	{
		if(karmaHome == null || !karmaHomeToContextParameters.containsKey(karmaHome))
		{
			ServletContextParameterMap contextParameters = new ServletContextParameterMap(karmaHome);
			karmaHomeToContextParameters.putIfAbsent(contextParameters.getKarmaHome(), contextParameters);
			karmaHome = contextParameters.getKarmaHome();
		}
		return karmaHomeToContextParameters.get(karmaHome);
	}

	public ServletContextParameterMap getContextParameters(String karmaHome) {
	
		return registerByKarmaHome(karmaHome);
	}
	
	public ServletContextParameterMap getDefault()
	{
		ServletContextParameterMap contextMap = registerByKarmaHome(defaultKarmaId);	
		defaultKarmaId = contextMap.getId();
		return contextMap;
	}
	
	public synchronized void deregister(String contextId) {
		karmaHomeToContextParameters.remove(contextId);
	}
}
