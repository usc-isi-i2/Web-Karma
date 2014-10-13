package edu.isi.karma.cleaning.features;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

public class Data2Features {

	private static Logger logger = LoggerFactory.getLogger(Data2Features.class);

	// convert the csv file to arff file
	// return the fpath of arff file
	/*
	 * public static void Data2arff(String csvpath, String arfpath) { try {
	 * CSVLoader loader = new CSVLoader(); loader.setSource(new File(csvpath));
	 * Instances data = loader.getDataSet(); // save ARFF ArffSaver saver = new
	 * ArffSaver(); saver.setInstances(data); saver.setFile(new File(arfpath));
	 * saver.setDestination(new File(arfpath)); saver.writeBatch(); } catch
	 * (Exception ex) { ex.printStackTrace(); } }
	 */

	public static void Testdata2CSV(Vector<String> tests, String fpath,
			RecordFeatureSet rf) {
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(new File(fpath)));
			// get attribute names
			// get attribute names
			Collection<String> attrStrings = rf.getFeatureNames();
			String[] attr_names = attrStrings.toArray(new String[attrStrings
					.size() + 1]);
			attr_names[attr_names.length - 1] = "label";
			writer.writeNext(attr_names);
			for (String Record : tests) {
				Vector<String> row = new Vector<String>();
				Collection<Feature> cf = rf.computeFeatures(Record, "");
				Feature[] x = cf.toArray(new Feature[cf.size()]);
				// row.add(f.getName());
				for (int k = 0; k < cf.size(); k++) {
					row.add(String.valueOf(x[k].getScore()));
				}
				row.add("");
				String[] dataEntry = row.toArray(new String[row.size()]);
				writer.writeNext(dataEntry);
			}
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// class: records convert them into csv file
	public static void Traindata2CSV(
			HashMap<String, Vector<String>> class2Records, String fpath,
			RecordFeatureSet rf) {
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(new File(fpath)));
			Vector<String> vsStrings = new Vector<String>();
			for (Vector<String> vecs : class2Records.values()) {
				vsStrings.addAll(vecs);
			}
			// get attribute names
			Collection<String> attrStrings = rf.getFeatureNames();
			String[] attr_names = attrStrings.toArray(new String[attrStrings
					.size() + 1]);
			attr_names[attr_names.length - 1] = "label";
			writer.writeNext(attr_names);
			for (String label : class2Records.keySet()) {

				for (String Record : class2Records.get(label)) {
					Vector<String> row = new Vector<String>();
					Collection<Feature> cf = rf.computeFeatures(Record, label);
					Feature[] x = cf.toArray(new Feature[cf.size()]);
					// row.add(f.getName());
					for (int k = 0; k < cf.size(); k++) {
						row.add(String.valueOf(x[k].getScore()));
					}
					row.add(label); // change this according to the dataset.
					String[] dataEntry = row.toArray(new String[row.size()]);
					writer.writeNext(dataEntry);
				}

			}
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			logger.error("" + Arrays.toString(ex.getStackTrace()));
		}
	}

}
