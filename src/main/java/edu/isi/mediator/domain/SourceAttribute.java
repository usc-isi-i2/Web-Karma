// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.domain;

import edu.isi.mediator.domain.DomainAttribute;
import edu.isi.mediator.gav.util.MediatorConstants;

public class SourceAttribute extends DomainAttribute{
	
	public boolean needsBinding;
	
	public SourceAttribute(String name, String type, String bound){
		super(name, type);

		if(bound.equals("B"))
			this.needsBinding=true;
		else
			this.needsBinding=false;
	}

	public boolean needsBinding(){
		return needsBinding;
	}
	
	public String toString(){
		String s = "";
		s += name + ":" + type + ":";
		if(needsBinding)
			s += MediatorConstants.BOUND;
		else s +=MediatorConstants.FREE;
		return s;
	}

}

