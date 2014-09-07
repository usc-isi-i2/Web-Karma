package edu.isi.karma.modeling.alignment.learner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.util.RandomGUID;

public class Coherence {

	private static Logger logger = LoggerFactory.getLogger(Coherence.class);
	private List<CoherenceItem> items;
	private int nodesCount;

	private Map<String, Integer> patternSize;
	private Map<String, String> patternGuid;

	public Coherence() {
		this.items = new ArrayList<>();
		this.nodesCount = 0;
		this.patternSize = new HashMap<String, Integer>();
		this.patternGuid = new HashMap<String, String>();
	}
	
//	public Coherence(int nodesCount) {
//		this.items = new ArrayList<>();
//		this.nodesCount = nodesCount;
//		this.patternSize = new HashMap<String, Integer>();
//		this.patternGuid = new HashMap<String, String>();
//	}
	
	public Coherence(Coherence coherence) {
		this.items = new ArrayList<CoherenceItem>(coherence.getItems());
		this.nodesCount = coherence.nodesCount;
		this.patternSize = new HashMap<String, Integer>(coherence.patternSize);
		this.patternGuid = new HashMap<String, String>(coherence.patternGuid);
	}
	
	public List<CoherenceItem> getItems() {
		return Collections.unmodifiableList(this.items);
	}
	
	public void addItem(CoherenceItem item) {
		if (item != null) {
			this.items.add(item);
		}
	}
	
	public Map<String, Integer> getPatternSize() {
		return patternSize;
	}

	public Map<String, String> getPatternGuid() {
		return patternGuid;
	}

	public List<Integer> getCoherenceList() {
		List<Integer> coherenceList = new ArrayList<Integer>(patternSize.values());
		Collections.sort(coherenceList, Collections.reverseOrder());
		coherenceList = coherenceList.subList(0, Math.min(coherenceList.size(), 5));
		return coherenceList;
	}
	
	public void updateCoherence(Node n) {
		
//		logger.debug("update coherence data ...");
		this.nodesCount ++;
//		System.out.println("=========================" + nodesCount);
		for (String p : n.getModelIds()) {
			Integer size = patternSize.get(p);
			if (size == null) 
				patternSize.put(p, 1);
			else
				patternSize.put(p, ++size);
			
//			if (!patternGuid.containsKey(p)) {
//				String guid = new RandomGUID().toString();
//				patternGuid.put(p, guid);
//			}
		}
	}
	
	public void sortAscending() {
		Collections.sort(items);
	}
	
	public void sortDescending() {
		Collections.sort(items, Collections.reverseOrder());
	}

	public double getCoherenceValue() {
		
		if (this.nodesCount == 0) {
			logger.error("cannot compute coherence when number of nodes is zero!");
			return 0.0;
		}
		
		this.sortDescending();

		BigDecimal value = BigDecimal.ZERO;
		
		BigDecimal denominator = BigDecimal.ONE;
		BigDecimal factor = new BigDecimal(this.nodesCount);
		BigDecimal b;
		
		for (CoherenceItem ci : this.items) {
			
			denominator = denominator.multiply(factor);
			b = new BigDecimal((double)ci.getX());
//			b = new BigDecimal(ci.getDouble());
			b= b.divide(denominator, 5, RoundingMode.HALF_UP);
			value = value.add(b);
		}
		
		return value.doubleValue();
	}
	
	public double getCoherenceValue2() {
		
		if (this.nodesCount == 0) {
			logger.error("cannot compute coherence when number of nodes is zero!");
			return 0.0;
		}
		
		List<Integer> coherenceList = getCoherenceList();

		BigDecimal value = BigDecimal.ZERO;
		
		BigDecimal denominator = BigDecimal.ONE;
		BigDecimal factor = new BigDecimal(this.nodesCount);
		BigDecimal b;
		
		for (Integer i : coherenceList) {
			
			denominator = denominator.multiply(factor);
			b = new BigDecimal((double)i.intValue());
			b= b.divide(denominator, 5, RoundingMode.HALF_UP);
			value = value.add(b);
		}
		
		return value.doubleValue();
	}

	public void computeCoherence(Set<Node> nodes) {
		
		logger.debug("computing coherence ...");
		if (nodes == null || nodes.size() == 0)
			return;

		logger.debug("finding nodes largest patterns ...");
		List<String> nodesLargetsPatterns = new LinkedList<String>();
		int maxSize = 0;
		String listOfMaxSizePatterns = "";
		for (Node n : nodes) {
			for (String p : n.getModelIds()) {
				int size = this.patternSize.get(p).intValue();
				if (size > maxSize) {
					maxSize = size;
					listOfMaxSizePatterns = "";
				} else if (size == maxSize) {
					listOfMaxSizePatterns += this.patternGuid.get(p);
				}
			}
			nodesLargetsPatterns.add(listOfMaxSizePatterns);
		}
		
		logger.debug("grouping coherence patterns ...");
		Function<String, String> stringEqualiy = new Function<String, String>() {
			  @Override public String apply(final String s) {
			    return s;
			  }
			};
				
		Multimap<String, String> index =
			Multimaps.index(nodesLargetsPatterns, stringEqualiy);
		
		int x, y;
		int guidSize = new RandomGUID().toString().length();
		this.items = new ArrayList<>();
		this.nodesCount = nodes.size();
		for (String s : index.keySet()) {
			if (s.trim().length() == 0)
				continue;
			x = index.get(s).size();
			y = x > 0 ? index.get(s).iterator().next().length() / guidSize : 0; 
			CoherenceItem ci = new CoherenceItem(x, y);
			this.addItem(ci);
		}
		
	}
}
