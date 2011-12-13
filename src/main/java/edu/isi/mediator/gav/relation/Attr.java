// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.relation;

public class Attr
{
  protected String name;
  protected AttrType type;


  /**
 * @param a_name
 */
public Attr(String a_name)
  {
      name = a_name;
      type = new AttrType(AttrType.CHAR_TYPE);
  }

  public Attr(String a_name, String a_type)
  {
      name = a_name;
      type = new AttrType(a_type);
  }
  public Attr(String a_name, int a_type)
  {
      name = a_name;
      type = new AttrType(a_type);
  }

  public Attr(String a_name, AttrType a_type)
  {
      name = a_name;
      type = a_type;
  }

  public String getName() { return name; }
  public int getType() { return type.getType(); }
  public AttrType getAttrType() { return type; }

  public String toString() 
  {
    return name+ " "+type.typeAsString();
  }

  public Attr clone() 
  {
    return new Attr(name, type);
  }
}
