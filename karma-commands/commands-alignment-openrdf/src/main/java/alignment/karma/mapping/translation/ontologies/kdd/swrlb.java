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
public class swrlb
{
	private static final String PREFIX = "swrlb";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.w3.org/2003/11/swrlb#");
	}

	public static String getNamespace()
	{
		return "http://www.w3.org/2003/11/swrlb#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// DatatypeProperty
	public static final Property args = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrlb#args");
	public static final Property maxArgs = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrlb#maxArgs");
	public static final Property minArgs = ResourceFactory.createProperty("http://www.w3.org/2003/11/swrlb#minArgs");
}

