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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.research.GraphVizUtil;
import edu.isi.karma.modeling.research.Util;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class ComputeGED {

	private static String importDir = "/Users/mohsen/Dropbox/Service Modeling/iswc2013/jgraph/";
	private static String exportDir = "/Users/mohsen/Dropbox/Service Modeling/iswc2013/output/";

	public static void main(String[] args) throws Exception {
		File ff = new File(importDir);
		File[] files = ff.listFiles();
		
		DirectedWeightedMultigraph<Node, Link> gMain, 
			gKarmaInitial, gKarmaInitial2, gKarmaFinal, 
			gRank1, gRank2, gRank3;
		
		HashSet<File> fileSet = new HashSet<File>(Arrays.asList(files));
		
		Function<File, String> sameService = new Function<File, String>() {
		  @Override public String apply(final File s) {
		    return s.getName().substring(0, s.getName().indexOf('.'));
		  }
		};

		Multimap<String, File> index =
				   Multimaps.index(fileSet, sameService);	
	
		for (String s : index.keySet()) {
			
			System.out.println(s);
			Collection<File> serviceFiles = index.get(s);
			gMain = null; 
			gKarmaInitial = null; gKarmaInitial2 = null; gKarmaFinal = null; 
			gRank1 = null; gRank2 = null; gRank3 = null;
			
			for (File f : serviceFiles) {
				if (f.getName().endsWith(".main.jgraph")) {
					gMain = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".karma.initial.jgraph")) {
					gKarmaInitial = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".karma.initial2.jgraph")) {
					gKarmaInitial2 = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".karma.final.jgraph")) {
					gKarmaFinal = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".rank1.jgraph")) {
					gRank1 = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".rank2.jgraph")) {
					gRank2 = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".rank3.jgraph")) {
					gRank3 = GraphUtil.deserialize(f.getPath());
				}					
			}
			
			if (gMain == null) continue;
			String label; double distance;
			
			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
			
			label = "0- Main";
			graphs.put(label, gMain);

			if (gKarmaInitial != null) {
				distance = Util.getDistance(gMain, gKarmaInitial);
				label = "1-Karma Initial" + "-distance:" + distance;
				graphs.put(label, gKarmaInitial);
			}
			
			if (gKarmaInitial2 != null) {
				distance = Util.getDistance(gMain, gKarmaInitial2);
				label = "2-Karma Initial2" + "-distance:" + distance;
				graphs.put(label, gKarmaInitial2);
			}
			
			if (gKarmaFinal != null) {
				distance = Util.getDistance(gMain, gKarmaFinal);
				label = "3-Karma Final" + "-distance:" + distance;
				graphs.put(label, gKarmaFinal);
			}
			
			if (gRank1 != null) {
				distance = Util.getDistance(gMain, gRank1);
				label = "4-Rank1" + "-distance:" + distance;
				graphs.put(label, gRank1);
			}
			
			if (gRank2 != null) {
				distance = Util.getDistance(gMain, gRank2);
				label = "5-Rank2" + "-distance:" + distance;
				graphs.put(label, gRank2);
			}

			if (gRank1 != null) {
				distance = Util.getDistance(gMain, gRank3);
				label = "6-Rank3" + "-distance:" + distance;
				graphs.put(label, gRank3);
			}

			GraphVizUtil.exportJGraphToGraphvizFile(graphs, s, exportDir + s + ".out.dot");
		}
	}
	
	
}
