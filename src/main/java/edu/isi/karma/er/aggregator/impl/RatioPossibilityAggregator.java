package edu.isi.karma.er.aggregator.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.test.calPosibility;

public class RatioPossibilityAggregator implements Aggregator {

	private calPosibility cal = null;
	
	public RatioPossibilityAggregator(calPosibility cal) {
		this.cal = cal;
	}
	public MultiScore match(JSONArray confArr, Resource res1, Resource res2) {
		
		MultiScore ms = new MultiScore();
		ms.setSrcSubj(res1);
		ms.setDstSubj(res2);
		List<Score> sList = new ArrayList<Score>();
		
		double score = cal.run(res1, 0.99);
		
		ms.setScoreList(sList);
		ms.setFinalScore(score);
		return ms;
	}

}
