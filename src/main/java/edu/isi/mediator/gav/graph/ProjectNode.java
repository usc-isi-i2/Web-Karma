// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.graph;

import java.util.ArrayList;

import edu.isi.mediator.gav.graph.cost_estimation.AttributeStatistics;
import edu.isi.mediator.gav.graph.cost_estimation.StatisticsInfo;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.ConstTerm;
import edu.isi.mediator.rule.FunctionTerm;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.Term;

/*
if we have X<Y in a query and X and Y come from different relations =>
cross product + a selection with X<Y
 */
public class ProjectNode extends BaseNode
{
	
	private Predicate project;
	public BaseNode child;

	public ProjectNode(Predicate project, BaseNode child){
		this.project=project;
		this.child=child;
	}
	
	public ArrayList<BaseNode> getSubNodes() {
		ArrayList<BaseNode> subnodes = new ArrayList();
		if (child != null)
			subnodes.add(child);
		return subnodes;
	}
	
	public void setSQL() throws MediatorException{
		
		child.setSQL();
		
		sqlFrom.addAll(child.sqlFrom);
		sqlWhere.addAll(child.sqlWhere);
		ArrayList<String> newSelect = new ArrayList<String>();
		
		//filter the select vars
		ArrayList<Term> vars = project.getTerms();
		for(int i=0; i<vars.size(); i++){
			Term t = vars.get(i);
			String var = t.getVar();
			String useThisVar=var;
			//the name used in the query; for SQL input this is different than var
			//if it exists use that instead of var
			String qVar = t.getQueryName();
			if(qVar!=null)
				useThisVar = qVar;
			//if it's a constant add a new select
			if(t instanceof ConstTerm){
				String oneSelect = t.getSqlValNoType() + " as " + useThisVar +" ";
				newSelect.add(oneSelect);
			}
			else if(t instanceof FunctionTerm){
				String actualName = child.getActualName(t.getFunction());
				String oneSelect = actualName + " as " + useThisVar + " ";
				newSelect.add(oneSelect);
			}
			else{
				//it's a VarTerm
				//System.out.println(" Var= " + var);
				for(int j=0; j<child.sqlSelect.size(); j++){
					String oneSelect = child.sqlSelect.get(j);
					//System.out.println("Select=" + oneSelect);
					if(oneSelect.endsWith(" as " + var + " ")){
						//System.out.println("Add it=" + oneSelect);
						if(qVar==null)
							newSelect.add(oneSelect);
						else{
							int ind = oneSelect.indexOf(" as " + var + " ");
							newSelect.add(oneSelect.substring(0, ind) + " as " + useThisVar + " ");
						}
						//I want to add it only once
						break;
					}
				}
			}
		}
		//System.out.println("NS=" + newSelect);
		sqlSelect = newSelect;
	}

    public String getString(){
    	String s = "";
    	s += "------------ Project " + this+ "---------\n";
    	s += project + "\n";
    	s += "child=" + child.getString();

    	s += "-------------------------------------\n";
    	return s;
    }

    // COST ESTIMATION

    // the AttributeStatistis for the projected attributes remaines unchanged
    // the only thing that changes is the "nrTuples" in StatisticsInfo;
    // the duplicates are removed
    // the nrTuples = max(distinct vals of all projected attributes)
    public StatisticsInfo evaluateSizeCost() throws MediatorException{

	System.out.println("Project::SizeStat..............");

	StatisticsInfo newStats = new StatisticsInfo();

	StatisticsInfo substats = child.evaluateSizeCost();
	System.out.println("Project::SizeStat..............");
	System.out.println("SUBSTATS..............");
	substats.print();

	int maxDistinct = 0;
	ArrayList<String> outputattrs = getAllActualVarNames();
	for(String actualName : outputattrs){
	    //System.out.println("ACTUAL NAME =" + actualName);

	    AttributeStatistics as = substats.getStats(actualName);

	    newStats.putStats(actualName, as);

	    if(maxDistinct<as.distinct) maxDistinct = as.distinct;
	}

	// get the max of the distinct vals of all projected attributes
	if(maxDistinct<substats.nrTuples)
	    newStats.nrTuples = maxDistinct;
	else
	    newStats.nrTuples = substats.nrTuples;

	stats=newStats;

	System.out.println("NEW STATS Project..............");
	newStats.print();
	System.out.println("*********************************");

	return newStats;
    }
    ////////////////////////////////////////////

}
