// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.graph;

import java.util.*;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.gav.main.MediatorInstance;
import edu.isi.mediator.gav.graph.BaseNode;
import edu.isi.mediator.gav.graph.cost_estimation.AttributeStatistics;
import edu.isi.mediator.gav.graph.cost_estimation.CostUtil;
import edu.isi.mediator.gav.graph.cost_estimation.StatisticsInfo;
import edu.isi.mediator.rule.ConstTerm;
import edu.isi.mediator.rule.FunctionTerm;
import edu.isi.mediator.rule.RelationPredicate;
import edu.isi.mediator.rule.Term;
import edu.isi.mediator.rule.VarTerm;
import edu.isi.mediator.gav.source.SourceAttribute;

public class DataAccessNode extends BaseNode
{
	
	private RelationPredicate source;
	private BaseNode child;
	
	
	/**
	 * Used to generate unique table names
	 */
	
	public DataAccessNode(RelationPredicate p){
		source = p;
	}
	
	public void setChild(BaseNode n){
		child=n;
	}
	
	public ArrayList<BaseNode> getSubNodes() {
		ArrayList<BaseNode> subnodes = new ArrayList<BaseNode>();
		if (child != null)
			subnodes.add(child);
		return subnodes;
	}

    protected SourceAttribute getSourceAttribute(String attr){
    	return source.getSourceAttribute(attr);
    }
    
    //returns "@" if this attribute is bound in the data source
    String needsBinding(String attr){
    	return source.needsBinding(attr);
    }
    
	//go to all DAN in dn and see if we find common attributes
	//returns the common attributes in commonAttrs
	public void findCommonAttrs(BaseNode dn, ArrayList<String> commonAttrs){
		//System.out.println("Find common DN=" + this.getString());
		if(dn instanceof DataAccessNode){
			//System.out.println("try with DN=" + dn.getString());
			ArrayList<String> joinAttrs = source.findCommonAttrs(((DataAccessNode)dn).source);
			for(int i=0; i<joinAttrs.size(); i++){
				String attr = joinAttrs.get(i);
				if(!commonAttrs.contains(attr))
					commonAttrs.add(attr);
			}
			//System.out.println(" commonAttrs " + commonAttrs);
		}
		else{
			ArrayList subnodes = dn.getSubNodes();
			for(int i=0; i<subnodes.size(); i++){
				BaseNode child = (BaseNode)subnodes.get(i);
				findCommonAttrs(child, commonAttrs);
			}	
		}
	}
	
	public void setSQL() throws MediatorException{
		//if (childNode != null)
			//return new ArrayList();
		
		//I already did setSQL() in this node
    	//because of binding patterns there may be 2 ways to this node
		if(!sqlSelect.isEmpty())
			return;
		
		if(child!=null){
			child.setSQL();
		}
		
		//MariaM:120810
		//if the table name is escaped, escape the entire alias
		String sourceName = source.getName();
		int sourceId = MediatorConstants.getTableId();
		String newName = sourceName + "_" + sourceId;
		//we use endsWith instead of startsWith because for sources that include the OGSA-DAI resource
		// it looks like: TestR2_`table name`
		if(sourceName.endsWith("`"))
			newName = sourceName.substring(0, sourceName.length()-1) + "_" + sourceId + "`";
		
		if(newName.length()>29)
			newName = "T"+MediatorConstants.getTableId();
		sqlFrom.add(source.getSQLName()+" "+ newName);
		String tableName = newName;
		

		//set select
		sqlSelect = getSelect(tableName);
		
		//System.out.println("DN select=" + sqlSelect);
		
		//set where
		sqlWhere = getWhere(tableName, child);
		
	}

	/**
	 * Returns the "select" SQL statement based on terms and source attributes
	 * @param tableName
	 * 			the source name of this relation
	 * @return
	 * 		"select" SQL statement
	 */
	public ArrayList<String> getSelect(String tableName){
		//System.out.println("P in getSelect():" + this + "***" + this.hashCode());
		ArrayList<String> select = new ArrayList<String>();
		for(int i=0; i<source.getTerms().size(); i++){
			Term t = source.getTerms().get(i);
			if(t.getVar()==null) continue;
			SourceAttribute sourceAttr = source.getSourceAttribute(i);
			//I'm adding a space at then end, so it's easier when I have to search for the var name
			//otherwise if I search for "S" it will return true for any variable that starts with "S"
			select.add(tableName+"."+sourceAttr.getSQLName()+" as "+t.getVar() + " ");
		}
		return select;
	}

	//if I have 2 terms that are the same variable we will have a join
	//if I have a bound attr that is not a constant it will be handled in the Join
	/**
	 * Returns the "where" SQL statement based on terms and sourceAttrs.
	 * <br> If the term is a constant or it is a bound variable that is satisfied by a constant
	 * <br> a comparison is added to the where clause
	 * @param tableName
	 * 		the source name of this relation
	 * @param child
	 * 		the child of the DataAccessNode that represents this relation
	 * @return
	 * 		"where" SQL statement
	 * @throws MediatorException
	 */
	public ArrayList<String> getWhere(String tableName, BaseNode child) throws MediatorException{
		ArrayList<String> where = new ArrayList<String>();
		for(int i=0; i<source.getTerms().size(); i++){
			Term t = source.getTerms().get(i);
			SourceAttribute sourceAttr = source.getSourceAttribute(i);
			boolean isBound = sourceAttr.needsBinding();
			//System.out.println("Pred=" + name + " SourceAttr is bound " + sourceAttr + " " + isBound);
			String bindMarker="";
			if(isBound){
				bindMarker="@";
			}
			if(t instanceof ConstTerm){
				String val = ((ConstTerm)t).getSqlVal(sourceAttr.isNumber());
				if(val.equals(MediatorConstants.NULL_VALUE))
					where.add(bindMarker + tableName+"."+sourceAttr.getSQLName()+" IS "+val);
				else
					where.add(bindMarker + tableName+"."+sourceAttr.getSQLName()+" = "+val);
			}
			if(t instanceof FunctionTerm){
				if(child!=null){
					//I have attribute binding patterns in the function
					where.add(bindMarker + tableName+"."+sourceAttr.getSQLName()+" = "+child.getActualName(((FunctionTerm)t).getFunction()));
				}
				else{
					//the function has only constants as input
					where.add(bindMarker + tableName+"."+sourceAttr.getSQLName()+" = "+((FunctionTerm)t).getFunctionForSQL());
				}
			}
		}
		for(int i=0; i<source.getTerms().size(); i++){
			Term t = source.getTerms().get(i);
			SourceAttribute sourceAttr1 = source.getSourceAttribute(i);
			//System.out.println("Find duplicate terms:" + t);
			if(t instanceof VarTerm){
				//System.out.println("Not Constant...");
				int j = i+1;
				while(j>0){
					j = source.findTerm(t, j);
					if(j>0){
						SourceAttribute sourceAttr2 = source.getSourceAttribute(j);
						where.add(tableName+"."+sourceAttr1.getSQLName()+" = "+tableName + "." + sourceAttr2.getSQLName());
						j++;
					}
				}
			}
		}
		return where;
	}

	public String getString(){
		alreadyPrinted=true;
		String s = "";
		s += getPrintName();
		s += source + "\n";
		if(child == null)
			s += "child=NULL\n";
		else{
    		if(child.alreadyPrinted)
    			s += "child=" + child.getPrintName();
    		else
    			s += "child="+ child.getString();
		}
		s += "Statistics:" + stats + "\n";
		s += "-------------------------------------\n";
		return s;
	}
	
    public String getPrintName(){
    	return "------------ DataAccess " + this+ "---------------\n";
    }

	/////////////////
    
    // COST ESTIMATION

    // if the childNode is something other than a ConstantRelNode, then
    // combine the statistics from the source with the statistics from the
    // child node
    //laptop@KM(computerid:string:f:100),(100, 1000, 1, false)
    //computerhasProcessor@KM(computerid:string:b:1, processor:number:f:1:0:100000),(1, 10, 1, false)
    // for attributes that appear both in the source stats and in the child
    // multiply the distinct values, take the max out of the 2 maxes and the min out of the 2 mins
    // for the number of tuples, multiply the one from the source with the
    // ones from the child
    public StatisticsInfo evaluateSizeCost() throws MediatorException{

    	String fullSourceName = source.getName();
    	System.out.println("Data Source::SizeStat.............." + fullSourceName);

    	StatisticsInfo newStats = new StatisticsInfo();
    	StatisticsInfo substats = getSourceStats(fullSourceName);
    	System.out.println("SUBSTATS..............");
    	substats.print();
    	if(child==null){
    		// similar to the ProjectNode evaluate
    		newStats = evaluateSizeCost(substats);
    	}
    	else{
    		//similar to the JoinNode evaluate; if I have binding patterns
    		if(child.getStats()==null)
    			newStats = evaluateSizeCost(substats, child.evaluateSizeCost());
    		else
    			newStats = evaluateSizeCost(substats, child.getStats());
    	}


    	stats = newStats;

    	System.out.println("NEW STATS Data Access..............");
    	newStats.print();

    	System.out.println("*********************************");

    	return newStats;
    }

    StatisticsInfo getSourceStats(String fullSourceName){
    	//Mediator.printSourceStatistics();
    	//System.out.println("GET STATS FOR:" + fullSourceName);
    	return MediatorInstance.sizeEstimation.get(fullSourceName);
    }

    // same as ProjectNode 
    public StatisticsInfo evaluateSizeCost(StatisticsInfo substats) throws MediatorException{

	StatisticsInfo newStats = new StatisticsInfo();
	int maxDistinct = 0;
	ArrayList<String> outputattrs = getAllActualVarNames();
	for(String actualName : outputattrs){
	    //System.out.println("ON="+outName + " AN=" + actualName);
	    AttributeStatistics as = substats.getStats(actualName);

	    newStats.putStats(actualName, as);

	    if(maxDistinct<as.distinct) maxDistinct = as.distinct;
	}

	// get the max of the distinct vals of all projected attributes
	if(maxDistinct<substats.nrTuples)
	    newStats.nrTuples = maxDistinct;
	else
	    newStats.nrTuples = substats.nrTuples;

	return newStats;
    }

    // similar to the JoinNode, but in the joinconditions I have only
    // equality
    public StatisticsInfo evaluateSizeCost(StatisticsInfo substatsA,
    									   StatisticsInfo substatsB) throws MediatorException{


    	System.out.println("SUBSTATS-A..............");
    	substatsA.print();

    	System.out.println("SUBSTATS-B..............");
    	substatsB.print();

    	StatisticsInfo new_stat = new StatisticsInfo();

    	if(substatsA.nrTuples ==0 || substatsB.nrTuples==0){
    		new_stat.nrTuples=0;
    		return new_stat;
    	}

    	int np[] = new int[2];
    	int n[] = new int[2];
    	np[0]=substatsA.nrTuples;
    	np[1]=substatsB.nrTuples;
    	n[0]=substatsA.nrTuples;
    	n[1]=substatsB.nrTuples;
    	StatisticsInfo localStats[] = new StatisticsInfo[2];
    	localStats[0]=substatsA.deepClone();
    	localStats[1]=substatsB.deepClone();

    	AttrAndDistinctValsList tail_part = new AttrAndDistinctValsList();

    	int new_r = 0;

    	ArrayList<String> boundAttr = source.getNeedBindingVars();
    	for(String var : boundAttr){
    		// EQUI-join

    		String var_a = getActualName(var);
    		String var_b = child.getActualName(var);
    		//System.out.println("var_a=" + var_a + " var_b=" + var_b);

    		// look for sts of var_a in substatsA, and var_b in substatsB
    		AttributeStatistics stat_a = localStats[0].getStats(var_a);
    		AttributeStatistics stat_b = localStats[1].getStats(var_b);

    		int v_a = CostUtil.phi(n[0], np[0], stat_a.distinct);
    		int v_b = CostUtil.phi(n[1], np[1], stat_b.distinct);

    		//System.out.println("V_A=" + v_a);
    		//System.out.println("V_B=" + v_b);

    		int k_a = np[0]/v_a;
    		int k_b = np[1]/v_b;

    		if(k_a==0) k_a=1;
    		if(k_b==0) k_b=1;

    		// update local variety
    		stat_a.distinct=v_a;
    		stat_b.distinct=v_b;
    		n[0] = np[0];
    		np[0] = k_a;
    		n[1] = np[1];
    		np[1] = k_b;

    		tail_part.add(var_a, v_a, var_b, v_b);
    	} // for all join conditions

    	// NEW SIZE

    	new_r = np[0]*np[1];

    	new_r *= tail_part.getValue();

    	new_stat.nrTuples=new_r;

    	/* for each projection variable, compute the new stat */
    	ArrayList<String> outputattrs = source.getVars();
    	for(String outName : outputattrs){
    		String actualName = getActualName(outName);

    		AttributeStatistics as = localStats[0].getStats(actualName);
    		int r = localStats[0].nrTuples;
    		if(as==null){//try the other one
    			as = localStats[1].getStats(actualName);
    			r = localStats[1].nrTuples;
    		}

    		/* compute new variety and new extremes if numerical  */
    		int new_v = CostUtil.phi(r, new_r, as.distinct);
    		AttributeStatistics ns = new AttributeStatistics();
    		ns.distinct= new_v;

    		if(as.isNumeric) {
    			if(as.distinct==new_v) { 
    				// if variety is same, no need to change the extreme values
    				ns.max=as.max;
    				ns.min=as.min;
    			} else {
    				ns.max=CostUtil.computeNewMax(as.min, as.max, new_v);
    				ns.min=CostUtil.computeNewMin(as.min, as.max, new_v);
    			}
    		}

    		new_stat.putStats(actualName, ns);
    	}   
    	stats = new_stat;

    	System.out.println("NEW STATS Join..............");
    	new_stat.print();
    	System.out.println("*********************************");

    	return new_stat;
    }

    class AttrAndDistinctVals{
    	// contains attr names
    	Vector<String> attrs = new Vector<String>();
    	// contains ints that represent nr of distinct vals; Integer
    	Vector<Integer> distinct = new Vector<Integer>();

    	boolean isIn(String name){
    		if(attrs.contains(name)) return true;
    		return false;
    	}
    	void add (String name, int dist){
    		attrs.add(name);
    		distinct.add(new Integer(dist));
    	}
    	int getMin(){
    		if(distinct.isEmpty()) return -1;
    		int ret = ((Integer)distinct.elementAt(0)).intValue();
    		for(int i=0; i<distinct.size(); i++){
    			int newD = ((Integer)distinct.elementAt(i)).intValue();
    			if(newD<ret) ret = newD;
    		}
    		return ret;
    	}
    } 

    class AttrAndDistinctValsList{
    	// contains AttrAndDistinctVals
    	Vector collections = new Vector();
    	int div2=0;

    	double getValue(){
    		double ret = 1.0;
    		for(int i=0; i<collections.size(); i++){
    			AttrAndDistinctVals  ad = 
    				(AttrAndDistinctVals)collections.elementAt(i);
    			ret *= ad.getMin();
    		}
    		if(div2!=0) ret /= (2.0 * div2);
    		return ret;
    	}

    	void add(String a, int va, String b, int vb) {
    		for(int i=0; i<collections.size(); i++){
    			AttrAndDistinctVals  ad = 
    				(AttrAndDistinctVals)collections.elementAt(i);
    			if(ad.isIn(a)) {
    				ad.add(b,vb);
    				return;
    			}
    			if(ad.isIn(b)) {
    				ad.add(a,va);
    				return;
    			}
    		}
    		AttrAndDistinctVals new_list = new AttrAndDistinctVals();
    		new_list.add(a,va);
    		new_list.add(b,vb);
    		collections.add(new_list);
    	}

    	void increaseDiv2() { ++div2; }
    }

    ///////////////////////////////


}
