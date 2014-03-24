/*
 * Copyright (c) 2014 CUBRC, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *               http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package alignment.karma.mapping.translation.query;

import alignment.karma.mapping.translation.ontologies.r2rml;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Class KarmaMappingSparqlQuery
 *
 * @since 12/04/2013
 */
public class KarmaMappingSparqlQuery
{
	private StringBuilder queryBody;
	private List<KarmaSparqlQueryVariable> selectVariables;
	private HashSet<KarmaSparqlQueryVariable> alreadyUsedVariables;

	public KarmaMappingSparqlQuery()
	{
		selectVariables = new ArrayList<KarmaSparqlQueryVariable>();
		queryBody = new StringBuilder();
		alreadyUsedVariables = new HashSet<KarmaSparqlQueryVariable>();
	}

	private void declareVariable(KarmaSparqlQueryVariable subject)
	{
		queryBody.append(subject.variableName+" <"+ r2rml.clazz+"> <"+ subject.type +"> .");
	}

	private static int connectionCount = 0;
	public void addStatement(KarmaSparqlQueryVariable subject, Property predicate, KarmaSparqlQueryVariable object)
	{
		if(!alreadyUsedVariables.contains(subject)) {
			if(subject.type!=null) {
				declareVariable(subject);
			}
			alreadyUsedVariables.add(subject);
		}
		if(!alreadyUsedVariables.contains(object)) {
			if(subject.type!=null) {
				declareVariable(object);
			}
			alreadyUsedVariables.add(object);
		}
		//SubjectTriplesMap
		if(subject.triplesMapVariable==null) {
		subject.triplesMapVariable = subject.variableName+"TriplesMap"+connectionCount;
			queryBody.append(subject.triplesMapVariable+" <"+RDF.type+"> <"+ r2rml.TriplesMap+"> .");
			queryBody.append(subject.triplesMapVariable+" <"+r2rml.subjectMap+"> "+subject.variableName+" .");
		}
		String subjectPredicateMap = subject.variableName+"PredicateMap"+connectionCount;
		String referenceObjectMap = subject.variableName+"ReferenceObject"+connectionCount;
		queryBody.append(subject.triplesMapVariable+" <"+r2rml.predicateObjectMap+"> "+subjectPredicateMap+" .");

		queryBody.append(subjectPredicateMap+" <"+r2rml.objectMap+"> "+referenceObjectMap+" .");
		queryBody.append(subjectPredicateMap+" <"+r2rml.predicate+"> <"+predicate+"> .");

		if(object.triplesMapVariable==null) {
			object.triplesMapVariable = object.variableName+"TriplesMap"+connectionCount;
			queryBody.append(object.triplesMapVariable+" <"+RDF.type+"> <"+r2rml.TriplesMap+"> .");
			queryBody.append(object.triplesMapVariable+" <"+r2rml.subjectMap+"> "+object.variableName+" .");
		}
		queryBody.append(referenceObjectMap+" <"+r2rml.parentTriplesMap+"> "+object.triplesMapVariable+" .");
		queryBody.append(referenceObjectMap+" <"+RDF.type+"> <"+r2rml.RefObjectMap+"> .");

		connectionCount++;
	}

	public void connectToColumn(KarmaSparqlQueryVariable subject, Property predicate, KarmaSparqlQueryVariable columnNameVariable)
	{
		if(!alreadyUsedVariables.contains(subject)) {
			if(subject.type!=null) {
				declareVariable(subject);
			}
			alreadyUsedVariables.add(subject);
		}
		//SubjectTriplesMap
		if(subject.triplesMapVariable==null) {
			subject.triplesMapVariable = subject.variableName+"TriplesMap"+connectionCount;
			queryBody.append(subject.triplesMapVariable+" <"+RDF.type+"> <"+ r2rml.TriplesMap+"> .");
			queryBody.append(subject.triplesMapVariable+" <"+r2rml.subjectMap+"> "+subject.variableName+" .");
		}
		String subjectPredicateMap = subject.variableName+"PredicateMap"+connectionCount;
		String referenceObjectMap = subject.variableName+"ReferenceObject"+connectionCount;
		queryBody.append(subject.triplesMapVariable+" <"+r2rml.predicateObjectMap+"> "+subjectPredicateMap+" .");

		queryBody.append(subjectPredicateMap+" <"+r2rml.objectMap+"> "+referenceObjectMap+" .");
		queryBody.append(subjectPredicateMap+" <"+r2rml.predicate+"> <"+predicate+"> .");

		queryBody.append(referenceObjectMap+" <"+r2rml.column+"> "+columnNameVariable.variableName+" .");
		setSelectVariables(columnNameVariable);
		connectionCount++;
	}

	public com.hp.hpl.jena.query.Query getQuery()
	{
		StringBuilder wholeQuery = new StringBuilder();
		wholeQuery.append("SELECT ");
		for (KarmaSparqlQueryVariable s : selectVariables)
		{
			wholeQuery.append(s.variableName + " ");
		}
		wholeQuery.append("\nWHERE\n{\n");
		wholeQuery.append(queryBody);
		wholeQuery.append("}");
		try
		{
			com.hp.hpl.jena.query.Query query = QueryFactory.create(wholeQuery.toString(), Syntax.syntaxSPARQL_11);
			return query;
		}
		catch (QueryParseException e){
			throw e;
		}
	}

	public void setSelectVariables(KarmaSparqlQueryVariable... variables) {
		selectVariables = Arrays.asList(variables);
	}
}
