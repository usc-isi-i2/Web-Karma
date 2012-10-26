package edu.isi.karma.er.helper.ontology;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class MatchOntologyUtil {

	public Model createMatchOntology(Model model, String subject, String srcId, String srcUrl, 
			String dstId, String dstUrl, String matched, String comment, String createTime, String creator) {
		
		String PREFIX = "http://www.semanticweb.org/ontologies/2012/9/OntologyMatchGroundTruth.owl#";
		
		Resource saamArtist = model.createResource();
		saamArtist.addLiteral(ResourceFactory.createProperty(PREFIX + "id"), srcId);
		saamArtist.addLiteral(ResourceFactory.createProperty(PREFIX + "url"), srcUrl);
		
		
		Resource otherArtist = model.createResource();
		otherArtist.addLiteral(ResourceFactory.createProperty(PREFIX + "id"), dstId);
		otherArtist.addLiteral(ResourceFactory.createProperty(PREFIX + "url"), dstUrl);
		
		Resource res = model.createResource(subject);
		res.addProperty(ResourceFactory.createProperty(PREFIX + "saamArtist"), saamArtist);
		res.addProperty(ResourceFactory.createProperty(PREFIX + "otherArtist"), otherArtist);
		res.addLiteral(ResourceFactory.createProperty(PREFIX + "matched"), matched);
		res.addLiteral(ResourceFactory.createProperty(PREFIX + "comment"), comment);
		res.addLiteral(ResourceFactory.createProperty(PREFIX + "createTime"), String.valueOf(createTime));
		
		return model;
	}

}
