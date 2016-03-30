package edu.isi.karma.kr2rml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.modeling.Uris;

public class ContextGenerator {
	private Model model;
	private boolean isGenerateAtIdType;
	Map<String, Set<ContextObject>> contextMapping = new HashMap<>();
	private abstract class ContextObject {
		public String prefix;
		public String URI;
		public ContextObject(String prefix, String URI) {
			this.prefix = prefix;
			this.URI = URI;
		}

		@Override
		public int hashCode() {
			return prefix.hashCode() * URI.hashCode();		
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ContextObject) {
				return ((ContextObject) o).URI == URI;
			}
			return false;
		}

		public abstract boolean isClassContext();
		public abstract String getDataType();
	}

	private class ClassContextObject extends ContextObject {
		public ClassContextObject(String prefix, String URI) {
			super(prefix, URI);
		}

		@Override
		public boolean isClassContext() {
			return true;
		}

		@Override
		public String getDataType() {
			return null;
		}

	}

	private class PredicateContextObject extends ContextObject {
		public String dataType;
		public PredicateContextObject(String prefix, String URI, String dataType) {
			super(prefix, URI);
			this.dataType = dataType;
		}
		@Override
		public boolean isClassContext() {
			return false;
		}
		@Override
		public String getDataType() {
			return dataType;
		}

	}
	public ContextGenerator(Model model, boolean isGenerateAtIdType) {
		this.model = model;
		this.isGenerateAtIdType = isGenerateAtIdType;
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
							existPrefixes = new HashSet<>();
						}
						existPrefixes.add(new ClassContextObject(prefix, fullURI));
						contextMapping.put(postfix, existPrefixes);
					}
				}
			}
			if (stmt.getPredicate().getURI().equals(Uris.RR_PREDICATE_URI)) {
				if (stmt.getObject().isURIResource()) {
					Property objectMapProp = model.getProperty(Uris.RR_OBJECTMAP_URI);
					Property dataTypeProp = model.getProperty(Uris.RR_DATATYPE_URI);
					RDFNode node = stmt.getSubject().getProperty(objectMapProp).getObject();
					String dataType = null;
					if (node != null && node.isResource()) {
						Statement s = node.asResource().getProperty(dataTypeProp);
						if (s != null) {
							dataType = model.shortForm(s.getObject().toString());
						}
					}
					String shortForm = model.shortForm(stmt.getObject().toString());
					String fullURI = stmt.getObject().toString();
					if (!shortForm.equals(fullURI)) {
						String postfix = shortForm.substring(shortForm.lastIndexOf(":") + 1);
						String prefix = shortForm.substring(0, shortForm.lastIndexOf(":"));
						Set<ContextObject> existPrefixes = contextMapping.get(postfix);
						if (existPrefixes == null) {
							existPrefixes = new HashSet<>();
						}
						existPrefixes.add(new PredicateContextObject(prefix, fullURI, dataType));
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
					JSONObject t = new JSONObject();
					t.put("@id", prefix.URI);
					if (prefix.isClassContext()) {
						t.put("@type", "@id");
					}
					String dataType = prefix.getDataType();
					if (dataType != null) {
						String p = dataType.substring(0, dataType.lastIndexOf(":"));
						obj.put(p, model.getNsPrefixURI(p));
						t.put("@type", dataType);
					}
					obj.put(entry.getKey(), t);
				}
			}
		}
		if (isGenerateAtIdType) {
			obj.put("uri", "@id");
			obj.put("a", "@type");
		}
		JSONObject top = new JSONObject();
		top.put("@context", obj);
		return top;
	}
}
