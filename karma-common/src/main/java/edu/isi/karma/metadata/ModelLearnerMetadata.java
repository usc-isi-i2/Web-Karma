package edu.isi.karma.metadata;

import edu.isi.karma.modeling.ModelingConfiguration;
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
	public void setup() {
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
		return ModelingConfiguration.getAlignmentGraphDir();
	}

	@Override
	public KarmaUserMetadataType getType() {
		return StandardUserMetadataTypes.MODEL_LEARNER;
	}
}

