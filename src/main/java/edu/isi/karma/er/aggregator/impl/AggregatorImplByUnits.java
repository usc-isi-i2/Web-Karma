package edu.isi.karma.er.aggregator.impl;

import java.io.File;
import java.sql.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.er.aggregator.Aggregator;
import edu.isi.karma.er.helper.ConfigUtil;
import edu.isi.karma.er.helper.entity.InputStruct;
import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.Ontology;
import edu.isi.karma.er.matcher.impl.MatcherImplByUnits;

public class AggregatorImplByUnits implements Aggregator {

	private Connection connection;
	private String url1 = "http://www.semanticweb.org/ontologies/2012/9/BuildingOntology.owl#:xInDecimalLongitude";
	private String url2 = "http://www.semanticweb.org/ontologies/2012/9/BuildingOntology.owl#:wellKnownBinary";
	private JSONArray confArr = new JSONArray();

	public AggregatorImplByUnits(Connection connection) {
		this.connection = connection;

		File file = new File("config/er_configuration_building.json");
		if (!file.exists()) {
			throw new IllegalArgumentException("file name "
					+ file.getAbsolutePath() + " does not exist.");
		}

		ConfigUtil util = new ConfigUtil();
		// util.loadConstants();
		this.confArr = util.loadConfig(file);

	}

	public MultiScore match(Ontology s1, Ontology s2) {

		InputStruct a = (InputStruct) s1;
		InputStruct b = (InputStruct) s2;
		/*
		 * Matching by Projected Units;
		 */
		MatcherImplByUnits matImpl = new MatcherImplByUnits();
		/*
		 * Matching by Meters;
		 */
		// MatcherImplByMeters matImpl=new MatcherImplByMeters();
		String str_a = null;
		String str_b = null;
		MultiScore result = new MultiScore();

		JSONArray propertyArr = new JSONArray();
		JSONObject propetyObj = null;

		for (int i = 0; i < this.confArr.length(); i++) {
			try {

				propertyArr = this.confArr.getJSONObject(i).getJSONArray(
						"property");
				for (int j = 0; j < propertyArr.length(); j++) {
					propetyObj = propertyArr.getJSONObject(j);
					if (this.url1.equalsIgnoreCase(propetyObj
							.optString("source")) && a.getX() != 0.0) {
						str_a = "POINT";
						if (this.url1.equalsIgnoreCase(propetyObj
								.optString("destination")) && b.getX() != 0.0) {
							str_b = "POINT";
							break;
						} else if (this.url2.equalsIgnoreCase(propetyObj
								.optString("destination")) && b.getPolygon() != null) {
							str_b = "POLYGON";
							break;
						} else {
							System.out
									.println("There is something wrong with JUDGE(1) in AggregatorImplByMeters");
						}
					} else if (this.url2.equalsIgnoreCase(propetyObj
							.optString("source")) && a.getPolygon() != null) {
						str_a = "POLYGON";
						if (this.url1.equalsIgnoreCase(propetyObj
								.optString("destination")) && b.getX() != 0.0) {
							str_b = "POINT";
							break;
						} else if (this.url2.equalsIgnoreCase(propetyObj
								.optString("destination")) && b.getPolygon()!= null) {
							str_b = "POLYGON";
							break;
						} else {
							System.out
									.println("There is something wrong with JUDGE(2) in AggregatorImplByMeters");
						}
					}

				}

			} catch (JSONException e) {
				e.printStackTrace();

			}

		}

		if (str_a != null && str_b != null) {
			System.out.println("str_a :" + str_a.toString() + ";  str_b :"
					+ str_b.toString());

			result.setSrcSubj(a);
			result.setDstSubj(b);
			result = matImpl.matchBuilding(this.connection, result, str_a,
					str_b, a, b);
		}

		return result;
	}

}
