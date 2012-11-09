package edu.isi.karma.er.helper.entity;

import java.util.List;

public class MultiScore {

	private double finalScore;				// the gross score of pair 
	
	private List<Score> scoreList = null;	// list of score objects for properties of pair to be compared 

	private Ontology srcSubj = null;		// the source subject to compare
	
	private Ontology dstSubj = null;		// the target subject to compare with
	
	public double getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(double finalScore) {
		this.finalScore = finalScore;
	}

	public List<Score> getScoreList() {
		return scoreList;
	}

	public void setScoreList(List<Score> scoreList) {
		this.scoreList = scoreList;
	}

	public Ontology getSrcSubj() {
		return srcSubj;
	}

	public void setSrcSubj(Ontology srcSubj) {
		this.srcSubj = srcSubj;
	}

	public Ontology getDstSubj() {
		return dstSubj;
	}

	public void setDstSubj(Ontology dstSubj) {
		this.dstSubj = dstSubj;
	}
	
	
}
