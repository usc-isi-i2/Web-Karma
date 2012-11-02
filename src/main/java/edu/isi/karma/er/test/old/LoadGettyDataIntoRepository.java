package edu.isi.karma.er.test.old;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;

import edu.isi.karma.er.helper.Constants;

public class LoadGettyDataIntoRepository {

	private static String BASE_URI = "http://www.getty.edu/vow/ULANFullDisplay?find=&role=&nation=&prev_page=1&subjectid=";
	private static Property SAAM_PERSON = ResourceFactory.createProperty("http://americanart.si.edu/saam/Person");
	private static Property FULL_NAME = ResourceFactory.createProperty("http://americanart.si.edu/saam/fullName");
	private static Property BIRTH_YEAR = ResourceFactory.createProperty("http://americanart.si.edu/saam/birthYear");
	private static Property DEATH_YEAR = ResourceFactory.createProperty("http://americanart.si.edu/saam/deathYear");
	private static Property RDF_TYPE = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String fileName = Constants.PATH_BASE + "getty/getty_export.csv";
		File file = new File(fileName);
		if (!file.exists()) {
			throw new IllegalArgumentException("file " + file.getAbsolutePath() + " not exists");
		}
		
		Model model = TDBFactory.createDataset(Constants.PATH_REPOSITORY + "getty/").getDefaultModel();

		model = loadRepositoryFromCSV(model, file);
		
		//model.commit();
		//model.close();
		 output2File(model, Constants.PATH_N3_FILE + "getty_sample.n3");
	}
	
	private static void output2File(Model model, String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			model.write(new FileOutputStream(file), "N3");
			//model.write(System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private static Model loadRepositoryFromCSV(Model model, File file) {
		
		String id = null, name = null, birthDate = null, deathDate = null, birthPlace = null, deathPlace = null, bio = null;
		RandomAccessFile raf = null;
		
		try {
			String str = null;
			raf = new java.io.RandomAccessFile(file, "rw");
			
			str = raf.readLine();  // ignore the first line containing title
			while ((str = raf.readLine()) != null) {			// read a text line each time from ratio file
				String[] lines = split(str, "\",\"");
				
				if (lines.length >= 9) {						// 2 elements separated by ':' for a text line.
					id = lines[1];
					name = lines[4];
					birthDate = lines[5];
					deathDate = lines[6];
					createResource(model, id, name, birthDate, deathDate, birthPlace, deathPlace, bio);
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
		
		return model;
	}

	private static Resource createResource(Model model, String id, String name,
			String birthDate, String deathDate, String birthPlace,
			String deathPlace, String bio) {
		Resource res = model.createResource(BASE_URI + id);
		res.addProperty(RDF_TYPE, SAAM_PERSON);
		res.addProperty(FULL_NAME, name);
		res.addProperty(BIRTH_YEAR, birthDate);
		res.addProperty(DEATH_YEAR, deathDate);
		return res;
	}
	
	private static String[] split(String str, String delimiter) {
		ArrayList<String> list = new ArrayList<String>();
		int index = str.indexOf(delimiter);
		while (index > -1) {
			list.add(str.substring(0, index));
			str = str.substring(index+delimiter.length());
			index = str.indexOf(delimiter);
		}
		list.add(str);
		String[] arr = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i);
		}
		
		return arr;
	}

}

