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

package edu.isi.karma.modeling.ontology;

import java.util.List;

import edu.isi.karma.rep.alignment.Label;

public class OntologyTreeNode {
	// should change the location of URI class to package karma.modeling 
	private Label label;
	private OntologyTreeNode parent;
	private List<OntologyTreeNode> children;
	
	public OntologyTreeNode(Label label, OntologyTreeNode parent, List<OntologyTreeNode> children) {
		this.label = label;
		this.parent = parent;
		this.children = children;
	}
	
	public Label getLabel() {
		return label;
	}
	public void setLabel(Label label) {
		this.label = label;
	}
	public OntologyTreeNode getParent() {
		return parent;
	}
	public void setParent(OntologyTreeNode parent) {
		this.parent = parent;
	}
	public List<OntologyTreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<OntologyTreeNode> children) {
		this.children = children;
	}

	public boolean hasChildren() {
		return (children != null && !children.isEmpty());
	}
	
	public void print() {
		printRecursively(this, 0);
	}
	
	private void printRecursively(OntologyTreeNode node, int level) {
		for (int i = 0; i < level; i++) System.out.print("---"); System.out.print(" ");
		System.out.println("URI: " + node.getLabel().getUri());
		for (int i = 0; i < level; i++) System.out.print("   "); System.out.print(" ");
		System.out.println("Label: " + node.getLabel().getRdfsLabel());
		for (int i = 0; i < level; i++) System.out.print("   "); System.out.print(" ");
		System.out.println("Comment: " + node.getLabel().getRdfsComment());
		if (node.children == null || node.children.isEmpty())
			return;
		for (OntologyTreeNode child : node.getChildren()) {
			printRecursively(child, level + 1);
		}
	}
}
