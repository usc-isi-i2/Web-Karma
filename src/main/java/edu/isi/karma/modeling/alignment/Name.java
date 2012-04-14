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

public class Name {

	private String uri;
	private String ns;
	private String prefix;
	
	public Name(String uri, String ns, String prefix) {
		this.uri = uri;
		this.ns = ns;
		this.prefix = prefix;
	}

	public Name(Name n) {
		this.uri = n.uri;
		this.ns = n.ns;
		this.prefix = n.prefix;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}


	public void setNs(String ns) {
		this.ns = ns;
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	public String getUri() {
		return uri;
	}

	public String getNs() {
		return ns;
	}

	public String getPrefix() {
		return prefix;
	}
	
	public String getLocalName() {
		if (uri == null)
			return null;
		
		String name = uri;
		if (ns != null)
			name = name.replaceFirst(ns, "");
		
		return name;
	}

	public String getLocalNameWithPrefix() {
		if (uri == null)
			return null;
		
		String name = uri;
		if (ns != null)
			name = name.replaceFirst(ns, "");
		
		if (prefix != null)
			name = prefix + ":" + name;
		
		return name;
	}

}
