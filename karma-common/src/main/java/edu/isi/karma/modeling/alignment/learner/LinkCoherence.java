package edu.isi.karma.modeling.alignment.learner;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.LabeledLink;

public class LinkCoherence extends Coherence {

	public LinkCoherence() {
		super();
	}
	
	public LinkCoherence(LinkCoherence coherence) {
		super(coherence);
	}
	
	public void updateCoherence(LabeledLink link) {
		if (link == null) return;
//		if (!(link.getTarget() instanceof InternalNode))
//				return;
		if (link.getTarget() instanceof ColumnNode) {
			ColumnNode cn = (ColumnNode)link.getTarget();
			if (cn.hasUserType())
				return;
		}
		this.itemsCount ++;
		if (link.getModelIds() == null || link.getModelIds().isEmpty()) 
			return;
		updateCoherence(link.getModelIds());
	}
	
	public double getCoherenceValue() {

		if (this.itemsCount == 0) {
			return 1.0;
		}
		
		if (numOfElementsInMaxPatterns > 0)
			return (double)patternSize.get(maxPatterns[0])/(double)this.itemsCount;
		else
			return 0.0;

	}
}
