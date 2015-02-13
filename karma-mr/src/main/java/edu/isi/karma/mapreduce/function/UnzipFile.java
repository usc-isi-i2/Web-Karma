package edu.isi.karma.mapreduce.function;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class UnzipFile {

	public static void main(String[] args) throws IOException {
		FileSystem hdfs = FileSystem.get(new Configuration());
		unzipFile(hdfs.open(new Path(args[0])), args[1], hdfs);

	}
	
	 public static void unzipFile(InputStream stream, String outputPath, FileSystem fs){
         
	        ZipInputStream zipIs = null;
	        ZipEntry zEntry = null;
	        try {
	            zipIs = new ZipInputStream(stream);
	            while((zEntry = zipIs.getNextEntry()) != null){
	                try{
	                    byte[] tmp = new byte[4*1024];
	                    OutputStream fos = null;
	                    String opFilePath = outputPath + zEntry.getName();
	                    fos = fs.create(new Path(opFilePath));
	                    int size = 0;
	                    while((size = zipIs.read(tmp)) != -1){
	                        fos.write(tmp, 0 , size);
	                    }
	                    fos.flush();
	                    fos.close();
	                } catch(Exception ex){
	                     
	                }
	            }
	            zipIs.close();
	        } catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	    }

}
