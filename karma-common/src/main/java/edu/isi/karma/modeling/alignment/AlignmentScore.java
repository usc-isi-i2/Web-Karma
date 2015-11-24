package edu.isi.karma.modeling.alignment;

import edu.isi.karma.modeling.alignment.learner.LinkCoherence;
import edu.isi.karma.rep.alignment.LabeledLink;

public class AlignmentScore implements Comparable<AlignmentScore>{

	private LabeledLink link;
	private LinkCoherence linkCoherence;
	private double cost;
	
	public AlignmentScore(LinkCoherence linkCoherence, double cost) {
		this.linkCoherence = new LinkCoherence(linkCoherence);
		this.cost = cost;
	}
	
	public AlignmentScore(LabeledLink link, AlignmentScore a) {
		this.link = link;
		if (a != null && link != null) {
			this.linkCoherence = new LinkCoherence(a.getLinkCoherence());
			this.linkCoherence.updateCoherence(this.link);
			this.cost = a.getCost() + link.getWeight();
		}
	}
	
	public LabeledLink getLink() {
		return link;
	}

	public LinkCoherence getLinkCoherence() {
		return linkCoherence;
	}

	public double getCost() {
		return cost;
	}

	@Override
	public int compareTo(AlignmentScore a) {
		
		int lessThan = 1;
		int greaterThan = -1;
		
		double linkCoherence1 = this.linkCoherence.getCoherenceValue();
		double linkCoherence2 = a.linkCoherence.getCoherenceValue();

		if (linkCoherence1 > linkCoherence2)
			return greaterThan;
		else if (linkCoherence1 < linkCoherence2)
			return lessThan;

		if (this.cost < a.cost)
			return greaterThan;
		else if (a.cost < this.cost)
			return lessThan;
		
		return 0;
		
	}
}
