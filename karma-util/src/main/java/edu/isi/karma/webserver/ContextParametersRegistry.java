package edu.isi.karma.webserver;

import java.util.concurrent.ConcurrentHashMap;


public class ContextParametersRegistry {
	private static ContextParametersRegistry singleton = new ContextParametersRegistry();

	private final ConcurrentHashMap<String, ServletContextParameterMap> karmaHomeToContextParameters = new ConcurrentHashMap<String, ServletContextParameterMap>();

	public static ContextParametersRegistry getInstance() {
		return singleton;
	}

	public ServletContextParameterMap registerByKarmaHome(String karmaHome)
	{
		if(karmaHome == null || !karmaHomeToContextParameters.containsKey(karmaHome))
		{
			ServletContextParameterMap contextParameters = new ServletContextParameterMap(karmaHome);
			karmaHomeToContextParameters.putIfAbsent(contextParameters.getKarmaHome(), contextParameters);
			karmaHome = contextParameters.getKarmaHome();
		}
		return karmaHomeToContextParameters.get(karmaHome);
	}
	public void register(ServletContextParameterMap contextParameters) {
		karmaHomeToContextParameters.put(contextParameters.getKarmaHome(), contextParameters);
	}

	public ServletContextParameterMap getContextParameters(String karmaHome) {
	
		return registerByKarmaHome(karmaHome);
	}
	
	public ServletContextParameterMap getDefault()
	{
		return registerByKarmaHome(null);	
	}
	
	public void deregister(String contextId) {
		karmaHomeToContextParameters.remove(contextId);
	}
}
