package edu.isi.mediator.rdf;

import java.util.HashMap;
import java.util.Map;

import edu.isi.mediator.domain.DomainModel;

/**
 * A domain model that includes information about namespaces.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class RDFDomainModel extends DomainModel{

	public static String SOURCE_PREFIX="s";
	
	/**
	 * Contains mapping of prefix name to source namespace.
	 * If prefix is not used in the source desc we have only one source namespace.
	 * key=prefix; value=namespace;
	 */
	private Map<String,String> sourceNamespaces = new HashMap<String,String>();
	/**
	 * Contains mapping of prefix name to ontology namespace.
	 * If prefix is not used in the source desc we have only one ontology namespace.
	 * key=prefix; value=namespace;
	 */
	private Map<String,String> ontologyNamespaces = new HashMap<String,String>();

	public RDFDomainModel(DomainModel dm){
		sourceSchemas = dm.getSourceSchemas();
		domainSchemas = dm.getDomainSchemas();
		gavRules=dm.getGAVRules();
		lavRules=dm.getLAVRules();
		glavRules=dm.getGLAVRules();
	}
	
	/**
	 * Returns the source namespaces.
	 * @return
	 * 		the source namespaces
	 */
	public Map<String,String> getSourceNamespaces(){
		return sourceNamespaces;
	}
	/**
	 * Returns the ontology namespaces.
	 * @return
	 * 		the ontology namespaces
	 */
	public Map<String,String> getOntologyNamespaces(){
		return ontologyNamespaces;
	}

	/**
	 * Adds a source namespace.
	 * @param prefix
	 * @param namespace
	 */
	public void addSourceNamespace(String prefix, String namespace){
		sourceNamespaces.put(prefix, namespace);
	}
	
	/**
	 * Adds a ontology namespace.
	 * @param prefix
	 * @param namespace
	 */
	public void addOntologyNamespace(String prefix, String namespace){
		ontologyNamespaces.put(prefix, namespace);
	}
}
