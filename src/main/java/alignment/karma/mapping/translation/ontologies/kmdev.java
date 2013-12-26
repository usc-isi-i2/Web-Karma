package alignment.karma.mapping.translation.ontologies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Class kmdev
 *
 * @since 12/03/2013
 */
public class kmdev
{
	private static final String PREFIX = "kmdev";


	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://isi.edu/integration/karma/dev#");
	}

	public static String getNamespace()
	{
		return "http://isi.edu/integration/karma/dev#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource R2RMLMapping = ResourceFactory.createResource("http://isi.edu/integration/karma/dev#R2RMLMapping");


	// Property
	public static final Property source_name = ResourceFactory.createProperty("http://isi.edu/integration/karma/dev#sourceName");
}
