package edu.isi.karma.modeling.semantictypes.myutils ;

import java.io.File;
import java.util.ArrayList;

public class FileOps {

	public static ArrayList<String> filesFromFolders(ArrayList<String> folderList, String qualifierEnding) {
		ArrayList<String> fileList = new ArrayList<String>() ;
		for(String folder : folderList) {
			fileList.addAll(filesFromFolder(folder, qualifierEnding)) ;
		}
		return fileList ;
	}

	public static ArrayList<String> filesFromFolder(String folder, String qualStr) {
		ArrayList<String> fileList = new ArrayList<String>() ;
		File[] files = new File(folder).listFiles() ;
		for(File f : files) {
			if(f.getName().endsWith(qualStr))
				fileList.add(f.getAbsolutePath()) ;
		}
		return fileList ;
	}
	
}