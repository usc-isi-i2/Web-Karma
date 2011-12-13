// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.relation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import edu.isi.mediator.gav.main.MediatorException;

public class AttrList
{
  protected ArrayList<Attr> attrs = new ArrayList<Attr>();

  public AttrList(){}
  
  public AttrList(String a_attrList) throws MediatorException
  {
	  // I'm using a SET to make the search faster
	  HashSet dups = new HashSet();
	  Vector names = new Vector();
	  ArrayList types = new ArrayList();
	  
	  StringTokenizer st = new StringTokenizer(a_attrList, ",");
	  while (st.hasMoreTokens()) {
		  StringTokenizer st2 = new StringTokenizer(st.nextToken());
		  String name = st2.nextToken().toLowerCase();
		  boolean isAdded = dups.add(name);
		  if(!isAdded)
			  throw new MediatorException("No duplicate attribute names are allowed in a relation. Attr=" + name);
		  types.add(st2.nextToken());
		  names.add(name);    
	  }
	  
	  Object[] oNames = names.toArray();
	  Object[] oTypes = types.toArray();
	  for (int i=0; i<oNames.length; i++)
		  attrs.add(new Attr((String)oNames[i], (String)oTypes[i]));
  }
  
  public AttrList(ArrayList<String> attr) throws MediatorException
  {
	  // check for duplicates
	  HashSet dups = new HashSet();
	  for(int i=0; i<attr.size(); i++){
		  Attr attrib = new Attr(attr.get(i));
		  String name = attrib.getName();
		  boolean isAdded = dups.add(name);
		  if(!isAdded)
			  throw new MediatorException("No duplicate attribute names are allowed in a relation. Attr=" + name);
		  attrs.add(attrib);
	  }
  }
  
  public AttrList clone()
  {
	  AttrList aList = new AttrList();
	  for (int i=0; i<attrs.size(); i++)
		  aList.addAttr(attrs.get(i).clone());
	  return aList;
  }
  
  public int size() 
  {
	  return attrs.size();
  }
  
  public void addAttr(Attr a){
	  attrs.add(a);
  }
  
  public void addAttr(String a){
	  Attr attrib = new Attr(a);
	  attrs.add(attrib);
  }

    public void removeAttr(int i){
    	if(i<attrs.size())
    	attrs.remove(i);
    }

    public void appendAttributes(AttrList aList){
        for (int i=0; i<aList.attrs.size(); i++){
        	Attr a = aList.attrs.get(i);
        	addAttr(a);
        }
    }
    
    public Attr getAttrAtPosition(int i){
    	return attrs.get(i);
    } 
    
    public void replaceAttr(String a_attr, Attr with)
    {
    	for (int i=0; i<attrs.size(); i++)
    		if (attrs.get(i).getName().toLowerCase().equals(a_attr.toLowerCase())){
    			attrs.set(i,with);
    			break;
    		}
    }
    
    public Attr findAttr(String a_attr)
  {
    for (int i=0; i<attrs.size(); i++)
      if (attrs.get(i).getName().toLowerCase().equals(a_attr.toLowerCase()))
        return attrs.get(i);
    return null;
  }

  public int findPosition(String a_attr)
    {	  
	for (int i=0; i<attrs.size(); i++){
	    if (attrs.get(i).getName().toLowerCase().
		equals(a_attr.toLowerCase()))
		return i;
	}
	return -1;
    }


  public boolean contains(String a_attr)
    {	  
	for (int i=0; i<attrs.size(); i++){
	    if (attrs.get(i).getName().toLowerCase().
		equals(a_attr.toLowerCase()))
		return true;
	}
	return false;
    }

  public ArrayList<Attr> getAttrs()
  {
    return attrs;
  }

  public ArrayList<String> getNames()
  {
	  ArrayList<String> names = new ArrayList<String>();
    for (int i=0; i<attrs.size(); i++) 
      names.add(attrs.get(i).getName());
    return names;
  }

  public String toString()
  {
    String data = "";
    for (int i=0; i<attrs.size(); i++) 
      data += (i>0 ? ", " : "") + attrs.get(i);
    return data;
  }

}

