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

package edu.isi.karma.rep.alignment;

import java.util.Comparator;

public class LinkPriorityComparator implements Comparator<LabeledLink> {

	@Override
	public int compare(LabeledLink o1, LabeledLink o2) {
		String p1 = getPriority(o1);
		String p2 = getPriority(o2);
		return p1.compareTo(p2);
	}

	private String getPriority(LabeledLink link) {
		if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.Direct) return "0";
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.Indirect) return "1";
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithOnlyDomain) return "2";
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithOnlyRange) return "2";
		else if (link instanceof ObjectPropertyLink && ((ObjectPropertyLink)link).getObjectPropertyType() == ObjectPropertyType.WithoutDomainAndRange) return "3";
		else if (link instanceof SubClassLink) return "4";
		else return "5";
	}

}
