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
package edu.isi.karma.kr2rml;


public class KR2RMLVersion implements Comparable<KR2RMLVersion>{

	public static final KR2RMLVersion unknown = new KR2RMLVersion(0, 0);
	public static final KR2RMLVersion current = new KR2RMLVersion(1, 7);
	
	public final int major;
	public final int minor;
	public final String value;
	
	public KR2RMLVersion(int major, int minor)
	{
		this.major = major;
		this.minor = minor;
		this.value = major + "." + minor;
	}

	public KR2RMLVersion(String version)
	{
		if(null == version )
		{
			throw new NullPointerException("No version to parse");
		}
		String[] components = version.split("\\.");
		if(components.length < 2)
		{
			throw new IllegalArgumentException("Not enough components in version string " + version);
		}
		major = Integer.parseInt(components[0]);
		minor = Integer.parseInt(components[1]);
		value = version;
	}
	@Override
	public int compareTo(KR2RMLVersion o) {
		
		if(this.getMajor() < o.getMajor())
		{
			return -1;
		}
		if(this.getMajor() == o.getMajor())
		{
			return this.getMinor() - o.getMinor();
		}
		else
		{
			return 1;
		}
	}
	
	public int getMajor()
	{
		return major;
	}
	public int getMinor()
	{
		return minor;
	}
	public String toString()
	{
		return value;
	}
	
	public static KR2RMLVersion getCurrent()
	{
		return current;
	}
}
