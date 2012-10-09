package edu.isi.karma.er.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

import edu.isi.karma.er.helper.Constants;
import edu.isi.karma.er.ratio.IRatio;
import edu.isi.karma.er.ratio.impl.RatioImpl;

public class TestRatioMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("start loading.");
		//Model model = FileManager.get().loadModel(Constants.PATH_N3_FILE + "dbpedia_fullname_birth_death_city_state_country.n3", "Turtle");
		Model model = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "all/").getDefaultModel();
		System.out.println("finish loading.");
		
		String attr = "associatedYear";
		String predicate = "http://americanart.si.edu/saam/" + attr;
		
		IRatio ratio = new RatioImpl();
		System.out.println("start ratio");
		Map<String, Integer> map = ratio.calcRatio(model, predicate);
		System.out.println("finish ratio");
		int count = 0;
		
		try {
			File file = new File(Constants.PATH_RATIO_FILE + attr + ".txt");
			if (file.exists()) {
				file.delete();
			} 
			file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            DecimalFormat df = new DecimalFormat("0.000000");
            
            int total = map.get("__total__");
            map.remove("__total__");
            bw.write("\"__total__\"::" + total + "\n");
            
			for (String str : map.keySet()) {
				System.out.print("[ " + str + " : " + df.format(map.get(str) * 1.0 / total * 1.0) + ":" + map.get(str) + " ] \t");
				bw.write("\"" + str + "\":" + df.format(map.get(str) * 1.0 / total) + ":" + map.get(str) + "\n");
		        bw.newLine();
				if (++count % 10 == 0) {
					System.out.println("");
				}
			}
			
		
        	bw.close();
        	//dataset.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		
		System.out.println("\n total item count:" + map.size());
	}

}
