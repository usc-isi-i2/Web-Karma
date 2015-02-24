/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.cleaning.Research;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.apache.mahout.math.Arrays;

import au.com.bytecode.opencsv.CSVWriter;
import edu.isi.karma.cleaning.DataPreProcessor;
import edu.isi.karma.cleaning.InterpreterType;
import edu.isi.karma.cleaning.Messager;
import edu.isi.karma.cleaning.ProgSynthesis;
import edu.isi.karma.cleaning.ProgramRule;

public class Tools {
	public void transformFile(String fpath) {
		try {
			Vector<String[]> examples = new Vector<String[]>();
			// read and write the data
			File nf = new File(fpath);
			BufferedReader cr = new BufferedReader(new FileReader(fpath));
			String pair = "";
			Vector<String> vtmp = new Vector<String>();
			while ((pair = cr.readLine()) != null) {
				pair = pair.trim();
				if (pair.length() == 0)
					continue;
				if (pair.charAt(0) == '\"') {
					pair = pair.substring(1);
				}
				if (pair.charAt(pair.length() - 1) == '\"') {
					pair = pair.substring(0, pair.length() - 1);
				}
				vtmp.add(pair);
			}
			DataPreProcessor dpp = new DataPreProcessor(vtmp);
			dpp.run();
			Messager msger = new Messager();
			while (true) {

				Vector<String[]> result = new Vector<String[]>();
				System.out.print("Enter raw value\n");
				// open up standard input
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				String raw = null;
				raw = br.readLine();
				if (raw.compareTo("end") == 0) {
					break;
				}
				System.out.print("Enter tar value\n");
				// open up standard input
				String tar = null;
				tar = br.readLine();

				// learn the program
				String[] xStrings = { "<_START>" + raw + "<_END>", tar };
				examples.add(xStrings);
				for (String[] elem : examples) {
					System.out.println("Examples inputed: "
							+ Arrays.toString(elem));
				}
				String ofpath = "/Users/bowu/Research/50newdata/tmp/"
						+ nf.getName();
				CSVWriter cw = new CSVWriter(new FileWriter(new File(ofpath)));
				ProgSynthesis psProgSynthesis = new ProgSynthesis();				
				psProgSynthesis.inite(examples,dpp,msger); //
				Collection<ProgramRule> ps = psProgSynthesis.run_main();
				msger.updateCM_Constr(psProgSynthesis.partiCluster
						.getConstraints());
				msger.updateWeights(psProgSynthesis.partiCluster.weights);
				ProgramRule pr = ps.iterator().next();
				System.out.println(""+psProgSynthesis.myprog.toString());
				System.out.println("" + pr.toString());
				for(String org: vtmp)
				{
					String ttar = pr.transform(org);
					String[] pValue = {org,ttar};
					cw.writeNext(pValue);
					System.out.println(String.format("%s,%s", org,ttar ));
					result.add(pValue);
				}
				cw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test1() {
		Vector<String[]> examples = new Vector<String[]>();
		String[] xStrings = { "<_START>Ruth Asawa<_END>", "Asawa, Ruth" };
		String[] yStrings = { "<_START>Robert Boardman Howard<_END>",
				"Howard, Robert Boardman" };
		// String[] zStrings =
		// {"<_START>Artist unknown Salem, Massachusetts area<_END>","Artist unknown"};
		examples.add(xStrings);
		examples.add(yStrings);
		ArrayList<String> data = new ArrayList<String>();
		// examples.add(zStrings);
		ProgSynthesis psProgSynthesis = new ProgSynthesis();
		DataPreProcessor dbDataPreProcessor = new DataPreProcessor(data);
		Vector<Vector<String[]>> cstrns = new Vector<Vector<String[]>>();

		psProgSynthesis.inite(examples,dbDataPreProcessor,cstrns);
		Collection<ProgramRule> ps = psProgSynthesis.run_main();
		ProgramRule pr = ps.iterator().next();
		System.out.println("" + pr.toString());
		String val = "J. B. Blunk";
		InterpreterType rule = pr.getRuleForValue(val);
		System.out.println(rule.execute(val));
	}

	public static void main(String[] args) {
		ConfigParameters cfg = new ConfigParameters();
		cfg.initeParameters();
		DataCollection.config = cfg.getString();
		Tools tools = new Tools();
		tools.transformFile("/Users/bowu/Research/testdata/workable_singleColumn/n0.csv");
		//tools.transformFile("/Users/bowu/Research/50newdata/tmp/example.csv");
		//tools.test1();

	}
}
