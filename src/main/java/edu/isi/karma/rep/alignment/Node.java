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

import org.apache.log4j.Logger;

import edu.isi.karma.util.RandomGUID;

public abstract class Node implements Comparable<Node> {

	static Logger logger = Logger.getLogger(Node.class);

	private final String id;
	private final Label label;
	
	public Node(String id, Label label) {
		
		if (id == null || id.trim().length() == 0) {
			logger.info("The input id is empty. A random Guid has been assigned.");
			id = new RandomGUID().toString();
		}
		
		this.id = id;
		this.label = label;
	}
	
	public Node(Node v) {
		this.id = v.id;
		this.label = v.label;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getLocalID() {
		String s = this.id;

		if (this.label != null)
			s = s.replaceAll(this.label.getNs(), "");
		
		return s;
	}
	
	public String getLocalName() {
		if (this.label == null)
			return null;

		String s = this.label.getUriString();
		s = s.replaceAll(this.label.getNs(), "");
		return s;
	}
	
	public String getUriString() {
		return this.label.getUriString();
	}
	
	public String getNs() {
		return this.label.getNs();
	}
	
	public String getPrefix() {
		return this.label.getPrefix();
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Node node = (Node) obj;
        return this.id == node.getID();
    }
    
    @Override
    public int hashCode() {
    	return this.getID().hashCode();
    }

    @Override
    public int compareTo(Node node) {       
        //compare id
        return this.id.compareTo(node.getID());
    }


}
