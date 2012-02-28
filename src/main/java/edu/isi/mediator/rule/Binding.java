/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *  
 *    This code was developed by the Information Integration Group as part 
 *    of the Karma project at the Information Sciences Institute of the 
 *    University of Southern California.  For more information, publications, 
 *    and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.mediator.rule;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.gav.util.MediatorUtil;

/**
 * Support class for unification.
 * @author mariam
 */
public class Binding{
	
	//key = rule var; val = query var/or constant
	/**
	 * key = rule var; val = query var/or constant
	 */
	private Hashtable<String, String> bindingList = new Hashtable<String, String>();

	//key = query var; val = const
	//this is the equivalent of an assign in the graph
	//R:
	//r1(x,y,"6") :- s1(x,y,z)
	//DQ:
	//q(a,b,c) :- r1(a,b,c)
	//SQ:
	//q(a,b,c) :- s1(a,b,z) ^ (c="6")
	/**
	 * stores corresponding vars/vals that will become Assign operators in the query graph.
	 */
	private Hashtable<String, String> additionalPredicates = new Hashtable<String, String>();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 * Returns a clone of the current Binding.
	 */
	public Binding clone(){
		Binding newB = new Binding();
		Set<String> keys = bindingList.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			String val = bindingList.get(key);
			newB.putVar(key, val);
		}
				
		keys =additionalPredicates.keySet();
		it = keys.iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			String val = additionalPredicates.get(key);
			newB.putConst(key, val);
		}				
		return newB;
	}
	
	/**
	 * Adds key/value pair to bindingList
	 * Example:
	 * unify(x,y) => (x,y)
	 * unify(x,1) => (x,y), (y,1)
	 * @param key
	 * @param val
	 * @return
	 * 		true if unification succeeds
	 * 		false otherwise
	 */
	public boolean unify(String key, String val){
		//System.out.println("Unify: key=" + key + " val=" + val);
		boolean valIsConstant=false;
		if(!MediatorUtil.isVar(val))
			valIsConstant=true;
		String existingVal = bindingList.get(key);
		while(existingVal!=null){
			if(!MediatorUtil.isVar(existingVal)){
				//it's a constant
				if(valIsConstant && !val.equals(existingVal)){
					//unification fails
					System.err.println("Unification failed for:(" + key + "," + val + ") in " + bindingList);
					return false;
				}
				else{
					//do nothing
					return true;
				}
			}
			else{
				key=existingVal;
				existingVal = bindingList.get(existingVal);
			}
		}
		bindingList.put(key,val);
		return true;
	}
	
	/**
	 * Add key/val pair to bindingList;
	 * @param key
	 * @param val
	 */
	public void putVar(String key, String val){
		bindingList.put(key,val);
	}
	
	/**
	 * Add key/val pair to additionalPredicates;
	 * @param key
	 * 		a variable name
	 * @param val
	 * 		a constant
	 */
	public void putConst(String key, String val){
		additionalPredicates.put(key, val);
	}
	
	/**
	 * @param key
	 * @return
	 * 		return the constant corresponding to key (from additionalPredicates)
	 */
	public String getConst(String key){
		return additionalPredicates.get(key);
	}

	/**
	 * @param key
	 * @return
	 * 		return unification var/val corresponding to key (from bindingList)
	 */
	public String getUnificationVar(String key){
		String var1 = bindingList.get(key);
		String var2=var1;
		while(var1!=null){
			if(!MediatorUtil.isVar(var1)){
				return var1;
			}
			else{
				var2=var1;
				var1=bindingList.get(var1);
				if(var2.equals(var1))
					return var1; //otherwise I get in infinite cycle
			}
		}
		
		return var2;
	}

	/**
	 * @return
	 * 		Key set for additionalPredicates
	 */
	public Set<String> keySetConst(){
		return additionalPredicates.keySet();
	}
	
	//if it's equality predicate add it to the bindings(I need to know about the constant)
	/**
	 * Adds the terms of an EQUALITY predicate to the bindingList.
	 * @param p
	 * @return
	 * 		true if the terms unify
	 * 		false otherwise
	 * @throws MediatorException
	 */
	public boolean addPredicate(Predicate p) throws MediatorException{
		if(p.getName().equals(MediatorConstants.EQUALS)){
			//get the 2 terms
			ArrayList<Term> terms = p.getTerms();
			Term t1 = terms.get(0);
			Term t2 = terms.get(1);
			if(t1 instanceof ConstTerm && t2 instanceof ConstTerm){
				//make sure that the constants are equal; otherwise unification fails
				String s1 = t1.getSqlValNoType();
				String s2 = t2.getSqlValNoType();
				boolean unify = s1.equals(s2);
				if(unify==false){
					System.err.println("Unification failed for:(" + s1 + "=" + s2 + ")");
				}
				return unify;
			}
			else{
				//either a var or a constant
				String var1 = t1.getTermValue();
				String var2 = t2.getTermValue();
				if(t1 instanceof ConstTerm){
					return unify(var2, var1);
				}
				else
					return unify(var1, var2);
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String s = "Vars=" + bindingList.toString() + "\n";
		s += "Const=" + additionalPredicates.toString() + "\n";
		return s;
	}
}
