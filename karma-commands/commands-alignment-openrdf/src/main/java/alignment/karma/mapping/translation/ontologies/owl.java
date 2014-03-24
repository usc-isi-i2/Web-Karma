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

package alignment.karma.mapping.translation.ontologies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class owl
{
	private static final String PREFIX = "owl";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.w3.org/2002/07/owl#");
	}

	public static String getNamespace()
	{
		return "http://www.w3.org/2002/07/owl#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource AllDifferent = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#AllDifferent");
	public static final Resource AllDisjointClasses = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#AllDisjointClasses");
	public static final Resource AllDisjointProperties = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#AllDisjointProperties");
	public static final Resource Annotation = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Annotation");
	public static final Resource AnnotationProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#AnnotationProperty");
	public static final Resource AsymmetricProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#AsymmetricProperty");
	public static final Resource Axiom = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Axiom");
	public static final Resource Class = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Class");
	public static final Resource DataRange = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#DataRange");
	public static final Resource DatatypeProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#DatatypeProperty");
	public static final Resource DeprecatedClass = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#DeprecatedClass");
	public static final Resource DeprecatedProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#DeprecatedProperty");
	public static final Resource FunctionalProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#FunctionalProperty");
	public static final Resource InverseFunctionalProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#InverseFunctionalProperty");
	public static final Resource IrreflexiveProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#IrreflexiveProperty");
	public static final Resource NamedIndividual = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#NamedIndividual");
	public static final Resource NegativePropertyAssertion = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#NegativePropertyAssertion");
	public static final Resource ObjectProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#ObjectProperty");
	public static final Resource Ontology = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Ontology");
	public static final Resource OntologyProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#OntologyProperty");
	public static final Resource ReflexiveProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#ReflexiveProperty");
	public static final Resource Restriction = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Restriction");
	public static final Resource SymmetricProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#SymmetricProperty");
	public static final Resource TransitiveProperty = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#TransitiveProperty");

	// Property
	public static final Property allValuesFrom = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#allValuesFrom");
	public static final Property annotatedProperty = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#annotatedProperty");
	public static final Property annotatedSource = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#annotatedSource");
	public static final Property annotatedTarget = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#annotatedTarget");
	public static final Property assertionProperty = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#assertionProperty");
	public static final Property cardinality = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#cardinality");
	public static final Property complementOf = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#complementOf");
	public static final Property datatypeComplementOf = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#datatypeComplementOf");
	public static final Property differentFrom = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#differentFrom");
	public static final Property disjointUnionOf = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#disjointUnionOf");
	public static final Property disjointWith = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#disjointWith");
	public static final Property distinctMembers = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#distinctMembers");
	public static final Property equivalentClass = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#equivalentClass");
	public static final Property equivalentProperty = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#equivalentProperty");
	public static final Property hasKey = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#hasKey");
	public static final Property hasSelf = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#hasSelf");
	public static final Property hasValue = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#hasValue");
	public static final Property intersectionOf = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#intersectionOf");
	public static final Property inverseOf = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#inverseOf");
	public static final Property maxCardinality = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#maxCardinality");
	public static final Property maxQualifiedCardinality = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");
	public static final Property members = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#members");
	public static final Property minCardinality = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#minCardinality");
	public static final Property minQualifiedCardinality = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#minQualifiedCardinality");
	public static final Property onClass = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#onClass");
	public static final Property onDataRange = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#onDataRange");
	public static final Property onDatatype = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#onDatatype");
	public static final Property onProperties = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#onProperties");
	public static final Property onProperty = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#onProperty");
	public static final Property oneOf = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#oneOf");
	public static final Property propertyChainAxiom = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#propertyChainAxiom");
	public static final Property propertyDisjointWith = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#propertyDisjointWith");
	public static final Property qualifiedCardinality = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#qualifiedCardinality");
	public static final Property sameAs = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs");
	public static final Property someValuesFrom = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#someValuesFrom");
	public static final Property sourceIndividual = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sourceIndividual");
	public static final Property targetIndividual = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#targetIndividual");
	public static final Property targetValue = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#targetValue");
	public static final Property unionOf = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#unionOf");
	public static final Property withRestrictions = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#withRestrictions");

	// Class
	public static final Resource Nothing = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Nothing");
	public static final Resource Thing = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Thing");

	// DatatypeProperty
	public static final Property bottomDataProperty = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#bottomDataProperty");
	public static final Property topDataProperty = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#topDataProperty");

	// ObjectProperty
	public static final Property bottomObjectProperty = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#bottomObjectProperty");
	public static final Property topObjectProperty = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#topObjectProperty");
}

