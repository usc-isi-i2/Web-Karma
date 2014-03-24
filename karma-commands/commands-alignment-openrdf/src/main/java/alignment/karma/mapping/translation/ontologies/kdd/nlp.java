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
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class nlp
{
	private static final String PREFIX = "nlp";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// ObjectProperty
	public static final Property adjunct = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#adjunct");
	public static final Property adverbial = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#adverbial");
	public static final Property appositional = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#appositional");
	public static final Property clause = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#clause");
	public static final Property complement = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#complement");
	public static final Property coordination = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#coordination");
	public static final Property direct_object = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#direct_object");
	public static final Property genitive = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#genitive");
	public static final Property indirect_object = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#indirect_object");
	public static final Property modifier = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#modifier");
	public static final Property nominal = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#nominal");
	public static final Property nominal_coordination = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#nominal_coordination");
	public static final Property prepositional = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#prepositional");
	public static final Property relation = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#relation");
	public static final Property subject = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/TextRelationOntology#subject");
}

