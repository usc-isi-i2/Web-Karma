package edu.isi.karma.kr2rml.affinity;

import edu.isi.karma.rep.HNodePath;

public class NoColumnAffinity implements ColumnAffinity {

	public static NoColumnAffinity INSTANCE = new NoColumnAffinity();
	@Override
	public int compareTo(ColumnAffinity o) {
		return 1;
	}

	@Override
	public boolean isValidFor(HNodePath a, HNodePath b) {
		return true;
	}

	@Override
	public boolean isCloserThan(ColumnAffinity otherAffinity) {
		return false;
	}

	@Override
	public ColumnAffinity generateAffinity(HNodePath a, HNodePath b) {
		return INSTANCE;
	}

}
