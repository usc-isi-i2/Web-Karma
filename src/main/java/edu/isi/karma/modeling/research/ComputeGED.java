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

package edu.isi.karma.modeling.research;

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
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class ComputeGED {

	public static void main(String[] args) throws Exception {
		computeGEDApp1();
		computeGEDApp2();
	}
	
	private static void computeGEDApp1() throws Exception {
		
		File ff = new File(Params.JGRAPHT_DIR);
		File[] files = ff.listFiles();
		
		DirectedWeightedMultigraph<Node, Link> gMain, 
			gKarmaInitial, gKarmaFinal, 
			gApp1Rank1, gApp1Rank2, gApp1Rank3;
		
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
			gKarmaInitial = null; gKarmaFinal = null; 
			gApp1Rank1 = null; gApp1Rank2 = null; gApp1Rank3 = null; 
			
			for (File f : serviceFiles) {
				if (f.getName().endsWith(".main.jgraph")) {
					gMain = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".karma.initial.jgraph")) {
					gKarmaInitial = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".karma.final.jgraph")) {
					gKarmaFinal = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".app1.rank1.jgraph")) {
					gApp1Rank1 = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".app1.rank2.jgraph")) {
					gApp1Rank2 = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".app1.rank3.jgraph")) {
					gApp1Rank3 = GraphUtil.deserialize(f.getPath());
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
			
			if (gKarmaFinal != null) {
				distance = Util.getDistance(gMain, gKarmaFinal);
				label = "3-Karma Final" + "-distance:" + distance;
				graphs.put(label, gKarmaFinal);
			}
			
			if (gApp1Rank1 != null) {
				distance = Util.getDistance(gMain, gApp1Rank1);
				label = "4-Rank1" + "-distance:" + distance;
				graphs.put(label, gApp1Rank1);
			}
			
			if (gApp1Rank2 != null) {
				distance = Util.getDistance(gMain, gApp1Rank2);
				label = "5-Rank2" + "-distance:" + distance;
				graphs.put(label, gApp1Rank2);
			}

			if (gApp1Rank3 != null) {
				distance = Util.getDistance(gMain, gApp1Rank3);
				label = "6-Rank3" + "-distance:" + distance;
				graphs.put(label, gApp1Rank3);
			}


			GraphVizUtil.exportJGraphToGraphvizFile(graphs, s, Params.OUTPUT_DIR + s + ".app1.out.dot");
		}
	}
	
	private static void computeGEDApp2() throws Exception {
		
		File ff = new File(Params.JGRAPHT_DIR);
		File[] files = ff.listFiles();
		
		DirectedWeightedMultigraph<Node, Link> gMain, 
			gKarmaInitial, gKarmaFinal, gApp2;
		
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
			gKarmaInitial = null; gKarmaFinal = null; gApp2 = null;
			
			for (File f : serviceFiles) {
				if (f.getName().endsWith(".main.jgraph")) {
					gMain = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".karma.initial.jgraph")) {
					gKarmaInitial = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".karma.final.jgraph")) {
					gKarmaFinal = GraphUtil.deserialize(f.getPath());
				} else if (f.getName().endsWith(".app2.jgraph")) {
					gApp2 = GraphUtil.deserialize(f.getPath());
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
			
			if (gKarmaFinal != null) {
				distance = Util.getDistance(gMain, gKarmaFinal);
				label = "3-Karma Final" + "-distance:" + distance;
				graphs.put(label, gKarmaFinal);
			}
			
			if (gApp2 != null) {
				distance = Util.getDistance(gMain, gApp2);
				label = "2-Output" + "-distance:" + distance;
				graphs.put(label, gApp2);
			}
			


			GraphVizUtil.exportJGraphToGraphvizFile(graphs, s, Params.OUTPUT_DIR + s + ".app2.out.dot");
		}
	}
	
}
