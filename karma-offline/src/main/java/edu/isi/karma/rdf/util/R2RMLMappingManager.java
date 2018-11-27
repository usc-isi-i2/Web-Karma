package edu.isi.karma.rdf.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.webserver.KarmaException;

public class R2RMLMappingManager {

	protected ConcurrentHashMap<String, R2RMLMappingIdentifier> modelIdentifiers;
	protected ConcurrentHashMap<String, WorksheetR2RMLJenaModelParser> readModelParsers;

	public R2RMLMappingManager() {
		this.modelIdentifiers = new ConcurrentHashMap<>();
		this.readModelParsers = new ConcurrentHashMap<>();
	}

	public void addModel(R2RMLMappingIdentifier modelIdentifier) {
		if (!modelIdentifiers.containsKey(modelIdentifier.getName())) {
			this.modelIdentifiers.put(modelIdentifier.getName(), modelIdentifier);
		}

	}

	public Map<String, R2RMLMappingIdentifier> getModels() {
		return Collections.unmodifiableMap(modelIdentifiers);
	}

	private synchronized WorksheetR2RMLJenaModelParser loadModel(R2RMLMappingIdentifier modelIdentifier)
			throws JSONException, KarmaException {
		if (readModelParsers.containsKey(modelIdentifier.getName())) {
			return readModelParsers.get(modelIdentifier.getName());
		}
		WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(modelIdentifier);
		this.readModelParsers.put(modelIdentifier.getName(), parser);
		return parser;
	}

	public R2RMLMappingIdentifier getMappingIdentifierByName(String modelName) throws KarmaException {
		R2RMLMappingIdentifier id = this.modelIdentifiers.get(modelName);
		if (id == null) {
			throw new KarmaException("Cannot generate RDF. Model named " + modelName + " does not exist");
		}
		return id;
	}

	public WorksheetR2RMLJenaModelParser getModelParser(String modelName) throws JSONException, KarmaException {
		WorksheetR2RMLJenaModelParser modelParser = readModelParsers.get(modelName);
		R2RMLMappingIdentifier id = this.modelIdentifiers.get(modelName);
		if (modelParser == null) {
			modelParser = loadModel(id);
		}
		return modelParser;
	}

}
