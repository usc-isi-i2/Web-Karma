package edu.isi.karma.modeling.steiner.topk;

import java.util.LinkedList;
import java.util.List;

public class ResultGraph {

	private Double score;
	private List<Fact> facts;
	
	public ResultGraph() {
		this.facts = new LinkedList<>();
	}
	
	public Double getScore() {
		return this.score;
	}
	
	public void setScore(Double score) {
		this.score = score;
	}
	
	public void addEdge(Fact f) {
		this.facts.add(f);
	}

	public List<Fact> getFacts() {
		return facts;
	}
	
	
}
