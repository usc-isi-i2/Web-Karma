package edu.isi.karma.semantictypes.typinghandler;

import edu.isi.karma.modeling.semantictypes.ISemanticTypeModelHandler;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.semantictypes.remote.SemanticLabelingService;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by alse on 10/23/16.
 * This class handles semantic typing using the service
 */
public class RemoteSTModelHandler implements ISemanticTypeModelHandler {
	static Logger logger = LoggerFactory
			.getLogger(HybridSTModelHandler.class.getSimpleName());

    private String contextId;
    private boolean modelEnabled = true;
	private List<String> namespaces; // list or URIs which limit the predictions to the ones loaded in this

    public RemoteSTModelHandler(String contextId) {
        this.contextId = contextId;
    }
    @Override
    public boolean addType(String label, List<String> examples) {
        return false;
    }

    @Override
    public List<SemanticTypeLabel> predictType(List<String> examples, int numPredictions) {
		if (!this.modelEnabled) {
			logger.warn("Semantic Type Modeling is not enabled");
			return null;
		}
		// Sanity checks for arguments
		if (examples == null || examples.isEmpty() || numPredictions <= 0) {
			logger.warn("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive");
			return null;
		}

		logger.debug("Predict Type for " + examples.toArray().toString());

		logger.warn("-----------------------------------------------------------------------------");

		StringBuilder sb = new StringBuilder();
		String sep = "\n";
		for(String s: examples) {
			sb.append(sep).append(s);
		}

		List<SemanticTypeLabel> predictions =  new SemanticLabelingService().predict(sb.toString(), numPredictions, this.namespaces);
		logger.debug("Got " + predictions.size() + " predictions");
		logger.warn("-----------------------------------------------------------------------------");
		return predictions;
    }

    @Override
    public boolean removeAllLabels() {
        return false;
    }

	@Override
	public boolean readModelFromFile(String filepath, boolean isNumeric) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		if (isNumeric) {
			contextParameters
			.setParameterValue(ServletContextParameterMap.ContextParameter.NUMERIC_SEMTYPE_MODEL_DIRECTORY, filepath);
		}
		else {
			contextParameters
			.setParameterValue(ServletContextParameterMap.ContextParameter.TEXTUAL_SEMTYPE_MODEL_DIRECTORY, filepath);
		}
		return true;
	}

	@Override
	public void setNamespaces(List<String> namespaces) {
		this.namespaces = new ArrayList<>(namespaces);
	}

	@Override
	public void setModelHandlerEnabled(boolean enabled) {
		this.modelEnabled = enabled;

	}

	@Override
	public boolean getModelHandlerEnabled() {
		return this.modelEnabled;
	}

	@Override
	public boolean readModelFromFile(String filepath) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		contextParameters
			.setParameterValue(ServletContextParameterMap.ContextParameter.SEMTYPE_MODEL_DIRECTORY, filepath);
		return true;
	}
}
