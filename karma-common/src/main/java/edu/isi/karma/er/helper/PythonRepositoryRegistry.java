package edu.isi.karma.er.helper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PythonRepositoryRegistry {

	private static PythonRepositoryRegistry singleton = new PythonRepositoryRegistry();

	private final Map<String, PythonRepository> karmaHomeToPythonRepository = new ConcurrentHashMap<String, PythonRepository>();

	public static PythonRepositoryRegistry getInstance() {
		return singleton;
	}

	public void register(PythonRepository pythonRepository) {
		karmaHomeToPythonRepository.put(pythonRepository.getKarmaHome(), pythonRepository);
	}

	public PythonRepository getPythonRepository(String workspaceId) {
		return karmaHomeToPythonRepository.get(workspaceId);
	}
	
	public void deregister(String workspaceId) {
		karmaHomeToPythonRepository.remove(workspaceId);
	}
}
