package com.mycompany.app;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
// import java.lang.module.ModuleDescriptor.Modifier;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder; 

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
        logger.info( "Starting App!" );
        String dirPath = "/Users/rutujarane/Desktop/ISI/Semantics/dsl_copy/dsl/data/soccer2";
        String modelFile = "model_fileexe";

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
                logger.info(fileList[i].toString());
                logger.info(listOfFiles[i].toString());
            }

            int count=0;
            for(int fileNum=0; fileNum<fileList.length; fileNum++){

                //Read the csv file: prediction from another folder
                // logger.info("Reading: "+fileList[fileNum]);
                // String[][] data = CreateDSLObjects.readFile(fileList[fileNum]);
                String fileNameDiffFolder = "/Users/rutujarane/Desktop/ISI/Semantics/dsl_copy/dsl/data/city2/s3.csv";
                logger.info("Reading from another folder: "+fileNameDiffFolder);
                String[][] data = CreateDSLObjects.readFile(fileNameDiffFolder);

                //Delete the modelFile if it exists
                CreateDSLObjects.deleteFile(modelFile);

                // String fileListTrain[] = new String[fileList.length-1];
                String fileListTrain[] = fileList;
                // System.arraycopy(fileList, 0, fileListTrain, 0, fileNum);
                // logger.info(fileList.length);
                // System.arraycopy(fileList, fileNum + 1, fileListTrain, fileNum, fileList.length - fileNum - 1);
                logger.info("FileList: "+fileList.length+" FileListTrain: "+fileListTrain.length);
                logger.info("FileListTrain: ");
                for(int i=0; i<fileListTrain.length; i++){
                    logger.info(fileListTrain[i]);
                }
                FeatureExtractor featureExtractorObject = CreateDSLObjects.create_feature_extractor(fileListTrain);
                logger.info("Created FeatureExtractorObject");

                DSL_main dsl_obj1 = new DSL_main("/Users/rutujarane/Desktop/ISI/Semantics/dsl/"+modelFile,featureExtractorObject,true,true,false);

                logger.info("Writing object to file");
                GsonBuilder builder = new GsonBuilder().serializeSpecialFloatingPointValues();  // STATIC|TRANSIENT in the default configuration
                builder.setPrettyPrinting(); 
                Gson gson = builder.create(); 
                String jsonString = gson.toJson(dsl_obj1); 
                FileWriter fw = new FileWriter("dslobject");
                fw.write(jsonString);
                fw.close();
                // FileOutputStream fos = new FileOutputStream("dslobject");
                // ObjectOutputStream oos = new ObjectOutputStream(fos);
                // oos.writeObject(dsl_obj1);
                // oos.close();
                logger.info("Successfully Wrote object to file");

                logger.info("Reading object from file");
                // FileReader fr = new FileReader("dslobject");
                // String jsonStringFromFile = fr.read();
                // fr.close();
                // DSL_main dsl_obj1 = gson.fromJson(jsonString, DSL_main.class); 
                // FileInputStream fileIS = new FileInputStream("dslobject");
                // ObjectInputStream file = new ObjectInputStream(fileIS);
                // DSL_main dsl_obj1 = (DSL_main) (file).readObject();
                logger.info("Successfully read object from file");

                logger.info("Test FileName:"+fileNameDiffFolder);
                count++;
                ColumnBasedTable column_based_table_obj_pred = CreateDSLObjects.findDatatype(data,fileNameDiffFolder); //For test table
                double total_inverse_rank = 0; //For each fold
                FileWriter myWriterr = new FileWriter("error_data.txt");
                myWriterr.write("Start!!!!!!\n");
                double ranks[] = new double[column_based_table_obj_pred.columns.size()];
                boolean writeFile = false;
                for(int col=0; col<column_based_table_obj_pred.columns.size(); col++){
                    List<SemTypePrediction> predictions = new ArrayList<SemTypePrediction>();
                    predictions = dsl_obj1.predictSemanticType(column_based_table_obj_pred.columns.get(col),100);

                    String column_type[] = data[1][col].split("-");
                    logger.info("Column_type="+column_type[0]+" "+column_type[column_type.length-1]);
                    String classID = column_type[0];
                    String predicate = column_type[column_type.length-1];

                    double rank = 1;
                    logger.info("Actual:"+classID+" "+predicate);
                    myWriterr.write("\n\nActual:"+classID+" "+predicate+"\n");
                    if("hi".equals("hi"))
                        logger.info("HIIIII .equals");
                    if("bye" == "bye")
                        logger.info("Byeeeee ==");
                    logger.info("Predictions size="+predictions.size());
                    for(int pred_ind=0; pred_ind<predictions.size(); pred_ind++){
                        logger.info("Prediction:"+predictions.get(pred_ind).sem_type.classID+" "+predictions.get(pred_ind).sem_type.predicate+" "+predictions.get(pred_ind).prob);
                        myWriterr.write("Prediction:"+predictions.get(pred_ind).sem_type.classID+" "+predictions.get(pred_ind).sem_type.predicate+" "+predictions.get(pred_ind).prob+"\n");
                        if(predictions.get(pred_ind).sem_type.classID.equals(classID) && predictions.get(pred_ind).sem_type.predicate.equals(predicate)){
                            total_inverse_rank += (1/rank);
                            break;
                        }
                        else
                            rank += 1;
                    }
                    ranks[col] = rank;
                    logger.info("Rank of this prediction:"+rank);
                    if(rank>=predictions.size()){
                        //Store the new column, semantic type and it's data.
                        writeFile = true;
                    }
                    
                }

                if(writeFile){
                    logger.info("Writing file");
                    BufferedWriter br = new BufferedWriter(new FileWriter("/Users/rutujarane/Desktop/ISI/Semantics/dsl_copy/dsl/data/soccer2/myfile1.csv"));
                    // logger.info("line:"+data[0]);
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
                myWriterr.write("End");
                myWriterr.close();
                for(int kkk=0; kkk<ranks.length; kkk++)
                    logger.info("Rank:"+ranks[kkk]);
                double mean_reciprocal_rank = total_inverse_rank / (double)column_based_table_obj_pred.columns.size();
                logger.info("DONE with "+fileNum+1+" FOLD(s)");
                logger.info("MEAN RECIPROCAL RANK = "+mean_reciprocal_rank+" total="+total_inverse_rank+" columnL"+column_based_table_obj_pred.columns.size());
                break;
            }

        logger.info("END!!!");
        logger.info(count+"/"+fileList.length+" Folds done");
    
        }
        else{
            logger.info("Error: Directory not found!");
        }
    }
}
