// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import edu.isi.mediator.gav.main.MediatorException;

/**
 * Represents a functional predicate.
 * @author mariam
 *
 */
public class FunctionPredicate extends RelationPredicate{
	
	/**
	 * Construct a predicate with a given name.
	 * @param name
	 * 		name of the predicate.
	 */
	public FunctionPredicate(String name){
		this.name=name;
	}
	
	/**
	 * Constructs a FunctionPredicate from a RelationPredicate.
	 * @param p
	 */
	public FunctionPredicate(RelationPredicate p){
		name=p.name;
		terms = p.terms;
		source=p.source;
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.rule.RelationPredicate#clone()
	 */
	public FunctionPredicate clone(){
		FunctionPredicate p = new FunctionPredicate(name);
		for(int i=0; i<terms.size(); i++){
			Term t = terms.get(i);
			p.addTerm(t.clone());
		}
		p.source=source;
		return p;
	}
	
	//for predicates that are functions there is only one out var = the only free attribute
	/**
	 * Returns the output variable.
	 * @return
	 * 		the output variable
	 */
	public String getOutVarForFunction(){
		//it's always the last one
		int i = source.getFirstFreeAttrPosition();
		if(i>=0){
			Term t = terms.get(i);
			return t.getVar();
		}
		return null;
	}

	/**
	 * Returns the output term.
	 * @return
	 */
	public Term getOutVarTerm(){
		//it's always the last one
		int i = source.getFirstFreeAttrPosition();
		if(i>=0){
			Term t = terms.get(i);
			return t;
		}
		return null;
	}

	/**
	 * Removes the output variable from the terms list.
	 */
	public void removeOutVarForFunction(){
		//it's always the last one
		int i = source.getFirstFreeAttrPosition();
		if(i>=0){
			terms.remove(i);
			return;
		}
	}
	
    /**
     * Returns the equivalent SQL statement.
     * @return
     * @throws MediatorException
     */
    public String getFunctionForSQL() throws MediatorException{
    	String actualName = getName() + "(";
    	ArrayList<Term> terms = getTerms();
    	for(int k=0; k<terms.size(); k++){
    		Term t = terms.get(k);
    		if(k!=0) actualName += ",";
    		if(t instanceof FunctionTerm)
    			actualName += ((FunctionTerm)t).getFunctionForSQL();
    		else if(t instanceof ConstTerm){
    			boolean isNumber = getSourceAttributeType(k);
				actualName += t.getSqlVal(isNumber);
    		}
    		else{
    			actualName += t.getVar();
    		}
    	}
    	actualName += ")";
    	return actualName;
    }

    /**
     * Returns true if the output is a number.
     * @return
     * 		true if the output is a number
     * 		false otherwise
     */
    public boolean outputIsNumber(){
		int i = source.getFirstFreeAttrPosition();
		//System.out.println("Func Source=" + source + " Attr=" + source.getAttr(i) + " " + source.getAttr(i).isNumber());
		return  source.getAttr(i).isNumber();
	}
    
    /**
     * Evaluate the function. All function definitions are in edu.isi.mediator.gav.rule.FunctionsRepository.
     * <p>Functions can have either a fixed number of input parameters, OR one variable length input parameter.
     * <p>For evaluation all terms should be ConstTerms, so that a value is present.
     * @return
     * @throws MediatorException 
     */
    public Object evaluate() throws MediatorException{

    	String funcName = name;
    	String className = "edu.isi.mediator.rule.FunctionRepository";

    	try{
    		Class<?> funcClass = Class.forName(className);
    		//set parameter types; all are String types for now
    		int paramSize = terms.size();
    		Class<?> paramTypes[] = new Class[paramSize];
    		for(int i=0; i<paramSize; i++){
    			paramTypes[i]=String.class;
    		}
    		
    		//get the function & set values
    		Method theFunc;
    		Object[] funcVals;
    		try{
    			//a function with a fixed set of input parameters
    			theFunc = funcClass.getMethod(funcName, paramTypes);
        		//set the values
        		funcVals = new Object[paramSize];
        		for(int i=0; i<paramSize; i++){
        			//when we evaluate the function all terms should be ConstTerms as we need values to evaluate it
        			Term t = terms.get(i);
        			if(!(t instanceof ConstTerm)){
        				throw new MediatorException("At function evaluation all terms should be ConstTerm " + this );
        			}
        			funcVals[i] = ((ConstTerm)t).getVal();
        		}
    		}catch(Exception e){
    			//the function probably has a variable number of inputs
    			//so when we set it up we have to set it up as "one array input"
    			paramTypes = new Class[1];
    			paramTypes[0] = String[].class;
    			theFunc = funcClass.getMethod(funcName, paramTypes);
        		//set the values
        		funcVals = new Object[1];
        		String[] values = new String[paramSize];
        		for(int i=0; i<paramSize; i++){
        			//when we evaluate the function all terms should be ConstTerms as we need values to evaluate it
        			Term t = terms.get(i);
        			if(!(t instanceof ConstTerm)){
        				throw new MediatorException("At function evaluation all terms should be ConstTerm " + t + " " + t.getClass());
        			}
        			values[i] = ((ConstTerm)t).getVal();
        		}
        		funcVals[0]=values;
    		}
    		
    		Object funcRet = theFunc.invoke(null, funcVals);

    		return funcRet;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		throw new MediatorException("Error during function invocation " + this + ";" + e.getMessage());
    	}
    }

    /**
     * Evaluates the function given values for the variables.
     * <p>Replaces all VarTerms with ConstTerms constructed with values from the 
     * <p> "values" Map.
     * @param values
     * 		a value map where key=varName & value = varValue;
     * <br>should contain values for the variables referred to in this function
     * @return
     * @throws MediatorException
     */
    public Object evaluate(Map<String, String> values) throws MediatorException{
		//I want to leave the initial function unchanged
		FunctionPredicate func = clone();

		for(int i=0; i< func.terms.size(); i++){
			Term term = func.terms.get(i);
			if(term instanceof ConstTerm){
				//it already has a value; skip it
				//if values starts with ", remove that
				ConstTerm ct = ((ConstTerm)term);
				String val = ct.getVal();
				//System.out.println("VAL=" + val);
				if(val.startsWith("'") || val.startsWith("\"")){
					val = val.substring(1,val.length()-1);
				}
				ct.setVal(val);
				continue;
			}
			String varName = term.getVar();
			//get its value
			String varVal = values.get(varName);
			if(varVal==null)
				throw new MediatorException("The values map does not contain variable: " + varName + " Map is:" + values);
			
			ConstTerm ct = new ConstTerm(varName, varVal);
			func.terms.set(i,ct);
		}
		return func.evaluate();
    }
}