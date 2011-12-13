package edu.isi.mediator.gav.graph.cost_estimation;

import java.util.*;

public class StatisticsInfo{

    //SIZE COST

    // the number of tuples returned by this node
    public int nrTuples=0;

    // the statistics of each attribute; contains AttributeStatistics
    public HashMap attrStats = new HashMap();
    ////////////////

    //TIME
    public int time = 0;
    ////////

    //CONFIDENCE; between 0 - 1
    public double confidence = 1;
    ///////////////////

    //COMPLETENESS; true = complete (all possible tuples are returned)
    public boolean completeness = true;
    ////////////////////

    public StatisticsInfo(int nrT){
	nrTuples = nrT;
    }
    public StatisticsInfo(){
    }
    public void setInfo(String snrTuples, String stime, 
		   String sconfidence, String scompleteness){
	try{
	    nrTuples = Integer.valueOf(snrTuples).intValue();
	    time = Integer.valueOf(stime).intValue();
	    confidence = Double.valueOf(sconfidence).doubleValue();
	    completeness = Boolean.valueOf(scompleteness).booleanValue();
	}
	catch(Exception e){
	    e.printStackTrace();
	}
    }
    public void setInfo(int snrTuples, int stime, 
		   double sconfidence, boolean scompleteness){
	try{
	    nrTuples = snrTuples;
	    time = stime;
	    confidence = sconfidence;
	    completeness = scompleteness;
	}
	catch(Exception e){
	    e.printStackTrace();
	}
    }

    public StatisticsInfo deepClone(){

	StatisticsInfo s = new StatisticsInfo();
	s.setInfo(nrTuples, time, confidence, completeness);
	Iterator it = attrStats.entrySet().iterator();
	while(it.hasNext()){
	    Map.Entry e = (Map.Entry)it.next();
	    s.putStats((String)e.getKey(), 
		       ((AttributeStatistics)e.getValue()).deepClone());
	}
	return s;
    }

    // get the statistics for the attribute specified by "name"
    // if name starts with l. or r. remove that and look again
    public AttributeStatistics getStats(String name){

	AttributeStatistics stats = (AttributeStatistics)attrStats.get(name);
	if(stats==null){
	    // if name starts with l. or r. remove that and look again
	    if(name.startsWith("l.") || name.startsWith("r.")){
		stats = (AttributeStatistics)attrStats.get(name.substring(2));
	    }
	}

	return stats;

    }
    public AttributeStatistics putStats(String name, AttributeStatistics as){

	return (AttributeStatistics)attrStats.put(name,as);

    }

    public void print(){
	System.out.println("----------------SIZE---------------");
	System.out.println("NR TUPLES = " + nrTuples);
	Iterator it = attrStats.entrySet().iterator();
	while(it.hasNext()){
	    Map.Entry e = (Map.Entry)it.next();
	    System.out.print("ATTR NAME=" + (String)e.getKey() + "\t");
	    ((AttributeStatistics)e.getValue()).print();
	}
	System.out.println("-------------------------------");
	System.out.println("TIME = " + time);
	System.out.println("CONFIDENCE = " + confidence);
	System.out.println("COMPLETENESS = " + completeness);
	System.out.println("-------------------------------");
    }
}
