// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rdf;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.isi.mediator.domain.parser.DomainParser;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorLogger;
import edu.isi.mediator.gav.util.MediatorUtil;
import edu.isi.mediator.rule.FunctionTerm;
import edu.isi.mediator.rule.LAVRule;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.RelationPredicate;
import edu.isi.mediator.rule.Rule;
import edu.isi.mediator.rule.Term;
import edu.isi.mediator.rule.VarTerm;


/**
 * Class that provides methods for generating RDF.
 * Takes as input a domain file containing one source description (rule) and  
 * splits the rule into subrules corresponding to each variable 
 * referenced in the rule.
 * Outputs the rdf in a file or stdout.
 * Output is in N3 notation.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class TableRDFGenerator {

	/**
	 * The source description defined in the domain model.
	 */
	private Rule tableRule;
	
	/**
	 * The RDFGenerator corresponding to the tableRule
	 */
	private RuleRDFGenerator rdfGenerator;
	/**
	 * HashMap that contains <Variable, Rule> pairs, where "Rule" is a
	 * subrule that corresponds to the given variable (determined from the initial rule).
	 */
	private HashMap<String, RuleRDFGenerator> rdfGenerators = new HashMap<String, RuleRDFGenerator>();
	
	/**
	 * HashMap that contains all variables that are needed for the RDF generation subrule
	 * belonging to a variable.
	 * key: a variable value: list of additional variables needed for the rdf generation
	 * Example: rule for Phone is: Rule(Name, Phone) -> Person(uri(Name)) ^ hasPhone(uri(Name), Phone)
	 * key: Phone; value:<Name> 
	 */
	private HashMap<String, Set<String>> rdfVariables = new HashMap<String, Set<String>>();
	/**
	 * Output writer. Either to a file or to System.out
	 */
	protected PrintWriter outWriter;
	
	/**
	 * Unique id, used for generating gensym URIs.
	 */
	private long uniqueId = Calendar.getInstance().getTimeInMillis();
	/**
	 * Contains mapping of prefix name to source namespace.
	 * If prefix is not used in the source desc we have only one source namespace.
	 * key=prefix; value=namespace;
	 */
	private Map<String,String> sourceNamespaces;
	/**
	 * Contains mapping of prefix name to ontology namespace.
	 * If prefix is not used in the source desc we have only one ontology namespace.
	 * key=prefix; value=namespace;
	 */
	private Map<String,String> ontologyNamespaces;
	
	/**
	 * Prefix used for inverse properties. When we see an inverse property 
	 * we have to change the order of the 2 attributes.
	 */
	public static String inverseProperty = "inverse__of___";

	private static final MediatorLogger logger = MediatorLogger.getLogger(TableRDFGenerator.class.getName());

	/**
	 * Constructs a TableRDFGenerator.
	 * @param domainStr
	 * 		The domain file as a string. Should contain "NAMESPACES" 
	 * 		and "LAV_RULES" sections (containing one rule).
	 * @param outputFile
	 * 		location of output file
	 * @throws MediatorException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public TableRDFGenerator(String domainStr, String outputFile) 
				throws MediatorException, ClassNotFoundException, IOException{
		initParams(domainStr,outputFile);
		generateSubrules();
	}

	/*
	 * Example:
	 * 	StringWriter outS = new StringWriter();
	 *  PrintWriter pw = new PrintWriter(outS);
	 *  
	 *  new PrintWriter(System.out) 
	 */
	/**
	 * Constructs a TableRDFGenerator.
	 * @param domainStr
	 * 		The domain file as a string. Should contain "NAMESPACES" 
	 * 		and "LAV_RULES" sections (containing one rule).
	 * @param writer
	 * 		PrintWriter to a String or to System.out
	 * @throws MediatorException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public TableRDFGenerator(String domainStr, PrintWriter writer) 
	throws MediatorException, ClassNotFoundException, IOException{
		initParams(domainStr,writer);
		generateSubrules();
	}

	/**
	 * Initialize class members.
	 * @param domainStr
	 * 		The domain file as string. Should contain "NAMESPACES" and "LAV_RULES" sections.
	 * @param outputFile
	 * 		location of output file OR null if output to Stdout
	 * @throws MediatorException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void initParams(String domainStr, String outputFile)
	throws MediatorException, ClassNotFoundException, IOException{
		PrintWriter outputWriter = null;
		if(outputFile!=null){
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8");
			BufferedWriter bw = new BufferedWriter (fw);
			outputWriter = new PrintWriter (bw);
		}else{
			outputWriter = new PrintWriter (System.out);			
		}
		initParams(domainStr, outputWriter);
	}

	/**
	 * Initialize class members.
	 * @param domainStr
	 * 		The domain file as string. Should contain "NAMESPACES" and "LAV_RULES" sections.
	 * @param writer
	 * 			the RDF will be written to this writer
	 * @throws MediatorException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void initParams(String domainStr, PrintWriter writer)
	throws MediatorException, ClassNotFoundException, IOException{
		DomainParser sp = new DomainParser();
		RDFDomainModel dm = (RDFDomainModel)sp.parseDomain(domainStr);

		sourceNamespaces = dm.getSourceNamespaces();
		ontologyNamespaces = dm.getOntologyNamespaces();
		logger.debug("SourceNamespaces="+sourceNamespaces);
		logger.debug("OntologyNamespaces="+ontologyNamespaces);
		//output file
		outWriter=writer;

		//write namespaces to out file
		RDFUtil.setNamespace(sourceNamespaces,ontologyNamespaces,outWriter);

		if(dm.getAllRules().size()!=1){
			throw new MediatorException("Only one rule should be present in the domain file.");
		}
		tableRule = dm.getAllRules().get(0);
		rdfGenerator = new RuleRDFGenerator(tableRule, sourceNamespaces,
				ontologyNamespaces, outWriter, uniqueId+"");
	}


	/**
	 * Takes the initial rule and generates subrules that correspond to each variable
	 * in the head of the rule.
	 * @throws MediatorException 
	 */
	private void generateSubrules() throws MediatorException{
		//get head of the rule
		ArrayList<String> allVars = tableRule.getAllAntecedentVars();
		for(String v:allVars){
			rdfVariables.put(MediatorUtil.removeBacktick(v),new HashSet<String>());
			Rule subRule = generateSubrule(tableRule,v);
			logger.info("Rule for "+ MediatorUtil.removeBacktick(v) + ":" + subRule);
        	RuleRDFGenerator rgen = new RuleRDFGenerator(subRule, sourceNamespaces,
        			ontologyNamespaces, outWriter, uniqueId+"");
        	rdfGenerators.put(MediatorUtil.removeBacktick(v),rgen);
		}
	}
	
	/** 
	 * Returns all variables needed for the RDF generation of "var".
	 * @param var
	 * 		a variable name
	 * @return
	 * 		all variables needed for the RDF generation of "var".
	 */
	public Set<String> getRelatedRDFVariables(String var){
		return rdfVariables.get(var);
	}
	
	/**
	 * Generates RDF triples using the rdf rule corresponding to the variable varName. 
	 * @param varName
	 * 		a variable name; generates rdf corresponding to this variable
	 * @param values
	 * 		values corresponding to the variables present in the rule
	 * @param gensymId
	 * 		a seed used for the generation of gensym URIs
	 * @throws UnsupportedEncodingException
	 * @throws MediatorException
	 */
	public void generateTriples(String varName, Map<String,String> values, String gensymId) throws UnsupportedEncodingException, MediatorException{
		//get the RDF generator for this variable
		RuleRDFGenerator rdfGen = rdfGenerators.get(varName);
		//set the seed
		rdfGen.setUniqueID(uniqueId+"_"+gensymId);
		rdfGen.generateTriples(values);
	}
	
	/**
	 * Generates triples and writes them to output.
	 * @param values
	 * 		map containing <column_name,value> where "column_name" are attribute names used in the rule.
	 * @throws MediatorException
	 * @throws UnsupportedEncodingException 
	 */
	public void generateTriples(Map<String,String> values) throws MediatorException, UnsupportedEncodingException{
		rdfGenerator.generateTriples(values);
	}

	/**
	 * Generates the subrule corresponding to variable v.
	 * 1. Determine the binary predicate that has v as a second term. hasV(uri(N), V) or hasV(uri(1), V) or (hasV(uri(N), uri(V))
	 * 2. Find all predicates related to uri(N) & uri(V)
	 * 3. create a rule that contains only the predicates relevant to the variable "v"
	 * @param rule
	 * @param v
	 * @return
	 * 		the subrule corresponding to variable v.
	 * @throws MediatorException 
	 */
	private Rule generateSubrule(Rule rule, String v) throws MediatorException {
		logger.debug("Generate subrule for=" + v);
		LAVRule subrule = new LAVRule();
		RelationPredicate antecedent = new RelationPredicate("Subrule");
		ArrayList<Predicate> consequent = new ArrayList<Predicate>();
		//get all predicates for this rule
		ArrayList<Predicate> preds = rule.getConsequent();
		//find a binary predicate that has "v" as the second variable
		//this is the predicate that I start from (hasV(uri(N), V) or hasV(uri(N), uri(V))
		ArrayList<Predicate> binaryP = findAllBinaryPredicates(v, preds);
		if(!binaryP.isEmpty()){
			for(Predicate p:binaryP){
				//System.out.println("Binary Pred="+p);
				//find the varbiable on the first position
				//this var is needed to construct this rule
				String firstVar = getFirstVariableName(p);
				if(firstVar!=null){
					antecedent.addTermIfUnique(firstVar);
				}
				consequent.add(getInverse(p.clone()));

				//both terms could be a URI
				findAllRelatedPredicates(p.getTerms().get(0), preds, antecedent, consequent);
				findAllRelatedPredicates(p.getTerms().get(1), preds, antecedent, consequent);
			}
		}
		else{
			//find a unary predicate hasName(uri(N))
			Predicate p = findUnaryPredicate(v, preds);
			if(p!=null){
				consequent.add(p.clone());				
			}
			else{
				throw new MediatorException("No rule found for variable: " + v);
			}
		}

		//add all related variables
		rdfVariables.get(MediatorUtil.removeBacktick(v)).addAll(antecedent.getVars());
		//add the initial variable
		antecedent.addTermIfUnique(v);
		subrule.addAntecedentPredicate(antecedent);
		subrule.addConsequent(consequent);
		return subrule;
	}

	
	/** Recursively finds all related predicates to a given term.
	 * 1. if the term is a function/uri term uri(1) or uri(V) find a unary predicate 
	 * that has "term" as it's only term. Person(uri(N)) or Activity(uri(1))
	 * 3. If the uri is NOT a gensym, STOP. This is the end of the rule.
	 * 4. If the uri is a gensym, find a binary predicate that has the gensym as a second arg.
	 * 5. If no such predicate exists, STOP.
	 * 6. If a binary predicate exists, start from 1 with the input term being the first 
	 * term of the  binary predicate.
	 * @param term
	 * 		finds all predicates related to this term
	 * @param preds
	 * 		a list of predicates
	 * @param antecedent
	 * 		a relation predicate used as the head of the final rule; new found 
	 * 		variables are being added to this
	 * @param consequent
	 * 		a list of predicates used as the body of the final rule; new found 
	 * 		predicates are being added to this
	 * @throws MediatorException 
	 */
	private void findAllRelatedPredicates(Term term,
			ArrayList<Predicate> preds, RelationPredicate antecedent,
			ArrayList<Predicate> consequent) throws MediatorException {

		if(term instanceof VarTerm)
			return;
		else if(term instanceof FunctionTerm){
			//it is a uri();
			//find the unary predicate for this URI
			Predicate p2 = findUnaryPredicate(term, preds);
			//System.out.println("Unary Pred ...="+p2);
			if(!consequent.contains(p2))
				consequent.add(p2.clone());
			if(gensymPredicate(p2)){
				//if it is a gensym see if you can find other related 
				Predicate p1 = findBinaryPredicate(p2.getTerms().get(0),preds);
				//System.out.println("Binary Pred="+p1);
				if(p1!=null){
					//find the varbiable on the first position
					//this var is needed to construct this rule
					String firstVar = getFirstVariableName(p1);
					if(firstVar!=null){
						antecedent.addTermIfUnique(firstVar);
					}
					if(!consequent.contains(p1))
						consequent.add(getInverse(p1.clone()));
					findAllRelatedPredicates(p1.getTerms().get(0),preds,antecedent,consequent);
				}
			}
		}
	}

	/**
	 * Returns the unary predicate that has the term equal to a given term.
	 * @param term
	 * @param preds
	 * 		a list of unary and binary perdicates
	 * @return
	 * 		the unary predicate that has the term equal to a given term.
	 */
	private Predicate findUnaryPredicate(Term term,ArrayList<Predicate> preds) {
		for(Predicate p:preds){
			if(p.getTerms().size()==1){
				Term t = p.getTerms().get(0);
				if(t.equals(term))
					return p;
			}
		}
		return null;
	}

	/**
	 * Returns the unary predicate that has a FunctionTerm containing the variable v.
	 * @param v
	 * 		variable name
	 * @param preds
	 * 		a list of unary and binary perdicates
	 * @return
	 * 		the unary predicate that has a FunctionTerm containing the variable v.
	 * <br> Exabple: hasName(uri(v))
	 */
	private Predicate findUnaryPredicate(String v,ArrayList<Predicate> preds) {
		for(Predicate p:preds){
			if(p.getTerms().size()==1){
				Term t = p.getTerms().get(0);
				if(t instanceof FunctionTerm){
					String var = t.getFunction().getTerms().get(0).getVar();
					if(var!=null && var.equals(v))
						return p;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the binary predicate that has the SECOND term equal to a given term.
	 * @param term
	 * @param preds
	 * 		a list of unary and binary perdicates
	 * @return
	 * 		the binary predicate that has the SECOND term equal to a given term.
	 */
	private Predicate findBinaryPredicate(Term term,ArrayList<Predicate> preds) {
		for(Predicate p:preds){
			if(p.getTerms().size()==2){
				//get second term
				Term t = p.getTerms().get(1);
				if(t.equals(term))
					return p;
			}
		}
		return null;
	}

	/**
	 * Returns a list of binary predicates that have the SECOND term equal to a given variable OR
	 * uri(V).
	 * @param v
	 * 		a variable name
	 * @param preds
	 * 		a list of unary and binary perdicates
	 * @return
	 * 		a list of binary predicates that have the SECOND term equal to a 
	 * 		given variable OR uri(V).
	 */
	private ArrayList<Predicate> findAllBinaryPredicates(String v,ArrayList<Predicate> preds) {
		ArrayList<Predicate> binaryP = new ArrayList<Predicate>();
		for(Predicate p:preds){
			if(p.getTerms().size()==2){
				//get second term
				Term t = p.getTerms().get(1);
				String var=null;
				if(t instanceof VarTerm){
					var = t.getVar();
				}
				else if(t instanceof FunctionTerm){
					var = t.getFunction().getTerms().get(0).getVar();
				}
				if(var!=null && var.equals(v))
					binaryP.add(p);
			}
		}
		return binaryP;
	}

	/**
	 * Returns true if the FunctionTerm for this unary predicate is a 
	 * gensym URI (e.g., uri(1))
	 * @param p
	 * 		a unary predicate that contains a uri() FunctionTerm
	 * @return
	 * 		true: if the FunctionTerm for this unary predicate is a 
	 * gensym URI (e.g., uri(1))
	 * 		false: otherwise
	 */
	private boolean gensymPredicate(Predicate p){
		Term t = p.getTerms().get(0);
		if(!(t instanceof FunctionTerm))
			return false;
		String varName = t.getFunction().getTerms().get(0).getVar();
		if(varName==null){
			//it is a gensym
			return true;
		}
		else return false;
	}
	
	/**
	 * Returns the variable belonging to the first term. The first term may be a URI function.
	 * @param p
	 * 		a Predicate
	 * @return
	 * 		the variable belonging to the first term. The first term may be a URI function.
	 */
	private String getFirstVariableName(Predicate p){
		Term t = p.getTerms().get(0);
		if(t instanceof VarTerm)
				return t.getVar();
		else if(t instanceof FunctionTerm)
				return t.getFunction().getTerms().get(0).getVar();
		else return null;
	}

	/**
	 * Returns a predicate that is the inverse of the input predicate.
	 * @param p
	 * @return
	 * 		a predicate that is the inverse of the input predicate.
	 * @throws MediatorException
	 * <br> Example:
	 * inverse__of___hasName(Name, uri(Name)) -> hasName(uri(Name), Name))
	 */
	private Predicate getInverse(Predicate p) throws MediatorException {
		//see if the name of this predicate starts with inverse__of
		if(p.getName().startsWith("`" + inverseProperty)){
			//change order of attributes
			if(p.getTerms().size()!=2)
				throw new MediatorException("Only a binary predicate can be inversed:"+p);
			Term t0 = p.getTerms().get(0);
			Term t1 = p.getTerms().get(1);
			p.setTerm(t0, 1);
			p.setTerm(t1, 0);
			p.setName("`" + p.getName().substring(inverseProperty.length()+1));
			return p;
		}
		else return p;
	}

	/**
	 * Close Output Writer.
	 */
	public void closeWriter(){
		outWriter.flush();
		outWriter.close();
	}

}