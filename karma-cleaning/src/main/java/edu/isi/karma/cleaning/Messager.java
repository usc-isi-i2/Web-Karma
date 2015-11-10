package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
//used to carry information accross iterations



import edu.isi.karma.cleaning.grammartree.Partition;

public class Messager {
	double[] weights = null;
	Vector<Vector<String[]>> cm_constr = new Vector<Vector<String[]>>();
	//HashMap<String, Traces> exp2Space = new HashMap<String, Traces>();
	public HashMap<String, String> exp2program = new HashMap<String,String>();
	public HashMap<String, Partition> exp2Partition = new HashMap<String, Partition>();
	public HashSet<String> allMultipleInterpretation = new HashSet<String>();
	//ExampleTraces expTraces = new ExampleTraces();
	public Messager()
	{
		
	}
	public void updateCM_Constr(Vector<Vector<String[]>> conVector)
	{
		this.cm_constr.addAll(conVector);
	}
	public void updateWeights(double[] weights)
	{
		this.weights = weights;
	}
	public double[] getWeights()
	{
		return this.weights;
	}
	public Vector<Vector<String[]>> getConstraints()
	{
		return cm_constr;
	}
	public void addMultiInterpreationRecords(HashSet<String> newrecords){
		allMultipleInterpretation.addAll(newrecords);
	}
}

