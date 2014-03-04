package edu.isi.karma.kr2rml;

import edu.isi.karma.rep.HNodePath;

public class RowColumnAffinity implements ColumnAffinity {

	public final static ColumnAffinity INSTANCE = new RowColumnAffinity();
	@Override
	public int compareTo(ColumnAffinity o) {
		
		return -1;
	}

	@Override
	public boolean isValidFor(HNodePath a, HNodePath b) {
		HNodePath commonPath = HNodePath.findCommon(a, b);
		if(commonPath == null)
			return false;
		int commonPathLength = commonPath.length();
		return commonPathLength +1 == a.length() && commonPathLength +1 == b.length();
	}

	@Override
	public boolean isCloserThan(ColumnAffinity otherAffinity) {
		return true;
	}

	@Override
	public ColumnAffinity generateAffinity(HNodePath a, HNodePath b) {
		return INSTANCE;
	}

}
