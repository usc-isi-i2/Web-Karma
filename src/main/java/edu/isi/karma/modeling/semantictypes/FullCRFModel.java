package edu.isi.karma.modeling.semantictypes;

import java.util.HashMap;

public class FullCRFModel {
	/**
	 * Keeps a map between the column HNodeIds and the corresponding column
	 * model
	 */
	HashMap<String, CRFColumnModel> columnModelMap = new HashMap<String, CRFColumnModel>();

	public void addColumnModel(String nodeId, CRFColumnModel columnModel) {
		columnModelMap.put(nodeId, columnModel);
	}

	public CRFColumnModel getModelByColumnName(String name) {
		// TODO Yet to be implemented
		return null;

	}

	public CRFColumnModel getModelByHNodeId(String hNodeId) {
		return columnModelMap.get(hNodeId);
	}
}
