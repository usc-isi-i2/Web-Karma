package edu.isi.karma.modeling.research.graph.roek.nlpged.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.modeling.research.graph.konstantinosnedas.HungarianAlgorithm;
import edu.isi.karma.modeling.research.graph.roek.nlpged.algorithm.GraphEditDistance;
import edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Edge;
import edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Graph;
import edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Node;


public class App {

	public static void main(String[] args) {
		Graph g1 = new Graph();
		Graph g2 = new Graph();
		
		Node n11 = new Node("", "n1");
		Node n12 = new Node("", "n2");
		Node n13 = new Node("", "n3");
		Edge e11 = new Edge("", n11, n12, "e12");
		Edge e12 = new Edge("", n11, n13, "e13");
		
		Node n21 = new Node("", "n1");
		Node n22 = new Node("", "n2");
		Node n23 = new Node("", "n3");
		Edge e21 = new Edge("", n21, n22, "e12");
		Edge e22 = new Edge("", n22, n23, "e13");
		
		g1.addNode(n11);
		g1.addNode(n12);
		g1.addNode(n13);
		g1.addEdge(e11);
		g1.addEdge(e12);
		
		g2.addNode(n21);
		g2.addNode(n22);
		g2.addNode(n23);
		g2.addEdge(e21);
		g2.addEdge(e22);
		
		getDistance(g1, g2);
		System.out.println("Exiting..");
	}
	
	public static double getDistance(Graph g1, Graph g2) {

		GraphEditDistance ged = new GraphEditDistance(g1, g2);

		System.out.println("Calculating graph edit distance for the two sentences:");
		System.out.println("Distance between the two sentences: "+ged.getDistance()+". Normalised: "+ged.getNormalizedDistance());
		System.out.println("Edit path:");
		for(String editPath : getEditPath(g1, g2, ged.getCostMatrix(), true)) {
			System.out.println(editPath);
		}
		
		return ged.getDistance();
	}

	public static String[] getInputTexts(String[] args)  {
		String text1="", text2="";
		if(args.length!=2) {
			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);
			try {
				System.out.println("Please enter the first sentence: ");
				text1 = in.readLine();

				System.out.println("Please enter the second sentence: ");
				text2 = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			return args;
		}

		return new String[] {text1, text2};
	}

	public static List<String> getEditPath(Graph g1, Graph g2, double[][] costMatrix, boolean printCost) {
		return getAssignment(g1, g2, costMatrix, true, printCost);
	}

	public static List<String> getFreeEdits(Graph g1, Graph g2, double[][] costMatrix) {
		return getAssignment(g1, g2, costMatrix, false, false);
	}

	public static List<String> getAssignment(Graph g1, Graph g2, double[][] costMatrix, boolean editPath, boolean printCost) {
		List<String> editPaths = new ArrayList<String>();
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, "min");

		for (int i = 0; i < assignment.length; i++) {
			String from = getEditPathAttribute(assignment[i][0], g1);
			String to = getEditPathAttribute(assignment[i][1], g2);

			double cost = costMatrix[assignment[i][0]][assignment[i][1]];
			if(cost != 0 && editPath) {
				if(printCost) {
					editPaths.add("("+from+" -> "+to+") = "+cost);
				}
			}else if(cost == 0 && !editPath) {
				editPaths.add("("+from+" -> "+to+")");
			}
		}

		return editPaths;

	}

	private static String getEditPathAttribute(int nodeNumber, Graph g) {
		if(nodeNumber < g.getNodes().size()) {
			Node n= g.getNode(nodeNumber);
			return n.getLabel();
		}else {
			return "ε";
		}
	}
	
	public static boolean shouldContinue() {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		System.out.println("continue? [y/n]");
		try {
			String answer = in.readLine();
			return (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
