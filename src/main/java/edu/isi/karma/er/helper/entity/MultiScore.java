package edu.isi.karma.er.helper.entity;

import java.util.List;

public class MultiScore {

	private double finalScore;				// the gross score of pair 
	
	private List<Score> scoreList = null;	// list of score objects for properties of pair to be compared 

	private SaamPerson srcSubj = null;		// the source subject to compare
	
	private SaamPerson dstSubj = null;		// the target subject to compare with
	
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

	public SaamPerson getSrcSubj() {
		return srcSubj;
	}

	public void setSrcSubj(SaamPerson srcSubj) {
		this.srcSubj = srcSubj;
	}

	public SaamPerson getDstSubj() {
		return dstSubj;
	}

	public void setDstSubj(SaamPerson dstSubj) {
		this.dstSubj = dstSubj;
	}
	
	
}
