package edu.isi.karma.er.linkage;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.helper.entity.ResultRecord;

public class LinkageFinderThread extends Thread {
	List<Ontology> srcList = null;
	List<Ontology> dstList = null;
	Aggregator aver = null;
	JSONArray confArr = null;
	List<ResultRecord> resultList = new Vector<ResultRecord>();

	public LinkageFinderThread(List<Ontology> srcList, List<Ontology> dstList, Aggregator aver, JSONArray confArr) {
		this.srcList = srcList;
		this.dstList = dstList;
		this.aver = aver;
		this.confArr = confArr;
	}
	
	public void run() {
		int i = 0;
		Logger log = Logger.getRootLogger();
		long startTime = System.currentTimeMillis();
		for (Ontology p1 : srcList) {
			
			ResultRecord rec = new ResultRecord();
			rec.setRes(p1);
			for (Ontology p2 : dstList) {
				
				MultiScore ms = aver.match(p1, p2); 	// compare 2 resource to return a result of match with match details
				if ( ms.getFinalScore() > rec.getCurrentMinScore() ) {	// to decide whether current pair can rank to top 5 
					rec.addMultiScore(ms);
				}
			}
			if (++i % 100 == 0) {
				log.info(this.getName() + " processed " + i + " rows in " + (System.currentTimeMillis() - startTime) + "ms.");
				startTime = System.currentTimeMillis();
				
			}
			//log.info("[" + df.format(rec.getCurrentMinScore()) + " | " + df.format(rec.getCurrentMaxScore()) + "] " + rec.getRes() + " has " + rec.getRankList().size() + " results");
			resultList.add(rec);
		}
	}

	public List<Ontology> getSrcList() {
		return srcList;
	}

	public void setSrcList(List<Ontology> srcList) {
		this.srcList = srcList;
	}

	public List<Ontology> getDstList() {
		return dstList;
	}

	public void setDstList(List<Ontology> dstList) {
		this.dstList = dstList;
	}

	public Aggregator getAver() {
		return aver;
	}

	public void setAver(Aggregator aver) {
		this.aver = aver;
	}

	public JSONArray getConfArr() {
		return confArr;
	}

	public void setConfArr(JSONArray confArr) {
		this.confArr = confArr;
	}

	public List<ResultRecord> getResultList() {
		return resultList;
	}

	public void setResultList(List<ResultRecord> resultList) {
		this.resultList = resultList;
	}
}
