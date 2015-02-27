package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.Relationship;
import org.apache.commons.math.optimization.linear.SimplexSolver;

public class LinearSolver {
	public static void main(String[] args) {
		//add residue to objective function
		LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {-3,-3,4,0,0,0
				}, 0);
		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		//data contraint
		constraints.add(new LinearConstraint(new double[] { 1, 1,-1,0,0,0},
				Relationship.LEQ, 0));
		constraints.add(new LinearConstraint(new double[] { 0, 1,0,1,0,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { -1, -1,0,0,1,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { -1, 0,0,0,0,1},
				Relationship.GEQ, 0));
		/*
		constraints.add(new LinearConstraint(new double[] { 0, 1,0,-1,0},
				Relationship.GEQ, -1));
		constraints.add(new LinearConstraint(new double[] { 0, 1,0,0,-1},
				Relationship.GEQ, -1));
		*/
		
		//auxiliary variable should be the biggest
		constraints.add(new LinearConstraint(new double[] { -1, -1,1,0,0,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { 0, -1,0,1,0,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { -1, -1,0,0,1,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { -1, 0,0,0,0,1},
				Relationship.GEQ, 0));
		//all parameters are positive
		constraints.add(new LinearConstraint(new double[] { 1, 0,0,0,0,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { 0, 1,0,0,0,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { 0, 0,1,0,0,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { 0, 0,0,1,0,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { 0, 0,0,0,1,0},
				Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] { 0, 0,0,0,0,1},
				Relationship.GEQ, 0));
		//max - max constraint >0
		constraints.add(new LinearConstraint(new double[] { 0, 0,2,-1,-1,-1},
				Relationship.GEQ, 0));
		//sum of weight equal 1
		constraints.add(new LinearConstraint(new double[] { 1, 1,0,0,0},
				Relationship.EQ, 1));

		// create and run the solver
		RealPointValuePair solution;
		try {
			solution = new SimplexSolver().optimize(f,
					constraints, GoalType.MINIMIZE, false);
			// get the solution
			double x = solution.getPoint()[0];
			double y = solution.getPoint()[1];
			double p = solution.getPoint()[2];
			double q1 = solution.getPoint()[3];
			double q2 = solution.getPoint()[4];

			double min = solution.getValue();
			System.out.println(String.format("%f,%f,%f,%f,%f,%f",x,y,p,q1,q2,min));
		} catch (OptimizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}
