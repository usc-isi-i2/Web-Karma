package edu.isi.karma.metadata;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphLoaderThread;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ModelLearnerMetadata extends KarmaUserMetadata {

	
	public ModelLearnerMetadata(ServletContextParameterMap contextParameters) throws KarmaException
	{
		super(contextParameters);
	}
	
	@Override
	public void setup(UpdateContainer uc, Workspace workspace) {
		OntologyManager ontologyManager = workspace.getOntologyManager();
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId()).getKarmaHome());
		if (modelingConfiguration.isLearnerEnabled())
			new ModelLearningGraphLoaderThread(ontologyManager).run();
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.ALIGNMENT_GRAPH_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "alignment-graph/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.MODEL_LEARNER;
	}
}

