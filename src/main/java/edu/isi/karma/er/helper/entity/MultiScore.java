package edu.isi.karma.er.helper.entity;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class MultiScore {

	private double finalScore;				// the gross score of pair 
	
	private List<Score> scoreList = null;	// list of score objects for properties of pair to be compared 

	private Resource srcSubj = null;		// the source subject to compare
	
	private Resource dstSubj = null;		// the target subject to compare with
	
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

	public Resource getSrcSubj() {
		return srcSubj;
	}

	public void setSrcSubj(Resource srcSubj) {
		this.srcSubj = srcSubj;
	}

	public Resource getDstSubj() {
		return dstSubj;
	}

	public void setDstSubj(Resource dstSubj) {
		this.dstSubj = dstSubj;
	}
	
	
}
