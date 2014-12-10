/*******************************************************************************
 * Copyright 2014 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
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
		if(commonPath == null || a == null || b == null)
			return false;
		
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
