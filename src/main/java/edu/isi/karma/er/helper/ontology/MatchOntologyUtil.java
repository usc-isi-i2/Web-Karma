package edu.isi.karma.er.helper.ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.NYTimes;
import edu.isi.karma.er.helper.entity.NameSpace;
import edu.isi.karma.er.helper.entity.Paginator;
import edu.isi.karma.er.helper.entity.ResultRecord;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.helper.entity.ScoreBoard;

public class MatchOntologyUtil {
	private double THRESHOLD = 0.9;
	private String SI_URI = "http://americanart.si.edu/collections/search/artist/?id=";
	private String WIKI_URI = "http://en.wikipedia.org/wiki/";
	private String SAAM_VERSION = "http://fusion.adx.isi.edu:8088/openrdf-workbench/repositories/SAAM3/";
	private String DBPEDIA_VERSION = "http://dbpedia.org/Downloads37";
	
	private String repositoryName = "match_result";
	
	public void setThreshold(double threshold) {
		this.THRESHOLD = threshold;
	}
	
	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public Model getModel() {
		return (TDBFactory.createDataset(Constants.PATH_REPOSITORY + repositoryName + "/").getDefaultModel());
	}

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
	private Resource createMatchOntology(Model model, double finalScore, String matched, long generatedTime, String srcURI, String dstURI, 
			String creator, String comment, String[] srcAttr, String[] srcVal, String[] dstAttr, String[] dstVal, NYTimes nyt) {
		Resource latest = null;
		
		// if creator is "karma", then retrieve latest one created by karma, else retrieve the latest one in spite of whoever created it.
		if ("Karma".equals(creator)) {
			latest = this.getLatestOneMatchResult(srcURI, dstURI, "Karma");
		} else {
			latest = this.getLatestOneMatchResult(srcURI, dstURI);
		}
		
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
		ent2.addProperty(NameSpace.RDF_TYPE, NameSpace.PROV_ENTITY);
		for (int i = 0; i < dstAttr.length; i++) {
			if (dstVal[i] != null) {
				Resource res = model.createResource();
				res.addProperty(NameSpace.RDF_TYPE, NameSpace.PROV_ENTITY);
				res.addProperty(NameSpace.RDF_PREDICATE, ResourceFactory.createResource(dstAttr[i]));
				res.addLiteral(NameSpace.PROV_VALUE, dstVal[i]);
				ent2.addProperty(NameSpace.PROV_HAD_MEMBER, res);
			}
		}
		ent2.addProperty(NameSpace.PROV_WAS_QUOTED_FROM, ResourceFactory.createResource(dstURI));
		ent2.addProperty(NameSpace.DCTERM_HAS_VERSION, ResourceFactory.createResource(DBPEDIA_VERSION));
		if (nyt != null) {	// entity for NYTimes
			Resource nEnt = model.createResource(nyt.getSubject());
			nEnt.addProperty(NameSpace.SKOS_PREF_LABEL, nyt.getName());
			nEnt.addProperty(NameSpace.NYTIMES_TOPIC_PAGE, ResourceFactory.createResource(nyt.getTopicPage()));
			nEnt.addProperty(NameSpace.NYTIMES_SEARCH_API_QUERY, nyt.getSearchLink());
			nEnt.addProperty(NameSpace.OWL_SAME_AS, ResourceFactory.createResource(nyt.getFreebaseUri()));
			ent2.addProperty(NameSpace.OWL_SAME_AS, nEnt);
		}
		
		Resource ag = model.createResource();
		if ("Karma".equals(creator)) {
			ag.addProperty(NameSpace.RDF_TYPE, NameSpace.PROV_SOFTWARE_AGENT);
			if (finalScore >= THRESHOLD) {
				gen.addProperty(NameSpace.MATCH_HAS_MATCH_TYPE, NameSpace.MATCH_EXACT_MATCH);
			} else {
				gen.addProperty(NameSpace.MATCH_HAS_MATCH_TYPE, NameSpace.MATCH_NOT_MATCH);
			}
			gen.addLiteral(NameSpace.SKOS_NOTE, comment);
			gen.addLiteral(NameSpace.RDFS_LABEL, comment);
		} else {
			ag.addProperty(NameSpace.RDF_TYPE, NameSpace.PROV_AGENT);
			gen.addLiteral(NameSpace.SKOS_NOTE, comment);
			gen.addLiteral(NameSpace.RDFS_COMMENT, comment);
			if ("M".equals(matched)) {
				gen.addProperty(NameSpace.MATCH_HAS_MATCH_TYPE, NameSpace.MATCH_EXACT_MATCH);
			} else if ("N".equals(matched)) {
				gen.addProperty(NameSpace.MATCH_HAS_MATCH_TYPE, NameSpace.MATCH_NOT_MATCH);
			} else {
				gen.addProperty(NameSpace.MATCH_HAS_MATCH_TYPE, NameSpace.MATCH_UNSURE);
			}
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
		
		
		gen.addLiteral(NameSpace.PROV_GENERATED_AT_TIME, toXSDDateTime(generatedTime));
		gen.addLiteral(NameSpace.MATCH_HAS_SCORE, finalScore);
		gen.addProperty(NameSpace.MATCH_SEE_ALSO_IN_SI, ResourceFactory.createResource(SI_URI + srcId));
		gen.addProperty(NameSpace.MATCH_SEE_ALSO_IN_WIKI, ResourceFactory.createResource(WIKI_URI + dstId));
		gen.addProperty(NameSpace.PROV_WAS_GENERATED_BY, act);
		gen.addProperty(NameSpace.PROV_WAS_ATTRIBUTED_TO, ag);
		gen.addProperty(NameSpace.RDF_TYPE, NameSpace.PROV_GENERATION);
		if (latest != null) {
			gen.addProperty(NameSpace.PROV_WAS_REVISION_OF, latest);
		}
		return gen;
	}
	
	/**
	 * Append match result data to repository from list of ResultRecord which is produce by Karma ER
	 * @param model the repository stores match result
	 * @param map for NYTimes links
	 * @param list the list of match result
	 * @return updated model
	 */
	public int createMatchOntology(List<ResultRecord> list, Map<String, NYTimes> map) {
		Model model = this.getModel();
		model.begin();
		Resource res = null;
		int count = 0;
		double finalScore;
		long generatedTime = System.currentTimeMillis();
		String srcURI, dstURI, creator, comment;
		String[] srcAttr, srcVal, dstAttr, dstVal;
		NYTimes nyt = null;
		creator = "Karma";
		
		for (ResultRecord rec : list) {
			finalScore = rec.getCurrentMaxScore();
			if (finalScore > 0) {
				MultiScore ms = rec.getRankList().get(0);
				srcURI = ms.getSrcSubj().getSubject();
				dstURI = ms.getDstSubj().getSubject();
				comment = "";
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
				
				if (map != null) {
					nyt = map.get(dstURI);
				}
				
				res = this.createMatchOntology(model, finalScore, "", generatedTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal, nyt);
				if (res != null) {
					count ++;
				}
			}
			
		}
		model.commit();
		TDB.sync(model);
		model.close();
		
		return count++;
	}
	
	public MatchResultOntology createMatchOntology(MatchResultOntology onto) {
		Model model = this.getModel();
		model.begin();
		double finalScore;
		String srcURI, dstURI, creator, comment, matched;
		String[] srcAttr, srcVal, dstAttr, dstVal;
		
		long generatedTime = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String updated = sdf.format(new Date(generatedTime));
		
		creator = onto.getCreator();
		finalScore = onto.getFinalScore();
		if (finalScore > 0) {
			srcURI = onto.getSrcUri();
			dstURI = onto.getDstUri();
			comment = onto.getComment();
			matched = onto.getMatched();
			onto.setUpdated(updated);
			List<Score> slist = onto.getMemberList();
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
			
			Resource res = this.createMatchOntology(model, finalScore, matched, generatedTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal, onto.getNytimes());
			if (res == null)
				return null;
			onto.setResId(res.getURI());
		}
		model.commit();
		TDB.sync(model);
		return onto;
	}
	
	
	public MatchResultOntology createMatchOntology(ScoreBoard sb, Map<String, NYTimes> map) {
		String srcURI, dstURI, creator, comment;
		double finalScore;
		MatchResultOntology onto = null;
		
		srcURI = sb.getSaamUri(); //"http://smithsonianart.si.edu/saam/Person_2482";
		dstURI = sb.getKarmaUri();
		if (sb.getFound() > 0) {
			onto = new MatchResultOntology();
			MultiScore ms = sb.getRankList().get(0);
			
			creator = "Karma";
			finalScore = sb.getFound();
			if (finalScore >= THRESHOLD) {
				comment = "Exact match (" + finalScore + ")";
			} else {
				comment = "Not match ( " + finalScore + ")";
			}
			
			List<Score> slist = ms.getScoreList();
			
			onto.setFinalScore(finalScore);
			onto.setMatched("");
			onto.setSrcUri(srcURI);
			onto.setDstUri(dstURI);
			onto.setCreator(creator);
			onto.setComment(comment);
			onto.setMemberList(slist);
			if (map != null ) {
				onto.setNytimes(map.get(dstURI));
			}
			
			this.createMatchOntology(onto);
		}
		
		//Model model = this.getModel();
		//MatchResultOntology onto = null;
		/*
		double finalScore;
		long generatedTime = System.currentTimeMillis();
		String srcURI, dstURI, creator, comment, matched;
		String[] srcAttr, srcVal, dstAttr, dstVal;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String updated = sdf.format(new Date(generatedTime));
		creator = sb.getCreator();
		
		
			finalScore = sb.getFound();
			if (finalScore > 0) {
				MultiScore ms = sb.getRankList().get(0);
				srcURI = ms.getSrcSubj().getSubject();
				dstURI = ms.getDstSubj().getSubject();
				comment = sb.getComment();
				matched = sb.getMatched();
				sb.setUpdated(updated);
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
				
				Resource res = this.createMatchOntology(model, finalScore, matched, generatedTime, srcURI, dstURI, creator, comment, srcAttr, srcVal, dstAttr, dstVal);
				if (res == null)
					return null;
				
				onto = new MatchResultOntology();
				onto.setSrcUri(srcURI);
				onto.setDstUri(dstURI);
				onto.setSeeAlsoInSI("");
				onto.setSeeAlsoInWiki("");
				onto.setComment(comment);
				onto.setMatched(matched);
				onto.setUpdated(updated);
				onto.setMemberList(slist);
				onto.setFinalScore(finalScore);
				onto.setResId(res.getURI());
			}
			model.commit();
			TDB.sync(model);
			model.close();
		*/
		return onto;
	}
	
	public Resource getLatestOneMatchResult(String srcUri, String dstUri) {
		return getLatestOneMatchResult(srcUri, dstUri, "");
	}

	/**
	 * Get the latest one result of a given srcUri and dstUri
	 * @param model where stores the match results
	 * @param srcUri uri of source resource
	 * @param dstUri uri of target resource
	 * @return the latest resource of MatchResult, returns null if no result.
	 */
	public Resource getLatestOneMatchResult(String srcUri, String dstUri, String creator) {
		Model model = this.getModel();
		Resource res = null;
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">\n" +
				"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>" + "\n" +
				"select  ?s ?time " + "\n" +
				"where {" + "\n" +
				"  ?s prov:generatedAtTime ?time." + "\n" +
				"  ?s prov:wasGeneratedBy ?act." + "\n" +
				"  ?s prov:wasAttributedTo ?ag." + "\n"; 
			if (creator != null && creator.length() > 0) {	
				sparql += "  ?ag rdfs:label \"" + creator + "\"^^xsd:string." + "\n" ;
			}
			sparql += 
				"  ?act match:hasMatchSource ?ent1." + "\n" +
				"  ?act match:hasMatchTarget ?ent2." + "\n" +
				"  ?ent1 prov:wasQuotedFrom <" + srcUri + ">." + "\n" +
				"  ?ent2 prov:wasQuotedFrom <" + dstUri + ">." + "\n" +
				"} order by desc(?time) ";
		//System.out.println(sparql);
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
	
	public MatchResultOntology getLatestOneMatchResultObject(String srcUri, String dstUri) {
		return getLatestOneMatchResultObject(srcUri, dstUri, "");
	}
	
	public MatchResultOntology getLatestOneMatchResultObject(String srcUri, String dstUri, String creator) {
		Model model = this.getModel();
		MatchResultOntology onto = null;
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				"PREFIX skos:<" + NameSpace.PREFIX_SKOS + ">" + "\n" + 
				"PREFIX nyt:<" + NameSpace.PREFIX_NYTIMES + ">" + "\n" +
				"PREFIX owl:<" + NameSpace.PREFIX_OWL + ">" + "\n" +
				"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>" + "\n" +
				" select ?sub ?matchType ?comment ?seeAlsoSI ?seeAlsoWiki ?score ?updated ?nyt ?nytname ?topicPage ?searchUri ?freebaseUri " + "\n" +
				" where {" + "\n" + 
					"?sub prov:generatedAtTime ?updated." + "\n" +
					"?sub match:seeAlsoInSmithsonian ?seeAlsoSI." + "\n" +
					"?sub match:seeAlsoInWikipedia ?seeAlsoWiki." + "\n" +
					"?sub match:hasScore ?score." + "\n" + 
					"?sub match:hasMatchType ?matchType." + "\n" + 
					"?sub skos:note ?comment." + "\n" +
					"?sub prov:wasGeneratedBy ?act." + "\n" +
					"?sub prov:wasAttributedTo ?ag." + "\n" ;
				if (creator != null && creator.length() > 0) {
					sparql += "?ag rdfs:label \"" + creator + "\"^^xsd:string.";
				}
				sparql+=	
					"?act match:hasMatchSource ?ent1." + "\n" +
					"?act match:hasMatchTarget ?ent2." + "\n" +
					"?ent1 prov:wasQuotedFrom <" + srcUri + ">." + "\n" +
					"?ent2 prov:wasQuotedFrom <" + dstUri + ">." + "\n" +
					"optional { ?ent2 owl:sameAs ?nyt." + "\n" +
					"  ?nyt skos:prefLabel ?nytname." + "\n" +
					"  ?nyt nyt:topicPage ?topicPage." + "\n" +
					"  ?nyt nyt:search_api_query ?searchUri." + "\n" +
					"  ?nyt owl:sameAs ?freebaseUri." + "\n" +
					"}" +
					"} order by desc(?updated) ";
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		QuerySolution solu = null;
		ResultSet rs = null;
		Resource res = null;
		String comment, seeSIUri, seeWikiUri, matchType, updated;
		double score = -1;
		RDFNode node = null;
		
		try {
			rs = exec.execSelect();
			if(rs.hasNext()) {
				solu = rs.next();
				onto = new MatchResultOntology();
				res = solu.getResource("sub");
				if (solu.getLiteral("comment") != null) {
					comment = solu.getLiteral("comment").getString();
				}else {
					comment = "";
				}
				seeSIUri = solu.getResource("seeAlsoSI").getURI();
				seeWikiUri = solu.getResource("seeAlsoWiki").getURI();
				node = solu.get("score");
				score = node.asLiteral().getDouble();
				matchType = solu.getResource("matchType").getURI();
				updated = fixXSDDate(solu.get("updated").asLiteral().getString());
				onto.setSrcUri(srcUri);
				onto.setDstUri(dstUri);
				onto.setSeeAlsoInSI(seeSIUri);
				onto.setSeeAlsoInWiki(seeWikiUri);
				onto.setUpdated(updated);
				onto.setResId(res.getURI());
				
				if (matchType.indexOf("ExactMatch") > -1) {
					onto.setMatched("M");
				} else if (matchType.indexOf("NotMatch") > -1) {
					onto.setMatched("N");
				} else {
					onto.setMatched("U");
				}
				onto.setComment(comment);
				
				node = solu.getResource("nyt");
				NYTimes nyt = null;
				if (node != null) {
					nyt = new NYTimes();
					nyt.setSubject(node.asResource().getURI());
					nyt.setName(solu.getLiteral("nytname").getString());
					nyt.setTopicPage(solu.getResource("topicPage").getURI());
					nyt.setSearchLink(solu.getLiteral("searchUri").getString());
					nyt.setFreebaseUri(solu.getResource("freebaseUri").getURI());
				}
				
				onto.setMemberList(getScoreListFromResource(res));
				onto.setFinalScore(score);
				onto.setNytimes(nyt);
				
			}
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		return onto;
	}
	
	/**
	 * Get the latest match result of each pair of resource(srcUri, dstUri)
	 * @param model where stored the match result
	 * @return list of resource links to a MatchResult
	 */
	public List<Resource> listLatestMatchResultResources() {
		Model model = this.getModel();
		List<Resource> list = new Vector<Resource>();
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				" select ?sub " + "\n" +
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
		QuerySolution solu = null;
		ResultSet rs = null;
		Resource res = null;
		try {
			rs = exec.execSelect();
			while(rs.hasNext()) {
				solu = rs.next();
				res = solu.getResource("sub");
				list.add(res);
			}
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		
		return list;
	}
	
	/**
	 * Get object list of the latest match result of each pair of resource(srcUri, dstUri)
	 * @param model where stored the match result
	 * @return list of object represents for a MatchResult
	 */
	public List<MatchResultOntology> listLatestMatchResultObjects(String sortBy, String creator) {
		Model model = this.getModel();
		MatchResultOntology onto = null;
		List<MatchResultOntology> list = new Vector<MatchResultOntology>();
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdf:<" + NameSpace.PREFIX_RDF + ">" + "\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				"PREFIX skos:<" + NameSpace.PREFIX_SKOS + ">" + "\n" + 
				"PREFIX nyt:<" + NameSpace.PREFIX_NYTIMES + ">" + "\n" +
				"PREFIX owl:<" + NameSpace.PREFIX_OWL + ">" + "\n" +
				"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>" + "\n" +
				" select ?sub ?matchType ?comment ?srcUri ?dstUri ?seeAlsoSI ?seeAlsoWiki ?score ?updated ?nyt ?nytname ?topicPage ?searchUri ?freebaseUri";
				if (creator == null || creator.length() <= 0) {
					sparql += " ?creator " + "\n";
				}
				sparql += " where {" + "\n" + 
					"?sub prov:generatedAtTime ?updated." + "\n" +
					"?sub match:seeAlsoInSmithsonian ?seeAlsoSI." + "\n" +
					"?sub match:seeAlsoInWikipedia ?seeAlsoWiki." + "\n" +
					"?sub match:hasScore ?score." + "\n" + 
					"?sub match:hasMatchType ?matchType." + "\n" + 
					"?sub skos:note ?comment." + "\n" +
					"?sub prov:wasGeneratedBy ?acti." + "\n" +
					"?acti prov:wasAssociatedWith ?ag." + "\n" ;
				if (creator == null || creator.length() <= 0) {
					sparql += "?ag rdfs:label ?creator." + "\n" ;
				} else {
					sparql += "?ag rdfs:label \"" + creator + "\"^^xsd:string." + "\n";
				}
				sparql += "?acti match:hasMatchSource ?src1." + "\n" +
					//"?src1 prov:hadMember ?mem1." + "\n" +
					//"?mem1 rdf:predicate <" + NameSpace.PREFIX_SAAM + "fullName" + ">." + "\n" +
					//"?mem1 prov:value ?memVal1." + "\n" + 
					"?acti match:hasMatchTarget ?dst1." + "\n" +
					"optional { ?dst1 owl:sameAs ?nyt." + "\n" +
					"  ?nyt skos:prefLabel ?nytname." + "\n" +
					"  ?nyt nyt:topicPage ?topicPage." + "\n" +
					"  ?nyt nyt:search_api_query ?searchUri." + "\n" +
					"  ?nyt owl:sameAs ?freebaseUri." + "\n" +
					"}" +
					"?src1 prov:wasQuotedFrom ?srcUri." + "\n" +
					"?dst1 prov:wasQuotedFrom ?dstUri." + "\n" +
					
					"{select  ?srcUri ?dstUri (max(?time) as ?maxTime) " + "\n" +
					" where {" + "\n" +
					"   ?s prov:generatedAtTime ?time." + "\n" +
					"   ?s prov:wasGeneratedBy ?act." + "\n" ;
					if (creator != null && creator.length() > 0) {
					sparql += "   ?s prov:wasAttributedTo ?ag." + "\n" +
							"   ?ag rdfs:label \"" + creator + "\"^^xsd:string." + "\n";
					}
				sparql+=	"   ?act match:hasMatchSource ?src." + "\n" + 
					"   ?src prov:wasQuotedFrom ?srcUri." + "\n" +
					"   ?act match:hasMatchTarget ?dst." + "\n" +
					"   ?dst prov:wasQuotedFrom ?dstUri." + "\n" +
					" } group by ?srcUri ?dstUri" + "\n" +
					"}." + "\n" +
					"filter(?updated = ?maxTime)" + "\n" +
				"} "; 
		if ("sim_desc".equals(sortBy)) {
			sparql += "order by desc(?score) ";
		//} else if ("name_asc".equals(sortBy)) {
		//	sparql += "order by asc(?memVal1) ";
		//} else if ("name_desc".equals(sortBy)) {
		//	sparql += "order by desc(?memVal1) ";
		} else if ("time_asc".equals(sortBy)) {
			sparql += "order by asc(?updated) ";
		} else if ("time_desc".equals(sortBy)) {
			sparql += "order by desc(?updated) ";
		} else if ("src_asc".equals(sortBy)) {
			sparql += "order by asc(?srcUri) ";
		} else {
			sparql += "order by desc(?score) ";
		}
		
		//System.out.println(sparql);
		
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		QuerySolution solu = null;
		ResultSet rs = null;
		Resource res = null;
		String comment, srcUri, dstUri, seeSIUri, seeWikiUri, matchType, updated;
		double score = -1;
		
		RDFNode node = null;
		
		try {
			rs = exec.execSelect();
			while(rs.hasNext()) {
				solu = rs.next();
				onto = new MatchResultOntology();
				res = solu.getResource("sub");
				if (solu.getLiteral("comment") != null) {
					comment = solu.getLiteral("comment").getString();
				}else {
					comment = "";
				}
				srcUri = solu.getResource("srcUri").getURI();
				dstUri = solu.getResource("dstUri").getURI();
				seeSIUri = solu.getResource("seeAlsoSI").getURI();
				seeWikiUri = solu.getResource("seeAlsoWiki").getURI();
				node = solu.get("score");
				score = node.asLiteral().getDouble();
				matchType = solu.getResource("matchType").getURI();
				updated = fixXSDDate(solu.get("updated").asLiteral().getString());
				if (creator == null || creator.length() <= 0) {
					creator = solu.getLiteral("creator").asLiteral().getString();
				}
				onto.setSrcUri(srcUri);
				onto.setDstUri(dstUri);
				onto.setSeeAlsoInSI(seeSIUri);
				onto.setSeeAlsoInWiki(seeWikiUri);
				onto.setUpdated(updated);
				if (matchType.indexOf("ExactMatch") > -1) {
					onto.setMatched("M");
				} else if (matchType.indexOf("NotMatch") > -1) {
					onto.setMatched("N");
				} else {
					onto.setMatched("U");
				}
				node = solu.getResource("nyt");
				NYTimes nyt = null;
				if (node != null) {
					nyt = new NYTimes();
					nyt.setSubject(node.asResource().getURI());
					nyt.setName(solu.getLiteral("nytname").getString());
					nyt.setTopicPage(solu.getResource("topicPage").getURI());
					nyt.setSearchLink(solu.getLiteral("searchUri").getString());
					nyt.setFreebaseUri(solu.getResource("freebaseUri").getURI());
				}
				onto.setNytimes(nyt);
				onto.setComment(comment);
				onto.setMemberList(getScoreListFromResource(res));
				onto.setFinalScore(score);
				onto.setResId(res.getURI());
				onto.setCreator(creator);
				list.add(onto);
			}
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		
		return list;
	}
	
	public List<MatchResultOntology> listLatestMatchResultObjectsHumanFirst(String sortBy) {
		List<MatchResultOntology> list = null, humanList = null;
		list = this.listLatestMatchResultObjects(sortBy, "");
		humanList = this.listLatestMatchResultObjects(sortBy, "Human");
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < list.size(); i++) {
			MatchResultOntology onto = list.get(i);
			map.put(onto.getSrcUri(), i);
		}
		
		for (MatchResultOntology onto : humanList) {
			if (map.containsKey(onto.getSrcUri())){
				list.set(map.get(onto.getSrcUri()), onto);
			}
		}
		
		return list;
	}
	
	public List<MatchResultOntology> listPagedLatestMatchResultObjects(Paginator pager, String sortBy) {
		
		List<MatchResultOntology> subList = null,  list = this.listLatestMatchResultObjectsHumanFirst(sortBy);
		int totalNumber, totalPage, beginPosition, endPosition;
		totalNumber = list.size();
		totalPage = (totalNumber - 1 )/ pager.getPageSize() + 1;
		pager.setTotalNumber(totalNumber);
		pager.setTotalPage(totalPage);
		beginPosition = (pager.getCurPage() -1) * pager.getPageSize();
		endPosition = (pager.getPageSize() * pager.getCurPage() < totalNumber ) ? pager.getPageSize() * pager.getCurPage() : totalNumber;
		
		subList = list.subList(beginPosition, endPosition);
		
		pager.setMap(getCountMap(list));
		
		return subList;
	}
	
	private Map<String, Integer> getCountMap(List<MatchResultOntology> list) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		int m = 0, n = 0, u = 0;
		for (MatchResultOntology onto : list) {
			if ("M".equals(onto.getMatched())) {
				m ++;
			} else if ("N".equals(onto.getMatched())) {
				n ++;
			} else if ("U".equals(onto.getMatched())) {
				u ++;
			}
		}
		map.put("M", m);
		map.put("N", n);
		map.put("U", u);
		return map;
	}
	
	private XSDDateTime toXSDDateTime(long genTime) {
		XSDDateTime dt = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(genTime));
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		dt = new XSDDateTime(cal);
		return dt;
	}
	private String fixXSDDate(String str) {
		if (str == null || str.length() <= 0) {
			return str;
		}
		str = str.replace('T', ' ').replace('Z', ' ').substring(0, 19);
		SimpleDateFormat isdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		isdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat osdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		osdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		Date date = null;
		try {
			date = isdf.parse(str);
			
			str = osdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return str;
	}

	private List<Score> getScoreListFromResource(Resource res) {
		List<Score> scoreList = new Vector<Score>();
		Resource actRes = res.listProperties(NameSpace.PROV_WAS_GENERATED_BY).next().getObject().asResource();
		Resource srcRes = actRes.listProperties(NameSpace.MATCH_HAS_MATCH_SOURCE).next().getObject().asResource();
		Resource dstRes = actRes.listProperties(NameSpace.MATCH_HAS_MATCH_TARGET).next().getObject().asResource();
		Map<String, String> srcMap = new TreeMap<String, String>(), dstMap = new TreeMap<String, String>();
		
		StmtIterator srcIter, dstIter;
		Score score = null;
		Resource memRes = null;
		String pred, value;
		
		srcIter = srcRes.listProperties(NameSpace.PROV_HAD_MEMBER);
		while (srcIter.hasNext()) {
			memRes = srcIter.next().getObject().asResource();
			pred = memRes.listProperties(NameSpace.RDF_PREDICATE).next().getObject().asResource().getURI();
			value = memRes.listProperties(NameSpace.PROV_VALUE).next().getObject().asLiteral().getString();
			srcMap.put(pred, value);
		}
		
		dstIter = dstRes.listProperties(NameSpace.PROV_HAD_MEMBER);
		while (dstIter.hasNext()) {
			memRes = dstIter.next().getObject().asResource();
			pred = memRes.getProperty(NameSpace.RDF_PREDICATE).getObject().asResource().getURI();
			value = memRes.getProperty(NameSpace.PROV_VALUE).getObject().asLiteral().getString();
			dstMap.put(pred, value);
		}
		
		Set<String> preds = null;
		if (srcMap.size() > dstMap.size()) {
			preds  = srcMap.keySet();
		} else {
			preds = dstMap.keySet();
		}
		
		for (String key : preds) {
			score = new Score();
			if (srcMap.containsKey(key)) {
				score.setPredicate(key);
				score.setSrcObj(srcMap.get(key));
			}
			if (dstMap.containsKey(key)) {
				score.setPredicate(key);
				score.setDstObj(dstMap.get(key));
			}
			scoreList.add(score);
		}
		return scoreList;
	}
	
	public List<MatchResultOntology> listMatchResultObjectWithGiven(String srcUri, String dstUri) {
		List<MatchResultOntology> list = new Vector<MatchResultOntology>();
		Model model = this.getModel();
		MatchResultOntology onto = null;
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				"PREFIX owl:<" + NameSpace.PREFIX_OWL + ">" + "\n" +
				"PREFIX nyt:<" + NameSpace.PREFIX_NYTIMES + ">" + "\n" +
				"PREFIX skos:<" + NameSpace.PREFIX_SKOS + ">" + "\n" + 
				" select ?sub ?matchType ?comment ?seeAlsoSI ?seeAlsoWiki ?score ?updated ?creator ?nyt ?nytname ?topicPage ?searchUri ?freebaseUri" + "\n" +
				" where {" + "\n" + 
					"?sub prov:generatedAtTime ?updated." + "\n" +
					"?sub match:seeAlsoInSmithsonian ?seeAlsoSI." + "\n" +
					"?sub match:seeAlsoInWikipedia ?seeAlsoWiki." + "\n" +
					"?sub match:hasScore ?score." + "\n" + 
					"?sub match:hasMatchType ?matchType." + "\n" + 
					"optional {?sub skos:note ?comment.}" + "\n" +
					"?sub prov:wasGeneratedBy ?act." + "\n" +
					"?act prov:wasAssociatedWith ?ag." + "\n" +
					"?ag rdfs:label ?creator." + "\n" +
					"?act match:hasMatchSource ?ent1." + "\n" +
					"?act match:hasMatchTarget ?ent2." + "\n" +
					"?ent1 prov:wasQuotedFrom <" + srcUri + ">." + "\n" +
					"?ent2 prov:wasQuotedFrom <" + dstUri + ">." + "\n" +
					"optional { ?ent2 owl:sameAs ?nyt." + "\n" +
					"  ?nyt skos:prefLabel ?nytname." + "\n" +
					"  ?nyt nyt:topicPage ?topicPage." + "\n" +
					"  ?nyt nyt:search_api_query ?searchUri." + "\n" +
					"  ?nyt owl:sameAs ?freebaseUri." + "\n" +
					"}" +
					"} order by desc(?updated) ";
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		QuerySolution solu = null;
		ResultSet rs = null;
		Resource res = null;
		String comment, seeSIUri, seeWikiUri, matchType, updated, creator;
		double score = -1;
		RDFNode node = null;
		
		try {
			rs = exec.execSelect();
			while(rs.hasNext()) {
				solu = rs.next();
				onto = new MatchResultOntology();
				res = solu.getResource("sub");
				if (solu.getLiteral("comment") != null) {
					comment = solu.getLiteral("comment").getString();
				}else {
					comment = "";
				}
				seeSIUri = solu.getResource("seeAlsoSI").getURI();
				seeWikiUri = solu.getResource("seeAlsoWiki").getURI();
				node = solu.get("score");
				score = node.asLiteral().getDouble();
				matchType = solu.getResource("matchType").getURI();
				updated = fixXSDDate(solu.get("updated").asLiteral().getString());
				creator = solu.getLiteral("creator").asLiteral().getString();
				onto.setSrcUri(srcUri);
				onto.setDstUri(dstUri);
				onto.setSeeAlsoInSI(seeSIUri);
				onto.setSeeAlsoInWiki(seeWikiUri);
				onto.setUpdated(updated);
				onto.setResId(res.getURI());
				onto.setCreator(creator);
				
				if (matchType.indexOf("ExactMatch") > -1) {
					onto.setMatched("M");
				} else if (matchType.indexOf("NotMatch") > -1) {
					onto.setMatched("N");
				} else {
					onto.setMatched("U");
				}
				onto.setComment(comment);

				node = solu.getResource("nyt");
				NYTimes nyt = null;
				if (node != null) {
					nyt = new NYTimes();
					nyt.setSubject(node.asResource().getURI());
					nyt.setName(solu.getLiteral("nytname").getString());
					nyt.setTopicPage(solu.getResource("topicPage").getURI());
					nyt.setSearchLink(solu.getLiteral("searchUri").getString());
					nyt.setFreebaseUri(solu.getResource("freebaseUri").getURI());
				}
				onto.setNytimes(nyt);
				onto.setMemberList(getScoreListFromResource(res));
				onto.setFinalScore(score);	
				list.add(onto);
			}
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		return list;
	}

	public void outputLatestOneMatchResultObject(String srcUri, String dstUri) {

		Model model = this.getModel();
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				"PREFIX skos:<" + NameSpace.PREFIX_SKOS + ">" + "\n" + 
				" select ?sub ?updated ?seeAlsoSI ?seeAlsoWiki ?score ?matchType ?comment  " + "\n" +
				" where { " + "\n" + 
					"?sub prov:generatedAtTime ?updated." + "\n" +
					"?sub match:seeAlsoInSmithsonian ?seeAlsoSI." + "\n" +
					"?sub match:seeAlsoInWikipedia ?seeAlsoWiki." + "\n" +
					"?sub match:hasScore ?score." + "\n" + 
					"?sub match:hasMatchType ?matchType." + "\n" + 
					"?sub skos:note ?comment." + "\n" +
					"?sub prov:wasGeneratedBy ?act." + "\n" +
					"?act match:hasMatchSource ?ent1." + "\n" +
					"?act match:hasMatchTarget ?ent2." + "\n" +
					"?ent1 prov:wasQuotedFrom <" + srcUri + ">." + "\n" +
					"?ent2 prov:wasQuotedFrom <" + dstUri + ">." + "\n" +
					"}";// order by asc(?updated) ";
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		QuerySolution solu = null;
		ResultSet rs = null;
		Resource res = null;
		String comment, seeSIUri, seeWikiUri, matchType, updated;
		double score = -10;

		RDFNode node = null;
		
		try {
			rs = exec.execSelect();
			if(rs.hasNext()) {
				solu = rs.next();
				res = solu.getResource("sub");
				if (solu.getLiteral("comment") != null) {
					comment = solu.getLiteral("comment").getString();
				}else {
					comment = "";
				}
				seeSIUri = solu.getResource("seeAlsoSI").getURI();
				seeWikiUri = solu.getResource("seeAlsoWiki").getURI();
				node = solu.get("score");
				score = node.asLiteral().getDouble();
				matchType = solu.getResource("matchType").getURI();
				updated = fixXSDDate(solu.get("updated").asLiteral().getString());
				System.out.println(res.getURI());
				System.out.println(comment);
				System.out.println(seeSIUri);
				System.out.println(seeWikiUri);
				System.out.println(score);
				System.out.println(matchType);
				System.out.println(updated);
				//System.out.println();
				
			}
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
	}
	
	public void outputAllMatchResult() {
		Model model = this.getModel();
		System.out.println("model size:" + model.size());
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				"select  ?s ?o " + "\n" +
				"where {" + "\n" +
				"  ?s prov:generatedAtTime ?o." + "\n" +
				//"  ?s prov:wasGeneratedBy ?act." + "\n" +
				//"  ?act rdfs:label ?actName." + "\n" + 
				//"  ?act match:hasMatchSource ?src." + "\n" +
				//"  ?act match:hasMatchTarget ?dst." + "\n" +
				//"  ?src prov:wasQuotedFrom ?srcUri." + "\n" +
				//"  ?dst prov:wasQuotedFrom ?dstUri." + "\n" +
				
				" }";
		
		QueryExecution exec = QueryExecutionFactory.create(sparql, model);
		ResultSet rs = null;
		RDFNode node = null;
		String s = null, p = null, o = null;
		int count = 0;
		try {
			rs = exec.execSelect();
			
			while(rs.hasNext()) {
				count++;
				QuerySolution solu = rs.next();
				node = solu.get("s");
				s = node.toString();
				//node = solu.get("p");
				//p = node.toString();
				node = solu.get("o");
				o = node.toString();
				//node = solu.get("srcUri");
				//act = node.asResource().getURI();
				//node = solu.get("dstUri");
				//time = node.asResource().getURI();
				System.out.println(count + ":" + s + "\t" + p + "\t" + o);// + "\t" + act + "\t" + time);
			}
			System.out.println(count + " records find matched.");
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		
	}
	public void outputLatestMatchResult() {
		Model model = this.getModel();
		String sparql = "PREFIX match:<" + NameSpace.PREFIX_MATCH + ">\n" +
				"PREFIX prov:<" + NameSpace.PREFIX_PROV + ">\n" +
				"PREFIX rdf:<" + NameSpace.PREFIX_RDF + ">\n" +
				"PREFIX rdfs:<" + NameSpace.PREFIX_RDFS + ">" + "\n" +
				"PREFIX skos:<" + NameSpace.PREFIX_SKOS + ">" + "\n" +
				" select ?srcUri ?dstUri ?comment  ?t " + "\n" +
				" where {" + "\n" + 
					"?sub prov:generatedAtTime ?t." + "\n" +
					"?sub prov:wasGeneratedBy ?acti." + "\n" +
					"?sub skos:note ?comment." + "\n" +
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
				node = solu.get("comment");
				time = node.asLiteral().getString();
				node = solu.get("t");
				System.out.println("[" + count + "]" + s + "\t" + act + "\t" + time + "\t" + this.fixXSDDate(node.asLiteral().getString()));
			}
			System.out.println(count + " records find matched.");
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			exec.close();
		}
		
	}

	public void clear() {
		Model model = this.getModel();
		model.begin();
		model.removeAll();
		model.commit();
		TDB.sync(model);
	}
	
	public static List<String> listRepositories() {
		List<String> list = new Vector<String>();
		String repoListFile = Constants.PATH_REPOSITORY + "repository_list.txt";
		
		File file = new File(repoListFile);
		if (file.exists()) {
			RandomAccessFile raf = null;
			String line = null;
			
			try {
				raf = new RandomAccessFile(file, "r");
				while ((line = raf.readLine()) != null) {
					if (line.length() > 0) {
						list.add(line);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return list;
	}

	public void addRepositoryName(String repoName) {
		String repoListFile = Constants.PATH_REPOSITORY + "repository_list.txt";
		
		File file = new File(repoListFile);
		if (!file.exists()) {
			throw new IllegalArgumentException("Repository file '" + file.getAbsolutePath() + "' DOES NOT exist!");
		}
		
		RandomAccessFile raf = null;
		String line = null;
		List<String> list = new Vector<String>();
		try {
			raf = new RandomAccessFile(file, "rw");
			while ((line = raf.readLine()) != null) {
				if (line.length() > 0) {
					list.add(line);
				}
			}
			
			raf.writeBytes(repoName + "\r\n");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (raf != null) raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
