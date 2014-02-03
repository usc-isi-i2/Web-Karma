/*******************************************************************************
 * Copyright 2012 University of Southern California
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

package edu.isi.karma.modeling.alignment;

import edu.isi.karma.modeling.ModelingParams;

public class LinkFrequency implements Comparable<LinkFrequency> {
	
	public LinkFrequency(String linkUri, int type, int count) {
		this.linkUri = linkUri;
		this.type = type;
		this.count = count;
	}
	
	private String linkUri;
	private int type;
	private int count;
	
	
	public String getLinkUri() {
		return linkUri;
	}

	public double getWeight() {
		
		double weight = 0.0;
		double w = ModelingParams.PROPERTY_DIRECT_WEIGHT;
		double epsilon = ModelingParams.PATTERN_LINK_WEIGHT;
//		double factor = 0.01;
		int c = this.count < (int)w ? this.count : (int)w - 1;
		
		if (type == 1) // match domain, link, and range
			weight = w - (epsilon / (w - c));
		else if (type == 2) // match link and range
			weight = w - (epsilon / ((w - c) * w));
		else if (type == 3) // match domain and link
			weight = w - (epsilon / ((w - c) * w));
		else if (type == 4) // match link
			weight = w - (epsilon / ((w - c) * w * w));
		else if (type == 5) // direct property
			weight = w;
		else if (type == 6) // indirect property
			weight = w + epsilon - (epsilon / (w - c));
		else if (type == 7) // property with only domain
			weight = w + epsilon + (epsilon / ((w - c) * w));
		else if (type == 8) // property with only range
			weight = w + epsilon + (epsilon / ((w - c) * w));
		else if (type == 9) // subClass
			weight = w + epsilon + (epsilon / ((w - c) * w * w));
		else if (type == 10) // property without domain and range
			weight = w + epsilon + (epsilon / ((w - c) * w * w * w));
		return weight;
	}
	
	@Override
	public int compareTo(LinkFrequency o) {
		if (linkUri == null && o.linkUri != null)
			return -1;
		else if (linkUri != null && o.linkUri == null)
			return 1;
		else if (linkUri == null && o.linkUri == null)
			return 0;
		else {
			if (type < o.type)
				return 1;
			else if (type > o.type)
				return -1;
			else {
				if (count >= o.count) 
					return 1;
				else 
					return -1;
			}
		}
	}
}
