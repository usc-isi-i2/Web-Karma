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

/**
 * Class r2rml
 *
 * @since 12/03/2013
 */
public class r2rml
{
	private static final String PREFIX = "r2rml";


	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.w3.org/ns/r2rml#");
	}

	public static String getNamespace()
	{
		return "http://www.w3.org/ns/r2rml#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
//	public static final Resource R2RMLMapping = ResourceFactory.createResource("http://www.w3.org/ns/r2rml#column");
	public static final Resource steinerTreeRootNode = ResourceFactory.createResource("http://www.w3.org/ns/r2rml#steinerTreeRootNode");
	public static final Resource TriplesMap = ResourceFactory.createResource("http://www.w3.org/ns/r2rml#TriplesMap");
	public static final Resource RefObjectMap = ResourceFactory.createResource("http://www.w3.org/ns/r2rml#RefObjectMap");

	// Property
	public static final Property column = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#column");
	public static final Property predicateObjectMap = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#predicateObjectMap");
	public static final Property subjectMap = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#subjectMap");
	public static final Property logicalTable = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#logicalTable");
	public static final Property clazz = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#class");
	public static final Property objectMap = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#objectMap");
	public static final Property predicate = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#predicate");
	public static final Property parentTriplesMap = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#parentTriplesMap");
}
