package edu.isi.karma.web.services.rdf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

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


@Path("/hello")
public class RDFGeneratorServlet{

	/**
	 * 
	 
	private static final long serialVersionUID = -979319404654953710L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
				response.getWriter().println("Hello world Germany!");
	
	}
	 * @throws IOException 
	 * @throws KarmaException */
	
	
	@GET
	@Path("/world")
	public Response getMsg()
	{
		try
		{
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
			
			gRDFGen.generateRDF("rdf-model", new File(filename), InputType.JSON, false, outWriter);
			
			
			
			String output = sw.toString();
			
			
			TripleStoreUtil tsu = new TripleStoreUtil();
			String tripleStoreURL = "http://localhost:3000/openrdf-sesame/repositories/karma_data";
			String context = "http://image-metadata.com";
			Boolean replace = true;
			String baseURI = "http://isiimagefinder/";
			
			boolean success = tsu.saveToStore(sw.toString(), tripleStoreURL, context, replace, baseURI);
			
			
			//String output = "hello";
			return Response.status(200).entity("h").build();
			
			//return Response.status(200).entity(output).build();
		}
		catch(IOException ioe)
		{
			return Response.status(200).entity(ioe.getMessage()).build();
		}
		catch(KarmaException ke)
		{
			return Response.status(200).entity(ke.getMessage()).build();
		}
		catch (Exception e)
		{
			return Response.status(200).entity(e.getMessage()).build();
		}
	}

}
