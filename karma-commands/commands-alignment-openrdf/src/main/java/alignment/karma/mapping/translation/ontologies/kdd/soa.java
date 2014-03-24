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

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class soa
{
	private static final String PREFIX = "soa";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#");
	}

	public static String getNamespace()
	{
		return "http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource Actor = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Actor");
	public static final Resource Composition = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Composition");
	public static final Resource Effect = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Effect");
	public static final Resource Element = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Element");
	public static final Resource Event = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Event");
	public static final Resource InformationType = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#InformationType");
	public static final Resource Policy = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Policy");
	public static final Resource Process = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Process");
	public static final Resource Service = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Service");
	public static final Resource ServiceCompostion = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#ServiceCompostion");
	public static final Resource ServiceContract = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#ServiceContract");
	public static final Resource ServiceInterface = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#ServiceInterface");
	public static final Resource System = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#System");
	public static final Resource Task = ResourceFactory.createResource("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#Task");

	// DatatypeProperty
	public static final Property compositionPattern = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#compositionPattern");
	public static final Property constraints = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#constraints");
	public static final Property interactionAspect = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#interactionAspect");
	public static final Property legalAspect = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#legalAspect");

	// ObjectProperty
	public static final Property appliesTo = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#appliesTo");
	public static final Property does = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#does");
	public static final Property doneBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#doneBy");
	public static final Property generatedBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#generatedBy");
	public static final Property generates = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#generates");
	public static final Property hasContract = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#hasContract");
	public static final Property hasInput = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#hasInput");
	public static final Property hasInterface = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#hasInterface");
	public static final Property hasOutput = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#hasOutput");
	public static final Property involvesParty = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#involvesParty");
	public static final Property isContractFor = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#isContractFor");
	public static final Property isInputAt = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#isInputAt");
	public static final Property isInterfaceOf = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#isInterfaceOf");
	public static final Property isOutputAt = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#isOutputAt");
	public static final Property isPartyTo = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#isPartyTo");
	public static final Property isSetBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#isSetBy");
	public static final Property isSpecifiedBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#isSpecifiedBy");
	public static final Property isSubjectTo = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#isSubjectTo");
	public static final Property orchestratedBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#orchestratedBy");
	public static final Property orchestrates = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#orchestrates");
	public static final Property performedBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#performedBy");
	public static final Property performs = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#performs");
	public static final Property representedBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#representedBy");
	public static final Property represents = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#represents");
	public static final Property respondedToBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#respondedToBy");
	public static final Property respondsTo = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#respondsTo");
	public static final Property setsPolicy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#setsPolicy");
	public static final Property specifies = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#specifies");
	public static final Property usedBy = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#usedBy");
	public static final Property uses = ResourceFactory.createProperty("http://www.semanticweb.org/ontologies/2010/01/core-soa.owl#uses");
}

