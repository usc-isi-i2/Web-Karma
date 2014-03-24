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
public class personMappingOntology
{
	private static final String PREFIX = "personMappingOntology";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://PersonMap.org#");
	}

	public static String getNamespace()
	{
		return "http://PersonMap.org#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource Associate = ResourceFactory.createResource("http://PersonMap.org#Associate");
	public static final Resource Birth = ResourceFactory.createResource("http://PersonMap.org#Birth");
	public static final Resource BirthDate = ResourceFactory.createResource("http://PersonMap.org#BirthDate");
	public static final Resource BirthLocation = ResourceFactory.createResource("http://PersonMap.org#BirthLocation");
	public static final Resource Date = ResourceFactory.createResource("http://PersonMap.org#Date");
	public static final Resource Death = ResourceFactory.createResource("http://PersonMap.org#Death");
	public static final Resource Document = ResourceFactory.createResource("http://PersonMap.org#Document");
	public static final Resource EyeColor = ResourceFactory.createResource("http://PersonMap.org#EyeColor");
	public static final Resource Family = ResourceFactory.createResource("http://PersonMap.org#Family");
	public static final Resource Father = ResourceFactory.createResource("http://PersonMap.org#Father");
	public static final Resource HairColor = ResourceFactory.createResource("http://PersonMap.org#HairColor");
	public static final Resource Height = ResourceFactory.createResource("http://PersonMap.org#Height");
	public static final Resource Identification = ResourceFactory.createResource("http://PersonMap.org#Identification");
	public static final Resource LanguageSkill = ResourceFactory.createResource("http://PersonMap.org#LanguageSkill");
	public static final Resource LifeEvents = ResourceFactory.createResource("http://PersonMap.org#LifeEvents");
	public static final Resource Location = ResourceFactory.createResource("http://PersonMap.org#Location");
	public static final Resource Marrage = ResourceFactory.createResource("http://PersonMap.org#Marrage");
	public static final Resource Mother = ResourceFactory.createResource("http://PersonMap.org#Mother");
	public static final Resource Name = ResourceFactory.createResource("http://PersonMap.org#Name");
	public static final Resource Nationality = ResourceFactory.createResource("http://PersonMap.org#Nationality");
	public static final Resource Person = ResourceFactory.createResource("http://PersonMap.org#Person");
	public static final Resource PersonConnection = ResourceFactory.createResource("http://PersonMap.org#PersonConnection");
	public static final Resource PhysicalAttribute = ResourceFactory.createResource("http://PersonMap.org#PhysicalAttribute");
	public static final Resource Sex = ResourceFactory.createResource("http://PersonMap.org#Sex");
	public static final Resource Skill = ResourceFactory.createResource("http://PersonMap.org#Skill");
	public static final Resource Weight = ResourceFactory.createResource("http://PersonMap.org#Weight");

	// DatatypeProperty
	public static final Property has_value = ResourceFactory.createProperty("http://PersonMap.org#has_value");

	// ObjectProperty
	public static final Property has_attribute = ResourceFactory.createProperty("http://PersonMap.org#has_attribute");
	public static final Property has_birth_date = ResourceFactory.createProperty("http://PersonMap.org#has_birth_date");
	public static final Property has_birth_location = ResourceFactory.createProperty("http://PersonMap.org#has_birth_location");
	public static final Property has_date = ResourceFactory.createProperty("http://PersonMap.org#has_date");
	public static final Property has_location = ResourceFactory.createProperty("http://PersonMap.org#has_location");
}

