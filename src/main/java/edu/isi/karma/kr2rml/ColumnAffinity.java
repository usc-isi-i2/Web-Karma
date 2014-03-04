package edu.isi.karma.kr2rml;

import edu.isi.karma.rep.HNodePath;

public interface ColumnAffinity extends Comparable<ColumnAffinity>{

	public boolean isValidFor(HNodePath a, HNodePath b);
	
	public boolean isCloserThan(ColumnAffinity otherAffinity);

	public ColumnAffinity generateAffinity(HNodePath a, HNodePath b);
}
