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

import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class ComputeGED {

//	private static Logger logger = LoggerFactory.getLogger(ComputeGED.class);

	public static void main(String[] args) throws Exception {
		computeGEDApp1();
//		computeGEDApp2();
	}
	

	
	private static void computeGEDApp1() throws Exception {
		
		File ff = new File(Params.MODEL_DIR);
		File[] files = ff.listFiles();
		
		SemanticModel mMain, 
			mKarmaInitial, mKarmaFinal, 
			mApp1Rank1, mApp1Rank2, mApp1Rank3;
		
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
			mMain = null; 
			mKarmaInitial = null; mKarmaFinal = null; 
			mApp1Rank1 = null; mApp1Rank2 = null; mApp1Rank3 = null; 
			
			for (File f : serviceFiles) {
				if (f.getName().endsWith(".main.model")) {
					try { mMain = SemanticModel.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".karma.initial.model")) {
					try { mKarmaInitial = SemanticModel.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".karma.final.model")) {
					try { mKarmaFinal = SemanticModel.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".app1.rank1.model")) {
					try { mApp1Rank1 = SemanticModel.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".app1.rank2.model")) {
					try { mApp1Rank2 = SemanticModel.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".app1.rank3.model")) {
					try { mApp1Rank3 = SemanticModel.deserialize(f.getPath()); } catch (Exception e) {}
				}					
			}
			
			if (mMain == null) continue;
			String label; double distance;
			
			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
			
			label = "0- Main";
			graphs.put(label, mMain.getGraph());

			if (mKarmaInitial != null) {
				distance = mMain.getDistance(mKarmaInitial);
				label = "1-Karma Initial" + "-distance:" + distance;
				graphs.put(label, mKarmaInitial.getGraph());
			}
			
			if (mKarmaFinal != null) {
				distance = mMain.getDistance(mKarmaFinal);
				label = "3-Karma Final" + "-distance:" + distance;
				graphs.put(label, mKarmaFinal.getGraph());
			}
			
			if (mApp1Rank1 != null) {
				distance = mMain.getDistance(mApp1Rank1);
				label = "4-Rank1" + "-distance:" + distance;
				graphs.put(label, mApp1Rank1.getGraph());
			}
			
			if (mApp1Rank2 != null) {
				distance = mMain.getDistance(mApp1Rank2);
				label = "5-Rank2" + "-distance:" + distance;
				graphs.put(label, mApp1Rank2.getGraph());
			}

			if (mApp1Rank3 != null) {
				distance = mMain.getDistance(mApp1Rank3);
				label = "6-Rank3" + "-distance:" + distance;
				graphs.put(label, mApp1Rank3.getGraph());
			}


			GraphVizUtil.exportJGraphToGraphvizFile(graphs, s, Params.OUTPUT_DIR + s + ".app1.out.dot");
		}
	}
	
//	private static void computeGEDApp2() throws Exception {
//		
//		File ff = new File(Params.JGRAPHT_DIR);
//		File[] files = ff.listFiles();
//		
//		DirectedWeightedMultigraph<Node, Link> gMain, 
//			gKarmaInitial, gKarmaFinal, gApp2;
//		
//		HashSet<File> fileSet = new HashSet<File>(Arrays.asList(files));
//		
//		Function<File, String> sameService = new Function<File, String>() {
//		  @Override public String apply(final File s) {
//		    return s.getName().substring(0, s.getName().indexOf('.'));
//		  }
//		};
//
//		Multimap<String, File> index =
//				   Multimaps.index(fileSet, sameService);	
//	
//		for (String s : index.keySet()) {
//			
//			System.out.println(s);
//			Collection<File> serviceFiles = index.get(s);
//			gMain = null; 
//			gKarmaInitial = null; gKarmaFinal = null; gApp2 = null;
//			
//			for (File f : serviceFiles) {
//				if (f.getName().endsWith(".main.jgraph")) {
//					try { gMain = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
//				} else if (f.getName().endsWith(".karma.initial.jgraph")) {
//					try { gKarmaInitial = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
//				} else if (f.getName().endsWith(".karma.final.jgraph")) {
//					try { gKarmaFinal = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
//				} else if (f.getName().endsWith(".app2.jgraph")) {
//					try { gApp2 = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
//					gApp2 = GraphUtil.deserialize(f.getPath());
//				}					
//			}
//			
//			if (gMain == null) continue;
//			String label; double distance;
//			
//			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
//					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
//			
//			label = "0- Main";
//			graphs.put(label, gMain);
//
//			if (gKarmaInitial != null) {
//				distance = getDistance(gMain, gKarmaInitial);
//				label = "1-Karma Initial" + "-distance:" + distance;
//				graphs.put(label, gKarmaInitial);
//			}
//			
//			if (gKarmaFinal != null) {
//				distance = getDistance(gMain, gKarmaFinal);
//				label = "3-Karma Final" + "-distance:" + distance;
//				graphs.put(label, gKarmaFinal);
//			}
//			
//			if (gApp2 != null) {
//				distance = getDistance(gMain, gApp2);
//				label = "2-Output" + "-distance:" + distance;
//				graphs.put(label, gApp2);
//			}
//			
//
//
//			GraphVizUtil.exportJGraphToGraphvizFile(graphs, s, Params.OUTPUT_DIR + s + ".app2.out.dot");
//		}
//	}
	
}
