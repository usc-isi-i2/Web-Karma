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

package alignment.karma.mapping.translation.query;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Class KarmaSparqlQueryVariable
 *
 * @since 12/04/2013
 */
public class KarmaSparqlQueryVariable
{
	public String variableName = "?karmaVariable";
	public String triplesMapVariable;
	Resource type;
	private static int variableCount;
	public KarmaSparqlQueryVariable() {
		variableName = variableName+(variableCount++);
	}

	public KarmaSparqlQueryVariable(String uri)
	{
		variableName = "<"+uri+">";
	}

	public KarmaSparqlQueryVariable(RDFNode rdfNode)
	{
		variableName = "_:"+rdfNode.toString();
	}

	public void setTriplesMapVariable(KarmaSparqlQueryVariable triplesMapVariable){
		this.triplesMapVariable = triplesMapVariable.variableName;
	}

	/**
	 * declare a variable with a spcific type
	 * @param type
	 */
	public KarmaSparqlQueryVariable(Resource type) {
		variableName = variableName+(variableCount++);
		this.type=type;
	}
}
