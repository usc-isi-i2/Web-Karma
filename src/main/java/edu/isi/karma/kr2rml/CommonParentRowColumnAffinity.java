package edu.isi.karma.kr2rml;

import edu.isi.karma.rep.HNodePath;

public class CommonParentRowColumnAffinity implements ColumnAffinity {

	public static final ColumnAffinity INSTANCE = new CommonParentRowColumnAffinity();
	private int distanceToParent;
	private CommonParentRowColumnAffinity()
	{
		
	}
	private CommonParentRowColumnAffinity(HNodePath a, HNodePath b)
	{
		distanceToParent = a.length() - HNodePath.findCommon(a, b).length();
	}
	
	@Override
	public ColumnAffinity generateAffinity(HNodePath a, HNodePath b)
	{
		return new CommonParentRowColumnAffinity(a, b);
	}
	@Override
	public int compareTo(ColumnAffinity o) {
		if(o instanceof NoColumnAffinity)
		{
			return -1;
		}
		else if(o instanceof CommonParentRowColumnAffinity)
		{
			CommonParentRowColumnAffinity c = (CommonParentRowColumnAffinity) o;
			return this.distanceToParent - c.getDistanceToParent();
		}
		return 1;
	}

	@Override
	public boolean isValidFor(HNodePath a, HNodePath b) {
		return (!HNodePath.findCommon(a, b).isEmpty());
	}

	@Override
	public boolean isCloserThan(ColumnAffinity otherAffinity) {
		
		return compareTo(otherAffinity) < 0;
	}

	public int getDistanceToParent()
	{
		return distanceToParent;
	}
	
	
}
