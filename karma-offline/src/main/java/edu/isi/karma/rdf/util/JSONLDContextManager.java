package edu.isi.karma.rdf.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.isi.karma.kr2rml.ContextIdentifier;

public class JSONLDContextManager {

	protected HashMap<String, ContextIdentifier> contextIdentifiers;
	protected HashMap<String, JSONObject> contextCache;

	public JSONLDContextManager() {

		this.contextCache = new HashMap<>();
		this.contextIdentifiers = new HashMap<>();
	}

	public ContextIdentifier getContextIdentifier(String contextName) {
		return this.contextIdentifiers.get(contextName);
	}

	public void addContext(ContextIdentifier id) {
		this.contextIdentifiers.put(id.getName(), id);
	}

	public JSONObject loadContext(ContextIdentifier id) throws IOException {
		if (contextCache.containsKey(id.getName())) {
			return contextCache.get(id.getName());
		}
		InputStream jsonStream;
		if (id.getContent() != null)
			jsonStream = IOUtils.toInputStream(id.getContent(), "utf-8");
		else
			jsonStream = id.getLocation().openStream();
		JSONTokener token = new JSONTokener(new InputStreamReader(jsonStream));
		JSONObject obj = new JSONObject(token);
		this.contextCache.put(id.getName(), obj);
		return obj;
	}

	public JSONObject getContext(String contextName, ContextIdentifier contextId) {
		JSONObject context;
		if (contextId == null) {
			context = new JSONObject();
		} else {
			context = this.contextCache.get(contextName);
		}
		if (context == null) {
			try {
				context = loadContext(contextId);
			} catch (Exception e) {
				context = new JSONObject();
			}
		}
		return context;
	}
}
