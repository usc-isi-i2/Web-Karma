package edu.isi.karma.modeling.research.graphmatching.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;


public class ResultPrinter {

	/**
	 * where the results are printed
	 */
	private String resultFolder;
	/**
	 * the filename and path of the source and target graph sets
	 */
	private String source;
	private String target;

	/**
	 * the decimalformat for the editdistances found
	 */
	private DecimalFormat decFormat;
	
	/**
	 * the properties defined via GUI or properties file
	 */
	private Properties properties;

	
	/**
	 * constructs a ResultPrinter 
	 * @param resultFolder
	 * @param properties
	 */
	public ResultPrinter(String resultFolder, Properties properties) {
		this.resultFolder = resultFolder;
		this.properties = properties;
		String s = this.properties.getProperty("source");
		String[] sarray = s.split("/");
		this.source = sarray[sarray.length-1];
		String t = this.properties.getProperty("target");
		String[] tarray = t.split("/");
		this.target = tarray[tarray.length-1];
	}

	/**prints out the properties and the distance-matrix @param d 
	 * in the resultFolder
	 */
	public void printResult(double[][] d) {
		this.decFormat = (DecimalFormat) NumberFormat
				.getInstance(Locale.ENGLISH);
		this.decFormat.applyPattern("0.00000");
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		String resultName = this.resultFolder+this.source+"_"+this.target+"_"+dateFormat.format(cal.getTime())+".txt";
		PrintWriter out;
		try {
			out = new PrintWriter(new FileOutputStream(resultName));
			out.println("*** The properties of the matching ***\n");
			out.println("Source graph set:\t"+this.properties.getProperty("source")+" ("+d.length+" graphs)");
			out.println("Target graph set:\t"+this.properties.getProperty("target")+" ("+d[0].length+" graphs)\n");
			out.print("Graph edit distance procedure:\t"+this.properties.getProperty("matching")+"\n");
			if (this.properties.getProperty("matching").equals("Beam")){
				out.println("s = "+ Integer.parseInt(properties.getProperty("s"))+"\n");
			}
			int undirected = Integer
					.parseInt(properties.getProperty("undirected"));
			out.print("Edge mode:\t\t");
			if (undirected == 1){
				out.println("undirected\n");
			} else {
				out.println("directed\n");;
			}
			out.println("Cost for node deletion/insertion:\t"+this.properties.getProperty("node"));
			out.println("Cost for edge deletion/insertion:\t"+this.properties.getProperty("edge")+"\n");
			out.println("Alpha weighting factor between node and edge costs:\t"+this.properties.getProperty("alpha")+"\n");
			int numOfNodeAttr = Integer.parseInt(properties
					.getProperty("numOfNodeAttr"));
			int numOfEdgeAttr = Integer.parseInt(properties
					.getProperty("numOfEdgeAttr"));
			
			
			for (int i = 0; i < numOfNodeAttr; i++) {
				out.print("Node attribute "+i+":\t"+ properties.getProperty("nodeAttr" + i)+";\t");
				out.print("Cost function:\t"+properties.getProperty("nodeCostType" + i)+";\t");
				if (properties.getProperty("nodeCostType" + i).equals("discrete")){
					out.print("mu = "+properties.getProperty("nodeCostMu" + i)+" nu = "+properties.getProperty("nodeCostNu" + i)+";\t");
				}
				out.println("Soft factor:\t"+properties.getProperty("nodeAttr" + i + "Importance"));
			}
			if (numOfNodeAttr==0){
				out.println("No attributes for nodes defined");
			}
			out.println();
			for (int i = 0; i < numOfEdgeAttr; i++) {
				out.print("Edge Attribute "+i+":\t"+ properties.getProperty("edgeAttr" + i)+";\t");
				out.print("Cost Function:\t"+properties.getProperty("edgeCostType" + i)+";\t");
				out.println("Soft Factor:\t"+properties.getProperty("edgeAttr" + i + "Importance"));
			}
			if (numOfEdgeAttr==0){
				out.println("No attributes for edges defined");
			}
			out.println();
			double squareRootNodeCosts = Double.parseDouble(properties
					.getProperty("pNode"));
			int multiplyNodeCosts = Integer.parseInt(properties
					.getProperty("multiplyNodeCosts"));
			double squareRootEdgeCosts = Double.parseDouble(properties
					.getProperty("pEdge"));
			int multiplyEdgeCosts = Integer.parseInt(properties
					.getProperty("multiplyEdgeCosts"));
			
			if (multiplyNodeCosts==1){
				out.println("Individual node costs are multiplied");
			} else {
				out.println("Individual node costs are added");
			}
			if (multiplyEdgeCosts==1){
				out.println("Individual edge costs are multiplied");
			} else {
				out.println("Individual edge costs are added");
			}	
			System.out.println();
			out.println("(Combined node cost)^(1/"+squareRootNodeCosts+")");
		
			out.println("(Combined edge cost)^(1/"+squareRootEdgeCosts+")");
			
			int simKernel=Integer.parseInt(properties.getProperty("simKernel"));
			
			switch (simKernel){
				case 0:
					out.println("\n*** The distance matrix ***\n");
					break;
				case 1:
					out.println("\n*** The similarity matrix (-d^2) ***\n");break;
				case 2:
					out.println("\n*** The similarity matrix (-d) ***\n");break;
				case 3:
					out.println("\n*** The similarity matrix tanh(-d) ***\n");break;
				case 4:
					out.println("\n*** The similarity matrix exp(-d) ***\n");break;
			}
			
			for (int i = 0; i < d.length; i++){
				for (int j = 0; j < d[0].length; j++){
					out.print(decFormat.format(d[i][j]));
					if (j+1 < d[0].length){
						out.print(",");
					}
				}
				out.println();
			}
			
			out.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

}
