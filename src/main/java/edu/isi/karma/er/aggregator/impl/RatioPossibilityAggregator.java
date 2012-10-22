package edu.isi.karma.er.aggregator.impl;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.SaamPerson;
import edu.isi.karma.er.helper.entity.Score;
import edu.isi.karma.er.test.calPosibility;

public class RatioPossibilityAggregator implements Aggregator {

	private calPosibility cal = null;
	
	public RatioPossibilityAggregator(calPosibility cal) {
		this.cal = cal;
	}
<<<<<<< HEAD
	@Override
	public MultiScore match(SaamPerson res1, SaamPerson res2) {
=======
	public MultiScore match(JSONArray confArr, Resource res1, Resource res2) {
>>>>>>> 994193e1d02c4c52ce098435623499e558ffc511
		
		MultiScore ms = new MultiScore();
		ms.setSrcSubj(res1);
		ms.setDstSubj(res2);
		List<Score> sList = new ArrayList<Score>();
		
		double score = cal.run(null, 0.99);
		
		ms.setScoreList(sList);
		ms.setFinalScore(score);
		return ms;
	}

}
