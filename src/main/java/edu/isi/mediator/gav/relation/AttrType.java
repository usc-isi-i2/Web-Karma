// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.relation;

public final class AttrType
{
  public static final int CHAR_TYPE = 0;
  public static final int NUM_TYPE = 1;
  public static final int DATE_TYPE = 2;
  public static final int REL_TYPE = 3;
  public static final int DOM_TYPE = 4;

    // only for relation types
    // contains the relation metadata
    public Relation rel_metadata;
    public int type;

  public AttrType(int attrType) { 
      type=attrType;
  }
  public AttrType(String attrType) { 
      type=convert(attrType);
  }

    public int getType(){
	return type;
    }

  public int convert(String a_type)
  {
    if (a_type.equals("number"))
      return AttrType.NUM_TYPE;
    else if (a_type.equals("date"))
      return AttrType.DATE_TYPE;
    else if (a_type.equals("rel"))
      return AttrType.REL_TYPE;
    else if (a_type.equals("dom"))
      return AttrType.DOM_TYPE;
    else 
      return AttrType.CHAR_TYPE;
  }

  public String typeAsString() 
  {  
    if (type==AttrType.NUM_TYPE)
      return "number";
    else if (type==AttrType.DATE_TYPE)
      return "date";
    else if (type==AttrType.REL_TYPE){
	String ret = "rel";
	if(rel_metadata!=null)
	    ret += "(" + rel_metadata.getName() + ")";
	return ret;
    }
    else if (type==AttrType.DOM_TYPE)
      return "dom";
    else 
      return "char";
  }

    public void setRelation(Relation rel){
	rel_metadata=rel;
    }

}
