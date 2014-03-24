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
public class swrl
{
	private static final String PREFIX = "swrl";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.w3.org/2003/11/swrl#");
	}

	public static String getNamespace()
	{
		return "http://www.w3.org/2003/11/swrl#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Property
	public static final Property argument2 = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#argument2");

	// Class
	public static final Resource Atom = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#Atom");
	public static final Resource AtomList = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#AtomList");
	public static final Resource Builtin = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#Builtin");
	public static final Resource BuiltinAtom = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#BuiltinAtom");
	public static final Resource ClassAtom = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#ClassAtom");
	public static final Resource DataRangeAtom = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#DataRangeAtom");
	public static final Resource DatavaluedPropertyAtom = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#DatavaluedPropertyAtom");
	public static final Resource DifferentIndividualsAtom = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#DifferentIndividualsAtom");
	public static final Resource Imp = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#Imp");
	public static final Resource IndividualPropertyAtom = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#IndividualPropertyAtom");
	public static final Resource SameIndividualAtom = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#SameIndividualAtom");
	public static final Resource Variable = ResourceFactory.createResource("http://www.w3.org/2003/11/swrl#Variable");

	// ObjectProperty
	public static final Property argument1 = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#argument1");
	public static final Property arguments = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#arguments");
	public static final Property body = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#body");
	public static final Property builtin = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#builtin");
	public static final Property classPredicate = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#classPredicate");
	public static final Property dataRange = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#dataRange");
	public static final Property head = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#head");
	public static final Property propertyPredicate = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrl#propertyPredicate");
}

