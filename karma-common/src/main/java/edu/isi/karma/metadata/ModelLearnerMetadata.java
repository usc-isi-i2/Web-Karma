package edu.isi.karma.metadata;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphLoaderThread;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ModelLearnerMetadata extends KarmaUserMetadata {

	
	public ModelLearnerMetadata(Workspace workspace) throws KarmaException
	{
		super(workspace);
	}
	
	@Override
	public void setup(UpdateContainer uc) {
		OntologyManager ontologyManager = workspace.getOntologyManager();
		if (ModelingConfiguration.isLearnerEnabled())
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

