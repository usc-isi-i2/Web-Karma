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

package edu.isi.mediator.domain.parser;

import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;

import edu.isi.mediator.domain.DomainAttribute;
import edu.isi.mediator.domain.DomainModel;
import edu.isi.mediator.domain.DomainSchema;
import edu.isi.mediator.domain.SourceAttribute;
import edu.isi.mediator.domain.SourceSchema;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorConstants;

/**
 * Domain File: Schema parser.
 * @author mariam
 *
 */
public class SchemaParser {
	
	/**
	 * Parse SourceSchema and populate Mediator data structures
	 * @param schema
	 * 			source_schema as AST
	 * @param functions
	 * 			the list of functions
	 * @param dm
	 * 			the DomainModel
	 * @throws MediatorException
	 */
	public void parseSourceSchema(CommonTree schema, ArrayList<String> functions, DomainModel dm) throws MediatorException{

		for(int i=0; i<schema.getChildCount(); i++){
			CommonTree source = (CommonTree) schema.getChild(i);
			SourceSchema ss = buildSourceSchema(source, functions);
			dm.addSourceSchema(ss);
		}
	}
	
	/**
	 * @param source
	 * 			definition of one source (table (col1 type1 bind1) ... )
	 * @param functions
	 * 			the list of functions
	 * @return	SourceSchema that corresponds to this source
	 * @throws MediatorException
	 */
	private SourceSchema buildSourceSchema(CommonTree source, ArrayList<String> functions) throws MediatorException{

		String tableName = source.getText();
		SourceSchema ss = new SourceSchema(tableName);
		for(int i=0; i<source.getChildCount(); i++){
			CommonTree attr = (CommonTree) source.getChild(i);
			SourceAttribute sa = buildSourceAttribute(attr);
			ss.addAttribute(sa);
		}

		//check if it is a function
		if(functions.contains(source.getText()))
			ss.setType(MediatorConstants.FUNCTION);

		return ss;
	}

	/**
	 * @param attr
	 * 			definition of one attribute (col1 type1 bind1)
	 * @return	SourceAttribute that corresponds to this attr
	 * @throws MediatorException
	 */
	private SourceAttribute buildSourceAttribute(CommonTree attr) throws MediatorException{
		String name = attr.getText();
		//remove ` if it exists
		//if I have an attribute called domain_schema I have to write 'domain_schema` in the model
		//otherwise the parser gets confused thinking that it is DOMAIN_SCHEMA:
		
		if(name.startsWith("`")){
			name=name.substring(1, name.length()-1);
		}
		
		if(attr.getChildCount()!=2)
			throw(new MediatorException("Attribute definition for SourceSchema is incorrect " + attr.toStringTree()));
		String type = attr.getChild(0).getText();
		String binding = attr.getChild(1).getText();
		
		//System.out.println("name=" + name + " type=" + type + " binding=" + binding);
		
		if(!binding.toUpperCase().equals(MediatorConstants.BOUND) && !binding.toUpperCase().equals(MediatorConstants.FREE))
			throw(new MediatorException("Binding can be only b|f " + attr.toStringTree()));
		
		SourceAttribute sa = new SourceAttribute(name, type.toUpperCase(), binding.toUpperCase());
		return sa;
	}

	/**
	 * Parse DomainSchema and populate Mediator data structures
	 * @param schema
	 * 			domain_schema as AST
	 * @param dm
	 * 			the DomainModel
	 * @throws MediatorException
	 */
	public void parseDomainSchema(CommonTree schema, DomainModel dm) throws MediatorException{
		for(int i=0; i<schema.getChildCount(); i++){
			CommonTree domainSch = (CommonTree) schema.getChild(i);
			DomainSchema ds = buildDomainSchema(domainSch);
			dm.addDomainSchema(ds);
		}
	}

	/**
	 * @param domainSch
	 * 			definition of one domain schema (table (col1 type1) ... )
	 * @return	DomainSchema that corresponds to this domainSch
	 * @throws MediatorException
	 */
	private DomainSchema buildDomainSchema(CommonTree domainSch) throws MediatorException{
		String tableName = domainSch.getText();
		DomainSchema ss = new DomainSchema(tableName);
		for(int i=0; i<domainSch.getChildCount(); i++){
			CommonTree attr = (CommonTree) domainSch.getChild(i);
			DomainAttribute sa = buildDomainAttribute(attr);
			ss.addAttribute(sa);
		}
		return ss;
	}

	/**
	 * @param attr
	 * 			definition of one domain attr (col1 type1)
	 * @return	DomainAttribute that corresponds to this attr
	 * @throws MediatorException
	 */
	private DomainAttribute buildDomainAttribute(CommonTree attr) throws MediatorException{
		String name = attr.getText();
		//remove ` if it exists
		//if I have an attribute called domain_schema I have to write 'domain_schema` in the model
		//otherwise the parser gets confused thinking that it is DOMAIN_SCHEMA:
		
		if(name.startsWith("`")){
			name=name.substring(1, name.length()-1);
		}
		
		if(attr.getChildCount()!=1)
			throw(new MediatorException("Attribute definition for DomainSchema is incorrect " + attr.toStringTree()));
		String type = attr.getChild(0).getText();
		
		DomainAttribute sa = new DomainAttribute(name, type.toUpperCase());
		return sa;
	}

	/**
	 * Parse Functions and populate Mediator data structures
	 * @param functions
	 * 			functions as AST
	 * @return list with functions
	 * @throws MediatorException
	 */
	public ArrayList<String> parseFunctions(CommonTree functions) throws MediatorException{
		ArrayList<String> f = new ArrayList<String>(); 
		
		for(int i=0; i<functions.getChildCount(); i++){
			CommonTree child = (CommonTree) functions.getChild(i);
			//System.out.println("func=" + child.getText());
			f.add(child.getText());
		}
		return f;
	}
}
