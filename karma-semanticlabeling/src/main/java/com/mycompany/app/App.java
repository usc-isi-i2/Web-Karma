package com.mycompany.app;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


// import java.lang.module.ModuleDescriptor.Modifier;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder; 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mycompany.app.CreateDSLObjects;
import com.mycompany.dsl.FeatureExtractor;
import com.mycompany.dsl.SemTypePrediction;
import com.mycompany.dsl.DSL_main;
import com.mycompany.dsl.ColumnBasedTable;



/**
 * This class is the main class for training and testing of the model.
 * 
 * @author rutujarane
 * 
 * mvn clean install
 * mvn exec:java -Dexec.mainClass="com.mycompany.app.App" 
 *
 */

public class App 
{

    static Logger logger = LogManager.getLogger(App.class.getName());
    public static void main( String[] args ) throws Exception{
        //logger.setLevel(Level.INFO);
        System.out.println( "Starting App!" );
        System.out.println( "Starting App!" );
        String dirPath = "/Users/rutujarane/Desktop/ISI/Semantics/dsl/data/soccer2";
        String modelFile = "new_model_Dec";

        File maindir = new File(dirPath);
        // File listOfFiles[];
        if(maindir.exists() && maindir.isDirectory()){
            //File listOfFileFolders[] = maindir.listFiles();
            File listOfFiles[] = maindir.listFiles();
            //for(int i=0; i<listOfFileFolders.length; i++){
                // if(listOfFileFolders[i].exists() && listOfFileFolders[i].isDirectory()){

                    // File listOfSubFiles[] = listOfFileFolders[i].listFiles();
                    // for(int j=0; j<listOfSubFiles.length; j++){
                    //     listOfFiles.add(listOfSubFiles[j]);
                    // }
                //}
            // }
            String fileList[] = new String[listOfFiles.length];
            for(int i=0; i<listOfFiles.length; i++){
                fileList[i] = listOfFiles[i].getAbsolutePath();
                System.out.println(fileList[i].toString());
                System.out.println(listOfFiles[i].toString());
            }

            int count=0;
            double MRR = 0;
            for(int fileNum=0; fileNum<fileList.length; fileNum++){

                //Read the csv file: prediction from another folder
                fileNum++;
                System.out.println("Reading: "+fileList[fileNum]);
                String[][] data = CreateDSLObjects.readFile(fileList[fileNum]);

                //Test:
                //String fileNameDiffFolder = "/Users/rutujarane/Desktop/ISI/Semantics/dsl_copy/dsl/data/city2/s1.csv";
                //System.out.println("Reading from diff folder: "+fileNameDiffFolder);
                //String[][] data = CreateDSLObjects.readFile(fileNameDiffFolder);

                //Delete the modelFile if it exists
                CreateDSLObjects.deleteFile(modelFile);

                String fileListTrain[] = new String[fileList.length-1];
                //Test: String fileListTrain[] = fileList;
                System.arraycopy(fileList, 0, fileListTrain, 0, fileNum);
                System.out.println(fileList.length);
                System.arraycopy(fileList, fileNum + 1, fileListTrain, fileNum, fileList.length - fileNum - 1);
                System.out.println("FileList: "+fileList.length+" FileListTrain: "+fileListTrain.length);
                System.out.println("FileListTrain: ");
                for(int i=0; i<fileListTrain.length; i++){
                    System.out.println(fileListTrain[i]);
                }
                TimeUnit.SECONDS.sleep(1);
                // String temp = fileListTrain[0];
                // fileListTrain[0] = fileListTrain[1];
                // fileListTrain[1] = temp;
                // System.out.println("Replaced FileListTrain: ");
                // for(int i=0; i<fileListTrain.length; i++){
                //     System.out.println(fileListTrain[i]);
                // }
                FeatureExtractor featureExtractorObject = CreateDSLObjects.create_feature_extractor(fileListTrain);
                System.out.println("Created FeatureExtractorObject");

                DSL_main dsl_obj = new DSL_main("/Users/rutujarane/Desktop/ISI/Semantics/dsl/"+modelFile,featureExtractorObject,true,true,false);

                // System.out.println("Writing object to file");
                // //Gson gson=new Gson();
                // GsonBuilder builder = new GsonBuilder().serializeSpecialFloatingPointValues();
                // builder.setPrettyPrinting(); 
                // Gson gson = builder.create(); 
                // System.out.println("creating json");
                // String  templateJson=gson.toJson(featureExtractorObject);

                // GsonBuilder builder = new GsonBuilder().serializeSpecialFloatingPointValues();  // STATIC|TRANSIENT in the default configuration
                // builder.setPrettyPrinting(); 
                // Gson gson = builder.create(); 
                // System.out.println("creating json");
                // String jsonString = gson.toJson(dsl_obj); 
                // System.out.println("created json");
                // FileWriter fw = new FileWriter("dslobject.json");
                // fw.write(jsonString);
                // fw.close();

                // FileOutputStream fos = new FileOutputStream("dslobject1");
                // ObjectOutputStream oos = new ObjectOutputStream(fos);
                // oos.writeObject(dsl_obj1);
                // oos.close();

                //HERE
                // PrintWriter out = new PrintWriter(new File("fe_model.txt"));
                // XStream xstream = new XStream(new StaxDriver());
                // System.out.println("XStream created");
                // xstream.toXML(dsl_obj, out);
                // System.out.println("Successfully Wrote object to file");
                
                // String xml = null;
                // try {
                //     xml = readFile("fe_model.txt", StandardCharsets.UTF_8);
                // } catch (IOException e) {
                //     e.printStackTrace();
                // }
                // DSL_main dsl_obj1 = (DSL_main)xstream.fromXML(xml);
                // FeatureExtractor featureExtractorObject1 = (FeatureExtractor)xstream.fromXML(xml);
                //TO HERE

                // DSL_main template = gson.fromJson(templateJson, DSL_main.class);

                // System.out.println("Reading object from file");
                // FileReader fr = new FileReader("dslobject.json");
                // Object jsonStringFromFile = fr.read();
                // fr.close();
                // DSL_main dsl_obj1 = (DSL_main) gson.fromJson(jsonString, DSL_main.class); 

                // FileInputStream fileIS = new FileInputStream("dslobject");
                // ObjectInputStream file = new ObjectInputStream(fileIS);
                // DSL_main dsl_obj3 = (DSL_main) (file).readObject();

                //JSONParser parser = new JSONParser();
                //Object obj = parser.parse(new FileReader("dslobject.json"));
                // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
                //JSONObject jsonObject = (JSONObject) obj;

                /*ObjectOutputStream oos = null;
                FileOutputStream fout = null;
                try{
                    fout = new FileOutputStream("stored_dsl_object", true);
                    oos = new ObjectOutputStream(fout);
                    oos.writeObject(dsl_obj);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if(oos != null){
                        oos.close();
                    } 
                }
                System.out.println("WROTE TO FILEEE");
                System.out.println("READING FROM FILE");
                DSL_main dsl_obj1 = null;
                ObjectInputStream objectinputstream = null;
                try {
                    FileInputStream streamIn = new FileInputStream("stored_dsl_object");
                    objectinputstream = new ObjectInputStream(streamIn);
                    dsl_obj1 = (DSL_main) objectinputstream.readObject();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(objectinputstream != null){
                        objectinputstream .close();
                    } 
                }*/
 
                // System.out.println("Successfully read object from file");

                //Test: System.out.println("Test FileName:"+fileNameDiffFolder);
                System.out.println("\n\n\n\n\n\nTest FileName:"+fileList[fileNum]);
                count++;
                ColumnBasedTable column_based_table_obj_pred = CreateDSLObjects.findDatatype(data,fileList[fileNum]); //For test table fileNameDiffFolder
                double total_inverse_rank = 0; //For each fold
                System.out.println("Done with object creation of test: "+column_based_table_obj_pred.columns.size());
                FileWriter myWriterr = new FileWriter("error_data.txt");
                myWriterr.write("Start!!!!!!\n");
                double ranks[] = new double[column_based_table_obj_pred.columns.size()];
                boolean writeFile = false;
                DSL_main dsl_obj1 = new DSL_main("/Users/rutujarane/Desktop/ISI/Semantics/dsl/"+modelFile,featureExtractorObject,true,true,false);
                for(int col=0; col<column_based_table_obj_pred.columns.size(); col++){
                    System.out.println("Predicting!");
                    List<SemTypePrediction> predictions = new ArrayList<SemTypePrediction>();
                    predictions =  dsl_obj.predictSemanticType(column_based_table_obj_pred.columns.get(col),100);

                    String column_type[] = data[1][col].trim().replaceAll("\"","").split("-");
                    System.out.println("Column_type="+column_type[0]+" "+column_type[column_type.length-1]);
                    String classID = column_type[0];
                    String predicate = column_type[column_type.length-1];

                    double rank = 1;
                    System.out.println("Actual:"+classID+" "+predicate);
                    myWriterr.write("\n\nActual:"+classID+" "+predicate+"\n");
                    if("hi".equals("hi"))
                        System.out.println("HIIIII .equals");
                    if("bye" == "bye")
                        System.out.println("Byeeeee ==");
                    System.out.println("Predictions size="+predictions.size());
                    for(int pred_ind=0; pred_ind<predictions.size(); pred_ind++){
                        System.out.println("Prediction:"+predictions.get(pred_ind).sem_type.classID+" "+predictions.get(pred_ind).sem_type.predicate+" "+predictions.get(pred_ind).prob);
                        myWriterr.write("Prediction:"+predictions.get(pred_ind).sem_type.classID+" "+predictions.get(pred_ind).sem_type.predicate+" "+predictions.get(pred_ind).prob+"\n");
                        if(predictions.get(pred_ind).sem_type.classID.equals(classID) && predictions.get(pred_ind).sem_type.predicate.equals(predicate)){
                            total_inverse_rank += (1/rank);
                            break;
                        }
                        else
                            rank += 1;
                    }
                    ranks[col] = rank;
                    System.out.println("Rank of this prediction:"+rank);
                    if(rank>=predictions.size()/2){
                        //Store the new column, semantic type and it's data.
                        System.out.println("Storing file");
                        writeFile = true;
                    }
                    else{
                        System.out.println("Not Storing file");
                    }
                    
                }
                System.out.println("Write file = "+writeFile);
                if(writeFile){
                    System.out.println("Writing file");
                    BufferedWriter br = new BufferedWriter(new FileWriter("/Users/rutujarane/Desktop/ISI/Semantics/dsl_copy/dsl/data/soccer2/myfile1.csv"));
                    // System.out.println("line:"+data[0]);
                    for(String line[]: data){
                        StringBuilder sb = new StringBuilder();
                        for (String element : line) {
                            sb.append(element);
                            sb.append(",");
                        }
                        System.out.println(sb.toString());
                        br.write(sb.toString());
                        br.write("\n");
                    }
                    br.close();
                }
                myWriterr.write("End");
                myWriterr.close();
                for(int kkk=0; kkk<ranks.length; kkk++)
                    System.out.println("Rank:"+ranks[kkk]);
                double mean_reciprocal_rank = total_inverse_rank / (double)column_based_table_obj_pred.columns.size();
                System.out.println("DONE with "+(int)fileNum+" FOLD(s)");
                System.out.println("MEAN RECIPROCAL RANK = "+mean_reciprocal_rank+" total="+total_inverse_rank+" columnL"+column_based_table_obj_pred.columns.size());
                //break;
                MRR += mean_reciprocal_rank;
                System.out.println("MRR: "+MRR);
                TimeUnit.SECONDS.sleep(2);
                break;
            }

        System.out.println("END!!!");
        System.out.println(count+"/"+fileList.length+" Folds done");
        System.out.println("Average MRR: "+MRR/12);
    
        }
        else{
            System.out.println("Error: Directory not found!");
        }
    }

    public static String readFile(String path, Charset encoding) throws IOException
	{
		List<String> lines = Files.readAllLines(Paths.get(path), encoding);
		return String.join(System.lineSeparator(), lines);
	}

}
