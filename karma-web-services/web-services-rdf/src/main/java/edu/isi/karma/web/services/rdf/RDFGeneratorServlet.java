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
	 
	private static final long serialVersionUID = -979319404654953710L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
				response.getWriter().println("Hello world Germany!");
	
	}
	 * @throws IOException 
	 * @throws KarmaException */
	
	
	@POST
	@Consumes("text/plain")
	@Path("/images")
	public Response getMsg(String metadataJSON)
	{
		try
		{
			logger.info("I got called");
			logger.info("Here is the json:" + metadataJSON);
			
			UpdateContainer uc = new UpdateContainer();
			KarmaMetadataManager userMetadataManager = new KarmaMetadataManager();
			userMetadataManager.register(new UserPreferencesMetadata(), uc);
			userMetadataManager.register(new UserConfigMetadata(), uc);
			userMetadataManager.register(new PythonTransformationMetadata(), uc);

	        SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
	        ModelingConfiguration.setLearnerEnabled(false); // disable automatic learning
			GenericRDFGenerator gRDFGen = new GenericRDFGenerator();
			
			R2RMLMappingIdentifier rmlID = new R2RMLMappingIdentifier("rdf-model",
					new File("C:\\Users\\saggu\\Documents\\GitHub\\Web-Karma\\karma-web\\src\\main\\webapp\\publish\\R2RML\\WSP1WS2-metadata.json-model.ttl").toURI().toURL());
			
			gRDFGen.addModel(rmlID);
			
			String filename = "C:\\metadata.json";
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			URIFormatter uriFormatter = new URIFormatter();
			KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
			
			//gRDFGen.generateRDF("rdf-model", new File(filename), InputType.JSON, false, outWriter);
			gRDFGen.generateRDF("rdf-model","PHONE",metadataJSON , InputType.JSON, false, outWriter);
			
			
			
			String output = sw.toString();
			
			
			TripleStoreUtil tsu = new TripleStoreUtil();
			String tripleStoreURL = "http://localhost:3000/openrdf-sesame/repositories/karma_data";
			String context = "http://image-metadata.com";
			Boolean replace = true;
			String baseURI = "http://isiimagefinder/";
			
			boolean success = tsu.saveToStore(sw.toString(), tripleStoreURL, context, replace, baseURI);
			
			
			//String output = "hello";
			return Response.status(200).entity(metadataJSON).build();
			
			//return Response.status(200).entity(output).build();
		}
		catch(IOException ioe)
		{
			logger.error("IOException:" + ioe.getMessage());
			return null;
		}
		catch(KarmaException ke)
		{
			return Response.status(200).entity(ke.getMessage()).build();
		}
		catch (Exception e)
		{
			logger.error("Exception:" + e.getMessage());
			return null;
		}
	}

}
