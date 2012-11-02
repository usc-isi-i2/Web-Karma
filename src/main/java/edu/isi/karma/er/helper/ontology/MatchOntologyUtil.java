package edu.isi.karma.er.helper.ontology;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.NameSpace;
import edu.isi.karma.er.helper.entity.ResultRecord;
import edu.isi.karma.er.helper.entity.Score;

public class MatchOntologyUtil {
	private double THRESHOLD = 0.9;
	private String SI_URI = "http://americanart.si.edu/collections/search/artist/?id=";
	private String WIKI_URI = "http://en.wikipedia.org/wiki/";
	private String SAAM_VERSION = "http://fusion.adx.isi.edu:8088/openrdf-workbench/repositories/SAAM3/";
	private String DBPEDIA_VERSION = "http://dbpedia.org/Downloads37";
	

	/**
	 * Create a match ontology base on original source information
	 * @param model
	 * @param finalScore
	 * @param generatedTime
	 * @param srcURI
	 * @param dstURI
	 * @param activity
	 * @param creator
	 * @param comment
	 * @param srcAttr
	 * @param srcVal
	 * @param dstAttr
	 * @param dstVal
	 * @return
	 */
	public Model createMatchOntology(Model model, double finalScore, long generatedTime, String srcURI, String dstURI, 
			String creator, String comment, String[] srcAttr, String[] srcVal, String[] dstAttr, String[] dstVal) {
		
		Resource latest = this.getLatestOneMatchResult(model, srcURI, dstURI);
		
		Resource gen = model.createResource();
		
		String srcId = srcURI.substring(srcURI.lastIndexOf('_') + 1);
		String dstId = dstURI.substring(dstURI.lastIndexOf('/') + 1);
		
		Resource ent1 = model.createResource();
		ent1.addProperty(NameSpace.RDF_TYPE, NameSpace.PREFIX_PROV + "Entity");
		for (int i = 0; i < srcAttr.length; i++) {
			if (srcVal[i] != null) {
				Resource res = model.createResource();
				res.addProperty(NameSpace.RDF_TYPE, NameSpace.PREFIX_PROV + "Entity");
				res.addProperty(NameSpace.RDF_PREDICATE, ResourceFactory.createResource(srcAttr[i]));
				res.addLiteral(NameSpace.PROV_VALUE, srcVal[i]);
				ent1.addProperty(NameSpace.PROV_HAD_MEMBER, res);
			}
		}
		ent1.addProperty(NameSpace.PROV_WAS_QUOTED_FROM, ResourceFactory.createResource(srcURI));
		ent1.addProperty(NameSpace.DCTERM_HAS_VERSION, ResourceFactory.createResource(SAAM_VERSION));
		
		Resource ent2 = model.createResource();
		ent2.addProperty(NameSpace.RDF_TYPE, ResourceFactory.createResource(NameSpace.PREFIX_PROV + "Entity"));
		for (int i = 0; i < dstAttr.length; i++) {
			if (dstVal[i] != null) {
				Resource res = model.createResource();
				res.addProperty(NameSpace.RDF_TYPE, ResourceFactory.createResource(NameSpace.PREFIX_PROV + "Entity"));
				res.addProperty(NameSpace.RDF_PREDICATE, ResourceFactory.createResource(dstAttr[i]));
				res.addLiteral(NameSpace.PROV_VALUE, dstVal[i]);
				ent2.addProperty(NameSpace.PROV_HAD_MEMBER, res);
			}
		}
		ent2.addProperty(NameSpace.PROV_WAS_QUOTED_FROM, ResourceFactory.createResource(dstURI));
		ent2.addProperty(NameSpace.DCTERM_HAS_VERSION, ResourceFactory.createResource(DBPEDIA_VERSION));
		
		Resource ag = model.createResource();
		if ("Karma".equals(creator)) {
			ag.addProperty(NameSpace.RDF_TYPE, ResourceFactory.createResource(NameSpace.PREFIX_PROV + "SoftwareAgent"));
		} else {
			ag.addProperty(NameSpace.RDF_TYPE, ResourceFactory.createResource(NameSpace.PREFIX_PROV + "Agent"));
			gen.addLiteral(NameSpace.SKOS_NOTE, comment);
			gen.addLiteral(NameSpace.RDFS_COMMENT, comment);
		}
		ag.addLiteral(NameSpace.RDFS_LABEL, creator);
		ag.addLiteral(NameSpace.SKOS_PREF_LABEL, creator);
		
		Resource act = model.createResource();
		if ("Karma".equals(creator)) {
			act.addLiteral(NameSpace.RDFS_LABEL, "create");
		} else {
			act.addLiteral(NameSpace.RDFS_LABEL, "revise");
		}
		act.addProperty(NameSpace.PROV_WAS_ASSOCIATED_WITH, ag);
		act.addProperty(NameSpace.PROV_GENERATED, gen);
		act.addProperty(NameSpace.MATCH_HAS_MATCH_SOURCE, ent1);
		act.addProperty(NameSpace.MATCH_HAS_MATCH_TARGET, ent2);
		if (latest != null) {
			act.addProperty(NameSpace.PROV_WAS_INFLUENCED_BY, latest);
		}
		
		if (finalScore >= THRESHOLD) {
			gen.addProperty(NameSpace.MATCH_HAS_MATCH_TYPE, ResourceFactory.createResource(NameSpace.PREFIX_MATCH + "ExactMatch"));
		} else {
			gen.addProperty(NameSpace.MATCH_HAS_MATCH_TYPE, ResourceFactory.createResource(NameSpace.PREFIX_MATCH + "NotMatch"));
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(generatedTime));
		gen.addLiteral(NameSpace.PROV_GENERATED_AT_TIME, new XSDDateTime(cal));
		gen.addLiteral(NameSpace.MATCH_HAS_SCORE, finalScore);
		gen.addProperty(NameSpace.MATCH_SEE_ALSO_IN_SI, ResourceFactory.createResource(SI_URI + srcId));
		gen.addProperty(NameSpace.MATCH_SEE_ALSO_IN_WIKI, ResourceFactory.createResource(WIKI_URI + dstId));
		gen.addProperty(NameSpace.PROV_WAS_GENERATED_BY, act);
		gen.addProperty(NameSpace.PROV_WAS_ATTRIBUTED_TO, ag);
		if (latest != null) {
			gen.addProperty(NameSpace.PROV_WAS_REVISION_OF, latest);
		}
		
		return model;
	}
	
	public Model createMatchOntology(Model model, List<ResultRecord> list) {
		double finalScore;
		long generatedTime = System.currentTimeMillis();
		String srcURI, dstURI, creator, comment;
		String[] srcAttr, srcVal, dstAttr, dstVal;
		
		creator = "Karma";
		
		for (ResultRecord rec : list) {
			finalScore = rec.getCurrentMaxScore();
			if (finalScore > 0) {
				MultiScore ms = rec.getRankList().get(0);
				srcURI = ms.getSrcSubj().getSubject();
				dstURI = ms.getDstSubj().getSubject();
				if (finalScore >= THRESHOLD) {
					comment = "Exact match, cause similarity is greater than " + THRESHOLD;
				} else {
					comment = "Not match, cause similarity is less than " + THRESHOLD;
				}
				List<Score> slist = ms.getScoreList();
				int len = slist.size();
				srcAttr = new String[len];
				srcVal = new String[len];
				dstAttr = new String[len];
				dstVal = new String[len];
				for (int i = 0; i < len; i++) {
					Score s = slist.get(i);
					srcAttr[i] = s.getPredicate();
					dstAttr[i] = s.getPredicate();
					srcVal[i] = s.getSrcObj();
					dstVal[i] = s.getDstObj();
				}
				
				this.createMatchOntology(model, finalScore, generatedTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
			}
			
		}
		
		return model;
	}

	
	public Resource getLatestOneMatchResult(Model model, String srcUri, String dstUri) {
		Resource res = null;
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"select  ?s ?time " + "\n" +
				"where {" + "\n" +
				"  ?s prov:generatedAtTime ?time." + "\n" +
				"  ?s prov:wasGeneratedBy ?act." + "\n" +
				"  ?act match:hasMatchSource ?ent1." + "\n" +
				"  ?act match:hasMatchTarget ?ent2." + "\n" +
				"  ?ent1 prov:wasQuotedFrom <" + srcUri + ">." + "\n" +
				"  ?ent2 prov:wasQuotedFrom <" + dstUri + ">." + "\n" +
				"} order by desc(?time) ";
		
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		ResultSet rs = null;
		
		try {
			rs = exec.execSelect();
			
			if(rs.hasNext()) {
				QuerySolution solu = rs.next();
				res = solu.getResource("s");
			}
			
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		
		return res;
	}
	
	public void outputAllMatchResult(Model model) {
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				"select  ?s ?actName ?time " + "\n" +
				"where {" + "\n" +
				"  ?s prov:generatedAtTime ?time." + "\n" +
				"  ?s prov:wasGeneratedBy ?act." + "\n" +
				"  ?act rdfs:label ?actName." + "\n" + 
				
				"}";
		
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		ResultSet rs = null;
		RDFNode node = null;
		String s, act, time;
		int count = 0;
		try {
			rs = exec.execSelect();
			
			while(rs.hasNext()) {
				count++;
				QuerySolution solu = rs.next();
				node = solu.get("s");
				s = node.toString();
				node = solu.get("actName");
				act = node.asLiteral().getString();
				node = solu.get("time");
				time = node.toString();
				System.out.println(s + "\t" + act + "\t" + time);
			}
			System.out.println(count + " records find matched.");
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		
	}
	
	public void outputLatestMatchResult(Model model) {
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				" select ?srcUri ?dstUri ?t " + "\n" +
				" where {" + "\n" + 
					"?sub prov:generatedAtTime ?t." + "\n" +
					"?sub prov:wasGeneratedBy ?acti." + "\n" +
					"?acti match:hasMatchSource ?src1." + "\n" +
					"?acti match:hasMatchTarget ?dst1." + "\n" +
					"?src1 prov:wasQuotedFrom ?srcUri." + "\n" +
					"?dst1 prov:wasQuotedFrom ?dstUri." + "\n" +
					"{select  ?srcUri ?dstUri (max(?time) as ?maxTime) " + "\n" +
					" where {" + "\n" +
					"   ?s prov:generatedAtTime ?time." + "\n" +
					"   ?s prov:wasGeneratedBy ?act." + "\n" +
					"   ?act match:hasMatchSource ?src." + "\n" + 
					"   ?src prov:wasQuotedFrom ?srcUri." + "\n" +
					"   ?act match:hasMatchTarget ?dst." + "\n" +
					"   ?dst prov:wasQuotedFrom ?dstUri." + "\n" +
					" } group by ?srcUri ?dstUri" + "\n" +
					"}." + "\n" +
					"filter(?t = ?maxTime)" + "\n" +
				"}";
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		ResultSet rs = null;
		RDFNode node = null;
		String s, act, time;
		int count = 0;
		try {
			rs = exec.execSelect();
			
			while(rs.hasNext()) {
				count++;
				QuerySolution solu = rs.next();
				node = solu.get("srcUri");
				s = node.toString();
				node = solu.get("dstUri");
				act = node.toString();
				node = solu.get("t");
				time = node.toString();
				System.out.println(s + "\t" + act + "\t" + time);
			}
			System.out.println(count + " records find matched.");
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		
	}
}
