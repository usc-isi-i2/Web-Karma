package edu.isi.karma.er.helper.entity;

import java.util.List;

/**
 * A single record about matching with the by-hand generated score board, including saam uri, corresponding dbpedia and wiki uri etc.
 * @author isi
 *
 */
public class ScoreBoard {

	private String subject; 	// subject name in saam dataset
	
	private String saamUri;		// uri in saam dataset
	
	private String dbpediaUri;	// uri in dbpedia dataset
	
	private String wikiUri;		// uri in wikidbpedia dataset
	
	private String karmaUri;	// uri found by karma
	
	private List<MultiScore> rankList;	// rank of top 5 similar results.
	
	double found;					// a flag indicates similarity of matching, 1 -- sure to match with dbpediaUri 

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSaamUri() {
		return saamUri;
	}

	public void setSaamUri(String saamUri) {
		this.saamUri = saamUri;
	}

	public String getDbpediaUri() {
		return dbpediaUri;
	}

	public void setDbpediaUri(String dbpediaUri) {
		this.dbpediaUri = dbpediaUri;
	}

	public String getWikiUri() {
		return wikiUri;
	}

	public void setWikiUri(String wikiUri) {
		this.wikiUri = wikiUri;
	}

	public String getKarmaUri() {
		return karmaUri;
	}

	public void setKarmaUri(String karmaUri) {
		this.karmaUri = karmaUri;
	}

	public double getFound() {
		return found;
	}

	public void setFound(double found) {
		this.found = found;
	}

	public List<MultiScore> getRankList() {
		return rankList;
	}

	public void setRankList(List<MultiScore> rankList) {
		this.rankList = rankList;
	}
	
	
}
