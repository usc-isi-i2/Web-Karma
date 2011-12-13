// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.source;

import java.util.ArrayList;

import edu.isi.mediator.gav.util.MediatorConstants;

public class SourceSchema{
	
	private String name;
	private ArrayList<SourceAttribute> attrs = new ArrayList<SourceAttribute>();
	//if name (the name of the table) has illegal chars (spaces for ex) i will have to ecape this in the SQL `table name`
	private boolean hasIllegalChars = false;
	
	//what type of source this is 
	//ogsadqp or function
	private String type = MediatorConstants.OGSADQP;
	
	//does this source need bindings
	private boolean needsBinding = false;
	
	public SourceSchema(String name){
		this.name=name;
		/*
		if(name.contains(ILLEGAR_CHARS))
			hasIllegalChars=true;
			*/
	}
	
	public void addAttribute(SourceAttribute sa){
		attrs.add(sa);
		if(sa.needsBinding())
			needsBinding=true;
	}
	
	public String getName(){
			return name;
	}

	public String getSQLName(){
		if(hasIllegalChars){
			return "`" + name.replaceAll(MediatorConstants.ILLEGAR_CHARS," ") + "`";
		}
		else
			return name;
	}

	public ArrayList<SourceAttribute> getAttrs(){
		return attrs;
	}
	
	public void setType(String type){
		if(type.equals("function"))
			this.type=MediatorConstants.FUNCTION;
		else this.type=MediatorConstants.OGSADQP;
	}
	
	public String getType(){
		return type;
	}
	
	public boolean needsBinding(){
		return needsBinding;
	}

	public SourceAttribute getAttr(int i){
		return attrs.get(i);
	}
	
	public String getFirstFreeAttr(){
		for(int i=0; i<attrs.size(); i++){
			SourceAttribute a = attrs.get(i);
			if(!a.needsBinding())
				return a.getName();
		}
		return null;
	}
	
	public int getFirstFreeAttrPosition(){
		for(int i=0; i<attrs.size(); i++){
			SourceAttribute a = attrs.get(i);
			if(!a.needsBinding())
				return i;
		}
		return -1;
	}

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