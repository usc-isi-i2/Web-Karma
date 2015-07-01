package edu.isi.karma.research.modeling;

import java.util.Comparator;

import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.rep.alignment.LabeledLink;

public class LOD_SemanticModelComparator implements Comparator<SemanticModel> {
	
	private double computeCost(SemanticModel model) {
		
		if (model == null || 
				model.getGraph() == null || 
				model.getGraph().edgeSet().size() == 0)
			return 0.0;

		double cost = 0.0;
		for (LabeledLink e : model.getGraph().edgeSet()) {
			cost += e.getWeight();
		}
		return cost;
	}
	
//	private List<Integer> computeCoherence(SemanticModel model) {
//
//		if (model == null || model.getGraph() == null || model.getGraph().edgeSet().size() == 0)
//			return null;
//
//		List<String> patternIds = new ArrayList<String>();
//
//		for (LabeledLink e : model.getGraph().edgeSet()) {
//			for (String s : e.getModelIds()) {
//				patternIds.add(s);
//			}
//		}
//
//		Function<String, String> stringEqualiy = new Function<String, String>() {
//			@Override public String apply(final String s) {
//				return s;
//			}
//		};
//
//		Multimap<String, String> index =
//				Multimaps.index(patternIds, stringEqualiy);
//
//		List<Integer> frequencies = new LinkedList<Integer>();
//		for (String s : index.keySet()) {
//			frequencies.add(index.get(s).size());
//		}
//
//		Collections.sort(frequencies);
//		frequencies = Lists.reverse(frequencies);
//		return frequencies;
//	}
//
//	private int compareCoherence(List<Integer> c1, List<Integer> c2) {
//		if (c1 == null || c2 == null)
//			return 0;
//
//		for (int i = 0; i < c1.size(); i++) {
//			if (i < c2.size()) {
//				if (c1.get(i) > c2.get(i)) return -1;
//				else if (c1.get(i) < c2.get(i)) return +1;
//			}
//		}
//		if (c1.size() < c2.size())
//			return -1;
//		else if (c2.size() < c1.size())
//			return +1;
//		else
//			return 0;
//	}
	
	@Override
	public int compare(SemanticModel m1, SemanticModel m2) {
		
//		List<Integer> m1Coherence = computeCoherence(m1);
//		List<Integer> m2Coherence = computeCoherence(m2);
//		int coherence = compareCoherence(m1Coherence, m2Coherence);
//		if (coherence != 0)
//			return coherence;

		double m1Cost = computeCost(m1);
		double m2Cost = computeCost(m2);
		
		if (m1Cost < m2Cost)
				return -1;
		else if (m1Cost > m2Cost)
				return +1;

		return 0;
	}

}
