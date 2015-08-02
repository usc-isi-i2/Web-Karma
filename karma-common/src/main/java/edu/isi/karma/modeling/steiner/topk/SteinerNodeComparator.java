package edu.isi.karma.modeling.steiner.topk;

import java.util.Comparator;
import java.util.Set;

public class SteinerNodeComparator implements Comparator<SteinerNode>{

	public static double distanceDifference = 5.0;
	
	ModelCoherence modelCoherence;
	public SteinerNodeComparator(ModelCoherence m) {
		this.modelCoherence = m;
	}
	@Override
	public int compare(SteinerNode n1, SteinerNode n2) {
		
		// prefer forced links
		if (n1.predecessorLink != null && n1.predecessorLink.isForced() 
				&& (n2.predecessorLink == null || !n2.predecessorLink.isForced()))
			return -1;
		else if (n2.predecessorLink != null && n2.predecessorLink.isForced() 
				&& (n1.predecessorLink == null || !n1.predecessorLink.isForced()))
			return 1;
		
//		else if (n1.predecessorLink != null && this.modelCoherence.getVisitedLinks().contains(n1.predecessorLink.label().name()) 
//				&& n2.predecessorLink != null && !this.modelCoherence.getVisitedLinks().contains(n2.predecessorLink.label().name()))
//			return -1;
//		else if (n2.predecessorLink != null && this.modelCoherence.getVisitedLinks().contains(n2.predecessorLink.label().name()) 
//				&& n1.predecessorLink != null && !this.modelCoherence.getVisitedLinks().contains(n1.predecessorLink.label().name()))
//			return -1;
		
		else if (Math.abs(n1.distancesToSources[0] - n2.distancesToSources[0]) <= distanceDifference)
			return compareModelIds(n1, n2);
		else
			return compareDistances(n1, n2);
	}
	
	public int compareModelIds(SteinerNode n1, SteinerNode n2) {
		
		int lessThan = -1;
		int greaterThan = 1;
		
		if (this.modelCoherence.getTopKModels().isEmpty()) {
			return compareDistances(n1, n2);
		}

		if (n1.predecessorLink == null && n2.predecessorLink == null) 
			return compareDistances(n1, n2);
		
		if (n1.predecessorLink == null)
			return lessThan;
		
		if (n2.predecessorLink == null)
			return greaterThan;
		
		if ((n1.predecessorLink.getModelIds() == null || n1.predecessorLink.getModelIds().isEmpty()) && 
				(n2.predecessorLink.getModelIds() == null || n2.predecessorLink.getModelIds().isEmpty()))
			return compareDistances(n1, n2);
		
		if ((n1.predecessorLink.getModelIds() == null || n1.predecessorLink.getModelIds().isEmpty()))
			return greaterThan;
		
		if ((n2.predecessorLink.getModelIds() == null || n2.predecessorLink.getModelIds().isEmpty()))
			return lessThan;
		
		Set<String> n1Ids = n1.predecessorLink.getModelIds();
		Set<String> n2Ids = n2.predecessorLink.getModelIds();
		
		boolean existIn1, existIn2;
		for (ModelFrequencyPair m : this.modelCoherence.getTopKModels()) {
			existIn1 = n1Ids.contains(m.getId()); 
			existIn2 = n2Ids.contains(m.getId()); 
			if (existIn1 && !existIn2)
				return lessThan;
			else if (!existIn1 && existIn2)
				return greaterThan;
		}
		
		return compareDistances(n1, n2);
	}

	public int compareDistances(SteinerNode n1, SteinerNode n2) {
		
		if(n1.distancesToSources[0]>n2.distancesToSources[0]) return 1;
		else if(n1.distancesToSources[0]<n2.distancesToSources[0])return -1;
		else return 0;

	}
}
