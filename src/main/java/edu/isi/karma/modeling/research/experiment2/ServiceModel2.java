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

package edu.isi.karma.modeling.research.experiment2;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.research.GraphVizUtil;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class ServiceModel2 {

	private String id;
	private String serviceNameWithPrefix;
	private String serviceName;
	private String serviceDescription;
	
	private DirectedWeightedMultigraph<Node, Link> model;

	public ServiceModel2(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getServiceName() {
		return serviceName;
	}


	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	public String getServiceNameWithPrefix() {
		return serviceNameWithPrefix;
	}


	public void setServiceNameWithPrefix(String serviceNameWithPrefix) {
		this.serviceNameWithPrefix = serviceNameWithPrefix;
	}


	public String getServiceDescription() {
		return serviceDescription;
	}


	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}


	public DirectedWeightedMultigraph<Node,Link> getModel() {
		return model;
	}
	
	public void addModel(DirectedWeightedMultigraph<Node, Link> graph) {
		this.model = graph;
	}
	
	public void print() {
		System.out.println(this.getServiceName());
		System.out.println();
		GraphUtil.printGraphSimple(this.model);
		System.out.println();
	}

	
	public void exportModelToGraphviz(String exportDirectory) throws FileNotFoundException {
		
		OutputStream out = new FileOutputStream(exportDirectory + this.getServiceNameWithPrefix() + ".dot");
		org.kohsuke.graphviz.Graph graphViz = new org.kohsuke.graphviz.Graph();
		
		graphViz.attr("fontcolor", "blue");
		graphViz.attr("remincross", "true");
		graphViz.attr("label", this.getServiceDescription());
//		graphViz.attr("page", "8.5,11");

		org.kohsuke.graphviz.Graph gViz = GraphVizUtil.exportJGraphToGraphviz(this.model);
		gViz.attr("label", "model");
		gViz.id("cluster");
		graphViz.subGraph(gViz);
		graphViz.writeTo(out);


	}
	
	
	
}