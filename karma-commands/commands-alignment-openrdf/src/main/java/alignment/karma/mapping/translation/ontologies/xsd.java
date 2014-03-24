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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Class xsd
 *
 * @since 12/03/2013
 */
public class xsd
{
	private static final String PREFIX = "xsd";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.w3.org/2001/XMLSchema#");
	}

	public static String getNamespace()
	{
		return "http://www.w3.org/2001/XMLSchema#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	//Classes
	public static final Resource String = ResourceFactory.createResource("http://www.w3.org/2001/XMLSchema#String");
	public static final Resource DateTime = ResourceFactory.createResource("http://www.w3.org/2001/XMLSchema#DateTime");
}
