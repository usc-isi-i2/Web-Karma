package edu.isi.mediator.gav.graph.cost_estimation;

public class AttributeStatistics{

    //SIZE COST
    //nr of distinct values
    public int      distinct=0;
    // for numeric attributes only
    public double   min;  // null if not applicable
    public double   max;  // null if not applicable
    public boolean  isNumeric=false;
    ///////////////////

    public AttributeStatistics(){}
    public AttributeStatistics(int d){distinct = d;}
    public AttributeStatistics(int d, double a, double b){
	distinct = d;
	min = a;
	max = b;
	isNumeric = true;
    }
    public AttributeStatistics(String d, String a, String b){
	try{
	    distinct = Integer.valueOf(d).intValue();
	    min = Double.valueOf(a).doubleValue();
	    max = Double.valueOf(b).doubleValue();
	}
	catch(Exception e){
	    e.printStackTrace();
	}
	isNumeric = true;
    }
    public AttributeStatistics(String d){
	try{
	    distinct = Integer.valueOf(d).intValue();
	}
	catch(Exception e){
	    e.printStackTrace();
	}
    }
    public AttributeStatistics deepClone(){
	AttributeStatistics as = new AttributeStatistics(distinct,min,max);
	as.isNumeric = isNumeric;
	return as;
    }
    public void print(){
	System.out.print("DISTINCT VALS = " + distinct);
	if(isNumeric){
	    System.out.println("\tMIN = " + min + "\tMAX = " + max);
	}
	else 	System.out.print("\n");
    }

}
