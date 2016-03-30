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

import java.util.HashMap;

public class LinkIdFactory {

	private HashMap<String, Integer> linksUris = new HashMap<>();
	public static final String separator = "---";

//	public String getLinkId(String uri) {
//		
//		int index;
//		String id;
//		
//		if (linksUris.containsKey(uri)) {
//			
//			index = linksUris.get(uri).intValue();
//			linksUris.put(uri, ++index);
//			id = uri + "" + index;
//			
//		} else {
//			index = 1;
//			linksUris.put(uri, index);
//			id = uri + "" + index;
////			id = uriString;
//		}
//		return id;
//	}
	
	public static String getLinkId(String uri, String sourceId, String targetId) {
		return sourceId + separator + uri + separator + targetId;
	}
	
	public static String getLinkUri(String linkId) {
		String[] parts = linkId.split(separator);
		if (parts != null && parts.length == 3)
			return parts[1];
		else
			return null;
	}
	
	public static String getLinkSourceId(String linkId) {
		String[] parts = linkId.split(separator);
		if (parts != null && parts.length == 3)
			return parts[0];
		else
			return null;
	}
	
	public static String getLinkTargetId(String linkId) {
		String[] parts = linkId.split(separator);
		if (parts != null && parts.length == 3)
			return parts[2];
		else
			return null;
	}
	
	public boolean duplicateUri(String uriString) {
		return this.linksUris.containsKey(uriString);
	}
	
	public int lastIndexOf(String uri) {
		if (linksUris.containsKey(uri))
			return linksUris.get(uri).intValue();
		else
			return -1;
	}
}
