package edu.isi.karma.kr2rml;

import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.kr2rml.ErrorReport.Priority;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;

public class URIFormatter {
	
	protected Map<String, String> prefixToNamespaceMap;
	private ErrorReport errorReport;



	public URIFormatter(OntologyManager ontMgr, ErrorReport errorReport)
	{
		prefixToNamespaceMap = new HashMap<String, String>();
		populatePrefixToNamespaceMap(ontMgr);
		this.errorReport = errorReport;
	}
	public String getExpandedAndNormalizedUri(String uri) {
		// Check if the predicate contains a predicate.
		if (!uri.startsWith("http:") && uri.contains(":")) {
			// Replace the prefix with proper namespace by looking into the ontology manager
			String prefix = uri.substring(0, uri.indexOf(":"));
			
			String namespace = this.prefixToNamespaceMap.get(prefix);
			if (namespace == null || namespace.isEmpty()) {
				this.errorReport.createReportMessage("Error creating predicate's URI: " + uri, 
						"No namespace found for the prefix: " + prefix, Priority.high);
//				logger.error("No namespace found for the predicate prefix: " + prefix);
			} else {
				uri = namespace + uri.substring(uri.indexOf(":")+1);
			}
		}
		
		// Remove all unwanted characters
		uri = normalizeUri(uri);
		
		// Put angled brackets if required
		if (!uri.startsWith(Uris.BLANK_NODE_PREFIX) && !uri.startsWith("<") && !uri.endsWith(">")) {
			uri = "<" + uri + ">";
		}
			
		return uri;
	}
	
	public static String normalizeUri(String inputUri) {

		boolean foundIssue = false;
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < inputUri.length(); i++)
		{
			char value = inputUri.charAt(i);
			if(value == ' ')
			{
				if(!foundIssue)
				{
					foundIssue = true;
					sb.append(inputUri.substring(0, i));
				}
				
				continue;
			}
			else if(value == ',' || value == '`' || value == '\'' )
			{
				if(!foundIssue)
				{
					foundIssue = true;
					sb.append(inputUri.substring(0, i));
				}
				else
				{
					sb.append('_');
				}
			}
			else
			{
				if(foundIssue)
				{
					sb.append(value);
				}
			}
		}
		if(foundIssue)
		{
			return sb.toString();
		}
		else
		{
			return inputUri;
		}
	}
	
	
	
	private void populatePrefixToNamespaceMap(OntologyManager ontMgr) {
		Map<String, String> prefixMapOntMgr = ontMgr.getPrefixMap(); 
		for (String ns:prefixMapOntMgr.keySet()) {
			String prefix = prefixMapOntMgr.get(ns);
			this.prefixToNamespaceMap.put(prefix, ns);
		}
	}
}
