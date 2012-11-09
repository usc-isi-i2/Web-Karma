package edu.isi.karma.er.helper.entity;


/**
 * The object records the result of calculation, especially with a score 
 * type which represents the result type, such as norm, ignore, and invalid
 * @author isi
 *
 */
public class Score {
	
	private String predicate; 	// the predicate used to compare between 2 values

	private double similarity;		// the similarity of compare result, also means score
	
	private double freq;
	
	private double ratio;
	
	private ScoreType scoreType;	// the type of compare result, see enum class ScoreType
	
	private String srcObj;		// the source statement to compare, including subject and object 
		
	private String dstObj;		// the target statement to compare with, including subject and object
	

	public double getFreq() {
		return freq;
	}

	public void setFreq(double freq) {
		this.freq = freq;
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public String getSrcObj() {
		return srcObj;
	}

	public void setSrcObj(String srcObj) {
		this.srcObj = srcObj;
	}

	public String getDstObj() {
		return dstObj;
	}

	public void setDstObj(String dstObj) {
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

