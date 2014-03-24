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

package alignment.karma.mapping.translation.translations.d2rq;

import alignment.karma.mapping.translation.ontologies.r2rml;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

/**
 * Class D2rqTranslation
 *
 * @since 12/27/2013
 */
public class D2rqTranslation
{
	protected static List<String> getConnectedColumns(RDFNode rdfNode, Property predicate, Model karmaModel)
	{
		List<String> columnNames = new ArrayList<>();
		Resource triplesMap = karmaModel.listStatements(null, r2rml.subjectMap, rdfNode).nextStatement().getSubject().asResource();
		if (triplesMap.getProperty(RDF.type).getObject().equals(r2rml.TriplesMap))
		{
			for (Statement s : triplesMap.listProperties(r2rml.predicateObjectMap).toList())
			{
				Resource columnConnector = s.getObject().asResource();
				if (columnConnector.getProperty(r2rml.predicate).getObject().asResource().equals(predicate))
				{
					for (Statement column : columnConnector.listProperties(r2rml.objectMap).toList())
					{
						for (Statement columnVal : karmaModel.listStatements(column.getObject().asResource(), r2rml.column, (RDFNode) null).toList())
						{
							columnNames.add(columnVal.getObject().asLiteral().getString());
						}
					}
				}
			}
		}
		return columnNames;
	}

	protected static boolean isThisAttributeMapped(Model karmaModel, Resource type)
	{
		return karmaModel.listStatements(null, r2rml.clazz, type).hasNext();
	}
}
