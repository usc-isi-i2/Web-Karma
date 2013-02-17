package edu.isi.karma.er.helper.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.helper.Constants;

public class NYTimes {

	private String subject = "";
	private String name = "";
	private String dbpediaUri = "";
	private String freebaseUri = "";
	private String topicPage = "";
	private String searchLink = "";
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDbpediaUri() {
		return dbpediaUri;
	}
	public void setDbpediaUri(String dbpediaUri) {
		this.dbpediaUri = dbpediaUri;
	}
	public String getFreebaseUri() {
		return freebaseUri;
	}
	public void setFreebaseUri(String freebaseUri) {
		this.freebaseUri = freebaseUri;
	}
	public String getTopicPage() {
		return topicPage;
	}
	public void setTopicPage(String topicPage) {
		this.topicPage = topicPage;
	}
	public String getSearchLink() {
		return searchLink;
	}
	public void setSearchLink(String searchLink) {
		this.searchLink = searchLink;
	}
	
	public List<NYTimes> listAll() {
		Model model = loadNytimesModel();
		List<NYTimes> list = new Vector<NYTimes>();
		ResIterator resList = model.listSubjectsWithProperty(NameSpace.RDF_TYPE);
		
		Resource subj;
		Statement stmt;
		String uri;
		while (resList.hasNext()) {
			NYTimes ent = new NYTimes();
			subj = resList.next();
			ent.setSubject(subj.getURI());
			ent.setName(subj.getProperty(NameSpace.SKOS_PREF_LABEL).getObject().asLiteral().getString());
			stmt = subj.getProperty(NameSpace.NYTIMES_TOPIC_PAGE);
			if (stmt != null)
				ent.setTopicPage(stmt.getObject().asResource().getURI());
			ent.setSearchLink(subj.getProperty(NameSpace.NYTIMES_SEARCH_API_QUERY).getObject().asLiteral().getString());
			
			StmtIterator sit = subj.listProperties(NameSpace.OWL_SAME_AS);
			while(sit.hasNext()) {
				uri = sit.next().getObject().asResource().getURI();
				if (uri.indexOf("dbpedia") > -1) {
					ent.setDbpediaUri(uri);
				} else if (uri.indexOf("freebase") > -1) {
					ent.setFreebaseUri(uri);
				}
			}
			list.add(ent);
		}
		model.close(); // close nytimes model
		return list;
	}
	
	private Model loadNytimesModel() {
		
		return TDBFactory.createDataset(Constants.PATH_REPOSITORY + "nytimes").getDefaultModel();
	}
	
	
	public Map<String, NYTimes> listAllToMap() {
		Model model = loadNytimesModel();
		Map<String, NYTimes> map = new HashMap<String, NYTimes>();
		ResIterator resList = model.listSubjectsWithProperty(NameSpace.RDF_TYPE);
		
		Resource subj;
		Statement stmt;
		String uri;
		while (resList.hasNext()) {
			NYTimes ent = new NYTimes();
			subj = resList.next();
			ent.setSubject(subj.getURI());
			ent.setName(subj.getProperty(NameSpace.SKOS_PREF_LABEL).getObject().asLiteral().getString());
			stmt = subj.getProperty(NameSpace.NYTIMES_TOPIC_PAGE);
			if (stmt != null)
				ent.setTopicPage(stmt.getObject().asResource().getURI());
			ent.setSearchLink(subj.getProperty(NameSpace.NYTIMES_SEARCH_API_QUERY).getObject().asLiteral().getString());
			
			StmtIterator sit = subj.listProperties(NameSpace.OWL_SAME_AS);
			while(sit.hasNext()) {
				uri = sit.next().getObject().asResource().getURI();
				if (uri.indexOf("dbpedia") > -1) {
					ent.setDbpediaUri(uri);
				} else if (uri.indexOf("freebase") > -1) {
					ent.setFreebaseUri(uri);
				}
			}
			if (ent.getDbpediaUri() != null) {
				map.put(ent.getDbpediaUri(), ent);
			}
		}
		return map;
	}
	
}
