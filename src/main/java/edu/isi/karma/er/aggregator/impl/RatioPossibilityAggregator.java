package edu.isi.karma.er.aggregator.impl;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.helper.entity.Score;

public class RatioPossibilityAggregator implements Aggregator {

	//private calPosibility cal = null;
	
	public RatioPossibilityAggregator() {
		//this.cal = cal;
	}

	public MultiScore match(Ontology o1, Ontology o2) {
		
		MultiScore ms = new MultiScore();
		ms.setSrcSubj(o1);
		ms.setDstSubj(o2);
		List<Score> sList = new ArrayList<Score>();
		
		double score = 0; //cal.run(null, 0.99);
		
		ms.setScoreList(sList);
		ms.setFinalScore(score);
		return ms;
	}

}
