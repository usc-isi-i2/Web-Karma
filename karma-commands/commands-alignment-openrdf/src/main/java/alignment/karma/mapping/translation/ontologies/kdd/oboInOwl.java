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
public class oboInOwl
{
	private static final String PREFIX = "oboInOwl";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.geneontology.org/formats/oboInOwl#");
	}

	public static String getNamespace()
	{
		return "http://www.geneontology.org/formats/oboInOwl#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource DbXref = ResourceFactory.createResource("http://www.geneontology.org/formats/oboInOwl#DbXref");
	public static final Resource Definition = ResourceFactory.createResource("http://www.geneontology.org/formats/oboInOwl#Definition");
	public static final Resource ObsoleteClass = ResourceFactory.createResource("http://www.geneontology.org/formats/oboInOwl#ObsoleteClass");
	public static final Resource Subset = ResourceFactory.createResource("http://www.geneontology.org/formats/oboInOwl#Subset");
	public static final Resource Synonym = ResourceFactory.createResource("http://www.geneontology.org/formats/oboInOwl#Synonym");
	public static final Resource SynonymType = ResourceFactory.createResource("http://www.geneontology.org/formats/oboInOwl#SynonymType");

	// ObjectProperty
	public static final Property ObsoleteProperty = ResourceFactory.createProperty("http://www.geneontology.org/formats/oboInOwl#ObsoleteProperty");
}

