package edu.isi.karma.kr2rml.affinity;

import edu.isi.karma.rep.HNodePath;

public class ParentRowColumnAffinity implements ColumnAffinity {

	public static final ColumnAffinity INSTANCE = new ParentRowColumnAffinity();
	private int distanceToParent;
	
	private ParentRowColumnAffinity()
	{
		
	}
	private ParentRowColumnAffinity(HNodePath a, HNodePath b)
	{
		setDistanceToParent(b.length() - a.length());
	}
	
	@Override
	public ColumnAffinity generateAffinity(HNodePath a, HNodePath b)
	{
		return new ParentRowColumnAffinity(a, b);
	}
	@Override
	public int compareTo(ColumnAffinity o) {

		if(o instanceof RowColumnAffinity)
		{
			return 1;
		}
		else if(o instanceof ParentRowColumnAffinity)
		{
			ParentRowColumnAffinity p = (ParentRowColumnAffinity) o;
			return getDistanceToParent() - p.getDistanceToParent();
		}
		else
		{
			return -1;
		}
	}

	@Override
	public boolean isValidFor(HNodePath a, HNodePath b) {
		HNodePath commonPath = HNodePath.findCommon(a, b);
		if(commonPath.length() == a.length() && b.length() > a.length())
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isCloserThan(ColumnAffinity otherAffinity) {
		return compareTo(otherAffinity) < 0;
	}

	public int getDistanceToParent() {
		return distanceToParent;
	}

	public void setDistanceToParent(int distanceToParent) {
		this.distanceToParent = distanceToParent;
	}

}
