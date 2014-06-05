package edu.isi.karma.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;


//If running in offline mode, need to set manual.alignment=true in modeling.peoperties
public class JSONRDFGenerator extends GenericRDFGenerator {

	private static JSONRDFGenerator instance = null;

	public static JSONRDFGenerator getInstance() {
		if(instance == null) {
			instance = new JSONRDFGenerator();
		}
		return instance;
	}
	
	public void addModel(R2RMLMappingIdentifier modelIdentifier) {
		this.modelIdentifiers.put(modelIdentifier.getName(), modelIdentifier);
	}

	public Map<String, R2RMLMappingIdentifier> getModels()
	{
		return Collections.unmodifiableMap(modelIdentifiers);
	}


	private JSONRDFGenerator() {
		
	}
	
	@Override
	protected Worksheet generateWorksheet(String sourceName, InputStream data,
			Workspace workspace, int maxNumLines) throws IOException{
		//Generate worksheet from the json data
		Object json = JSONUtil.createJson(IOUtils.toString(data));
		JsonImport imp = new JsonImport(json, sourceName, workspace, "utf-8", maxNumLines);
		Worksheet worksheet = imp.generateWorksheet();
		return worksheet;
	}

}
