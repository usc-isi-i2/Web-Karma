/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.modeling.alignment.learner;


import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.rep.alignment.LabeledLink;

public class SortableSemanticModel_Old extends SemanticModel
	implements Comparable<SortableSemanticModel_Old>{
	
	private double cost;
	private SteinerNodes steinerNodes;
	
	// the number of patterns shared among all the links 
	// private int frequency;
	
//	private class Coherence implements Comparable<Coherence>{
//
//		private int linkCount;
//		private int patternFrequency;
//		
//		public Coherence(int linkCount,int patternFrequency) {
//			this.linkCount = linkCount;
//			this.patternFrequency = patternFrequency;
//		}
//		
//		@Override
//		public int compareTo(Coherence c) {
//			if (c == null)
//				return 1;
//			else if (this.linkCount > c.linkCount)
//				return 1;
//			else if (this.linkCount < c.linkCount)
//				return -1;
//			else if (this.patternFrequency > c.patternFrequency)
//				return 1;
//			else if (this.patternFrequency < c.patternFrequency)
//				return -1;
//			else
//				return 0;
//		}
//	}

//	private List<Integer> cohesion;
	
	public SortableSemanticModel_Old(SemanticModel semanticModel, SteinerNodes steinerNodes) {
		
		super(semanticModel);
		
		this.steinerNodes = steinerNodes;
		
		if (this.graph != null && !this.graph.edgeSet().isEmpty()) {
			this.cost = this.computeCost();
//			this.frequency = this.computeFrequency();
//			this.cohesion = this.computeCohesion();
//			this.coherence = this.computeCoherence();
		}
	}
	
	public SortableSemanticModel_Old(SemanticModel semanticModel) {
		
		super(semanticModel);
		
		if (this.graph != null && !this.graph.edgeSet().isEmpty()) {
			this.cost = this.computeCost();
//			this.frequency = this.computeFrequency();
//			this.cohesion = this.computeCohesion();
//			this.coherence = this.computeCoherence();
		}
	}

	public SemanticModel getBaseModel() {
		return new SemanticModel(this);
	}
	
	public double getCost() {
		return cost;
	}

	public double getScore() {
		return this.steinerNodes.getScore();
	}
	
	public SteinerNodes getSteinerNodes() {
		return this.steinerNodes;
	}
	
//	public String getEdgeCoherenceString() {
//		String s = "";
//		for (Coherence c : this.coherence)
//			s += "(" + c.linkCount + "," + c.patternFrequency + ")";
//		return s;
//	}
	
//	private static double roundTwoDecimals(double d) {
//        DecimalFormat twoDForm = new DecimalFormat("#.##");
//        return Double.valueOf(twoDForm.format(d));
//	}
	
//	public String getDescription() {
//		String s = "";
//		
//		s += "coherence: ";
//		for (CoherenceItem ci : this.getSteinerNodes().getCoherenceList())
//			s += "(" + ci.getX() + "," + ci.getY() + ")";
//		s += " --- ";
//		
//		s += "score: " + this.steinerNodes.getScore();
//		s += " --- ";
//		
//		s += "cost: " + roundTwoDecimals(this.getCost());
//
//		return s;
//	}
	
//	public int getFrequency() {
//		return frequency;
//	}
//
//	public String getCohesionString() {
//		String s = "";
//		for (Integer i : this.cohesion)
//			s += String.valueOf(i);
//		return s;
//	}
	
	private double computeCost() {
		double cost = 0.0;
		for (LabeledLink e : this.graph.edgeSet()) {
			cost += e.getWeight();
		}
		return cost;
	}

//	private int computeFrequency() {
//		
//		Set<String> commonPatterns = null;
//		
//		if (model == null || model.edgeSet().size() == 0)
//			return 0;
//		   
//		boolean firstVisit = true;
//		
//		for (Link e : model.edgeSet()) {
//
//			if (firstVisit) {
//				commonPatterns = e.getPatternIds();
//				firstVisit = false;
//			}
//			else
//				commonPatterns = Sets.intersection(e.getPatternIds(), commonPatterns);
//			
//			if (commonPatterns.size() == 0)
//				return 0;
//		}
//		
//		return commonPatterns.size();
//	}
	
//	private List<Coherence> computeCoherence() {
//		
//		if (this.graph == null || this.graph.edgeSet().size() == 0)
//			return null;
//		  
//		List<Coherence> coherence = new ArrayList<Coherence>();
//
//		List<String> patternIds = new ArrayList<String>();
//		HashMap<String, HashSet<String>> patternToLinks = new HashMap<String, HashSet<String>>();
//		HashMap<String, Integer> patternToFrequency = new HashMap<String, Integer>();
//		
//		for (Link e : this.graph.edgeSet()) 
//			for (String s : e.getModelIds()) {
//				
//				if (!patternIds.contains(s))
//					patternIds.add(s);
//				
//				patternToFrequency.put(s, 1);
//				
//				HashSet<String> links = patternToLinks.get(s);
//				if (links == null) {
//					links = new HashSet<String>();
//					patternToLinks.put(s, links);
//				}
//				links.add(e.getId());
//			}
//		
//		int size1, size2, size3;
//		String p1, p2;
//		for (int i = 0; i < patternIds.size() - 1; i++) {
//			p1 = patternIds.get(i);
//			if (!patternToLinks.containsKey(p1)) continue;
//			size1 = patternToLinks.get(p1).size();
//			for (int j = i + 1; j < patternIds.size(); j++) {
//				p2 = patternIds.get(j);
//				if (!patternToLinks.containsKey(p1)) continue;
//				if (!patternToLinks.containsKey(p2)) continue;
//				size2 = patternToLinks.get(p2).size();
//				
//				Set<String> shared = Sets.intersection(patternToLinks.get(p1), patternToLinks.get(p2));
//				if (shared == null) continue;
//				
//				size3 = shared.size();
//				if (size3 < size2 && size3 < size1) continue;
//				else if (size3 == size1 && size3 < size2) {
//					patternToLinks.remove(p1);
//					patternToFrequency.remove(p1);
//				} else if (size3 == size2 && size3 < size1) { 
//					patternToLinks.remove(p2);
//					patternToFrequency.remove(p2);
//				} else if (size3 == size1 && size3 == size2) {
//					Integer count = patternToFrequency.get(p1);
//					patternToFrequency.put(p1, count + 1);
//					patternToLinks.remove(p2);
//					patternToFrequency.remove(p2);
//				}
//			}
//		}
//		
//		for (Entry<String, HashSet<String>> entry : patternToLinks.entrySet()) {
//			Coherence c = new Coherence(entry.getValue().size(), patternToFrequency.get(entry.getKey()).intValue());
//			coherence.add(c);
//		}
//		
//		Collections.sort(coherence);
//		coherence = Lists.reverse(coherence);
//		
//		return coherence;
//
//	}
	
//	private List<Integer> computeCohesion() {
//		
//		if (model == null || model.edgeSet().size() == 0)
//			return null;
//		  
//		List<String> patternIds = new ArrayList<String>();
//		
//		for (Link e : model.edgeSet()) 
//			for (String s : e.getPatternIds())
//				patternIds.add(s);
//		
//		Function<String, String> stringEqualiy = new Function<String, String>() {
//		  @Override public String apply(final String s) {
//		    return s;
//		  }
//		};
//
//		Multimap<String, String> index =
//				   Multimaps.index(patternIds, stringEqualiy);
//		
//		List<Integer> frequencies = new ArrayList<Integer>();
//		for (String s : index.keySet()) {
//			frequencies.add(index.get(s).size());
//		}
//
//		Collections.sort(frequencies);
//		frequencies = Lists.reverse(frequencies);
//		return frequencies;
//	}

//	private int compareCohesions(List<Integer> c1, List<Integer> c2) {
//		if (c1 == null || c2 == null)
//			return 0;
//		
//		for (int i = 0; i < c1.size(); i++) {
//			if (i < c2.size()) {
//				if (c1.get(i) > c2.get(i)) return 1;
//				else if (c1.get(i) < c2.get(i)) return -1;
//			}
//		}
//		if (c1.size() < c2.size())
//			return 1;
//		else if (c2.size() < c1.size())
//			return -1;
//		else
//			return 0;
//	}
	
//	private int compareCoherence(List<Coherence> c1, List<Coherence> c2) {
//		if (c1 == null || c2 == null)
//			return 0;
//		
//		for (int i = 0; i < c1.size(); i++) {
//			if (i < c2.size()) {
//				if (c1.get(i).compareTo(c2.get(i)) > 0) return 1;
//				else if (c1.get(i).compareTo(c2.get(i)) < 0) return -1;
//			}
//		}
//		if (c1.size() < c2.size())
//			return 1;
//		else if (c2.size() < c1.size())
//			return -1;
//		else
//			return 0;
//	}
	
	@Override
	public int compareTo(SortableSemanticModel_Old m) {
		
		double score1 = this.getScore();
		double score2 = m.getScore();
		if (score1 > score2)
			return -1;
		else if (score1 < score2)
			return 1;
		else if (this.cost < m.cost)
			return -1;
		else if (m.cost < this.cost)
			return 1;
		else {
			return 0;
		}
	}

}
