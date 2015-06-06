package edu.isi.karma.modeling.alignment.learner;

import edu.isi.karma.rep.alignment.Node;

public class NodeCoherence extends Coherence {

	public NodeCoherence() {
		super();
	}
	
	public NodeCoherence(NodeCoherence coherence) {
		super(coherence);
	}
	
	public void updateCoherence(Node node) {
		if (node == null) return;
		this.itemsCount ++;
		if (node.getModelIds() == null || node.getModelIds().isEmpty()) 
			return;
		updateCoherence(node.getModelIds());
	}
	
	public double getCoherenceValue() {

		if (this.itemsCount == 0) {
			return Double.MIN_VALUE;
		}
		
		if (numOfElementsInMaxPatterns > 0)
			return (double)patternSize.get(maxPatterns[0])/(double)this.itemsCount;
		else
			return 0.0;

	}
}
