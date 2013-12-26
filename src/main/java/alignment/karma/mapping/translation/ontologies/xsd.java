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
