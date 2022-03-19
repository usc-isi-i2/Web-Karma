package com.mycompany.app;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mycompany.dsl.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.mycompany.dsl.featureextraction.columnbase.Textual;
import org.json.simple.parser.ParseException;


/**
 * This class is the main class for training and testing of the model.
 *
 * @author rutujarane, bdasbaksi
 * <p>
 * mvn clean install
 * mvn exec:java -Dexec.mainClass="com.mycompany.app.App"
 */

public class App {

    static Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {
        logger.setLevel(Level.INFO);
        logger.log(Level.INFO, "Starting App!");
        String dirPath = "/Users/bidishadasbaksi/Docs_no_icloud/GitHub/iswc-2016-semantic-labeling/data/datasets/soccer/data";
        String modelFile = "/Users/bidishadasbaksi/Docs_no_icloud/GitHub/new_model_Dec";
        String featureExtractorFile = "/Users/bidishadasbaksi/Docs_no_icloud/GitHub/tfidf_features.txt";
        String semanticLabelFile = "/Users/bidishadasbaksi/Docs_no_icloud/GitHub/iswc-2016-semantic-labeling/data/datasets/soccer/model";
        File maindir = new File(semanticLabelFile);
        HashMap<String, SemType> sem_col = new HashMap<>();
        logger.log(Level.INFO, "Starting Semantic Labeling!");
        if (maindir.exists() && maindir.isDirectory()) {
            File listOfFiles[] = maindir.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                String filename = listOfFiles[i].getAbsolutePath();
                JSONParser jsonParser = new JSONParser();
                try (FileReader reader = new FileReader(filename)) {
                    //Read JSON file
                    Object obj = jsonParser.parse(reader);
                    JSONObject jsonObject = (JSONObject) obj;
                    JSONObject graph = (JSONObject) jsonObject.get("graph");
                    JSONArray nodes = (JSONArray) graph.get("nodes");
                    Iterator<JSONObject> iterator = nodes.iterator();
                    while (iterator.hasNext()) {
                        JSONObject node = iterator.next();
                        String type = (String) node.get("type");
                        if (type.equals("ColumnNode")) {
                            String columnName = (String) node.get("columnName");
                            JSONArray userSemanticTypes = (JSONArray) node.get("userSemanticTypes");
                            JSONObject domainObj = (JSONObject) ((JSONObject) userSemanticTypes.get(0)).get("domain");
                            String domainName = (String) domainObj.get("uri");
                            JSONObject typeObj = (JSONObject) ((JSONObject) userSemanticTypes.get(0)).get("type");
                            String typeName = (String) typeObj.get("uri");
                            SemType semType = new SemType(domainName, typeName);
                            sem_col.put(columnName, semType);

                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }


        logger.log(Level.INFO, "Semantic Labeling Done!");


        maindir = new File(dirPath);
        //Create Semtype Dictionaries


        if (maindir.exists() && maindir.isDirectory()) {
            File listOfFiles[] = maindir.listFiles();
            String fileList[] = new String[listOfFiles.length];
            logger.log(Level.INFO, "Starting File Iteration!");
            for (int i = 0; i < listOfFiles.length; i++) {
                fileList[i] = listOfFiles[i].getAbsolutePath();
            }
            logger.log(Level.INFO, "File Iteration Done!");
            int count = 0;
            double MRR = 0;
            CreateDSLObjects.sem_col = sem_col;
            logger.log(Level.INFO, "Starting Feature Extraction!");

            int fileNum = fileList.length - 1;
            //fileNum=2;
            //int fileNum = ;
            //Delete the modelFile if it exists
            CreateDSLObjects.deleteFile(modelFile); // Remove this line

            String fileListTrain[] = new String[fileList.length - 1];
            System.arraycopy(fileList, 0, fileListTrain, 0, fileNum);
            System.arraycopy(fileList, fileNum+1, fileListTrain , fileNum, fileList.length-fileNum-1);

            System.out.println("FileList: " + fileList.length + " FileListTrain: " + fileListTrain.length);
            System.out.println("FileListTrain: ");
            for (int i = 0; i < fileListTrain.length; i++) {
                System.out.println(fileListTrain[i]);
            }
            TimeUnit.SECONDS.sleep(1);

            File f = new File(featureExtractorFile);
            FeatureExtractor featureExtractorObject = null;
            if (!f.exists() || true) {
                featureExtractorObject = CreateDSLObjects.create_feature_extractor(fileListTrain);
                System.out.println("Created FeatureExtractorObject");
                FileOutputStream fos = new FileOutputStream(featureExtractorFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(featureExtractorObject);
                oos.flush();
                oos.close();
            } else {
                FileInputStream fis = new FileInputStream(featureExtractorFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                featureExtractorObject = (FeatureExtractor) ois.readObject();
                Textual textual = new Textual();
                //featureExtractorObject.tfidfDB.pipeline = textual.get_pipeline();
                ois.close();
            }
            logger.log(Level.INFO, "Feature Extraction Done !");
            logger.log(Level.INFO, "Starting model train !");
            //Extract Tf-idf in a separate file - not needed mostly
//            HashMap<String, List<Double>> cache_col2tfidf = featureExtractorObject.tfidfDB.getCache_col2tfidf();
//            List<Column> trainCol = featureExtractorObject.getTrainColumns();

//            FileWriter writer = new FileWriter("/Users/bidishadasbaksi/Docs_no_icloud/GitHub/tfidf_list.txt");
//            for(int i=0;i<trainCol.size();i++)
//            {
//                Column c = trainCol.get(i);
//                List<Double> tf_vector = cache_col2tfidf.get(c.id);
//                writer.write(c.name+" : "+ tf_vector+System.lineSeparator());
//            }
//            writer.close();
            DSL_main dsl_obj = new DSL_main(modelFile, featureExtractorObject, true, true, false);
            logger.log(Level.INFO, "Model train done !");
            System.out.println("Test FileName:" + fileList[fileNum]);
            count++;
            String[][] data = CreateDSLObjects.readFile(fileList[fileNum]);
            ColumnBasedTable column_based_table_obj_pred = CreateDSLObjects.findDatatype(data, fileList[fileNum]); //For test table fileNameDiffFolder
            double total_inverse_rank = 0; //For each fold

            double ranks[] = new double[column_based_table_obj_pred.columns.size()];
            boolean writeFile = false;
            //DSL_main dsl_obj1 = new DSL_main(modelFile,featureExtractorObject,true,true,false);
            for (int col = 0; col < column_based_table_obj_pred.columns.size(); col++) {
                //System.out.println("Predicting!");
                List<SemTypePrediction> predictions = new ArrayList<SemTypePrediction>();
                predictions = dsl_obj.predictSemanticType(column_based_table_obj_pred.columns.get(col), 100);
                SemType semType = column_based_table_obj_pred.columns.get(col).semantic_type;
                String classID = semType.classID;
                String predicate = semType.predicate;

                double rank = 1;
                //System.out.println("Actual:" + classID + " " + predicate);
                //System.out.println("Predictions size=" + predictions.size());
                for (int pred_ind = 0; pred_ind < predictions.size(); pred_ind++) {
                    //System.out.println("Prediction:" + predictions.get(pred_ind).sem_type.classID + " " + predictions.get(pred_ind).sem_type.predicate + " " + predictions.get(pred_ind).prob);
                    if (predictions.get(pred_ind).sem_type.classID.equals(classID) && predictions.get(pred_ind).sem_type.predicate.equals(predicate)) {
                        total_inverse_rank += (1 / rank);
                        break;
                    } else
                        rank += 1;
                }
                ranks[col] = rank;
                //System.out.println("Rank of this prediction:" + rank);


            }
//            for (int kkk = 0; kkk < ranks.length; kkk++)
//                System.out.println("Rank:" + ranks[kkk]);
            double mean_reciprocal_rank = total_inverse_rank / (double) column_based_table_obj_pred.columns.size();
            //System.out.println("DONE with " + (int) fileNum + " FOLD(s)");
            System.out.println("MEAN RECIPROCAL RANK = " + mean_reciprocal_rank + " total=" + total_inverse_rank + " columnL " + column_based_table_obj_pred.columns.size());
            MRR += mean_reciprocal_rank;
            System.out.println("MRR: " + MRR);
            //TimeUnit.SECONDS.sleep(2);
            //  }

            System.out.println("END!!!");
            //System.out.println(count + "/" + fileList.length + " Folds done");
            //System.out.println("Average MRR: "+MRR/12);

        } else {
            System.out.println("Error: Directory not found!");
        }
    }


}
