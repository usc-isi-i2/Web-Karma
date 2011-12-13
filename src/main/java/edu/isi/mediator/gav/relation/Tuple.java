// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.relation;


import java.util.ArrayList;
public class Tuple
{ 
  protected ArrayList values = new ArrayList();

  public Tuple(){
  }
  
  public Tuple(ArrayList a_values)
  {
    values = a_values;
  }

  public Tuple clone() 
  {
    ArrayList objs = new ArrayList();
    for (int i=0; i<values.size(); i++)
      objs.add(new String(values.get(i).toString())); 
    return new Tuple(objs);  
  }

  public void appendTuple(Tuple t){
	  for(int i=0; i<t.values.size(); i++)
		  values.add(t.values.get(i));
  }
  
  public Object getValue(int a_pos)
  {
      if(a_pos<0)
	  return null;
      return a_pos<values.size() ? values.get(a_pos) : null;
  }

  public void setValueAtPosition(int a_pos, Object a_obj)
  {
    if (a_pos < values.size())
      values.set(a_pos,a_obj);
  }

  public void addValue(Object a_obj)
  {
      values.add(a_obj);
  }

  public Tuple removeValues(ArrayList<Integer> pos)
  {
	  Tuple t = new Tuple();
	  for(int i=0; i<values.size(); i++){
		  boolean addIt=true;
		  for(int k=0; k<pos.size(); k++){
			  int onePos = pos.get(k).intValue();
			  //System.out.println("One Pos=" + onePos + " "  + i) ;
			  if(i==onePos){
				  addIt=false;
				  break;
			  }
		  }
		  if(addIt){
			  //System.out.println("Add IT........");
			  t.addValue(values.get(i));
		  }
	  }
	  return t;
  }

    // returns true if the values of these 2 tuples are equal
    public boolean equals(Tuple t){
	ArrayList values = t.values;
	if(values.size()!=values.size())
	    return false;
	for(int i=0; i<values.size(); i++)
	    if(!values.get(i).equals(values.get(i)))
		return false;
	return true;
    }

    //join this tuple with t on the attributes in joinAttr1 and joinAttr2
    //return null if they don't join
    public Tuple join(Tuple t, ArrayList<Integer> joinAttr1, ArrayList<Integer> joinAttr2){
    	
    	//System.out.println("Join Tuples:" + this + " AND " + t + "\n on attributes:" + joinAttr1 + " AND " + joinAttr2);
    	
    	Tuple result = new Tuple();
    	boolean noJoin=false;
    	
    	if(joinAttr1==null && joinAttr2==null){
    		//cross product
    		result.appendTuple(this);
    		result.appendTuple(t);
    	}
    	else{
    		for(int i=0; i<joinAttr1.size(); i++){
    			int i1 = joinAttr1.get(i).intValue();
    			int i2 = joinAttr2.get(i).intValue();
    			//System.out.println("Values are: " + getValue(i1) + " AND " + getValue(i2));
    			if(!getValue(i1).equals(t.getValue(i2))){
    				noJoin=true; break;
    			}
    		}
    		
    		if(!noJoin){
    			//join the 2 tuples
    			result.appendTuple(this);
    			result.appendTuple(t);
    			//now remove the join values
    			result = result.removeValues(joinAttr1);
    		}
    	}
    	if(noJoin)
    		return null;
    	else
    		return result;    	
    }
    
    //return this tuple if the value at position pos is val
    public Tuple select(int pos, String val){
    	if(getValue(pos).equals(val)){
    		return this.clone();
    	}
    	else return null;
    }
    
    //return this tuple if the value at position pos1 is = with value at pos2
    public Tuple select(int pos1, int pos2){
    	//System.out.println("Tuple=" + this + " select on " + pos1 + " and " + pos2);
    	String val1 = (String)getValue(pos1);
    	String val2 = (String)getValue(pos2);
    	//System.out.println("val1= " + val1 + " and val2=" + val2);
    	if(val1.equals(val2)){
    		return this.clone();
    	}
    	else return null;
    }
    
    // returns true if the 2 tuples are unique along a set of attributes
    // whose positions are described in pos[]
    public boolean distinct(Tuple t, int pos[]){
	for(int i=0; i<pos.length; i++){
	    if(getValue(pos[i])!=null){
		if(!getValue(pos[i]).
		   equals(t.getValue(pos[i])))
		    return true;
	    }
	    else{
		if(t.getValue(pos[i])!=null)
		    return true;
	    }
	}
	return false;
    }

    // returns true if the 2 tuples are unique along ALL attributes
    public boolean distinct(Tuple t){
	for(int i=0; i<values.size(); i++){
	    if(values.get(i)!=null){
		if(!values.get(i).equals(t.values.get(i)))
		    return true;
	    }
	    else{
		if(t.values.get(i)!=null)
		    return true;
	    }
	}
	return false;
    }

 
  public String toString() 
  { 
    String data = "(";
    for (int i=0; i<values.size(); i++){
	    String value = "null";
	    if(values.get(i)!=null)
		value = values.get(i).toString();
	    data += (i>0 ? ", " : "")+value;
    }
    return data + ")";
  } 

 }
