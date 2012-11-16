package edu.isi.karma.er.helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.er.aggregator.impl.AggregatorImplByMeters;
import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.ResultRecord;
import edu.isi.karma.er.matcher.Matcher;

public class CalculateResultScore {// calculating

	private InputStruct[] resource1;
	private InputStruct[] resource2;
	private ResultRecord[] matchedPair;
	private int struct_number;
	private Connection connection;
	private String csvFileAddress;

	public CalculateResultScore(Connection connection, InputStruct[] resource1,
			InputStruct[] resource2, int struct_number,
			ResultRecord[] matchedPair, String csvFileAddress) {
		this.resource1 = resource1;
		this.resource2 = resource2;
		this.struct_number = struct_number;
		this.connection = connection;
		this.matchedPair = matchedPair;
		this.csvFileAddress = csvFileAddress;
	}

	// outputResultScore(InputStruct[] resource1,InputStruct[] resource2,int
	// struct_number);
	public void calculateResults() {
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();

		} catch (Exception ex) {
			ex.getStackTrace();
		}

		// WriteCSVImpl wcsv = new WriteCSVImpl(this.csvFileAddress);

		AggregatorImplByMeters aggImpl = new AggregatorImplByMeters(
				this.connection);

		MultiScore received = new MultiScore();
		int k = 0;

		for (int i = 0; i < this.struct_number; i++) {

			if (((InputStruct) this.resource1[i]).getX() != 0
					|| ((InputStruct) this.resource1[i]).getY() != 0
					|| ((InputStruct) this.resource1[i]).getPolygon() != null) {

				for (int j = 0; j < this.struct_number; j++) {

					// checking the isOverlaps between two InputStruct;
					// **
					String sourcePolygon = this.resource1[i].getPolygon();
					String destinationPolygon = this.resource2[j].getPolygon();
					boolean flag = true;
                    String isO=" ";
					if (sourcePolygon != "null" && destinationPolygon != "null") {

						try {
							rs = stmt.executeQuery("select ST_Overlaps "
									+ "(ST_GeomFromText(ST_AsText(\'"
									+ sourcePolygon
									+ "\')),ST_GeomFromText(ST_AsText(\'"
									+ destinationPolygon + "\'))) ");

							while (rs.next()) {

								try {
									boolean overlaps = rs.getBoolean(1);

									System.out.println("boolean overlaps is:"
											+ overlaps);

									flag = overlaps;
									if(overlaps==true){
										isO="true";
									}else if(overlaps==false){
										isO="false";
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}

							}

						} catch (SQLException ee) {
							ee.getStackTrace();
						}

					}// **

					if ((((InputStruct) this.resource2[j]).getX() != 0
							|| ((InputStruct) this.resource2[j]).getY() != 0 || ((InputStruct) this.resource2[j])
							.getPolygon() != null) ){
						//.getPolygon() != null) && flag) {
						// System.out.println("resource[i]="+this.resource1[i].toString());
						this.matchedPair[k]
								.setRes((InputStruct) this.resource1[i]);
						received = aggImpl.match(
								(InputStruct) this.resource1[i],
								(InputStruct) this.resource2[j]);
						received.setSrcSubj((InputStruct) this.resource1[i]);
						received.setDstSubj((InputStruct) this.resource2[j]);
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("IsOverlaps",new String(isO));
						received.getScoreList().get(0).setObjectMap(map);
						//System.out.println("map:"+received.getScoreList().get(0).getObjectMap().get("IsOverlaps"));
						
						
						double sim = received.getScoreList().get(0)
								.getSimilarity();
						received.setFinalScore(sim);
						this.matchedPair[k].addMultiScore(received);

						System.out.println("k=" + k);

						System.out.println("matchedPair[k]"
								+ received.getSrcSubj().toString());
						System.out.println("matchedPair[k]  &&"
								+ received.getDstSubj().toString());

						// ++k;
					} // else {
						// break;
					// }

				}
				++k;
				System.out.println("k2=" + k);


			} else {
				break;
			}

		}
		
		/**
		 * output the result to the csv file;
		 */
		OutputToCSV otc=new OutputToCSV(this.csvFileAddress, matchedPair);
		otc.outputToFile();

	}

}
