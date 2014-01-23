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
