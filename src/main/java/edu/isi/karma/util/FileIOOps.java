package edu.isi.karma.util ;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class FileIOOps {

	public static ArrayList<String> allLinesFromFile(String file, boolean removeEmptyLines)  {
		ArrayList<String> lines = new ArrayList<String>() ;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file)) ;
			String line = "" ;

			while(true) {
				if ((line = br.readLine()) == null) {
					break ;
				}
				line = line.trim() ;
				if (line.length() != 0 || !removeEmptyLines) {
					lines.add(line) ;
				}
			}

			br.close();
		}
		catch (Exception e) {
			Prnt.endIt("FileIOOps.allLinesFromFile: Error in reading file " + file + ". Exiting.") ;
		}
		return lines ;
	}

}

