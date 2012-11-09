package edu.isi.karma.er.helper.ontology;

import java.util.List;

import edu.isi.karma.er.helper.entity.Score;

public class MatchResultOntology {
	private String resId;	// id for resource in jena repository
	
	private String srcUri; 	// subject name in saam dataset
	
	private String dstUri;		// uri in saam dataset
	
	private String seeAlsoInSI;	// uri in dbpedia dataset
	
	private String seeAlsoInWiki;		// uri in wikidbpedia dataset
	
	double finalScore;					// a flag indicates similarity of matching, 1 -- sure to match with dbpediaUri 
	
	private String matched;		// match type, M -- Exact Match, N -- NotMatch, U -- Unsure
	
	private String updated;		// last updated time
	
	private List<Score> memberList;	// property of member involved in comparison.
	
	private String comment; 	// comment for this result
	
	private String creator;		// creator for this result, "Karma" or "Human"
	
	private String action; 		// "create" or "update"
	
	private String srcVersion;	// uri links to source data in a specific version
	
	private String dstVersion;	// uri links to target data in a specific version
	
	private MatchResultOntology originalOntology;	// the original ontology of this ontology, which means this ontology is originated from

	public String getSrcUri() {
		return srcUri;
	}

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

	public void setSrcUri(String srcUri) {
		this.srcUri = srcUri;
	}

	public String getDstUri() {
		return dstUri;
	}

	public void setDstUri(String dstUri) {
		this.dstUri = dstUri;
	}

	public String getSeeAlsoInSI() {
		return seeAlsoInSI;
	}

	public void setSeeAlsoInSI(String seeAlsoInSI) {
		this.seeAlsoInSI = seeAlsoInSI;
	}

	public String getSeeAlsoInWiki() {
		return seeAlsoInWiki;
	}

	public void setSeeAlsoInWiki(String seeAlsoInWiki) {
		this.seeAlsoInWiki = seeAlsoInWiki;
	}

	public double getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(double finalScore) {
		this.finalScore = finalScore;
	}

	public String getMatched() {
		return matched;
	}

	public void setMatched(String matched) {
		this.matched = matched;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public List<Score> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<Score> memberList) {
		this.memberList = memberList;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getSrcVersion() {
		return srcVersion;
	}

	public void setSrcVersion(String srcVersion) {
		this.srcVersion = srcVersion;
	}

	public String getDstVersion() {
		return dstVersion;
	}

	public void setDstVersion(String dstVersion) {
		this.dstVersion = dstVersion;
	}

	public MatchResultOntology getOriginalOntology() {
		return originalOntology;
	}

	public void setOriginalOntology(MatchResultOntology originalOntology) {
		this.originalOntology = originalOntology;
	}
	
	
}
