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

package edu.isi.karma.kr2rml;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.kr2rml.template.TemplateTermSet;

public class SubjectMap extends TermMap {
	
	private NamedGraph graph;
	private List<TemplateTermSet> rdfsTypes;
	private boolean isBlankNode;
	private boolean isSteinerTreeRootNode;
	
	public SubjectMap(String id) {
		super(id);
		this.template = new TemplateTermSet();
		this.rdfsTypes = new ArrayList<>();
		this.isBlankNode = false;
		this.setAsSteinerTreeRootNode(false);
	}
	
	public SubjectMap(String id, TemplateTermSet template, NamedGraph graph, 
			List<TemplateTermSet> rdfsType) {
		super(id);
		this.template = template;
		this.graph = graph;
		this.rdfsTypes = rdfsType;
	}
	
	public void setAsBlankNode(boolean flag) {
		this.isBlankNode = flag;
	}
	
	public NamedGraph getGraph() {
		return graph;
	}
	
	public void setGraph(NamedGraph graph) {
		this.graph = graph;
	}
	
	public void addRdfsType(TemplateTermSet type) {
		rdfsTypes.add(type);
	}
	
	public List<TemplateTermSet> getRdfsType() {
		return rdfsTypes;
	}
	
	public void setRdfsType(List<TemplateTermSet> rdfsType) {
		this.rdfsTypes = rdfsType;
	}

	@Override
	public String toString() {
		return "SubjectMap [\n" +
				"\t\t\ttemplate=" + template + ",\n" +
				"\t\t\tgraph=" + graph + ",\n" +
				"\t\t\trdfsTypes=" + rdfsTypes + ",\n" +
				"\t\t\tisBlankNode=" +isBlankNode+ ",\n"+
				"\t\t\tisSteinerTreeRootNode=" +isSteinerTreeRootNode+ "]";
	}
	
	public boolean isBlankNode() {
		return isBlankNode;
	}

	public boolean isSteinerTreeRootNode() {
		return isSteinerTreeRootNode;
	}

	public void setAsSteinerTreeRootNode(boolean isSteinerTreeRootNode) {
		this.isSteinerTreeRootNode = isSteinerTreeRootNode;
	}
}