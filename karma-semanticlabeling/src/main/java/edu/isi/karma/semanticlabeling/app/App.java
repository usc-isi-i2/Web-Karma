package edu.isi.karma.semanticlabeling.app;

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

import edu.isi.karma.semanticlabeling.app.CreateDSLObjects;
import edu.isi.karma.semanticlabeling.dsl.FeatureExtractor;
import edu.isi.karma.semanticlabeling.dsl.SemTypePrediction;
import edu.isi.karma.semanticlabeling.dsl.DSL_main;
import edu.isi.karma.semanticlabeling.dsl.ColumnBasedTable;



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
        logger.info( "Starting App!" );
        String dirPath = "/Users/rutujarane/Desktop/ISI/Semantics/dsl/data/soccer2";
        String modelFile = "model_file_rf";

        File maindir = new File(dirPath);
        if(maindir.exists() && maindir.isDirectory()){
            // Read all the files in the directory
            File listOfFiles[] = maindir.listFiles();
            
            String fileList[] = new String[listOfFiles.length];
            for(int i=0; i<listOfFiles.length; i++){
                fileList[i] = listOfFiles[i].getAbsolutePath();
                logger.info(fileList[i].toString());
            }

            int count=0;
            double MRR = 0;
            for(int fileNum=0; fileNum<fileList.length; fileNum++){

                //Read the csv file: prediction
                fileNum++;
                logger.info("Reading: "+fileList[fileNum]);
                String[][] data = CreateDSLObjects.readFile(fileList[fileNum]);

                //Delete the modelFile if it exists. Do this only if you are testing cross validation. Do not delete model file in real time application.
                CreateDSLObjects.deleteFile(modelFile);

                String fileListTrain[] = new String[fileList.length-1];
                //Test: String fileListTrain[] = fileList;
                // Trying to test on different files within folder.
                System.arraycopy(fileList, 0, fileListTrain, 0, fileNum);
                logger.info(fileList.length);
                System.arraycopy(fileList, fileNum + 1, fileListTrain, fileNum, fileList.length - fileNum - 1);
                logger.info("FileList: "+fileList.length+" FileListTrain: "+fileListTrain.length);
                logger.info("FileListTrain: ");
                for(int i=0; i<fileListTrain.length; i++){
                    logger.info(fileListTrain[i]);
                }
                // TimeUnit.SECONDS.sleep(1);
                
                FeatureExtractor featureExtractorObject = CreateDSLObjects.create_feature_extractor(fileListTrain);
                logger.info("Created FeatureExtractorObject");

                DSL_main dsl_obj = new DSL_main("/Users/rutujarane/Desktop/ISI/Semantics/dsl/"+modelFile,featureExtractorObject,true,true,false);

                // logger.info("Writing object to file");
                // To write the feature Extractor object, I tried Gson and File output stream and Xtream xml.
                // Need to write out the feature extractor object to file in order to reduce time in real time.

                logger.info("\nTest FileName:"+fileList[fileNum]);
                count++;
                // Test table columns:
                ColumnBasedTable column_based_table_obj_pred = CreateDSLObjects.findDatatype(data,fileList[fileNum]); 
                double total_inverse_rank = 0; //For each fold
                logger.info("Done with object creation of test: "+column_based_table_obj_pred.columns.size());
                double ranks[] = new double[column_based_table_obj_pred.columns.size()];
                boolean writeFile = false;
                // Here the written featureExtractorObject is to be read and new DSL object is to be created. For saving time in cross validation, this is directly done here without i/o.
                DSL_main dsl_obj1 = new DSL_main("/Users/rutujarane/Desktop/ISI/Semantics/dsl/"+modelFile,featureExtractorObject,true,true,false);
                for(int col=0; col<column_based_table_obj_pred.columns.size(); col++){
                    logger.info("Predicting!");
                    List<SemTypePrediction> predictions = new ArrayList<SemTypePrediction>();
                    predictions =  dsl_obj.predictSemanticType(column_based_table_obj_pred.columns.get(col),100);

                    String column_type[] = data[1][col].trim().replaceAll("\"","").split("-");
                    logger.info("Column_type="+column_type[0]+" "+column_type[column_type.length-1]);
                    String classID = column_type[0];
                    String predicate = column_type[column_type.length-1];

                    double rank = 1;
                    logger.info("Actual:"+classID+" "+predicate);
                    logger.info("Predictions size="+predictions.size());
                    for(int pred_ind=0; pred_ind<predictions.size(); pred_ind++){
                        logger.info("Prediction:"+predictions.get(pred_ind).sem_type.classID+" "+predictions.get(pred_ind).sem_type.predicate+" "+predictions.get(pred_ind).prob);
                        if(predictions.get(pred_ind).sem_type.classID.equals(classID) && predictions.get(pred_ind).sem_type.predicate.equals(predicate)){
                            total_inverse_rank += (1/rank);
                            break;
                        }
                        else
                            rank += 1;
                    }
                    ranks[col] = rank;
                    logger.info("Rank of this prediction:"+rank);
                    if(rank>=predictions.size()/2){
                        //Store the new column, semantic type and it's data.
                        logger.info("Storing file");
                        writeFile = true;
                    }
                    else{
                        logger.info("Not Storing file");
                    }
                    
                }
                logger.info("Write file = "+writeFile);
                // Storing the new column that we could not predict in our data.
                if(writeFile){
                    logger.info("Writing file");
                    BufferedWriter br = new BufferedWriter(new FileWriter("/Users/rutujarane/Desktop/ISI/Semantics/dsl_copy/dsl/data/soccer2/myfile1.csv"));
                    for(String line[]: data){
                        StringBuilder sb = new StringBuilder();
                        for (String element : line) {
                            sb.append(element);
                            sb.append(",");
                        }
                        logger.info(sb.toString());
                        br.write(sb.toString());
                        br.write("\n");
                    }
                    br.close();
                }
                // A check needs to be added if the data exceeds the available amount of memory.
                
                for(int kkk=0; kkk<ranks.length; kkk++)
                    logger.info("Rank:"+ranks[kkk]);
                double mean_reciprocal_rank = total_inverse_rank / (double)column_based_table_obj_pred.columns.size();
                logger.info("DONE with "+(int)fileNum+" FOLD(s)");
                // logger.info("MEAN RECIPROCAL RANK = "+mean_reciprocal_rank+" total="+total_inverse_rank+" columnL"+column_based_table_obj_pred.columns.size());
                MRR += mean_reciprocal_rank;
                logger.info("MRR: "+MRR);
                TimeUnit.SECONDS.sleep(2);
                // break;
            }

        logger.info("END!!!");
        logger.info(count+"/"+fileList.length+" Folds done");
        logger.info("Average MRR: "+MRR/12);
    
        }
        else{
            logger.info("Error: Directory not found!");
        }
    }

    public static String readFile(String path, Charset encoding) throws IOException
	{
		List<String> lines = Files.readAllLines(Paths.get(path), encoding);
		return String.join(System.lineSeparator(), lines);
	}

}
