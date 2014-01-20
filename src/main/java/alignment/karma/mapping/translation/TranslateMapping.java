package alignment.karma.mapping.translation;

import alignment.karma.mapping.translation.translations.d2rq.D2rqMapping;
import alignment.karma.mapping.translation.translations.d2rq.EventMappingTranslations;
import alignment.karma.mapping.translation.translations.d2rq.PersonMappingTranslations;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.*;

/**
 * Class TranslateMapping
 *
 * @since 11/27/2013
 */
public class TranslateMapping
{
	public static void main(String[] args) throws IOException
	{
		InputStream stringInputStream = new FileInputStream("/home/adam.czerniejewski/dataSets/PersonMapping.ttl");
		Model m = ModelFactory.createDefaultModel();
		m.read(stringInputStream, null, "TTL");
		m.write(System.out, "RDF/XML");
		TranslateMapping tm = new TranslateMapping();
		tm.produceD2RQMapping(m, new FileWriter("test.n3"));
	}

	//	public static void main(String[] args) throws IOException
//	{
//		InputStream stringInputStream = new FileInputStream("/home/adam.czerniejewski/.airs/conf/sda/alignment/CUBRC-Interpol.n3");
//		Model m = OntologyLoader.getInstance().getInstanceOntModel();
//		m.read(stringInputStream,null,"TTL");
//		m.write(System.out,"RDF/XML");
////		TranslateMapping tm = new TranslateMapping();
////		tm.produceD2RQMapping(m, new FileWriter("test.n3"));
//	}
	private D2rqMapping d2rqMapping;
	public void produceD2RQMapping(Model model, Writer writer) {
		d2rqMapping = new D2rqMapping(model);
		//Person Mapping chains
		PersonMappingTranslations.translatePersonMappings(model, d2rqMapping);
		//Organization Mapping Chains
		//TODO
		//Event Mapping Chains
		EventMappingTranslations.translatEventMappings(model, d2rqMapping);
		//write out mapping
		d2rqMapping.writeD2rqMapping(writer);
	}


}
