package edu.isi.karma.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.webserver.KarmaException;

public class GenerateContextFromModel {

	public static void main(String[] args) throws JSONException, KarmaException, IOException {
		String fileName = args[0];
		String output = args[1];
		File file = new File(fileName);
		Model model = ModelFactory.createDefaultModel();
        InputStream s = new FileInputStream(file);
        model.read(s, null, "TURTLE");
        StmtIterator itr = model.listStatements();
        JSONObject obj = new JSONObject();
        while(itr.hasNext()) {
        	Statement stmt = itr.next();
        	if (stmt.getPredicate().getURI().equals(Uris.RR_CLASS_URI)) {
        		if (stmt.getObject().isURIResource()) {
        			String shortForm = model.shortForm(stmt.getObject().toString());
        			String fullURI = stmt.getObject().toString();
        			if (!shortForm.equals(fullURI)) {
        				obj.put(shortForm.substring(shortForm.lastIndexOf(":") + 1), new JSONObject().put("@id", fullURI));
        			}
        		}
        	}
        	if (stmt.getPredicate().getURI().equals(Uris.RR_PREDICATE_URI)) {
        		if (stmt.getObject().isURIResource()) {
        			String shortForm = model.shortForm(stmt.getObject().toString());
        			String fullURI = stmt.getObject().toString();
        			if (!shortForm.equals(fullURI)) {
        				obj.put(shortForm.substring(shortForm.lastIndexOf(":") + 1), new JSONObject().put("@id", fullURI));
        			}
        		}
        	}
        }
        JSONObject top = new JSONObject();
        top.put("@context", obj);
        PrintWriter pw = new PrintWriter(output);
        pw.println(top.toString(4));
        pw.close();
	}

}
