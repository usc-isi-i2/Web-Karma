package edu.isi.karma.er.helper.entity;

import java.util.LinkedList;
import java.util.List;

public class ResultRecord {

	SaamPerson res = null;		// the owner of this result record, which points to a resource in data model.
	
	int limit = 5;
	
	List<MultiScore> rankList = new LinkedList<MultiScore>();	// the related results list ranking by the similarity descendant.
	
	double minSimilarity = 0;	// the minimum similarity of records in current ranklist
	
	double maxSimilarity = 0;	// the maximum similarity of records in current rankList
	
	/**
	 * maintain the ranking MultiScore list of the given resource, it means that there are at most 'limit' number of resources 
	 * similar to the give resource, which are stored in the list in a descendant order.
	 * @param s  the new MultiScore element to decide whether it should be joined or not, and which position should be put if neccesary
	 * @return
	 */
	public boolean addMultiScore(MultiScore s) {
		boolean result = false;
		if (s != null) {
			// find the position of insertion for the s.
			int pos = 0;
			for (; pos < rankList.size(); pos++) {
				MultiScore ts = rankList.get(pos);
				if (s.getFinalScore() > ts.getFinalScore()) {
					break;
				}
			}
			
			if (pos < limit) {	// the new MultiScore is a valid value which should be inserted into position 'pos'
				if (rankList.size() < limit) { // the number of elements in ranklist is currently less that limit, just insert.
					rankList.add(pos, s);
				} else {						// the number of elements is more than the limit, insert the new one, then delete the last one (smallest one)
					rankList.add(pos, s);
					rankList.remove(limit);
					//set current minimum simiarity of ranklist,  return the similarity of last element.
					minSimilarity = (rankList.get(limit-1)).getFinalScore();
					
				}
			}
			maxSimilarity = rankList.get(0).getFinalScore();
			result = true;
		}
		
		return result;
	}

	public SaamPerson getRes() {
		return res;
	}

	public void setRes(SaamPerson res) {
		this.res = res;
	}

	public List<MultiScore> getRankList() {
		return rankList;
	}

	public void setRankList(List<MultiScore> rankList) {
		this.rankList = rankList;
	}
	
	public String toString() {
		String str = "";
		str += res.getSubject() + "\t [";
		for(int j = 0; j < rankList.size(); j++) {
			MultiScore s = rankList.get(j);
			str += s.getFinalScore() + ",";
		}
		str += "]";
		return str;
	}
	
	public double getCurrentMinScore() {
		
		return minSimilarity;
	}
	
	public double getCurrentMaxScore() {
		return maxSimilarity;
	}
}
