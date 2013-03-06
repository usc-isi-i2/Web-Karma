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
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.mediator.gav.main.MediatorException;

/**
 * @author cgueret (https://github.com/cgueret)
 * @see 
 *      /Web-Karma-Public/src/main/java/edu/isi/karma/controller/command/publish/
 *      PublishRDFCommand.java
 * @see 
 *      /Web-Karma-Public/src/main/java/edu/isi/karma/modeling/alignment/Alignment
 *      .java
 * @see 
 *      /Web-Karma-Public/src/main/java/edu/isi/karma/controller/command/service/
 *      PublishModelCommand.java
 * @see /Web-Karma-Public/src/main/java/edu/isi/karma/controller/command/
 *      ImportCSVFileCommand.java
 * 
 * 
 */
public class OfflineCSVGenerator {

    /**
     * @param args
     * 		 model file, data file, output file, (optional)csv delimiter - default is '\t'
     * @throws MediatorException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws KarmaException
     */
    public static void main(String[] args) throws ClassNotFoundException,
	    IOException, MediatorException, KarmaException {
	if (args.length < 3)
	    throw new IllegalArgumentException(
		    "Arguments are: <model file> <data file> <output file> <csv delimiter>");

	String modelFilePath = args[0];
	String dataFilePath = args[1];
	String outputFilePath = args[2];
	char csvDelimiter = '\t';
	
	if(args.length==4)
		csvDelimiter = args[3].charAt(0);

	//System.out.println("CSV Delimiter:" + csvDelimiter);
	
	OfflineCSVGenerator generator = new OfflineCSVGenerator();
	generator.process(modelFilePath, dataFilePath, csvDelimiter, outputFilePath);
    }

    /**
     * @param model
     * @param data
     * @throws IOException
     * @throws MediatorException
     * @throws ClassNotFoundException
     * @throws KarmaException
     */
    public void process(String modelFilePath, String dataFilePath, char csvDelimiter,
	    String outputFilePath) throws IOException, ClassNotFoundException,
	    MediatorException, KarmaException {

	// Create the workspace
    Workspace workspace = WorkspaceManager._getNewInstance().createWorkspace();
    RepFactory factory = workspace.getFactory();
	
	// Load the CSV file into a new worksheet
	CSVFileImport fileImport = new CSVFileImport(1, 2, csvDelimiter, '\"',
		new File(dataFilePath), factory, workspace);
	Worksheet worksheet = fileImport.generateWorksheet();

	// Load the model into Jena
	File file = new File(modelFilePath);
	if (!file.exists())
	    throw new FileNotFoundException("Model file not found: "
		    + file.getAbsolutePath());
	Model model = ModelFactory.createDefaultModel();
	InputStream s = new FileInputStream(file);
	model.read(s, null, "N3");
	s.close();

	// Get the alignment rules
	Property hasSourceDesc = model.createProperty(Namespaces.KARMA,
		"hasSourceDescription");
	ResIterator itr = model.listResourcesWithProperty(hasSourceDesc);
	List<Resource> sourceList = itr.toList();
	if (sourceList.size() == 0)
	    throw new KarmaException("No source found in the model file.");
	Resource resource = sourceList.get(0);
	Statement stmt = model.getProperty(resource, hasSourceDesc);
	String domainStr = stmt.getObject().toString();
	//logger.info(domainStr);

	// Close the model
	model.close();

	// Write the triples
	OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(
		outputFilePath), "UTF-8");
	BufferedWriter bw = new BufferedWriter(fw);
	PrintWriter pw = new PrintWriter(bw);
	WorksheetRDFGenerator wrg = new WorksheetRDFGenerator(factory,
		domainStr, pw); // new PrintWriter(System.out)
	wrg.generateTriplesRow(worksheet, true);
	pw.close();
    }
}