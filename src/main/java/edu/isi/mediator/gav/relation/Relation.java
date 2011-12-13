// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.relation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import edu.isi.mediator.gav.main.MediatorException;

public class Relation 
{ 
  private String name;
  private AttrList attrList;
  private ArrayList<Tuple> tuples = new ArrayList<Tuple>();

  public Relation(String a_name, String a_attrList) 
      throws MediatorException
  {
    name = a_name.toLowerCase();
    attrList = new AttrList(a_attrList);
  } 

  public Relation(String a_name, AttrList a_attrList) 
  {
    name = a_name.toLowerCase();
    attrList = a_attrList;
  } 

  public Relation(String a_name, int nr_attr) throws MediatorException
  {
    name = a_name.toLowerCase();
    String attrNames = "";
    for(int i=0; i<nr_attr; i++){
    	attrNames += "attr_" + i; 
    }
    AttrList al = new AttrList(attrNames);
    setAttrList(al);
  } 

  public Relation(String a_name)
  {
    name = a_name.toLowerCase();
    attrList=null;
  } 

  /*
  public Relation clone(){
	  Relation r = new Relation(name, attrList.clone());
	  for(int i=0; i<tuples.size(); i++){
		  r.addTuple(tuples.get(i).clone());
	  }
	  return r;
  }
  */

  public Relation clone(){
	  Relation r = new Relation(name);
	  if(attrList!=null){
		  r.attrList=attrList.clone();
	  }
	  for(int i=0; i<tuples.size(); i++){
		  r.addTuple(tuples.get(i).clone());
	  }
	  return r;
  }

  public boolean isEmpty(){
	  return tuples.isEmpty();
  }
  
  public void addTuple(Tuple a_row)
  {
    tuples.add(a_row);
  }

  public String getName() 
  { 
    return name;
  }

  public void setName(String name){
	  this.name=name;
  }
  
  public AttrList getAttrList() 
  { 
    return attrList;
  }

  public ArrayList<Tuple> getTuples() 
  { 
    return tuples;
  }

    public void setAttrList(AttrList attrList){
    	this.attrList=attrList;
    }

    public void setAttrListAllowDuplicates(ArrayList<String> attrs){
    	attrList = new AttrList();
    	for(int i=0; i<attrs.size(); i++){
    		attrList.addAttr(attrs.get(i));
    	}
    }
    
    public void addAttr(String attr) throws MediatorException{
    	attrList.addAttr(attr);
    }

    public int findPosition(String attrName){
    	return attrList.findPosition(attrName);
    }

    public ArrayList<Integer> findPosition(ArrayList<String> attrNames){
    	ArrayList<Integer> inds = new ArrayList<Integer>();
    
		for(int i=0; i<attrNames.size(); i++){
			String attrName = attrNames.get(i);
			inds.add(attrList.findPosition(attrName));
		}
		return inds;
    }

    //return all common attributes between this relation and r
	public ArrayList<String> findCommonAttrs(Relation r){
		//System.out.println("Common in: " + this + " and " + p);
		ArrayList<String> attrs = new ArrayList<String>();
		ArrayList<String> names1 = attrList.getNames();
		ArrayList<String> names2 = r.attrList.getNames();
		for(int i=0; i<names1.size(); i++){
			String name1 = names1.get(i);
			if(names2.contains(name1))
				attrs.add(name1);
		}
		if(attrs.isEmpty())
			return null;
		else
			return attrs;
	}

    //return all common attributes between this relation and r
	public ArrayList<Integer> findCommonAttrsInd(Relation r){
		//System.out.println("Common in: " + this + " and " + p);
		ArrayList<Integer> ind = new ArrayList<Integer>();
		ArrayList<String> names1 = attrList.getNames();
		ArrayList<String> names2 = r.attrList.getNames();
		for(int i=0; i<names1.size(); i++){
			String name1 = names1.get(i);
			if(names2.contains(name1))
				ind.add(i);
		}
		if(ind.isEmpty())
			return null;
		else
			return ind;
	}

	//returns true if we find a tuple which has the values of "attrs" equals to "vals"
	public boolean containsTuple(ArrayList<String> attrs, ArrayList<String> vals){

		System.out.println("Check Contains Tuple in:" + this);
		System.out.println("For Attributes:" + attrs);
		System.out.println("Vals:" + vals);
		
		  for(int i=0; i<tuples.size(); i++){
			  Tuple t = tuples.get(i);
			  boolean foundTuple = true;
			  for(int j=0; j<attrs.size(); j++){
				  String val2= vals.get(j);
				  //not_allowed is added during the expansion with uac rules; this var can be ignored as it
				  //shouldn't even be there; we add it so the user knows that he's not allowed to see it, and to padd for unions
				  if(val2.equals("\"NOT ALLOWED\"")) continue;
				  int pos = findPosition(attrs.get(j));
				  if(pos==-1) return false;
				  String val1 = (String)t.getValue(pos);
				  //System.out.println("val1=" + val1 + " val2=" + val2);
				  if(!val1.equals(val2)){
					  foundTuple=false;
					  break;
				  }
			  }
			  if(foundTuple)
				  return true;
		  }
		return false;
	}
	
  public Relation union(Relation a_rel)
  {
    /* FIX: should ensure that both are type compatible */
    Relation r = new Relation(getName()+"_"+a_rel.getName(), getAttrList());

    for (int i=0; i<tuples.size(); i++) 
      r.addTuple(tuples.get(i));

    ArrayList<Tuple> relTuples = a_rel.tuples;
    for (int i=0; i<relTuples.size(); i++) 
      r.addTuple(relTuples.get(i));

    return r;
  }

  public Relation join(Relation rel2){
	  
	  Relation rel = null;
	  if(rel2==null){
		  rel = clone();
		  rel.setName("ResultRelation");
	  }
	  else{
		  //join this with rel2
		  //first find the common attributes; I will join on those
		  ArrayList<String> joinAttrs = findCommonAttrs(rel2);
		  ArrayList<Integer> joinAttrsInd = findCommonAttrsInd(rel2);
		  rel = join(rel2,joinAttrs,joinAttrsInd, "ResultRelation");
		  //just for testing when I want to compare the 2 joins
		  //Relation relHash = joinNL(rel2,joinAttrs, "ResultRelation");
	  }
	  return rel;
  }

  //NestestLoops join
  //join this relation with rel
  //if joinAttrs=null => cross product
  //else join on joinAttrs
  public Relation joinNL(Relation rel, ArrayList<String> joinAttrs, String name){
	  
	  //System.out.println("Join Rel1:" + this + " Join rel2: " + rel);
	  //System.out.println("Join on Attrs:" + joinAttrs);
	  //System.out.println("Join Rel1:" + this.getTuples().size() + " Join rel2: " + rel.getTuples().size());
	  
	  Relation out = new Relation(name);
	  //I don't want the duplicate joinAttr in the attribute list
	  AttrList newAttrs = new AttrList();
	  if(joinAttrs==null){
		  newAttrs=getAttrList();
	  }
	  else{
		  for(int k=0; k<getAttrList().size(); k++){
			  Attr a = getAttrList().getAttrAtPosition(k);
			  String attrName = a.getName();
			  if(!joinAttrs.contains(attrName))
				  newAttrs.addAttr(a);
		  }
	  }
	  newAttrs.appendAttributes(rel.getAttrList());
	  out.setAttrList(newAttrs);
	  
	  //position of joinAttr
	  ArrayList<Integer> i1=null, i2=null;
	  if(joinAttrs!=null){
		  //find location of joinAttrs
		  i1 = findPosition(joinAttrs);
		  i2 = rel.findPosition(joinAttrs);
	  }
	  
	  for(int i=0; i<tuples.size(); i++){
		  Tuple t1 = tuples.get(i);
		  for(int j=0; j<rel.tuples.size(); j++){
			  Tuple t2 = rel.tuples.get(j);
			  //i1 and i2 will be null for cross product
			  Tuple t = t1.join(t2, i1, i2);
			  if(t!=null)
				  out.addTuple(t);
		  }
	  }
	  return out;
  }

  //HashJoin
  //join this relation with rel
  //if joinAttrs=null => cross product
  //else join on joinAttrs
  public Relation join(Relation rel, ArrayList<String> joinAttrs, ArrayList<Integer> joinAttrsInd, String name){
	  
	  //System.out.println("Join Rel1:" + this + " Join rel2: " + rel);
	  //System.out.println("Join on Attrs:" + joinAttrs);
	  //System.out.println("JoinHash Rel1:" + this.getTuples().size() + " JoinHash rel2: " + rel.getTuples().size());
	  
	  Relation out = new Relation(name);
	  //I don't want the duplicate joinAttr in the attribute list
	  AttrList newAttrs = new AttrList();
	  if(joinAttrs==null){
		  newAttrs=getAttrList();
	  }
	  else{
		  for(int k=0; k<getAttrList().size(); k++){
			  Attr a = getAttrList().getAttrAtPosition(k);
			  String attrName = a.getName();
			  if(!joinAttrs.contains(attrName))
				  newAttrs.addAttr(a);
		  }
	  }
	  newAttrs.appendAttributes(rel.getAttrList());
	  out.setAttrList(newAttrs);
	  
	  if(joinAttrs==null){
		  //just do a cross product
		  for(int i=0; i<tuples.size(); i++){
			  Tuple t1 = tuples.get(i);
			  for(int j=0; j<rel.tuples.size(); j++){
				  Tuple t2 = rel.tuples.get(j);
				  Tuple t = t1.join(t2, null, null);
				  if(t!=null)
					  out.addTuple(t);
			  }
		  }
		  return out;
	  }
	  
	  //I have join condition => HASHJOIN
	  //position of joinAttr
	  ArrayList<Integer> i1=null, i2=null;
	  if(joinAttrs!=null){
		  //find location of joinAttrs
		  //i1 = findPosition(joinAttrs);
		  i1 = joinAttrsInd;
		  i2 = rel.findPosition(joinAttrs);
	  }
	  
	  //construct hashtables to be used in the join
	  Hashtable<String, ArrayList<Tuple>> leftTuples = new Hashtable<String, ArrayList<Tuple>>();
	  Hashtable<String, ArrayList<Tuple>> rightTuples = new Hashtable<String, ArrayList<Tuple>>();
	  
	  addTuples(leftTuples,i1);
	  rel.addTuples(rightTuples,i2);
	  
	  //System.out.println("Left Tuples=" + leftTuples);
	  //System.out.println("Right Tuples=" + rightTuples);
	  
	  //get all tuples of the left buffer and join them with the ones in the
	  //right buffer
	  Iterator keys = leftTuples.keySet().iterator();
	  while(keys.hasNext()){
		  String key = (String)keys.next();
		  ArrayList<Tuple> joinLeftTuples = leftTuples.get(key);
		  ArrayList<Tuple> joinRightTuples = rightTuples.get(key);
		  if(joinLeftTuples == null || joinRightTuples == null)
			  continue;

		  //System.out.println("LeftTups :" + joinLeftTuples.size() + " RightTups: " + joinRightTuples.size());

		  //I can join the leftTuples with the rightTuples because they have
		  // the same key; and these are the only ones that I can join
		  for(int i=0; i<joinLeftTuples.size(); i++){
			  Tuple t1 = joinLeftTuples.get(i);
			  for(int j=0; j<joinRightTuples.size(); j++){
				  Tuple t2 = joinRightTuples.get(j);
				  //I know I can join these 2 tuples because they have the same key
				  Tuple t = t1.join(t2, null, null);
				  //I don't want duplicat attributes in the result; the join attr would be duplicates
				  t=t.removeValues(i1);
				  if(t!=null)
					  out.addTuple(t);
			  }
		  }
	  }
	  return out;
  }

  //add the tuples to the hashtables creating the key as a combination
  //of the join values
  void addTuples(Hashtable<String, ArrayList<Tuple>> hashTuples, ArrayList<Integer> pos){

	  for(int i=0; i<tuples.size(); i++){
		  Tuple t = tuples.get(i);
		  
		  //get hashFunc to be used as key
		  String hashFunc = "";
		  for(int j=0; j<pos.size(); j++){
			  int intPos =(pos.get(j)).intValue();
			  String val = (String)t.getValue(intPos);
			  hashFunc += val + "#@#";
			  //System.out.println("HashFunc=" + hashFunc);
		  }
		  ArrayList<Tuple> al = hashTuples.get(hashFunc);
		  if(al==null){
			  al = new ArrayList<Tuple>();
			  hashTuples.put(hashFunc, al);
		  }
		  al.add(t);
	  }
  }
  
  public Relation select(String var, String val){
	  
	  if(!val.startsWith("\""))
		  return select(var,val,true);
	  
	  Relation out = new Relation(name, getAttrList());
	  int pos = findPosition(var);
	  
	  //System.out.println("Find position of " + var + " = " + pos);
	  
	  for(int i=0; i<tuples.size(); i++){
		  Tuple t1 = tuples.get(i);
		  Tuple t = t1.select(pos,val);
			  if(t!=null)
				  out.addTuple(t);
	  }
	  return out;
  }

  public Relation select(String var1, String var2, boolean selectOnVar){
	  
	  Relation out = new Relation(name, getAttrList());
	  int pos1 = findPosition(var1);
	  int pos2 = findPosition(var2);
	  
	  for(int i=0; i<tuples.size(); i++){
		  Tuple t1 = tuples.get(i);
		  Tuple t = t1.select(pos1,pos2);
			  if(t!=null)
				  out.addTuple(t);
	  }
	  return out;
  }

  public int getNrRows(){
	  return tuples.size();
  }
  
  public Vector getColumnValues(String name){
	  Vector result = new Vector();
	  if(tuples==null)
		  return result;
	  for (int i=0; i<tuples.size(); i++){ 
		  Tuple t = tuples.get(i);
		  result.add(t.getValue(findPosition(name)));
	  }
	  return result;
  }
  
	
  public String toString()
  {
      String out = "";
    out += " RELATION NAME: "+name + "\n"; 
    out += "ATTRIBUTE LIST: "+attrList + "\n"; 
    out += "--------------------------------------------------\n";
    if (tuples != null) {
      Object[] objs = tuples.toArray();
      for (int i=0; i<objs.length; i++) {
        out += 
          (i<100 ? "0" : "")+
          (i<10 ? "0" : "")+i+": \n";
        Tuple t = (Tuple)objs[i];
        out += t.toString() + "\n";
      }
    }
    out += "--------------------------------------------------\n";
    return out;
  }

  public void prettyPrint()
  {
    System.out.println(" RELATION NAME: "+name); 
    System.out.println("ATTRIBUTE LIST: "+attrList); 
    System.out.println("--------------------------------------------------");
    if (tuples != null) {
      Object[] objs = tuples.toArray();
      for (int i=0; i<objs.length; i++) {
        System.out.print(
          (i<100 ? "0" : "")+
          (i<10 ? "0" : "")+i+": ");
        Tuple t = (Tuple)objs[i];
        System.out.println(t.toString());
      }
    }
    System.out.println("--------------------------------------------------");
  }


}
