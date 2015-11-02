package edu.isi.karma.metadata;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.TrivialErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class OntologyMetadata extends KarmaUserMetadata {

	private static final Logger logger = LoggerFactory.getLogger(OntologyMetadata.class);
	
	public OntologyMetadata(ServletContextParameterMap contextParameters) throws KarmaException
	{
		super(contextParameters);
	}
	
	@Override
	public void setup(UpdateContainer uc, Workspace workspace) {
		logger.info("Start OntologyMetadata.setup");
		OntologyManager ontologyManager = workspace.getOntologyManager();
		/** Check if any ontology needs to be preloaded **/
		String preloadedOntDir = contextParameters.getParameterValue(ServletContextParameterMap.ContextParameter.PRELOADED_ONTOLOGY_DIRECTORY);
		File ontDir = new File(preloadedOntDir);
		logger.info("Load ontologies from " + preloadedOntDir);
		if (ontDir.exists()) {
			File[] ontologies = ontDir.listFiles();
			for (File ontology: ontologies) {
				if(ontology.getName().startsWith(".") || ontology.isDirectory()) {
					continue; //Ignore . files
				}
				if (ontology.getName().endsWith(".owl") || 
						ontology.getName().endsWith(".rdf") || 
						ontology.getName().endsWith(".n3") || 
						ontology.getName().endsWith(".nt") || 
						ontology.getName().endsWith(".ttl") || 
						ontology.getName().endsWith(".xml")) {
					if(ontology.getName().matches("catalog\\-v[0-9]{3}\\.xml")) {
						logger.info("Ignore: " + ontology.getAbsolutePath());
						continue; //ignore mac catalog-v001.xml file
					}
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try {
						String encoding = EncodingDetector.detect(ontology);
						ontologyManager.doImport(ontology, encoding);
					} catch (Exception t) {
						logger.error ("Error loading ontology: " + ontology.getAbsolutePath(), t);
						uc.add(new TrivialErrorUpdate("Error loading ontology: " + ontology.getAbsolutePath()));
					}
				} else {
					logger.error ("the file: " + ontology.getAbsolutePath() + " does not have proper format: xml/rdf/n3/ttl/owl");
					uc.add(new TrivialErrorUpdate("Error loading ontology: " + ontology.getAbsolutePath() + ". The file does not have proper format: xml/rdf/n3/ttl/owl"));
				}
			}
			// update the cache at the end when all files are added to the model
			ontologyManager.updateCache();
		} else {
			logger.info("No directory for preloading ontologies exists.");
		}
		SemanticTypeUtil.setSemanticTypeTrainingStatus(true);
	}
	
	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.PRELOADED_ONTOLOGY_DIRECTORY;
	}
	@Override
	protected String getDirectoryPath() {
		return "preloaded-ontologies/";
	}
	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.ONTOLOGY;
	}
}
