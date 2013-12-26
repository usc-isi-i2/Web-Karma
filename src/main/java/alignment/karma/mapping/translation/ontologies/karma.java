package alignment.karma.mapping.translation.ontologies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Class karma
 *
 * @since 12/02/2013
 */
public class karma
{
	private static final String PREFIX = "karma";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://isi.edu/integration/karma/ontologies/model/current#");
	}

	public static String getNamespace()
	{
		return "http://isi.edu/integration/karma/ontologies/model/current#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	//Classes
	public static final Resource Attribute = ResourceFactory.createResource("http://isi.edu/integration/karma/ontologies/model/current#Attribute");
	public static final Resource Source = ResourceFactory.createResource("http://isi.edu/integration/karma/ontologies/model/current#Source");
	//Properties
	public static final Property has_column_name = ResourceFactory.createProperty("http://isi.edu/integration/karma/ontologies/model/current#hasColumnName");
	public static final Property has_name = ResourceFactory.createProperty("http://isi.edu/integration/karma/ontologies/model/current#hasName");
}
