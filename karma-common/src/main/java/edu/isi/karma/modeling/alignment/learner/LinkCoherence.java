package edu.isi.karma.modeling.alignment.learner;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSemanticTypeStatus;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;

public class LinkCoherence extends Coherence {

	public LinkCoherence() {
		super();
	}
	
	public LinkCoherence(LinkCoherence coherence) {
		super(coherence);
	}
	
//	public void updateCoherence(DirectedWeightedMultigraph<Node, LabeledLink> model, LabeledLink link) {
	public void updateCoherence(LabeledLink link) {
		if (link == null) return;
		
		if (link.getStatus() == LinkStatus.ForcedByUser)
			return;
		
//		if (!(link.getTarget() instanceof InternalNode))
//				return;
		if (link.getTarget() instanceof ColumnNode) {
			ColumnNode cn = (ColumnNode)link.getTarget();
			if (cn.getSemanticTypeStatus() == ColumnSemanticTypeStatus.UserAssigned)
				return;
		}
		this.itemsCount ++;
		// don't remember why I skipped the nodes that don't have any outgoing link.
//		if (link.getTarget() instanceof InternalNode) {
//			InternalNode in = (InternalNode)link.getTarget();	
//			if (model.outgoingEdgesOf(in) == null || model.outgoingEdgesOf(in).isEmpty())
//				return;
//		}
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
