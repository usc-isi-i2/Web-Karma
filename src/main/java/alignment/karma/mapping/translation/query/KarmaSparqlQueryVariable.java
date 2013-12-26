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
