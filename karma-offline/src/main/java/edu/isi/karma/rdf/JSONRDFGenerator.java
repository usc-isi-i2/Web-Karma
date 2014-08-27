package edu.isi.karma.rdf;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONException;

import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.N3KR2RMLRDFWriter;
import edu.isi.karma.webserver.KarmaException;


//If running in offline mode, need to set manual.alignment=true in modeling.peoperties
/**
 * JSONRDFGenerator
 *
 * @deprecated use {@link GenericRDFGenerator} instead.  
 */
@Deprecated
public class JSONRDFGenerator extends GenericRDFGenerator {

	private static JSONRDFGenerator instance = null;

	public static JSONRDFGenerator getInstance(String selectionName) {
		if(instance == null) {
			instance = new JSONRDFGenerator(selectionName);
		}
		return instance;
	}

	private JSONRDFGenerator(String selectionName) {
		super(selectionName);
	}
	
	void generateRDF(String modelName, String jsonData, boolean addProvenance, PrintWriter pw) throws KarmaException, JSONException, IOException {
		URIFormatter uriFormatter = new URIFormatter();
		KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
		this.generateRDF(modelName, null, jsonData, InputType.JSON, addProvenance, outWriter);
	}
	

}
