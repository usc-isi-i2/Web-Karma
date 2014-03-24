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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Class graph
 *
 * @since 08/29/2013
 */
public class graph
{
	private static final String PREFIX = "graph";
	private static final String NAMESPACE = "http://www.cubrc.org/ontologies/KDD/Mid/GraphOntology#";

	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, NAMESPACE);
	}

	public static String getNamespace()
	{
		return NAMESPACE;
	}

	public static final Resource Graph = ResourceFactory.createResource(NAMESPACE + "Graph");
	public static final Resource Node = ResourceFactory.createResource(NAMESPACE + "Node");
	public static final Resource Edge = ResourceFactory.createResource(NAMESPACE + "Edge");
	//public static final Resource



}
