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

public class URI {

	private String uriString;
	private String ns;
	private String prefix;
	
	public URI(String uri, String ns, String prefix) {
		this.uriString = uri;
		this.ns = ns;
		this.prefix = prefix;
	}

	
	public URI(String uri) {
		this.uriString = uri;
		this.ns = "";
		this.prefix = "";
	}
	
	public URI(URI uri) {
		this.uriString = uri.getUriString();
		this.ns = uri.getNs();
		this.prefix = uri.getPrefix();
	}
	
	public void setUriString(String uri) {
		this.uriString = uri;
	}


	public void setNs(String ns) {
		this.ns = ns;
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	public String getUriString() {
		return uriString;
	}

	public String getNs() {
		return ns;
	}

	public String getPrefix() {
		return prefix;
	}
	
	public String getLocalName() {
		if (uriString == null)
			return null;
		
		String name = uriString;
		if (ns != null)
			name = name.replaceFirst(ns, "");
		
		return name;
	}

	public String getLocalNameWithPrefix() {
		if (uriString == null)
			return null;
		
		String name = uriString;
		if (ns != null && prefix != null)
			name = name.replaceFirst(ns, "");
		
		if (prefix != null)
			name = prefix + ":" + name;
		
		return name;
	}

}
