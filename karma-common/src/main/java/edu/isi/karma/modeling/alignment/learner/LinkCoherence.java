package edu.isi.karma.modeling.alignment.learner;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSemanticTypeStatus;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;

public class LinkCoherence extends Coherence {
	
	private boolean interactiveMode;

	public LinkCoherence() {
		super();
		this.interactiveMode = true;
	}
	
	public LinkCoherence(boolean interactiveMode) {
		super();
		this.interactiveMode = interactiveMode;
	}
	
	public LinkCoherence(LinkCoherence coherence) {
		super(coherence);
	}
	
	public void updateCoherence(LabeledLink link) {
		if (link == null) return;
		
		if (interactiveMode) {
			
			if (link.getStatus() == LinkStatus.ForcedByUser)
				return;
		
			if (link.getTarget() instanceof ColumnNode) {
				ColumnNode cn = (ColumnNode)link.getTarget();
				if (cn.getSemanticTypeStatus() == ColumnSemanticTypeStatus.UserAssigned)
					return;
			}
		} else {

			if (!(link.getTarget() instanceof InternalNode))
				return;

		}

		this.itemsCount ++;
		if (link.getModelIds() == null || link.getModelIds().isEmpty()) 
			return;
		updateCoherence(link.getModelIds());
	}
	
	public void updateCoherence(DirectedWeightedMultigraph<Node, LabeledLink> model, LabeledLink link) {
		if (link == null) return;
		
		if (interactiveMode) {
			
			if (link.getStatus() == LinkStatus.ForcedByUser)
				return;
		
			if (link.getTarget() instanceof ColumnNode) {
				ColumnNode cn = (ColumnNode)link.getTarget();
				if (cn.getSemanticTypeStatus() == ColumnSemanticTypeStatus.UserAssigned)
					return;
			}
		} else {

			if (!(link.getTarget() instanceof InternalNode))
				return;

		}

		this.itemsCount ++;
		// internal nodes that do not reach to a column node in the model
		if (link.getTarget() instanceof InternalNode) {
			InternalNode in = (InternalNode)link.getTarget();
			if (model.outgoingEdgesOf(in) == null || model.outgoingEdgesOf(in).isEmpty()) {
				this.itemsCount += model.edgeSet().size();
				return;
			}
		}
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
	
	public Integer[] getCoherencePair() { // <number of pattern links, total links>

		if (this.itemsCount == 0) {
			return new Integer[]{0,0};
		}
		
		if (numOfElementsInMaxPatterns > 0)
			return new Integer[]{patternSize.get(maxPatterns[0]),this.itemsCount};
		else
			return new Integer[]{0,this.itemsCount};

	}
}
