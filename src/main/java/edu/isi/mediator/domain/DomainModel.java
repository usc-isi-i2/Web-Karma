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

package edu.isi.mediator.domain;

import java.util.ArrayList;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.GAVRule;
import edu.isi.mediator.rule.GLAVRule;
import edu.isi.mediator.rule.LAVRule;
import edu.isi.mediator.rule.Rule;


/**
 * Representation of a Mediator DomainModel
 * @author mariam
 *
 */
public class DomainModel{
	
	protected ArrayList<SourceSchema> sourceSchemas = new ArrayList<SourceSchema>();
	protected ArrayList<DomainSchema> domainSchemas = new ArrayList<DomainSchema>();
	protected ArrayList<GAVRule> gavRules = new ArrayList<GAVRule>();
	protected ArrayList<LAVRule> lavRules = new ArrayList<LAVRule>();
	protected ArrayList<GLAVRule> glavRules = new ArrayList<GLAVRule>();
	
	
	public DomainModel(){}
	
	/**
	 * Sets the LAV rules.
	 * @param lavRules
	 */
	public void setLAVRules(ArrayList<LAVRule> lavRules){
		this.lavRules = lavRules;
	}
	
	/**
	 * Sets the GLAV rules.
	 * @param glavRules
	 */
	public void setGLAVRules(ArrayList<GLAVRule> glavRules){
		this.glavRules = glavRules;
	}

	/**
	 * Adds a source schema to the domain model
	 * @param ss
	 * 			the source schema 
	 */
	public void addSourceSchema(SourceSchema ss){
		sourceSchemas.add(ss);
	}
	/**
	 * Adds a domain schema to the domain model
	 * @param ds
	 * 			the domain schema 
	 */
	public void addDomainSchema(DomainSchema ds){
		domainSchemas.add(ds);
	}
	/**
	 * Adds a rule to the domain model
	 * @param r
	 * 			the Rule 
	 */
	public void addGAVRule(GAVRule r){
		gavRules.add(r);
	}
	
	public ArrayList<DomainSchema> getDomainSchemas(){
		return domainSchemas;
	}
	public ArrayList<SourceSchema> getSourceSchemas(){
		return sourceSchemas;
	}
	
	//jmora 072310
	/**
	 * @return
	 * 			all domain schema names
	 */
	public ArrayList<String> getDomainSchemaNames(){
		ArrayList<String> r = new ArrayList<String>();
		for (int i=0; i<domainSchemas.size(); i++){
			r.add(domainSchemas.get(i).getName());
		}
		return r;
	}

	/**
	 * @return
	 * 		all GAV rules (GAV rules may contain UAC rules)
	 */
	public ArrayList<GAVRule> getGAVRules(){
		ArrayList<GAVRule> r = new ArrayList<GAVRule>();
		for(int i=0; i<gavRules.size(); i++){
			r.add(gavRules.get(i));
		}
		return r;
	}
	
	/**
	 * @return
	 * 		all LAV rules
	 */
	public ArrayList<LAVRule> getLAVRules(){
		ArrayList<LAVRule> r = new ArrayList<LAVRule>();
		for(int i=0; i<lavRules.size(); i++){
			r.add(lavRules.get(i));
		}
		return r;
	}

	/**
	 * @return
	 * 		all LAV rules
	 */
	public ArrayList<GLAVRule> getGLAVRules(){
		ArrayList<GLAVRule> r = new ArrayList<GLAVRule>();
		for(int i=0; i<glavRules.size(); i++){
			r.add(glavRules.get(i));
		}
		return r;
	}

	/**
	 * Returns all rules: GAV, LAV and GLAV
	 * @return
	 */
	public ArrayList<Rule> getAllRules(){
		ArrayList<Rule> r = new ArrayList<Rule>();
		for(int i=0; i<glavRules.size(); i++){
			r.add(glavRules.get(i));
		}
		for(int i=0; i<lavRules.size(); i++){
			r.add(lavRules.get(i));
		}
		for(int i=0; i<gavRules.size(); i++){
			r.add(gavRules.get(i));
		}
		return r;
	}

	/** Returns the LAV rule with the given name.
	 * @param name
	 * 		name of rule head
	 * @return
	 * 	the LAV rule with the given name, null if the rule was not found.
	 */
	public LAVRule getLAVRule(String name){
		for(int i=0; i<lavRules.size(); i++){
			LAVRule r = lavRules.get(i);
			if(r.getHead().getName().equals(name))
				return r;
		}
		return null;
	}
	
	/** Returns the GAV rule with the given name.
	 * @param name
	 * 		name of rule head
	 * @return
	 * 	the GAV rule with the given name, null if the rule was not found.
	 */
	public GAVRule getGAVRule(String name){
		for(int i=0; i<gavRules.size(); i++){
			GAVRule r = gavRules.get(i);
			if(r.getHead().getName().equals(name))
				return r;
		}
		return null;
	}
		
	/**
	 * Returns the source schema with the specified name
	 * @param name
	 * 			source schema name
	 * @return
	 * 			the source schema with specified name
	 */
	public SourceSchema getSourceSchema(String name){
		for(int i=0; i<sourceSchemas.size(); i++){
			SourceSchema s = sourceSchemas.get(i);
			if(s.getName().equals(name))
				return s;
		}
		return null;
	}
	
	/**
	 * Returns the domain schema with specified name
	 * @param name
	 * 			domain schema name
	 * @return
	 * 			the domain schema with specified name
	 */
	public DomainSchema getDomainSchema(String name){
		for(int i=0; i<domainSchemas.size(); i++){
			DomainSchema s = domainSchemas.get(i);
			if(s.getName().equals(name))
				return s;
		}
		return null;
	}

	/** Returns the attribute names of a specific domain schema.
	 * @param name
	 * 			the name of the domain schema
	 * @return
	 * 		the attribute names of a specific domain schema 
	 * @throws MediatorException 
	 */
	public ArrayList<String> getDomainColumnNames(String name) throws MediatorException{
		DomainSchema ds = getDomainSchema(name);
		if(ds==null){
			throw new MediatorException("No domain schema defined for: " + name);
		}
		return ds.getAttributes();
	}
	
	/**
	 * Returns true if attribute domainAttr belongs to domain concept domainTable
	 * @param domainTable
	 * 			domain schema name
	 * @param domainAttr
	 * 			attribute name
	 * @return return true if attribute domainAttr belongs to domain concept domainTable
	 * @throws MediatorException 
	 */
	public boolean isValidDomainAttr(String domainTable, String domainAttr) throws MediatorException {
		if(domainTable==null){
			//it's a constant var ; 'hhh' as a
			//it is valid
			return true;
		}
		DomainSchema ds = getDomainSchema(domainTable);
		if(ds==null)
			throw new MediatorException("Domain concept " +  domainTable + " does not exist.");
		
		if(ds.hasAttribute(domainAttr))
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String s = "";
		s += "DomainModel\n";
		s += "SourceSchemas\n";
		for(int i=0; i<sourceSchemas.size(); i++){
			s += sourceSchemas.get(i).toString() + "\n";
		}
		s += "DomainSchemas\n";
		for(int i=0; i<domainSchemas.size(); i++){
			s += domainSchemas.get(i).toString() + "\n";
		}
		for(int i=0; i<gavRules.size(); i++){
			s += gavRules.get(i).toString() + "\n";
		}
		return s;
	}

}
