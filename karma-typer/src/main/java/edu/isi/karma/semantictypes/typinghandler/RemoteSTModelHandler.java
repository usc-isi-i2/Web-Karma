package edu.isi.karma.semantictypes.typinghandler;

import edu.isi.karma.modeling.semantictypes.ISemanticTypeModelHandler;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

import java.util.List;

/**
 * Created by alse on 10/23/16.
 */
public class RemoteSTModelHandler implements ISemanticTypeModelHandler {

    private String contextId;
    private boolean modelEnabled = false;

    public RemoteSTModelHandler(String contextId) {
        this.contextId = contextId;

    }
    @Override
    public boolean addType(String label, List<String> examples) {
        return false;
    }

    @Override
    public List<SemanticTypeLabel> predictType(List<String> examples, int numPredictions) {
        return null;
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
	public void setModelHandlerEnabled(boolean enabled) {
		this.modelEnabled = enabled;

	}

	@Override
	public boolean readModelFromFile(String filepath) {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(contextId);
		contextParameters
			.setParameterValue(ServletContextParameterMap.ContextParameter.SEMTYPE_MODEL_DIRECTORY, filepath);
		return true;
	}
}
