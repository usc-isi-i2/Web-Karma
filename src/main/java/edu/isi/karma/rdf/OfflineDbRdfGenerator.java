package edu.isi.karma.rdf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.rep.metadata.SourceInformation;
import edu.isi.karma.rep.metadata.SourceInformation.InfoAttribute;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.mediator.gav.main.MediatorException;

public class OfflineDbRdfGenerator {
	private Model   jenaModel;
	private String 	hostname;
	private int 	portnumber;
	private String 	username;
	private String 	password;
	private String 	dBorSIDName;
	private String  tableName;
	private String 	sourceDesc;
	private AbstractJDBCUtil.DBType dbType;
	private String  modelFilePath;
	private String  outputFilePath;
	
	private static int DATABASE_TABLE_FETCH_SIZE = 200;
	
	public void setModelFilePath(String modelFilePath) {
		this.modelFilePath = modelFilePath;
	}

	public void setOutputFilePath(String outputFileName) {
		this.outputFilePath = outputFileName;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 3)
			throw new IllegalArgumentException("Illegal arguments! Please specify the model file path, the output file path and the database password as arguments.");
		
		OfflineDbRdfGenerator rdfGen = new OfflineDbRdfGenerator(args[0], args[1], args[2]);
		
		try {
			// Load the model file into a Jena Model
			System.out.print("Loading the model file into Jena model ...");
			rdfGen.loadModelFile();
			System.out.println("done.");
			
			// Query the model to populate the required arguments
			System.out.print("Parsing the model file to extract database information and source description ...");
			rdfGen.extractDbInformationAndSourceDescription();
			System.out.println("done.");
						
			// Create a connection to the database
			System.out.print("Creating a connection to the database, retrieving rows and generating RDF ...");
			rdfGen.connectAndGenerateRDF();
			System.out.println("done.");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KarmaException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (MediatorException e) {
			e.printStackTrace();
		}
		
	}

	public OfflineDbRdfGenerator(String modelFilePath, String outputFilePath, String password) {
		this.modelFilePath = modelFilePath;
		this.outputFilePath = outputFilePath;
		this.password =password;
	}

	private void loadModelFile() throws IOException {
		File file = new File(modelFilePath);
		if(!file.exists())
			throw new FileNotFoundException("Model file not found. Constructed path: " + file.getAbsolutePath());
		
		jenaModel = ModelFactory.createDefaultModel();
		InputStream s = new FileInputStream(file);
		jenaModel.read(s,null,"N3");
		s.close();
		
	}
	

	private void extractDbInformationAndSourceDescription() throws KarmaException {
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
			} else if(attr == InfoAttribute.dBorSIDName) {
				dBorSIDName = stmt.getString();
			} else if(attr == InfoAttribute.tableName) {
				tableName = stmt.getString();
			}
		}
		
		/** Get the source description **/
		Statement stmt = jenaModel.getProperty(source, hasSourceDesc);
		sourceDesc = stmt.getString();
	}
	

	private void connectAndGenerateRDF() throws SQLException, ClassNotFoundException, MediatorException, IOException {
		AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(this.dbType);
		Connection conn = dbUtil.getConnection(hostname, portnumber, username, password, dBorSIDName);
		
		String query = "Select * FROM " + tableName;
		java.sql.Statement stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(DATABASE_TABLE_FETCH_SIZE);
		
		ResultSet r = stmt.executeQuery(query);
		ResultSetMetaData meta = r.getMetaData();;
		
		// Get the column names
		ArrayList<String> columnNames = dbUtil.getColumnNames(tableName, conn);
		
		Workspace workspace = WorkspaceManager.getInstance().getFactory().createWorkspace();
		RepFactory factory = workspace.getFactory();
		Worksheet wk = factory.createWorksheet(tableName, workspace);
		ArrayList<String> headersList = addHeaders(wk, columnNames, factory);
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFilePath),"UTF-8");
		BufferedWriter bw = new BufferedWriter (fw);
		PrintWriter pw = new PrintWriter (bw);
		
		int counter = 0;
		
		while (r.next()) {
			
			// Create a new worksheet for every DATABASE_TABLE_FETCH_SIZE rows 
			if(counter%DATABASE_TABLE_FETCH_SIZE == 0 && counter != 0) {
				System.out.println("Done for " + counter + " rows ..." );
				
				WorksheetRDFGenerator rdfGen = new WorksheetRDFGenerator(factory, sourceDesc, pw);
				rdfGen.generateTriplesRow(wk, false, true);
				
				WorkspaceManager mgr = WorkspaceManager._getNewInstance();
				workspace = mgr.getFactory().createWorkspace();
				factory = workspace.getFactory();
				
				wk = factory.createWorksheet(tableName, workspace);
				headersList = addHeaders(wk, columnNames, factory);
			}
			
			/** Add the data **/
	        Table dataTable = wk.getDataTable();
	        Row row = dataTable.addRow(factory);
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				String val = r.getString(i);
				row.setValue(headersList.get(i-1), val, factory);
			}
			counter++;
		}
		
		WorksheetRDFGenerator rdfGen = new WorksheetRDFGenerator(factory, sourceDesc, pw);
		rdfGen.generateTriplesRow(wk, false, true);
		wk = factory.createWorksheet(tableName, workspace);
		headersList = addHeaders(wk, columnNames, factory);
		
		// Releasing all the resources
		r.close();
		conn.close();
		stmt.close();
		pw.flush();
		pw.close();
	}
	
	private ArrayList<String> addHeaders (Worksheet wk, ArrayList<String> columnNames, RepFactory factory) {
		HTable headers = wk.getHeaders();
		ArrayList<String> headersList = new ArrayList<String>();
        for(int i=0; i< columnNames.size(); i++){
        	HNode hNode = null;
        	hNode = headers.addHNode(columnNames.get(i), wk, factory);
        	headersList.add(hNode.getId());
        }
        return headersList;
	}

}
