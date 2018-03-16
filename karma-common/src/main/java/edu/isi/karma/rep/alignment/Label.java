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

import java.io.Serializable;

public class Label implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String uri;
	private String ns;
	private String prefix;
	private String rdfsLabel;
	private String rdfsComment;
	
	public Label(String uri, String ns, String prefix, String rdfsLabel, String rdfsComment) {
		this.uri = uri;
		this.ns = ns;
		this.prefix = prefix;
		this.rdfsLabel = rdfsLabel;
		this.rdfsComment = rdfsComment;
	}
	
	public Label(String uri, String ns, String prefix) {
		this.init();
		this.uri = uri;
		this.ns = ns;
		this.prefix = prefix;

	}

	public Label(String uri) {
		this.init();
		this.uri = uri;
	}
	
	public Label(Label uri) {
		if (uri == null) this.init();
		else {
			this.uri = uri.getUri();
			this.ns = uri.getNs();
			this.prefix = uri.getPrefix();
			this.rdfsLabel = uri.getRdfsLabel();
			this.rdfsComment = uri.getRdfsComment();
		}
	}
	
	private void init() {
		this.uri = null;
		this.ns = null;
		this.prefix = null;
		this.rdfsLabel = null;
		this.rdfsComment = null;
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

	public void setRdfsLabel(String label) {
		this.rdfsLabel = label;
	}
	
	public void setRdfsComment(String comment) {
		this.rdfsComment = comment;
	}
	
	public String getUri() {
		return uri;
	}

	public String getNs() {
		if (ns == null || ns.trim().length() == 0)
			return null;		
		
		return ns;
	}

	public String getPrefix() {
		if (prefix == null || prefix.trim().length() == 0)
			return null;
		
		return prefix;
	}
	
	public String getLocalName() {
		if (uri == null)
			return null;
		
		String localName = uri;
		String ns = getNs();
		if (ns != null && !ns.equalsIgnoreCase(localName))
			localName = localName.replaceFirst(ns, "");
		
		return localName;
	}

	public String getDisplayName() {
		String name;
		if (getPrefix() == null) {
			name = getUri();
		} else
			name = prefix + ":" + getLocalName();
		if(name == null || name.length() == 0)
			name = uri;
		return name;
	}

	public String getRdfsLabel() {
		return rdfsLabel;
	}

	public String getRdfsComment() {
		return rdfsComment;
	}

	@Override
	public String toString() {
		return this.getUri() + "," + this.getDisplayName();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Label) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}
}
