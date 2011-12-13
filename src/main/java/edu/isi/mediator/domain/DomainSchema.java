// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.domain;

import java.util.ArrayList;

import edu.isi.mediator.gav.util.MediatorConstants;


/**
 * Representation of DomainSchema
 * @author mariam
 *
 */
public class DomainSchema{
	
	private String name;
	private ArrayList<DomainAttribute> attrs = new ArrayList<DomainAttribute>();
	//if name (the name of the table) has illegal chars (spaces for ex) i will have to ecape this in the SQL `table name`
	private boolean hasIllegalChars = false;
	
	public DomainSchema(String name){
		this.name=name;
		/*
		if(name.contains(ILLEGAR_CHARS))
			hasIllegalChars=true;
			*/
	}
	
	/**
	 * Adds attribute sa to the domain schema
	 * @param sa
	 * 		attribute name
	 */
	public void addAttribute(DomainAttribute sa){
		attrs.add(sa);
	}
	
	/**
	 * @return name of the domain schema
	 */
	public String getName(){
			return name;
	}

	/**
	 * @return name with illegal characters escaped
	 */
	public String getSQLName(){
		if(hasIllegalChars){
			return "`" + name.replaceAll(MediatorConstants.ILLEGAR_CHARS," ") + "`";
		}
		else
			return name;
	}

	/**
	 * @return all attributes as DomainAttribute
	 */
	public ArrayList<DomainAttribute> getAttrs(){
		return attrs;
	}
	
	/**
	 * @return
	 * 		all attributes as String
	 */
	public ArrayList<String> getAttributes(){
		ArrayList<String> names = new ArrayList<String>();
		for(int i=0; i<attrs.size(); i++){
			DomainAttribute da = attrs.get(i);
			names.add(da.name);
		}
		return names;
	}

	/**
	 * @param i
	 * 		the position of the returned attribute
	 * @return
	 * 		attribute at given position
	 */
	public DomainAttribute getAttr(int i){
		return attrs.get(i);
	}
	
	/**
	 * @param name
	 * 		the attribute name
	 * @return
	 * 		true if the domain schema contains the specified attribute
	 */
	public boolean hasAttribute(String name){
		for(int i=0; i<attrs.size(); i++){
			DomainAttribute da = attrs.get(i);
			if(da.name.equals(name))
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String s = "";
		s += name + "(";
		for(int i=0; i<attrs.size(); i++){
			if(i>0) s += ",";
			s += attrs.get(i).toString();
		}
		s+= ")";
		return s;
	}

}