package edu.isi.karma.er.helper.entity;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * The object records the result of calculation, especially with a score 
 * type which represents the result type, such as norm, ignore, and invalid
 * @author isi
 *
 */
public class Score {
	
	private Property predicate; 	// the predicate used to compare between 2 values

	private double similarity;		// the similarity of compare result, also means score
	
	private ScoreType scoreType;	// the type of compare result, see enum class ScoreType
	
	private Statement srcObj;		// the source statement to compare, including subject and object 
		
	private Statement dstObj;		// the target statement to compare with, including subject and object
	

	public Property getPredicate() {
		return predicate;
	}

	public void setPredicate(Property predicate) {
		this.predicate = predicate;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public Statement getSrcObj() {
		return srcObj;
	}

	public void setSrcObj(Statement srcObj) {
		this.srcObj = srcObj;
	}

	public Statement getDstObj() {
		return dstObj;
	}

	public void setDstObj(Statement dstObj) {
		this.dstObj = dstObj;
	}

	public ScoreType getScoreType() {
		return scoreType;
	}

	public void setScoreType(ScoreType scoreType) {
		this.scoreType = scoreType;
	}
	
	public Score() {
		
	}
	
	public Score(double similarity, ScoreType scoreType) {
		this.similarity = similarity;
		this.scoreType = scoreType;
	}

}

