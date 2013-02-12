package edu.isi.karma.er.helper;

import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.ResultRecord;

public class OutputToCSV {
	private String csvFileAddress;
	private ResultRecord[] matchedPair;

	public OutputToCSV(String csvFileAddress, ResultRecord[] matchedPair) {
		this.csvFileAddress = csvFileAddress;
		this.matchedPair = matchedPair;
	}

	/*
	 * writing to csv file...
	 */
	public void outputToFile() {
		WriteCSVImpl wcsv = new WriteCSVImpl(this.csvFileAddress);
		for (int i = 0; i < matchedPair.length; i++) {
			for (int j = 0; j < this.matchedPair[i].getRankList().size(); j++) {
				wcsv.WriteOneLine(((InputStruct) this.matchedPair[i].getRes())
						.getResource().toString()
						+ ","
						+ ((InputStruct) this.matchedPair[i].getRes())
								.getNameforBuilding()
						+ ","
						+ ((InputStruct) this.matchedPair[i].getRes()).getX()
						+ ","
						+ ((InputStruct) this.matchedPair[i].getRes()).getY()
						+ ","
						+ ((InputStruct) this.matchedPair[i].getRes())
								.getPolygon()
						+ ","
						+ ((InputStruct) ((this.matchedPair[i].getRankList()
								.get(j)).getDstSubj())).getResource()
								.toString()
						+ ","
						+ ((InputStruct) ((this.matchedPair[i].getRankList()
								.get(j)).getDstSubj())).getNameforBuilding()
						+ ","
						+ ((InputStruct) ((this.matchedPair[i].getRankList()
								.get(j)).getDstSubj())).getX()
						+ ","
						+ ((InputStruct) ((this.matchedPair[i].getRankList()
								.get(j)).getDstSubj())).getY()
						+ ","
						+ ((InputStruct) ((this.matchedPair[i].getRankList()
								.get(j)).getDstSubj())).getPolygon()
						+ ","
						+ ((this.matchedPair[i].getRankList().get(j)))
								.getScoreList().get(0).getFreq()
						+ ","
						+ ((this.matchedPair[i].getRankList().get(j)))
								.getScoreList().get(0).getSimilarity()
												+ ","
						+ ((this.matchedPair[i].getRankList().get(j))
								.getScoreList().get(0).getObjectMap()
								.get("IsOverlaps"))
						+ ","
						+ ((this.matchedPair[i].getRankList().get(j))
								.getScoreList().get(0).getObjectMap()
								.get("IsContained")));
			}// for j
		}// for i;
		wcsv.Close();
	}
}
