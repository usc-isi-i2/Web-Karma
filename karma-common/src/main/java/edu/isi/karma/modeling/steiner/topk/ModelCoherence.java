package edu.isi.karma.modeling.steiner.topk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelCoherence {

	private ModelFrequencyPair[] modelFrequency;
	private Map<String,Integer> modelIndex;
	private List<ModelFrequencyPair> topKModels;
	private int k;
	private Set<String> visitedLinks;

	public ModelCoherence(int size, int k) {
		this.k = k;
		this.modelFrequency = new ModelFrequencyPair[size];
		this.modelIndex = new HashMap<>();
		this.topKModels = new ArrayList<>();
		this.visitedLinks = new HashSet<>();
	}

	public void update(SteinerEdge e) {
		Integer index;
		Set<String> modelIds = e.getModelIds();
		if (this.visitedLinks.contains(e.label().name())) return;
		else this.visitedLinks.add(e.label().name());
		if (modelIds == null) return;
		for (String id : modelIds) {
			index = modelIndex.get(id);
			if (index == null) {
				int pos = modelIndex.size();
				if (pos < modelFrequency.length) {
					modelIndex.put(id, pos);
					modelFrequency[pos] = new ModelFrequencyPair(id, 1);
				}
			} else {
				ModelFrequencyPair mfp = modelFrequency[index.intValue()];
				mfp.setFrequency(mfp.getFrequency() + 1);
			}
		}
		computeTopK();
	}
	
	private void computeTopK() {
		this.topKModels.clear();
		if (this.k >= modelIndex.size()) {
			for (ModelFrequencyPair m : this.modelFrequency) {
				if (m != null) {
					this.topKModels.add(m);
				}
			}
		} else {
			ModelFrequencyPair[] copy = Arrays.copyOf(this.modelFrequency, this.modelIndex.size());
			ModelFrequencyPair kth = QuickSelect.select(copy, k);
			if (kth != null) {
				for (ModelFrequencyPair m : this.modelFrequency) {
					if (m != null) {
						if (m.compareTo(kth) >= 0 && this.topKModels.size() < k) {
							this.topKModels.add(m);
						}
					}
				}
			}
		}
		Collections.sort(this.topKModels);
	}

	public List<ModelFrequencyPair> getTopKModels() {
		return topKModels;
	}
	
	public Set<String> getVisitedLinks() {
		return visitedLinks;
	}

	public void print() {
		System.out.print("model coherence: ");
		for (ModelFrequencyPair m : this.topKModels) {
			System.out.print(m.getFrequency() + ", ");
		}
		System.out.println();
	}

}
