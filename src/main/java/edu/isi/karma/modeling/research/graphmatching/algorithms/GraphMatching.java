/**
 * 
 */
package edu.isi.karma.modeling.research.graphmatching.algorithms;

import java.io.FileInputStream;
import java.util.Properties;

import edu.isi.karma.modeling.research.graphmatching.util.CostFunction;
import edu.isi.karma.modeling.research.graphmatching.util.EditDistance;
import edu.isi.karma.modeling.research.graphmatching.util.Graph;
import edu.isi.karma.modeling.research.graphmatching.util.GraphSet;
import edu.isi.karma.modeling.research.graphmatching.util.MatrixGenerator;
import edu.isi.karma.modeling.research.graphmatching.util.ResultPrinter;
import edu.isi.karma.modeling.research.graphmatching.xml.XMLParser;

/**
 * @author riesen
 * 
 */
public class GraphMatching {

	/**
	 * the sets of graphs to be matched 
	 */
	private GraphSet source, target;

	/**
	 * the resulting distance matrix D = (d_i,j), where d_i,j = d(g_i,g_j) 
	 * (distances between all graphs g_i from source and all graphs g_j from target) 
	 */
	private double[][] distanceMatrix;

	/**
	 * the source and target graph actually to be matched (temp ist for temporarily swappings)
	 */
	private Graph sourceGraph, targetGraph, temp;

	/**
	 * whether the edges of the graphs are undirected (=1) or directed (=0)
	 */
	private int undirected;

	/**
	 * progess-counter
	 */
	private int counter;

	/**
	 * log options:
	 * output the individual graphs
	 * output the cost matrix for bipartite graph matching
	 * output the matching between the nodes based on the cost matrix (considering the local substructures only)
	 * output the edit path between the graphs
	 */
	private int outputGraphs;
	private int outputCostMatrix;
	private int outputMatching;
	private int outputEditpath;

	/**
	 * the cost function to be applied
	 */
	private CostFunction costFunction;

	/**
	 * number of rows and columns in the distance matrix
	 * (i.e. number of source and target graphs)
	 */
	private int r;
	private int c;

	/**
	 * computes an optimal bipartite matching of local graph structures
	 */
	private BipartiteMatching bipartiteMatching;
	
	/**
	 * computes the approximated or exact graph edit distance 
	 */
	private EditDistance editDistance;

	/**
	 * the matching procedure defined via GUI or properties file
	 * possible choices are 'Hungarian', 'VJ' (VolgenantJonker)
	 * 'AStar' (exact tree search) or 'Beam' (approximation based on tree-search)
	 */
	private String matching;
	
	/**
	 * the maximum number of open paths (used for beam-search)
	 */
	private int s;
	
	/**
	 * generates the cost matrix whereon the optimal bipartite matching can
	 * be computed
	 */
	private MatrixGenerator matrixGenerator;

	/**
	 * whether or not a similarity kernel is built upon the distance values:
	 * 0 = distance matrix is generated: D = (d_i,j), where d_i,j = d(g_i,g_j) 
	 * 1 = -(d_i,j)^2
	 * 2 = -d_i,j
	 * 3 = tanh(-d)
	 * 4 = exp(-d)
	 */
	private int simKernel;

	/**
	 * prints the results
	 */
	private ResultPrinter resultPrinter;
	
	
	public double getDistance() {
		System.out.println( distanceMatrix[0][0]);
		return distanceMatrix[0][0] * 2;
	}

	/**
	 * the matching procedure
	 * @throws Exception
	 */
	public GraphMatching(String graph1, String graph2) throws Exception {
		// initialize the matching
		System.out.println("Initializing the matching according to the properties...");
		System.out.println(graph1);
		System.out.println(graph2);
		this.init(graph1, graph2);
		// the cost matrix used for bipartite matchings
		double[][] costMatrix;
		// counts the progress
		this.counter = 0;
		
		// iterate through all pairs of graphs g_i x g_j from (source, target)
		System.out.println("Starting the matching...");
		System.out.println("Progress...");
		int numOfMatchings = this.source.size() * this.target.size();
		// distance value d
		double d = -1;
		// swapped the graphs?
		boolean swapped = false;
		for (int i = 0; i < r; i++) {
			sourceGraph = this.source.get(i);
			for (int j = 0; j < c; j++) {
				swapped = false;
				targetGraph = this.target.get(j);
				this.counter++;
				System.out.println("Matching "+counter+" of "+numOfMatchings);
				
				// log the current graphs on the console
				if (this.outputGraphs == 1) {
					System.out.println("The Source Graph:");
					System.out.println(sourceGraph);
					System.out.println("\n\nThe Target Graph:");
					System.out.println(targetGraph);
				}
				// if both graphs are empty the distance is zero and no computations have to be carried out!
				if (this.sourceGraph.size()<1 && this.targetGraph.size()<1){
					d = 0;
				} else {
				// calculate the approximated or exact edit-distance using tree search algorithms 
				// AStar: number of open paths during search is unlimited (s=infty)
				// Beam: number of open paths during search is limited to s
				if (this.matching.equals("AStar") || this.matching.equals("Beam")){
					d = this.editDistance.getEditDistance(
								sourceGraph, targetGraph, costFunction, this.s);
				} else { // approximation of graph edit distances via bipartite matching
					// in order to get determinant edit costs between two graphs
					if (this.sourceGraph.size()<this.targetGraph.size()){
						this.swapGraphs();
						swapped= true;
					}
					// generate the cost-matrix between the local substructures of the source and target graphs
					costMatrix = this.matrixGenerator.getMatrix(sourceGraph, targetGraph);
					// compute the matching using Hungarian or VolgenantJonker (defined in String matching)
					int[][] matching = this.bipartiteMatching.getMatching(costMatrix);
					// calculate the approximated edit-distance according to the bipartite matching 
					d = this.editDistance.getEditDistance(
							sourceGraph, targetGraph, matching, costFunction);
				}
				}
				// whether distances or similarities are computed
				if (this.simKernel < 1){
					this.distanceMatrix[i][j] = d;
				} else {
					switch (this.simKernel){
					case 1:
						this.distanceMatrix[i][j] = -Math.pow(d,2.0);break;
					case 2:
						this.distanceMatrix[i][j] = -d;break;
					case 3:
						this.distanceMatrix[i][j] = Math.tanh(-d);break;
					case 4:
						this.distanceMatrix[i][j] = Math.exp(-d);break;
                    }			
				}
				if (swapped){
					this.swapGraphs();
				}
			}
		}
		
		System.out.println("Finished...");

	}

	

	/**
	 * swap the source and target graph
	 */
	private void swapGraphs() {
		this.temp = this.sourceGraph;
		this.sourceGraph = this.targetGraph;
		this.targetGraph = this.temp;
	}

	private void init(String graph1, String graph2) throws Exception {
		
		// the node and edge costs, the relative weighting factor alpha
		double node = 1.0;
		double edge = 1.0;
		double alpha = 0.5;
		
		// the node and edge attributes (the names, the individual cost functions, the weighting factors)
		int numOfNodeAttr = 1;
		int numOfEdgeAttr = 1;
		String[] nodeAttributes = new String[numOfNodeAttr];
		String[] edgeAttributes = new String[numOfEdgeAttr];
		String[] nodeCostTypes = new String[numOfNodeAttr];
		String[] edgeCostTypes = new String[numOfEdgeAttr];
		double[] nodeAttrImportance = new double[numOfNodeAttr];
		double[] edgeAttrImportance = new double[numOfEdgeAttr];
		double[] nodeCostMu = new double[numOfNodeAttr];
		double[] nodeCostNu = new double[numOfNodeAttr];
		
		nodeAttributes[0] = "label";
		nodeCostTypes[0] = "discrete";
		nodeCostMu[0] = 0.0;
		nodeCostNu[0] = 1.0;
		nodeAttrImportance[0] = 1.0;

		edgeAttributes[0] = "label";
		edgeCostTypes[0] = "equality";
		edgeAttrImportance[0] = 1.0;

		
		// whether or not the costs are "p-rooted"
		double squareRootNodeCosts = 1.0;
		double squareRootEdgeCosts = 1.0;
		
		// whether costs are multiplied or summed
		int multiplyNodeCosts = 0;
		int multiplyEdgeCosts = 0;
		
		// what is logged on the console (graphs, cost-matrix, matching, edit path)
		this.outputGraphs = 0;
		this.outputCostMatrix = 0;
		this.outputMatching = 0;
		this.outputEditpath = 0;
		
		// whether the edges of the graphs are directed or undirected
		this.undirected = 0;
		
		// the graph matching paradigm actually employed 		
		this.matching =  "AStar";
		this.s = Integer.MAX_VALUE; // AStar


		// initialize the cost function according to properties
		this.costFunction = new CostFunction(node, edge, alpha, nodeAttributes,
				nodeCostTypes, nodeAttrImportance, edgeAttributes,
				edgeCostTypes, edgeAttrImportance, squareRootNodeCosts,
				multiplyNodeCosts, squareRootEdgeCosts, multiplyEdgeCosts, nodeCostMu, nodeCostNu);	
		
		
		// the matrixGenerator generates the cost-matrices according to the costfunction
		this.matrixGenerator = new MatrixGenerator(this.costFunction,
				this.outputCostMatrix);
		
		// bipartite matching procedure (Hungarian or VolgenantJonker)
		this.bipartiteMatching = new BipartiteMatching(this.matching, this.outputMatching);
		
		// editDistance computes either the approximated edit-distance according to the bipartite matching 
		// or computes the exact edit distance
		this.editDistance = new EditDistance(this.undirected, this.outputEditpath);
		
		// whether or not a similarity is derived from the distances 
		this.simKernel=0;
		
		// load the source and target set of graphs
		System.out.println("Load the source and target graph sets...");
		XMLParser xmlParser = new XMLParser();
		
		this.source = new GraphSet();
		this.target = new GraphSet();
		
		this.source.add(xmlParser.parseGXLFromString(graph1));
		this.target.add(xmlParser.parseGXLFromString(graph2));
		
		// create a distance matrix to store the resulting dissimilarities
		this.r = this.source.size();
		this.c = this.target.size();
		this.distanceMatrix = new double[this.r][this.c];
				
	}

	/**
	 * @return the progress of the matching procedure
	 */
	public int getCounter() {
		return counter;
	}


}
