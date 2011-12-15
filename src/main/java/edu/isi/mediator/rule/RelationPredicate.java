// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.domain.SourceAttribute;
import edu.isi.mediator.domain.SourceSchema;
import edu.isi.mediator.gav.util.MediatorConstants;

/**
 * Represents a relation.
 * @author mariam
 *
 */
public class RelationPredicate extends Predicate{
	
	/**
	 * the source schema that corresponds to this predicate
	 */
	protected SourceSchema source;
	
	/**
	 * Construct an empty RelationPredicate
	 */
	public RelationPredicate(){}
	/**
	 * Constructs a RelationPredicate with a given name.
	 * @param name relation name
	 */
	public RelationPredicate(String name){
		this.name=name;
	}

	/**
	 * Constructs a copy of the given predicate.
	 * @param p
	 */
	public RelationPredicate (RelationPredicate p){
		name = p.name;
		for(int i=0; i<p.terms.size(); i++){
			Term t = p.terms.get(i);
			addTerm(t.clone());
		}
		source=p.source;
	}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Predicate#clone()
	 */
	public RelationPredicate clone(){
		RelationPredicate p = new RelationPredicate(name);
		for(int i=0; i<terms.size(); i++){
			Term t = terms.get(i);
			p.addTerm(t.clone());
		}
		p.source=source;
		return p;
	}

	/**
	 * Constructs a RelationPredicate from an input string
	 * @param s
	 * 		of the form q(v1,v2,...,vn)
	 */
	public void buildPredicate(String s){
		int i = s.indexOf("(");
		if(i>0){
			name = s.substring(0, i);
			StringTokenizer st = new StringTokenizer(s.substring(i),"(),");
			while(st.hasMoreTokens()){
				String tok = st.nextToken();
				VarTerm vt = new VarTerm(tok);
				addTerm(vt);
			}
		}
	}

	/**
	 * Returns an SQL compliant predicate name (escapes illegar chars in predicate name)
	 * @return
	 * 		an SQL compliant predicate name (escapes illegar chars in predicate name)
	 */
	public String getSQLName(){
		if(source==null){
			//no source predicate is associated with this pred
			return name;
		}
		return source.getSQLName();
	}
	
	/** 
	 * Returns the type of this predicate
	 * @return
	 * 	type of this predicate; one of:
	 * 	<br> domain_predicate
	 * 	<br>function
	 * 	<br>ogsadqp
	 */
	public String getType(){
		if(source==null){
			//it's a domain predicate
			return "domain_predicate";
		}
		return source.getType();
	}
	
	/**
	 * @return
	 * true if domain predicate
	 * <br> false if source predicate
	 */
	public boolean isDomainPredicate(){
		if(source==null)
			return true;
		else return false;
	}

	/**	Returns all variables that need to be bound
	 * @return
	 * 		name of all variables that need to be bound
	 */
	public ArrayList<String> getNeedBindingVars(){
		ArrayList<String> vars = new ArrayList<String>();
		for(int i=0; i<terms.size(); i++){
			Term t = terms.get(i);
			SourceAttribute sa = source.getAttr(i);
			String var = t.getVar();
			if(t.needsBinding(sa.needsBinding())){
				if(t instanceof FunctionTerm){
					vars.addAll(t.getFunction().getNeedBindingVars());
				}
				else
					vars.add(var);
			}
		}
		return vars;
	}
	
	/**
	 * Returns true if the predicate contains variables that need to be bound.
	 * @return
	 * 		true if the predicate contains variables that need to be bound.
	 * 		<br> false otherwise
	 */
	public boolean needsBinding(){
		ArrayList<String> bindVars = getNeedBindingVars();
		if(bindVars.isEmpty())
			return false;
		else return true;
	}
	
    /**
     * Returns "@" if the specified attribute is bound in the data source
     * @param attr the attribute name
     * @return
     * 		"@" if the specified attribute is bound in the data source
     */
    public String needsBinding(String attr){
    	SourceAttribute sa = getSourceAttribute(attr);
    	if(sa.needsBinding)
    		return "@";
    	else 
    		return "";
    }
	
	/**
	 * Returns attribute names that are common between the 2 relations
	 * @param p the relation 
	 * @return
	 * 		attribute names that are common between the 2 relations
	 */
	public ArrayList<String> findCommonAttrs(RelationPredicate p){
		//System.out.println("Common in: " + this + " and " + p);
		ArrayList<String> attrs = new ArrayList<String>();
		for(int i=0; i<terms.size(); i++){
			Term t = terms.get(i);
			String var = t.getVar();
			if(var!=null && p.containsTerm(t))
				attrs.add(var);
		}
		return attrs;
	}
	
    /**
     * Returns the corresponding SourceAttr for the specified domainAttr
     * @param domainAttr
     * 			domain attribute name
     * @return
     * 		the corresponding SourceAttr for the specified domainAttr
     */
    public SourceAttribute getSourceAttribute(String domainAttr){
    	//System.out.println("Relation=" + this);
    	//System.out.println("Source=" + source);
    	//System.out.println("domainAttr=" + domainAttr);
		for(int i=0; i<terms.size(); i++){
			Term t = terms.get(i);
			String var = t.getVar();
	    	//System.out.println("Var=" + var);
			if(var!=null && var.equals(domainAttr)){
				if(source==null){
					//no source predicate is associated with this pred
					//just return the domainAttr
					return new SourceAttribute(domainAttr, "string", "F");
				}
				else return source.getAttr(i);
			}
		}
		return null;
    }
	
    /**
     * Returns the source attribute at specified position
     * @param i
     * 			the position
     * @return the source attribute at specified position
     */
    public SourceAttribute getSourceAttribute(int i){
		if(source==null){
			//no source predicate is associated with this pred
			//just return the variable name
			return new SourceAttribute(getVars().get(i), "string", "F");
		}
    	return source.getAttr(i);
    }
    
    /**
     * Returns the type for this source attribute
     * @param i
     * 			the position
     * @return 	true if the type is a number
     * 			false otherwise
     */
    public boolean getSourceAttributeType(int i){
    	return source.getAttr(i).isNumber();
    }

	/**
	 * Sets the SourceSchema that corresponds to this Relation
	 * @param s
	 * 		the source schema
	 * @throws MediatorException
	 */
	public void setSource(SourceSchema s) throws MediatorException{
		if(s==null){
			//name is not a source, it's probably a rule head from the query
			return;
		}
		source=s;
		if(source.getAttrs().size()!=terms.size()){
			throw(new MediatorException("Number of attributes in predicate " + this + " has to be equal with attrs in " + source.getAttrs()));
		}

	}

	/**
	 * Returns the expanded conjunctive formula for this relation.
	 * <br> Example: for a relation of the form: (x,y,x,"10")
	 * <br> the formula: (x0,y1,x2,const3) ^ (const3="10") ^ (x0=x2) is returned.
	 * @return
	 * 		the expanded conjunctive formula
	 */
	public ArrayList<Predicate> expandInConjunctiveFormula(){
		ArrayList<Predicate> formula = new ArrayList<Predicate>();
		
		// (x, (0,2)) (y,1) 
		//holds positions of the vars
		Hashtable<String,ArrayList<String>> pos = new Hashtable<String,ArrayList<String>>();
		//(const3, "10")
		//holds vars that have constants assigned
		Hashtable<String,String> constants = new Hashtable<String,String>();
		
		RelationPredicate p = this.clone();
		//rename each term to a unique name
		for(int i=0; i<p.terms.size(); i++){
			Term t = p.terms.get(i);
			t.changeVarName(i);

		}
		formula.add(p);

		ArrayList<String> attrsOld = getVarsAndConst();
		ArrayList<String> attrsNew = p.getVarsAndConst();

		
		for(int i=0; i<attrsOld.size(); i++){
			Term t = terms.get(i);
			if(t instanceof ConstTerm){
				//get the constant value
				constants.put(attrsNew.get(i),t.getVal());
			}
			
			ArrayList<String> posVals = pos.get(attrsOld.get(i));
			if(posVals==null){
				posVals=new ArrayList<String>();
				pos.put(attrsOld.get(i),posVals);
			}
			posVals.add(String.valueOf(i));

		}
		
		//System.out.println("Position of attrs=" + pos + " Constants=  " + constants);
	
		//deal with var equality x0=x2
		Iterator<String> it = pos.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			ArrayList<String> vals = pos.get(key);
			if(vals.size()>1){
				//add x0=x2 & x2=x9, etc
				for(int i=1; i<vals.size(); i++){
					BuiltInPredicate p1 = new BuiltInPredicate(MediatorConstants.EQUALS);
					//p1.addTerm(new VarTerm(key+vals.get(i)));
					//p1.addTerm(new VarTerm(key+vals.get(i+1)));
					VarTerm v1 = new VarTerm(key);
					v1.changeVarName(Integer.valueOf(vals.get(i-1)).intValue());
					p1.addTerm(v1);
					VarTerm v2 = new VarTerm(key);
					v2.changeVarName(Integer.valueOf(vals.get(i)).intValue());
					p1.addTerm(v2);
					formula.add(p1);
					//i=i+1;
				}
			}
		}
		//deal with constants
		it = constants.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			String val = constants.get(key);
			//it's a constant
			//add const3="10"
			BuiltInPredicate p1 = new BuiltInPredicate(MediatorConstants.EQUALS);
			p1.addTerm(new VarTerm(key));
			p1.addTerm(new ConstTerm(val));
			formula.add(p1);
		}
		return formula;
	}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Predicate#toString()
	 */
	public String toString(){
		String s = "";
		s += name + "(";
		for(int i=0; i<terms.size(); i++){
			if(i>0) s += ",";
			s += terms.get(i).toString();
		}
		s+= ")";
		return s;
	}

}