package edu.isi.karma.web.services.rdf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.N3KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rdf.GenericRDFGenerator;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.webserver.KarmaException;


@Path("/metadata") 
public class RDFGeneratorServlet{
	
	private static Logger logger = LoggerFactory.getLogger(RDFGeneratorServlet.class);

	/**
	 * 
	 * @throws IOException 
	 * @throws KarmaException */
	
	
	@POST
	@Consumes("text/plain")
	@Path("/images")
	public Response getMsg(String metadataJSON)
	{
		try
		{
			logger.info("Calling the web service");
			logger.info("Parse, model and pulish JSON:" + metadataJSON);
			
			UpdateContainer uc = new UpdateContainer();
			KarmaMetadataManager userMetadataManager = new KarmaMetadataManager();
			userMetadataManager.register(new UserPreferencesMetadata(), uc);
			userMetadataManager.register(new UserConfigMetadata(), uc);
			userMetadataManager.register(new PythonTransformationMetadata(), uc);

	        SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
	        ModelingConfiguration.setLearnerEnabled(false); // disable automatic learning
			GenericRDFGenerator gRDFGen = new GenericRDFGenerator();
			
			R2RMLMappingIdentifier rmlID = new R2RMLMappingIdentifier("rdf-model",
					new File("/home/amandeep/GitHub/Web-Karma/karma-web/src/main/webapp/publish/R2RML/WSP1WS2-metadata.json-model.ttl").toURI().toURL());
			
			gRDFGen.addModel(rmlID);
			
			//String filename = "C:\\metadata.json";
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			URIFormatter uriFormatter = new URIFormatter();
			KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
			
			//gRDFGen.generateRDF("rdf-model", new File(filename), InputType.JSON, false, outWriter);
			logger.info("Generating RDF");
			gRDFGen.generateRDF("rdf-model","PHONE",metadataJSON , InputType.JSON, false, outWriter);
			
			
			
			//String output = sw.toString();
			
			
			TripleStoreUtil tsu = new TripleStoreUtil();
			String tripleStoreURL = "http://localhost:3000/openrdf-sesame/repositories/karma_data";
			
			String context = "http://image-metadata.com";
			Boolean replace = true;
			String baseURI = "http://isiimagefinder/";
			
			logger.info("Publishing rdf to triplestore:" + "http://localhost:3000/openrdf-sesame/repositories/karma_data");
			boolean success = tsu.saveToStore(sw.toString(), tripleStoreURL, context, replace, baseURI);
			
			
			if(success)
				return Response.status(201).entity(metadataJSON).build(); //successfully created
			else
				return Response.status(503).entity("OOPS it did not work").build(); //Something went wrong
			//TODO set better return codes in case of error
			
			//return Response.status(200).entity(output).build();
		}
		catch(IOException ioe)
		{
			logger.error("IOException:" + ioe.getMessage());
			return Response.status(400).entity("Its an IOException:" + ioe.getMessage()).build(); //Something went wrong
		}
		catch(KarmaException ke)
		{
			logger.error("Exception:" + ke.getMessage());
			return Response.status(400).entity("Its a Karma Exception:" + ke.getMessage()).build(); //Something went wrong
		}
		catch (Exception e)
		{
			logger.error("Exception:" + e.getMessage());
			return Response.status(400).entity("Its an Exception:" + e.getMessage()).build(); //Something went wrong
		}
	}

}
