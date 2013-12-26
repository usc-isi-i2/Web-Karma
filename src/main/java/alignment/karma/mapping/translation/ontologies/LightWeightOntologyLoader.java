package alignment.karma.mapping.translation.ontologies;

import alignment.karma.mapping.translation.ontologies.kdd.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class LightWeightOntologyLoader
 *
 * @since 12/06/2013
 */
public class LightWeightOntologyLoader
{
	private static Model rootModel;
	private static Map<String,String> prefixes;
	private static void initRootModel() {
		rootModel = ModelFactory.createDefaultModel();
		try
		{
			String resourcePath = "/ontologies";
			ClassPathResource classPathResource = new ClassPathResource(resourcePath);
			if(classPathResource.getFile().isDirectory()){
				for(File f:classPathResource.getFile().listFiles()) {
					String fileName = f.getName();
					if(fileName.contains(".owl")) {
						InputStream stringInputStream = new FileInputStream(f);
						rootModel.read(stringInputStream,null,"RDF/XML");
					}
					else if(fileName.contains(".rdf")) {
						InputStream stringInputStream = new FileInputStream(f);
						rootModel.read(stringInputStream,null,"RDF/XML");
					}
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
	public static synchronized Model getRootModel() {
		if(rootModel==null) {
			initRootModel();
		}
		return rootModel;
	}

	public static synchronized Map<String,String> getPrefixes() {
		if (prefixes==null) {
			initPrefixes();
		}
		return prefixes;
	}

	private static void initPrefixes()
	{
		prefixes = new HashMap<>();
		prefixes.put(d2rq.getPrefix(),d2rq.getNamespace());
		prefixes.put(karma.getPrefix(),karma.getNamespace());
		prefixes.put(kmdev.getPrefix(),kmdev.getNamespace());
		prefixes.put(owl.getPrefix(),owl.getNamespace());
		prefixes.put(BadOntology.getPrefix(), BadOntology.getNamespace());
		prefixes.put(r2rml.getPrefix(),r2rml.getNamespace());
		prefixes.put(swrl.getPrefix(),swrl.getNamespace());
		prefixes.put(xsd.getPrefix(),xsd.getNamespace());
		prefixes.put(agent.getPrefix(), agent.getNamespace());
		prefixes.put(amo.getPrefix(),amo.getNamespace());
		prefixes.put(ao.getPrefix(),ao.getNamespace());
		prefixes.put(artifact.getPrefix(),artifact.getNamespace());
		prefixes.put(bfo.getPrefix(),bfo.getNamespace());
		prefixes.put(bridge.getPrefix(),bridge.getNamespace());
		prefixes.put(cto.getPrefix(),cto.getNamespace());
		prefixes.put(emo.getPrefix(),emo.getNamespace());
		prefixes.put(ero.getPrefix(),ero.getNamespace());
		prefixes.put(event.getPrefix(),event.getNamespace());
		prefixes.put(geo.getPrefix(),geo.getNamespace());
		prefixes.put(info.getPrefix(),info.getNamespace());
		prefixes.put(ito.getPrefix(),ito.getNamespace());
		prefixes.put(meta.getPrefix(),meta.getNamespace());
		prefixes.put(nlp.getPrefix(),nlp.getNamespace());
		prefixes.put(oboInOwl.getPrefix(),oboInOwl.getNamespace());
		prefixes.put(purl.getPrefix(),purl.getNamespace());
		prefixes.put(purl_rordf.getPrefix(),purl_rordf.getNamespace());
		prefixes.put(quality.getPrefix(), quality.getNamespace());
		prefixes.put(ro.getPrefix(),ro.getNamespace());
		prefixes.put(snap.getPrefix(),snap.getNamespace());
		prefixes.put(spin.getPrefix(),spin.getNamespace());
		prefixes.put(spin_rule.getPrefix(),spin_rule.getNamespace());
		prefixes.put(spin_sp.getPrefix(),spin_sp.getNamespace());
		prefixes.put(swrlb.getPrefix(),swrlb.getNamespace());
		prefixes.put(time.getPrefix(),time.getNamespace());
		prefixes.put(xo.getPrefix(),xo.getNamespace());
	}

	public static void main(String[] args) throws IOException
	{
		File f = new File("bigOntologies.owl");
		FileWriter fw = new FileWriter(f);
		LightWeightOntologyLoader.initRootModel();
		LightWeightOntologyLoader.getRootModel().write(fw, "RDF/XML", null);
		fw.flush();
		fw.close();
	}
}
