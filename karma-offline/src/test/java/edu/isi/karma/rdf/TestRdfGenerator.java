package edu.isi.karma.rdf;

import org.junit.BeforeClass;

import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;

public abstract class TestRdfGenerator {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

        KarmaMetadataManager userMetadataManager = new KarmaMetadataManager();
        userMetadataManager.register(new UserPreferencesMetadata());
        userMetadataManager.register(new PythonTransformationMetadata());
	}
	
}
