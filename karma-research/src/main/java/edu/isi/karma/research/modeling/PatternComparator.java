package edu.isi.karma.research.modeling;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class PatternComparator implements Comparator<Pattern> {

	private List<String> types;

	public PatternComparator() {
	}
	
	public PatternComparator(List<String> types) {
		this.types = types;
	}
	
	@Override
	public int compare(Pattern p1, Pattern p2) {
		
		if (types != null) {
			Multiset<String> sourceTypes = HashMultiset.create(this.types);
			Multiset<String> p1Types = HashMultiset.create(p1.getTypes());
			Multiset<String> p2Types = HashMultiset.create(p2.getTypes());
			
			int a = Multisets.intersection(sourceTypes, p1Types).size();
			int b = Multisets.intersection(sourceTypes, p2Types).size();

			// pattern with more common type has higher priority
			if (a > b) 
				return -1; 
			else if (a < b) 
				return 1; 
		}

		if (p1.getLength() < p2.getLength()) // prefer more concise models 
			return -1;
		else if (p1.getLength() > p2.getLength()) 
			return 1;
		else {
			if (p1.getFrequency() > p2.getFrequency())  // prefer more frequent patterns
				return -1;
			else if (p1.getFrequency() < p2.getFrequency()) 
				return 1;
			else 
				return 0;
		}
	}

}
