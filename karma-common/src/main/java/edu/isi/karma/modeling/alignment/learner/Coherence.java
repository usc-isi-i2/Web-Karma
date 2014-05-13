package edu.isi.karma.modeling.alignment.learner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Coherence {

	private static Logger logger = LoggerFactory.getLogger(Coherence.class);
	private List<CoherenceItem> items;
	private int nodesCount;

	public Coherence() {
		this.items = new ArrayList<>();
		this.nodesCount = 0;
	}
	
	public Coherence(int nodesCount) {
		this.items = new ArrayList<>();
		this.nodesCount = nodesCount;
	}
	
	public Coherence(Coherence coherence) {
		this.items = new ArrayList<CoherenceItem>(coherence.getItems());
		this.nodesCount = coherence.nodesCount;
	}
	
	public List<CoherenceItem> getItems() {
		return Collections.unmodifiableList(this.items);
	}
	
	public void addItem(CoherenceItem item) {
		if (item != null) {
			this.items.add(item);
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
}
