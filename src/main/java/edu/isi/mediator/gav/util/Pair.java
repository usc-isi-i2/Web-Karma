// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.util;

public class Pair implements Comparable{

    public Object o1;
    public Object o2;

    public Pair(Object o11, Object o22){
	o1=o11;
	o2=o22;
    }

    // is o2 is an Integer it compares the 2
    public int compareTo(Object o){
	Pair thisPair = (Pair)o;
	Integer i1 = (Integer)o2;
	Integer i2 = (Integer)thisPair.o2;
	//System.out.println("i1=" + i1);
	//System.out.println("i2=" + i2);
	if(i1.intValue()==i2.intValue()) return 0; 
	else if(i1.intValue()>i2.intValue()) return 1;
	else return -1;
    }

    public String toString(){
	String ret = "", ret1="", ret2="";
	if(o1 == null)
	    ret1 = "null";
	else ret1=o1.toString();
	if(o2 == null)
	    ret2 = "null";
	else ret2=o2.toString();
	ret += ret1 + " " + ret2;
	return ret;
    }
}
