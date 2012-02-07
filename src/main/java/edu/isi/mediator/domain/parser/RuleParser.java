// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__


package edu.isi.mediator.domain.parser;

import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;

import edu.isi.mediator.rule.BuiltInPredicate;
import edu.isi.mediator.rule.FunctionPredicate;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.rule.FunctionTerm;
import edu.isi.mediator.rule.RelationPredicate;
import edu.isi.mediator.domain.SourceSchema;
import edu.isi.mediator.domain.DomainModel;
import edu.isi.mediator.gav.main.MediatorException;

/**
 * Rule parser helper methods.
 * @author mariam
 *
 */
public class RuleParser {
	
	/**
	 * Parse one rule.
	 * @param rule
	 * 			rule as AST
	 * @param dm
	 * 			the DomainModel
	 * @return	Mediator Rule
	 * @throws MediatorException 
	 */
	public ArrayList<Predicate> parseConjunction(CommonTree rule,DomainModel dm) throws MediatorException{

		//System.out.println("The Rule is:" + rule.toStringTree());
		
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();

		for(int i=0; i<rule.getChildCount(); i++){
			CommonTree predicate = (CommonTree)rule.getChild(i);
			if(predicate.getText().equals(MediatorConstants.RELATION_PRED)){
				RelationPredicate p = parsePredicateRelation(predicate, i);
				//this is a source predicate, so I want to have a pointer
				//to the actual attributes of this source
				if(dm!=null){
					//System.out.println("Find source:");
					SourceSchema s = dm.getSourceSchema(p.getName());
					p.setSource(s);
				}
				if(p.getType().equals(MediatorConstants.FUNCTION))
					p= new FunctionPredicate(p);
				predicates.add(p);
			}
			else if(predicate.getText().equals(MediatorConstants.BUILTIN_PRED)){
				//it's a predicate of the form var op var/val (x="bubu")
				BuiltInPredicate p = parsePredicateBuiltIn(predicate);
				predicates.add(p);				
			}
		}
		return predicates;
	}	

	/**
	 * Parse a relation predicate.
	 * @param predicate
	 * 			predicate as AST
	 * @param predIndex
	 * 			for generating unique var names
	 * @return	RelationPredicate
	 * @throws MediatorException 
	 */
	public RelationPredicate parsePredicateRelation(CommonTree predicate, int predIndex) throws MediatorException{
		
		int id =0; 
		//the name of the predicate
		String name = predicate.getChild(0).getText();
		//name = MediatorUtil.removeBacktick(name);
		
		RelationPredicate p = new RelationPredicate(name);

		for(int j=1; j<predicate.getChildCount(); j++){
			String var = predicate.getChild(j).getText();

			if(var.equals(MediatorConstants.FUNCTION_PRED)){
				FunctionPredicate fp = parseFunctionPredicate((CommonTree)predicate.getChild(j));
				FunctionTerm ft = new FunctionTerm();
				ft.setFunction(fp);
				p.addTerm(ft);
			}
			else{
				//it's a regular variable
				if(var.equals("_")){
					//give it a unique name
					var += String.valueOf(predIndex) + (id++);
					//System.out.println("Var==" + var + " predI=" + predIndex + "id=" + id);
				}
				
				//why do I remove the backtick for the variables? If they have a backtick I must
				//need them mariam:01/24/2012
				//at least I should record that it had a backtick
				//I especially need this for the RDF generation; If I loose the info when I generate
				//subrules I can't preserve the backtick
				//remove the backtick when you need to use it
				//var = MediatorUtil.removeBacktick(var);
				p.addTerm(var);
			}
		}
		return p;
	}

	/**
	 * Parse a function predicate (with possibly embedded function predicates).
	 * @param predicate
	 * 			predicate as AST
	 * @return	FunctionPredicate
	 * 		Example: (FUNCTION_PRED f1 b (FUNCTION_PRED f2 a)))
	 * @throws MediatorException 
	 */
	private FunctionPredicate parseFunctionPredicate(CommonTree predicate) throws MediatorException{
		
		//the name of the predicate
		String name = predicate.getChild(0).getText();
		FunctionPredicate p = new FunctionPredicate(name);

		for(int j=1; j<predicate.getChildCount(); j++){
			String var = predicate.getChild(j).getText();
			if(var.equals(MediatorConstants.FUNCTION_PRED)){
				FunctionPredicate fp = parseFunctionPredicate((CommonTree)predicate.getChild(j));
				FunctionTerm ft = new FunctionTerm();
				ft.setFunction(fp);
				p.addTerm(ft);
			}
			else{
				//regular variable
				//why do I remove the backtick for the variables? If they have a backtick I must
				//need them mariam:01/24/2012
				//remove the backtick when you need to use it
				//var = MediatorUtil.removeBacktick(var);
				p.addTerm(var);
			}
		}
		return p;
	}

	/**
	 * Parse a relation predicate.
	 * @param predicate
	 * 			predicate as AST
	 * @param predIndex
	 * 			for generating unique var names
	 * @return	RelationPredicate
	 */
	//OLD implementation
/*
	private RelationPredicate parsePredicateRelation(CommonTree predicate, int predIndex){
		
		int id =0; 
		//the name of the predicate
		String name = predicate.getChild(0).getText();
		RelationPredicate p = new RelationPredicate(name);

		for(int j=1; j<predicate.getChildCount(); j++){
			String var = predicate.getChild(j).getText();

			if(var.equals("_")){
				//give it a unique name
				var += String.valueOf(predIndex) + (id++);
				//System.out.println("Var==" + var + " predI=" + predIndex + "id=" + id);
			}
			p.addTerm(var);
		}
		return p;
	}
*/	
	/**
	 * Parse a built-in predicate.
	 * @param predicate
	 * 			predicate as AST
	 * @return	BuiltInPredicate
	 */
	public BuiltInPredicate parsePredicateBuiltIn(CommonTree predicate) throws MediatorException{
		
		//has 3 arguments
		String op = predicate.getChild(0).getText();

		//the name of the predicate
		String name = getOperator(op.toUpperCase());

		if(name.equals(MediatorConstants.IN) || name.equals(MediatorConstants.NOT_IN)){
			return parseINPredicate(name, predicate);
		}
		BuiltInPredicate p = new BuiltInPredicate(name);

		String var1 = predicate.getChild(1).getText();
		String var2 = MediatorConstants.NULL_VALUE;
		if(predicate.getChild(2)!=null)
			var2 = predicate.getChild(2).getText();
		//System.out.println("R=" + var1);
		//System.out.println("R=" + op);
		//System.out.println("R=" + var2);

		p.addTerm(var1);
		p.addTerm(var2);
		return p;
	}

	/**
	 * Parse a 'IN' predicate ( a in ['m','n','o']).
	 * @param name
	 * 		the name of the predicate (IN or NOT_IN)
	 * @param predicate
	 * 			predicate as AST
	 * @return	BuiltInPredicate
	 */
	public BuiltInPredicate parseINPredicate(String name, CommonTree predicate) throws MediatorException{
		
		BuiltInPredicate p = new BuiltInPredicate(name);

		String var1 = predicate.getChild(1).getText();
		p.addTerm(var1);
		for(int i=2; i<predicate.getChildCount(); i++){
			String var2 = predicate.getChild(i).getText();
			p.addTerm(var2);			
		}
		return p;
	}

	/**
	 * @param op
	 * 		operator as it appears in AST
	 * @return
	 * 		operator required in sql query
	 * @throws MediatorException 
	 */
	private String getOperator(String op) throws MediatorException{
		String name;
		if(op.equals("="))
			name=MediatorConstants.EQUALS;
		else if(op.equals("!="))
			name=MediatorConstants.NOT_EQUAL1;
		else if(op.equals("<>"))
			name=MediatorConstants.NOT_EQUAL2;
		else if(op.equals("<"))
			name=MediatorConstants.LESS_THAN;
		else if(op.equals("<="))
			name=MediatorConstants.LESS_THAN_EQ;
		else if(op.equals(">"))
			name=MediatorConstants.GREATER_THAN;
		else if(op.equals(">="))
			name=MediatorConstants.GREATER_THAN_EQ;
		else if(op.equals("LIKE"))
			name=MediatorConstants.LIKE;
		else if(op.equals("IN"))
			name=MediatorConstants.IN;
		else if(op.equals("NOT_IN"))
			name=MediatorConstants.NOT_IN;
		else if(op.equals("IS_NULL"))
			name=MediatorConstants.IS_NULL;
		else if(op.equals("ISNOT_NULL"))
			name=MediatorConstants.ISNOT_NULL;
		else
			throw new MediatorException("Operator not supported: " + op);				
		return name;
	}
}
