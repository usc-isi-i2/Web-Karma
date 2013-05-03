/**
 * 
 */
package edu.isi.karma.modeling.research.graphmatching.algorithms;

/**
 * @author riesen
 *
 */
public class BipartiteMatching {

	/**
	 * the matching procedure defined via GUI or properties file
	 * possible choices are 'Hungarian' or 'VJ' (VolgenantJonker)
	 */
	private String matching;
	
	/**
	 * whether (1) or not (0) the matching found is logged on the console
	 */
	private int outputMatching;

	/**
	 * @param matching 
	 * @param outputMatching 
	 */
	public BipartiteMatching(String matching, int outputMatching) {
		this.matching = matching;
		this.outputMatching = outputMatching;
	}
	
	
	/**
	 * @return the optimal matching according to the @param costMatrix
	 * the matching actually used is defined in the string "matching"
	 */
	public int[][] getMatching(double[][] costMatrix) {
		int[][] assignment = null;;
		if (this.matching.equals("Hungarian")){
			HungarianAlgorithm ha = new HungarianAlgorithm();
			assignment = ha.hgAlgorithm(costMatrix);
		}
		if (this.matching.equals("VJ") ){
			VolgenantJonker vj = new VolgenantJonker();
			vj.computeAssignment(costMatrix);
			int[] solution = vj.rowsol;
			assignment = new int[costMatrix.length][2];
			// format the assignment correctly
			for (int i = 0; i < solution.length; i++){
				assignment[i][0]=i;
				assignment[i][1]=solution[i];
			}
			
		}
		// log the matching on the console
		if (this.outputMatching==1){
			System.out.println("\nThe Optimal Matching:");
			for (int k = 0; k < assignment.length; k++){
				System.out.print(assignment[k][0]+" -> "+assignment[k][1]+" ");
			}
			System.out.println();
		}
		
		return assignment;
	}
	

}
