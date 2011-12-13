// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.graph;

import java.util.*;

import edu.isi.mediator.gav.graph.cost_estimation.AttributeStatistics;
import edu.isi.mediator.gav.graph.cost_estimation.CostUtil;
import edu.isi.mediator.gav.graph.cost_estimation.StatisticsInfo;
import edu.isi.mediator.gav.main.MediatorException;

public class JoinNode extends BaseNode
{
	
	ArrayList<String> joinAttributes;
    public BaseNode child1;
    public BaseNode child2;

    public JoinNode(ArrayList<String> joinAttrs, BaseNode child1, BaseNode child2){
    	joinAttributes=joinAttrs;
    	this.child1=child1;
    	this.child2=child2;
    }
    
    public ArrayList<BaseNode> getSubNodes() {
        ArrayList<BaseNode> subnodes = new ArrayList();
	if (child1 != null)
	    subnodes.add(child1);
	if (child2 != null)
	    subnodes.add(child2);
	return subnodes;
    }
    
    public void setSQL() throws MediatorException{
    	
		//I already did setSQL() in this node
    	//because of binding patterns there may be 2 ways to this node
		if(!sqlSelect.isEmpty())
			return;

    	child1.setSQL();
    	child2.setSQL();
    	
    	//where = child1.where + child2.where + the joinConditions
    	for(int i=0; i<joinAttributes.size(); i++){
    		String joinAttr = joinAttributes.get(i);
    		String actualName1 = child1.getActualName(joinAttr);
    		String actualName2 = child2.getActualName(joinAttr);
    		String bindMarker1 = child1.needsBinding(joinAttr);
    		String bindMarker2 = child2.needsBinding(joinAttr);
    		//System.out.println("Add Join=" + bindMarker1 + actualName1 + "=" + bindMarker2 + actualName2);
    		sqlWhere.add(bindMarker1 + actualName1 + "=" + bindMarker2 + actualName2);
    	}
    	sqlWhere.addAll(child1.sqlWhere);
    	sqlWhere.addAll(child2.sqlWhere);
    	
    	//union the froms
    	sqlFrom.addAll(child1.sqlFrom);
    	sqlFrom.addAll(child2.sqlFrom);
    	
    	//for each of the joinAttrs leave in the select only one
    	ArrayList<String> selectWithoutJoinAttrs = child1.removeFromSelect(joinAttributes);
    	sqlSelect.addAll(child2.sqlSelect);
    	sqlSelect.addAll(selectWithoutJoinAttrs);
    	for(int i=0; i<selectWithoutJoinAttrs.size(); i++){
    		String oneSelect = selectWithoutJoinAttrs.get(i);
    		if(!sqlSelect.contains(oneSelect))
    			sqlSelect.add(oneSelect);
    	}
		//System.out.println("Join select=" + sqlSelect);
    }

    public String getString(){
    	alreadyPrinted=true;
    	String s = "";
    	s += getPrintName();
    	s += "joinAttributes=" + joinAttributes + "\n";
    	if(child1 == null)
    		s += "child1=NULL\n";
    	else{
    		if(child1.alreadyPrinted)
    			s += "child1=" + child1.getPrintName();
    		else
    			s += "child1=" + child1.getString();
    	}
    	if(child2 == null)
    		s += "child2=NULL\n";
    	else{
    		if(child2.alreadyPrinted)
    			s += "child2=" + child2.getPrintName();
    		else
    			s += "child2=" + child2.getString();
    	}
    	s += "-------------------------------------\n";
    	return s;
    }
    
    public String getPrintName(){
    	return "------------ Join " + this+ "---------------\n";
    }

    // COST ESTIMATION
    /* EQUI-join :  R1 X R2 / max(Va, Vb) = R1/Va X R2/Vb X min(Va, Vb)
     *            = (ka X kb) X min(Va, Vb)
     * THETA-join : I'd like to do little more acurate measure of theta-join, but
     *              for now, let's use simplest way(theta-joins are rare anyway)
     *              R1 X R2 / 2
     * --------------------------------------------------------------------------
     * Instead of dividing the size of result by 2 for the second join condition,
     * I am going to use the following to compute the size of result.
     * underlying assumption : I.  uniform distribution of values
     *                         II. no attribute dependency.
     * --------------------------------------------------------------------------
     * for each join condition (op a b) where op is [=|<|>...], a and b are attributes
     * {
     *    ka = r1/Va, kb = r2/Vb
     *    equi-join case:
     *         dynamic_part = ka * kb
     *         tail_part    = min(Va, Vb)
     *    theta-join case:
     *         dynamic_part(no change)
     *         tail_part /= 2
     *    r1 = ka, r2 = kb // !Note that r1 and r2 are assigned to size of each class a,b!
     * }
     * r' = dynamic_part * tail_part
     * compute new stat for projection variable using function phi()
     * --------------------------------------------------------------------------
     * example : rel1(a,c,e) |X| rel2(b,d,f)
     *          |rel1| = R1, |rel2| = R2
     *          join condition jc1 (= a b), jc2 (= c d), jc3(> e f)
     *
     *   (= a b) : r' =  (ka * kb)    *   min(Va,Vb)
     *                  --dynamic--       -tail------
     *   (= c d) : r' =  (kc * kd)    *   min(Va,Vb) * min(Vc,Vd)
     *                  --dynamic--       -tail------------------
     *   (> e f) : r' =  (kc * kd)    *   min(Va,Vb) * min(Vc,Vd) / 2
     *                  --dynamic--       -tail-----------------------
     *
     * take the final r' as the size of result for all join conditions.
     */
    /*
    public StatisticsInfo evaluateSizeCost(){
    	System.out.println("Join::SizeStat..............EvalA");
    	StatisticsInfo substatsA = null;
    	// the children can point to nodes for which I already 
    	// computed the statistics
    	if(child1.getStats()==null)
    		substatsA = child1.evaluateSizeCost();
    	else
    		substatsA = child1.getStats();

    	System.out.println("Join::SizeStat..............EvalA");
    	System.out.println("SUBSTATS-A..............");
    	substatsA.print();

    	System.out.println("Join::SizeStat..............EvalB");
    	StatisticsInfo substatsB = null;
    	if(child2.getStats()==null)
    		substatsB = child2.evaluateSizeCost();
    	else
    		substatsB = child2.getStats();
    	System.out.println("Join::SizeStat..............EvalB");
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

    	for(String var : joinAttributes){
    			// EQUI-join

    			// look for sts of var_a in substatsA, and var_b in substatsB
    			AttributeStatistics stat_a = localStats[0].getStats(var);
    			AttributeStatistics stat_b = localStats[1].getStats(var);

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

    			//NOT SURE ABOUT THIS
    			tail_part.add(var, v_a, var, v_b);
    			//tail_part.add(var_a, v_a, var_b, v_b);
    		
    		else {
    			// THETA-join
    			//we never have this in current mediator
    			tail_part.increaseDiv2();
    		}
    		
    	} // for all join conditions

    	// NEW SIZE

    	new_r = np[0]*np[1];

    	new_r *= tail_part.getValue();

    	new_stat.nrTuples=new_r;

    	// for each projection variable, compute the new stat 
    	if(outputattrs == null) return new_stat;

    	for(int i=0; i<outputattrs.size(); i++){
    		String outName = ((String)outputattrs.get(i)).trim();
    		String actualName = getActualName(outName);

    		AttributeStatistics as = localStats[0].getStats(actualName);
    		int r = localStats[0].nrTuples;
    		if(as==null){//try the other one
    			as = localStats[1].getStats(actualName);
    			r = localStats[1].nrTuples;
    		}

    		// compute new variety and new extremes if numerical  
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
    	Vector<AttrAndDistinctVals> collections = new Vector<AttrAndDistinctVals>();
    	int div2=0;

    	double getValue(){
    		double ret = 1.0;
    		for(int i=0; i<collections.size(); i++){
    			AttrAndDistinctVals  ad =collections.elementAt(i);
    			ret *= ad.getMin();
    		}
    		if(div2!=0) ret /= (2.0 * div2);
    		return ret;
    	}

    	void add(String a, int va, String b, int vb) {
    		for(int i=0; i<collections.size(); i++){
    			AttrAndDistinctVals  ad = collections.elementAt(i);
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
*/
    /////////////////////////////////////////

}
