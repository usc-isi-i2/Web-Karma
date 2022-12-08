package edu.isi.karma.semanticlabeling.app;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.isi.karma.semanticlabeling.dsl.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * This class is the main class for training and testing of the model.
 *
 * @author rutujarane, Bidisha Das Baksi (bidisha.bksh@gmail.com)
 * <p>
 * mvn clean install
 * mvn exec:java -Dexec.mainClass="com.mycompany.app.App"
 */

public class App implements Serializable{

    static Logger logger = Logger.getLogger(App.class.getName());
    String dataFolder = "train/data";
    String modelFilename = "train/random_forest_model";
    String semanticLabelDir = "train/model";

    public List<File> getAllFilesFromResource(String folder)
            throws URISyntaxException, IOException {

        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(folder);

        List<File> collect = Files.walk(Paths.get(resource.toURI()))
                .filter(Files::isRegularFile)
                .map(x -> x.toFile())
                .collect(Collectors.toList());

        return collect;
    }

    public HashMap<String, SemType> getSemantics(String semanticLabelDir) throws URISyntaxException, IOException {

        logger.log(Level.INFO, "Starting Semantic Labeling!");
        HashMap<String, SemType> sem_col = new HashMap<>();
        List<File> model_files = getAllFilesFromResource(semanticLabelDir);
        for (int i = 0; i < model_files.size(); i++) {
            JSONParser jsonParser = new JSONParser();
            try (FileReader reader = new FileReader(model_files.get(i))) {
                //Read JSON file
                JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
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
        logger.log(Level.INFO, "Semantic Labeling Done!");
        return sem_col;

    }

    public String[] getFileAbsolutePath(List<File> data_files) {
        String fileList[] = new String[data_files.size()];
        logger.log(Level.INFO, "Starting File Iteration!");
        for (int i = 0; i < data_files.size(); i++) {
            fileList[i] = data_files.get(i).getAbsolutePath();
        }
        logger.log(Level.INFO, "File Iteration Done!");
        return fileList;
    }

    public void testModel(String[][] data,String fileName,DSL_main dsl_obj) throws Exception {
        ColumnBasedTable column_based_table_obj_pred = CreateDSLObjects.findDatatype(data,fileName);
        double total_inverse_rank = 0;

        double ranks[] = new double[column_based_table_obj_pred.columns.size()];
        for (int col = 0; col < column_based_table_obj_pred.columns.size(); col++) {
            List<SemTypePrediction> predictions = new ArrayList<SemTypePrediction>();
            predictions = dsl_obj.predictSemanticType(column_based_table_obj_pred.columns.get(col), 100);
            SemType semType = column_based_table_obj_pred.columns.get(col).semantic_type;
            String classID = semType.classID;
            String predicate = semType.predicate;
            double rank = 1;
            for (int pred_ind = 0; pred_ind < predictions.size(); pred_ind++) {
                if (predictions.get(pred_ind).sem_type.classID.equals(classID) && predictions.get(pred_ind).sem_type.predicate.equals(predicate)) {
                    total_inverse_rank += (1 / rank);
                    break;
                } else
                    rank += 1;
            }
            ranks[col] = rank;


        }

        double mean_reciprocal_rank = total_inverse_rank / (double) column_based_table_obj_pred.columns.size();
        logger.log(Level.INFO,"MEAN RECIPROCAL RANK = " + mean_reciprocal_rank + " total=" + total_inverse_rank + " columnL " + column_based_table_obj_pred.columns.size());

    }

    public static void main(String[] args) throws Exception {

        logger.setLevel(Level.INFO);
        logger.log(Level.INFO, "Starting App!");
        App app = new App();
        HashMap<String, SemType> semCol = app.getSemantics(app.semanticLabelDir);
        List<File> dataFiles = app.getAllFilesFromResource(app.dataFolder);
        String fileList[] = app.getFileAbsolutePath(dataFiles);
        CreateDSLObjects.sem_col = semCol;
        logger.log(Level.INFO, "Starting Feature Extraction!");
        int fileNum = fileList.length - 1;
        String fileListTrain[] = new String[fileList.length - 1];
        System.arraycopy(fileList, 0, fileListTrain, 0, fileNum);
        System.arraycopy(fileList, fileNum + 1, fileListTrain, fileNum, fileList.length - fileNum - 1);
        FeatureExtractor featureExtractorObject = CreateDSLObjects.create_feature_extractor(fileListTrain);
        logger.log(Level.INFO, "Feature Extraction Done ! \n Starting model train !");
        DSL_main dsl_obj = new DSL_main(app.modelFilename, featureExtractorObject, true, true, false); // To re-train the model pass the value of load the model as false.
        logger.log(Level.INFO, "Model train done !");
        logger.log(Level.INFO, "Test FileName:" + fileList[fileNum]);
        String[][] data = CreateDSLObjects.readFile(fileList[fileNum]);
        app.testModel(data,fileList[fileNum],dsl_obj);
        logger.log(Level.INFO, "END!!!");


    }
}

