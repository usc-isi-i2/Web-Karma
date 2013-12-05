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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class ComputeGED {

	private static Logger logger = LoggerFactory.getLogger(ComputeGED.class);

	public static void main(String[] args) throws Exception {
		computeGEDApp1();
//		computeGEDApp2();
	}
	
	public static double getDistance(DirectedWeightedMultigraph<Node, Link> targetGraph,
			DirectedWeightedMultigraph<Node, Link> sourceGraph) {
				
		if (targetGraph == null || sourceGraph == null)
			return -1.0;
		
		int nodeInsertion = 0, nodeDeletion = 0, linkInsertion = 0, linkDeletion = 0;
		
		HashMap<String, Integer> sourceNodes = new HashMap<String, Integer>();
		HashMap<String, Integer> targetNodes = new HashMap<String, Integer>();

		HashMap<String, Integer> sourceLinks = new HashMap<String, Integer>();
		HashMap<String, Integer> targetLinks = new HashMap<String, Integer>();

		String key;
		Integer count = 0;
		
		// Adding the nodes to the maps
		for (Node n : sourceGraph.vertexSet()) {
			if (n instanceof InternalNode) key = n.getLabel().getUri();
			else if (n instanceof ColumnNode) key = ((ColumnNode)n).getColumnName();
			else continue;
			
			count = sourceNodes.get(key);
			if (count == null) sourceNodes.put(key, 1);
			else sourceNodes.put(key, ++count);
		}
		for (Node n : targetGraph.vertexSet()) {
			if (n instanceof InternalNode) key = n.getLabel().getUri();
			else if (n instanceof ColumnNode) key = ((ColumnNode)n).getColumnName();
			else continue;
			
			count = targetNodes.get(key);
			if (count == null) targetNodes.put(key, 1);
			else targetNodes.put(key, ++count);
		}
		
		// Adding the links to the maps
		Node source, target;
		for (Link l : sourceGraph.edgeSet()) {			
			source = l.getSource();
			target = l.getTarget();
			
			if (!(source instanceof InternalNode)) continue;
			
			key = source.getLabel().getUri();
			key += l.getLabel().getUri();
			if (target instanceof InternalNode) key += target.getLabel().getUri();
			else if (target instanceof ColumnNode) key += ((ColumnNode)target).getColumnName();
			else continue;
			
			count = sourceLinks.get(key);
			if (count == null) sourceLinks.put(key, 1);
			else sourceLinks.put(key, ++count);
		}
		for (Link l : targetGraph.edgeSet()) {
			source = l.getSource();
			target = l.getTarget();
			
			if (!(source instanceof InternalNode)) continue;
			
			key = source.getLabel().getUri();
			key += l.getLabel().getUri();
			if (target instanceof InternalNode) key += target.getLabel().getUri();
			else if (target instanceof ColumnNode) key += ((ColumnNode)target).getColumnName();
			else continue;
			
			count = targetLinks.get(key);
			if (count == null) targetLinks.put(key, 1);
			else targetLinks.put(key, ++count);
		}
		
		int diff;
		for (Entry<String, Integer> targetNodeEntry : targetNodes.entrySet()) {
			count = sourceNodes.get(targetNodeEntry.getKey());
			if (count == null) count = 0;
			diff = targetNodeEntry.getValue() - count;
			nodeInsertion += diff > 0? diff : 0;
		}
		for (Entry<String, Integer> sourceNodeEntry : sourceNodes.entrySet()) {
			count = targetNodes.get(sourceNodeEntry.getKey());
			if (count == null) count = 0;
			diff = sourceNodeEntry.getValue() - count;
			nodeDeletion += diff > 0? diff : 0;
		}
		
		for (Entry<String, Integer> targetLinkEntry : sourceLinks.entrySet()) {
			count = sourceNodes.get(targetLinkEntry.getKey());
			if (count == null) count = 0;
			diff = targetLinkEntry.getValue() - count;
			linkInsertion += diff > 0? diff : 0;
		}
		for (Entry<String, Integer> sourceLinkEntry : targetLinks.entrySet()) {
			count = targetNodes.get(sourceLinkEntry.getKey());
			if (count == null) count = 0;
			diff = sourceLinkEntry.getValue() - count;
			linkDeletion += diff > 0? diff : 0;
		}
		
		logger.info("node insertion cost: " + nodeInsertion);
		logger.info("node deletion cost: " + nodeDeletion);
		logger.info("link insertion cost: " + linkInsertion);
		logger.info("link deletion cost: " + linkDeletion);

		return nodeInsertion + nodeDeletion + linkInsertion + linkDeletion;
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
					try { gMain = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".karma.initial.jgraph")) {
					try { gKarmaInitial = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".karma.final.jgraph")) {
					try { gKarmaFinal = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".app1.rank1.jgraph")) {
					try { gApp1Rank1 = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".app1.rank2.jgraph")) {
					try { gApp1Rank2 = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(".app1.rank3.jgraph")) {
					try { gApp1Rank3 = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
				}					
			}
			
			if (gMain == null) continue;
			String label; double distance;
			
			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
			
			label = "0- Main";
			graphs.put(label, gMain);

			if (gKarmaInitial != null) {
				distance = getDistance(gMain, gKarmaInitial);
				label = "1-Karma Initial" + "-distance:" + distance;
				graphs.put(label, gKarmaInitial);
			}
			
			if (gKarmaFinal != null) {
				distance = getDistance(gMain, gKarmaFinal);
				label = "3-Karma Final" + "-distance:" + distance;
				graphs.put(label, gKarmaFinal);
			}
			
			if (gApp1Rank1 != null) {
				distance = getDistance(gMain, gApp1Rank1);
				label = "4-Rank1" + "-distance:" + distance;
				graphs.put(label, gApp1Rank1);
			}
			
			if (gApp1Rank2 != null) {
				distance = getDistance(gMain, gApp1Rank2);
				label = "5-Rank2" + "-distance:" + distance;
				graphs.put(label, gApp1Rank2);
			}

			if (gApp1Rank3 != null) {
				distance = getDistance(gMain, gApp1Rank3);
				label = "6-Rank3" + "-distance:" + distance;
				graphs.put(label, gApp1Rank3);
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
