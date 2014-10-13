package edu.isi.karma.kr2rml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.modeling.Uris;

public class ContextGenerator {
	private Model model;
	Map<String, Set<ContextObject>> contextMapping = new HashMap<String, Set<ContextObject>>();
	private class ContextObject {
		public String prefix;
		public String URI;
		public ContextObject(String prefix, String URI) {
			this.prefix = prefix;
			this.URI = URI;
		}
		
		public int hashCode() {
			return prefix.hashCode() * URI.hashCode();		
		}
		
		public boolean equals(Object o) {
			if (o instanceof ContextObject) {
				return ((ContextObject) o).URI == URI;
			}
			return false;
		}
	}
	public ContextGenerator(Model model) {
		this.model = model;
	}
	public JSONObject generateContext() {
		StmtIterator itr = model.listStatements();
        JSONObject obj = new JSONObject();
        while(itr.hasNext()) {
        	Statement stmt = itr.next();
        	if (stmt.getPredicate().getURI().equals(Uris.RR_CLASS_URI)) {
        		if (stmt.getObject().isURIResource()) {
        			String shortForm = model.shortForm(stmt.getObject().toString());
        			String fullURI = stmt.getObject().toString();
        			if (!shortForm.equals(fullURI)) {
        				String postfix = shortForm.substring(shortForm.lastIndexOf(":") + 1);
        				String prefix = shortForm.substring(0, shortForm.lastIndexOf(":"));
        				Set<ContextObject> existPrefixes = contextMapping.get(postfix);
        				if (existPrefixes == null) {
        					existPrefixes = new HashSet<ContextObject>();
        				}
        				existPrefixes.add(new ContextObject(prefix, fullURI));
        				contextMapping.put(postfix, existPrefixes);
        			}
        		}
        	}
        	if (stmt.getPredicate().getURI().equals(Uris.RR_PREDICATE_URI)) {
        		if (stmt.getObject().isURIResource()) {
        			String shortForm = model.shortForm(stmt.getObject().toString());
        			String fullURI = stmt.getObject().toString();
        			if (!shortForm.equals(fullURI)) {
        				String postfix = shortForm.substring(shortForm.lastIndexOf(":") + 1);
        				String prefix = shortForm.substring(0, shortForm.lastIndexOf(":"));
        				Set<ContextObject> existPrefixes = contextMapping.get(postfix);
        				if (existPrefixes == null) {
        					existPrefixes = new HashSet<ContextObject>();
        				}
        				existPrefixes.add(new ContextObject(prefix, fullURI));
        				contextMapping.put(postfix, existPrefixes);
        			}
        		}
        	}
        }
        for (Entry<String, Set<ContextObject>> entry : contextMapping.entrySet()) {
        	Set<ContextObject> prefixes = entry.getValue();
        	if (prefixes.size() > 1) {
        		for (ContextObject prefix : prefixes) {
        			String prefixURI = prefix.URI.replace(entry.getKey(), "");
        			obj.put(prefix.prefix, prefixURI);
        		}
        	}
        	else {
        		for (ContextObject prefix : prefixes) {
        			obj.put(entry.getKey(), new JSONObject().put("@id", prefix.URI));
        		}
        	}
        }
        JSONObject top = new JSONObject();
        top.put("@context", obj);
        return top;
	}
}
