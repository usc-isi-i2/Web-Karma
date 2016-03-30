package edu.isi.karma.modeling.alignment.learner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Coherence {

//	private static Logger logger = LoggerFactory.getLogger(Coherence.class);
	protected List<CoherenceItem> items;
	protected int itemsCount;

	protected static int NUM_OF_MAX_SIZE_PATTERNS = 50;
	protected Map<String, Integer> patternSize;
	protected Map<String, String> patternGuid;
	protected String[] maxPatterns;
	protected int numOfElementsInMaxPatterns;
	protected HashMap<String, Integer> patternIndex;
	
	public Coherence() {
		this.items = new ArrayList<>();
		this.itemsCount = 0;
		this.patternSize = new HashMap<>();
		this.patternGuid = new HashMap<>();
		this.maxPatterns = new String[NUM_OF_MAX_SIZE_PATTERNS];
		this.numOfElementsInMaxPatterns = 0;
		this.patternIndex = new HashMap<>();
	}
	
	public Coherence(Coherence coherence) {
		this.items = new ArrayList<>(coherence.getItems());
		this.itemsCount = coherence.itemsCount;
		this.patternSize = new HashMap<>(coherence.patternSize);
		this.patternGuid = new HashMap<>(coherence.patternGuid);
		this.maxPatterns = coherence.maxPatterns.clone();
		this.numOfElementsInMaxPatterns = coherence.numOfElementsInMaxPatterns;
		this.patternIndex = new HashMap<>(coherence.patternIndex);
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
		List<Integer> coherenceList = new ArrayList<>(patternSize.values());
		Collections.sort(coherenceList, Collections.reverseOrder());
		coherenceList = coherenceList.subList(0, Math.min(coherenceList.size(), 5));
		return coherenceList;
	}
	
	protected void updateCoherence(Set<String> modelIds) {
		
		if (modelIds == null || modelIds.isEmpty())
			return;
		
//		this.itemsCount ++;

//		logger.debug("update coherence data ...");
//		System.out.println("=========================" + nodesCount);
//		System.out.println("=========================" + n.getModelIds() != null ? n.getModelIds().size() : "null");
		Integer index;
		for (String p : modelIds) {
			Integer size = patternSize.get(p);
			if (size == null) size = 0;
			patternSize.put(p, ++size);
			
			index = patternIndex.get(p);
			if (index != null) {
				int i = index.intValue();
				while (i > 0 && size > patternSize.get(maxPatterns[i - 1])) {
					maxPatterns[i] = maxPatterns[i-1];
					patternIndex.put(maxPatterns[i-1], i);
					i--;
				}
				maxPatterns[i] = p;
				patternIndex.put(p, i);
			} else if (numOfElementsInMaxPatterns < NUM_OF_MAX_SIZE_PATTERNS) {
				int i = 0;
				while (i < numOfElementsInMaxPatterns && size <= patternSize.get(maxPatterns[i]) )
					i++;
				for (int j = numOfElementsInMaxPatterns; j > i; j--) {
					maxPatterns[j] = maxPatterns[j-1];
					patternIndex.put(maxPatterns[j-1], j);
				}
				maxPatterns[i] = p;
				patternIndex.put(p, i);
				numOfElementsInMaxPatterns ++;
			} else if (maxPatterns[NUM_OF_MAX_SIZE_PATTERNS - 1] != null && size > patternSize.get(maxPatterns[NUM_OF_MAX_SIZE_PATTERNS - 1])) {
					int i = numOfElementsInMaxPatterns - 1;
					patternIndex.remove(maxPatterns[i]);
					while (i > 0 && size > patternSize.get(maxPatterns[i])) {
						maxPatterns[i] = maxPatterns[i-1];
						patternIndex.put(maxPatterns[i-1], i);
						i--;
					}
					maxPatterns[i] = p;
					patternIndex.put(p, i);
			}
			
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

//	public double getCoherenceValue() {
//		
//		if (this.itemsCount == 0) {
//			logger.error("cannot compute coherence when number of nodes is zero!");
//			return 0.0;
//		}
//		
//		this.sortDescending();
//
//		BigDecimal value = BigDecimal.ZERO;
//		
//		BigDecimal denominator = BigDecimal.ONE;
//		BigDecimal factor = new BigDecimal(this.itemsCount);
//		BigDecimal b;
//		
//		for (CoherenceItem ci : this.items) {
//			
//			denominator = denominator.multiply(factor);
//			b = new BigDecimal((double)ci.getX());
////			b = new BigDecimal(ci.getDouble());
//			b= b.divide(denominator, 5, RoundingMode.HALF_UP);
//			value = value.add(b);
//		}
//		
//		return value.doubleValue();
//	}

	public abstract double getCoherenceValue();


//	public void computeCoherence(Set<Node> nodes) {
//		
//		logger.debug("computing coherence ...");
//		if (nodes == null || nodes.size() == 0)
//			return;
//
//		logger.debug("finding nodes largest patterns ...");
//		List<String> nodesLargetsPatterns = new LinkedList<String>();
//		int maxSize = 0;
//		String listOfMaxSizePatterns = "";
//		for (Node n : nodes) {
//			for (String p : n.getModelIds()) {
//				int size = this.patternSize.get(p).intValue();
//				if (size > maxSize) {
//					maxSize = size;
//					listOfMaxSizePatterns = "";
//				} else if (size == maxSize) {
//					listOfMaxSizePatterns += this.patternGuid.get(p);
//				}
//			}
//			nodesLargetsPatterns.add(listOfMaxSizePatterns);
//		}
//		
//		logger.debug("grouping coherence patterns ...");
//		Function<String, String> stringEqualiy = new Function<String, String>() {
//			  @Override public String apply(final String s) {
//			    return s;
//			  }
//			};
//				
//		Multimap<String, String> index =
//			Multimaps.index(nodesLargetsPatterns, stringEqualiy);
//		
//		int x, y;
//		int guidSize = new RandomGUID().toString().length();
//		this.items = new ArrayList<>();
//		this.itemsCount = nodes.size();
//		for (String s : index.keySet()) {
//			if (s.trim().length() == 0)
//				continue;
//			x = index.get(s).size();
//			y = x > 0 ? index.get(s).iterator().next().length() / guidSize : 0; 
//			CoherenceItem ci = new CoherenceItem(x, y);
//			this.addItem(ci);
//		}
//		
//	}
	
	public String printCoherenceList() {
		String s = "(";
		for (int i = 0; i < numOfElementsInMaxPatterns; i++) {
			s += patternSize.get(maxPatterns[i]);
			s += ",";
		}
		s += ")";
		return s;
	}
}
