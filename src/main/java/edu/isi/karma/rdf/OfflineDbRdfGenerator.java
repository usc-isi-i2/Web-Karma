package edu.isi.karma.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.isi.karma.rep.metadata.SourceInformation;
import edu.isi.karma.rep.metadata.SourceInformation.InfoAttribute;
import edu.isi.karma.service.Namespaces;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.webserver.KarmaException;

public class OfflineDbRdfGenerator {
	private Model   jenaModel;
	private AbstractJDBCUtil.DBType dbType;
	private String 	hostname;
	private int 	portnumber;
	private String 	username;
	private String 	password;
	private String 	dBorSIDName;
	private String  tableName;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 1)
			throw new IllegalArgumentException("Illegal arguments! Please specify the model file path as the (only) argument.");
		
		OfflineDbRdfGenerator rdfGen = new OfflineDbRdfGenerator();
		try {
			// Load the model file into a Jena Model
			System.out.print("Loading the model into Jena model ...");
			rdfGen.loadModelFile(args[0]);
			System.out.println("done.");
			
			// Query the model to populate the required arguments
			System.out.print("Parsing the model file to extract database information ...");
			rdfGen.extractDbInformation();
			System.out.println("done.");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KarmaException e) {
			e.printStackTrace();
		} 
		
		// Create a connection to the database
		
		// Create a worksheet and generate RDF
		
	}

	private void loadModelFile(String modelFilePath) throws IOException {
		File file = new File(modelFilePath);
		if(!file.exists())
			throw new FileNotFoundException("Model file not found. Constructed path: " + file.getAbsolutePath());
		
		jenaModel = ModelFactory.createDefaultModel();
		InputStream s = new FileInputStream(file);
		jenaModel.read(s,null,"N3");
		s.close();
		
	}
	

	private void extractDbInformation() throws KarmaException {
		/** Get the resource from the Jena model corresponding to the source **/
		Property hasSourceDesc = jenaModel.createProperty(Namespaces.KARMA, "hasSourceDescription");
		ResIterator itr = jenaModel.listResourcesWithProperty(hasSourceDesc);
		List<Resource> sourceList = itr.toList();
		
		if(sourceList.size() == 0)
			throw new KarmaException("No source found in the model file.");
		Resource source = sourceList.get(0);
		
		/** Loop through the database properties to get the required information **/
		List<InfoAttribute> dbAttributeList = SourceInformation.getDatabaseInfoAttributeList();
		for (InfoAttribute attr : dbAttributeList) {
			Property prop = jenaModel.createProperty(Namespaces.KARMA, "hasSourceInfo_" + attr.name());
			Statement stmt = jenaModel.getProperty(source, prop);
			if (stmt == null)
				throw new KarmaException("RDF statement for the " + prop.getURI() + " property of source not found!");
			
			if(attr == InfoAttribute.dbType) {
				dbType = DBType.valueOf(stmt.getString());
			} else if(attr == InfoAttribute.hostname) {
				hostname = stmt.getString();
			} else if(attr == InfoAttribute.portnumber) {
				portnumber = Integer.parseInt(stmt.getString());
			} else if(attr == InfoAttribute.username) {
				username = stmt.getString();
			} else if(attr == InfoAttribute.password) {
				password = stmt.getString();
			} else if(attr == InfoAttribute.dBorSIDName) {
				dBorSIDName = stmt.getString();
			} else if(attr == InfoAttribute.tableName) {
				tableName = stmt.getString();
			}
			
			
		}
		
		System.out.println("Username:" + username);
		
	}

}
