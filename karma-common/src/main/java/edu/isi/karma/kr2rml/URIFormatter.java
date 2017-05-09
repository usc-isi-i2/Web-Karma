/*******************************************************************************
 * Copyright 2014 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.kr2rml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.kr2rml.ErrorReport.Priority;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.ontology.OntologyManager;

public class URIFormatter {
	
	protected final Map<String, String> prefixToNamespaceMap;
	private final ErrorReport errorReport;
	private final boolean reportErrors;
	private final boolean replacePrefixesWithNamespaces;
	public URIFormatter()
	{
		reportErrors = false;
		replacePrefixesWithNamespaces = false;
		prefixToNamespaceMap = new HashMap<>();
		errorReport = null;
	}

	public URIFormatter(OntologyManager ontMgr, ErrorReport errorReport)
	{
		reportErrors = true;
		replacePrefixesWithNamespaces = true;
		prefixToNamespaceMap = new HashMap<>();
		populatePrefixToNamespaceMap(ontMgr);
		this.errorReport = errorReport;
	}
	public URIFormatter(List<Prefix> prefixes, ErrorReport errorReport)
	{
		reportErrors = true;
		replacePrefixesWithNamespaces = true;
		prefixToNamespaceMap = new HashMap<>();
		populatePrefixToNamespaceMap(prefixes);
		this.errorReport = errorReport;
	}
	public String getExpandedAndNormalizedUri(String uri) {
		// Check if the predicate contains a predicate.
		if (replacePrefixesWithNamespaces && !uri.startsWith("<") && !uri.startsWith("http:") && !uri.startsWith("https:") && uri.contains(":") && !uri.startsWith("_:")) {
			// Replace the prefix with proper namespace by looking into the ontology manager
			String prefix = uri.substring(0, uri.indexOf(":"));
			
			String namespace = this.prefixToNamespaceMap.get(prefix);
			if (namespace == null || namespace.isEmpty()) {
				if(reportErrors)
				{
					this.errorReport.addReportMessage(new ReportMessage("Error creating predicate's URI: " + uri, 
						"No namespace found for the prefix: " + prefix, Priority.high));
				}
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
		//System.out.print("Normalize:" + inputUri + " = ");
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
				sb.append("%20");
				continue;
			}
			else if(value == ',' || value == '`' || value == '\'' )
			{
				if(!foundIssue)
				{
					foundIssue = true;
					sb.append(inputUri.substring(0, i));
				}
				if(value == ',')
					sb.append("%2C");
				else if (value == '`')
					sb.append("%60");
				else if (value == '\'')
					sb.append("%27");
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
			//System.out.println(sb.toString());
			return sb.toString();
		}
		else
		{
			//System.out.println(inputUri);
			return inputUri;
		}
	}
	
	
	
	private void populatePrefixToNamespaceMap(OntologyManager ontMgr) {
		Map<String, String> prefixMapOntMgr = ontMgr.getPrefixMap(); 
		for (Map.Entry<String, String> stringStringEntry : prefixMapOntMgr.entrySet()) {
			String prefix = stringStringEntry.getValue();
			this.prefixToNamespaceMap.put(prefix, stringStringEntry.getKey());
		}
	}
	
	private void populatePrefixToNamespaceMap(List<Prefix>prefixes) {
		
		for(Prefix prefix : prefixes)
		{
			this.prefixToNamespaceMap.put(prefix.getPrefix(), prefix.getNamespace());
		}
	}
}
