package edu.isi.karma.semanticlabeling.dsl;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.lang.*;

import org.apache.commons.lang3.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import weka.classifiers.trees.RandomForest;
import weka.core.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class is responsible for loading and training of the model as well as prediction of semantic labels for new columns.
 *
 * @author rutujarane, Bidisha Das Baksi (bidisha.bksh@gmail.com)
 */


public class DSL_main implements Serializable {


    static Logger logger = LogManager.getLogger(DSL_main.class.getName());
    File modelFile;
    FeatureExtractor featureExtractorObject;
    RandomForest model;

    public InputStream getModelFromResource() {

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("train/random_forest_model");
            if (inputStream == null) {
                throw new IllegalArgumentException("Model not found! ");
            } else {
                return inputStream;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public DSL_main(String modelFile, FeatureExtractor featureExtractorObject, boolean loadTheModel, boolean autoTrain, boolean allowOneClass) throws Exception {
        this.featureExtractorObject = featureExtractorObject;
        InputStream inputStream = getModelFromResource();
        if (inputStream != null && loadTheModel) {
            ObjectInputStream oi = new ObjectInputStream(inputStream);
            RandomForest rf_model = (RandomForest) oi.readObject();
            this.model = rf_model;
            oi.close();
        } else
            this.loadModel(autoTrain, allowOneClass, modelFile);
    }

    public void loadModel(boolean autoTrain, boolean allowOneClass, String modelFile) throws Exception {
        logger.info("in load Model");
        if (this.model != null)
            return;

        if (autoTrain) {
            this.trainModel(allowOneClass, modelFile);
        } else {
            System.out.println("Exception: Model doesn't exist... ");
        }

    }

    public void trainModel(boolean allowOneClass, String modelFile) throws Exception {
        System.out.println("Train model...");
        logger.info("Calling generate training data");
        GenerateTrainingData generateTrainingData = new GenerateTrainingData();
        generateTrainingData.generateTrainingDataForMain(this.featureExtractorObject);
        logger.info("Returned from generate training data:" + generateTrainingData.XTrain + "  " + generateTrainingData.YTrain);
        if (!allowOneClass) {
            Set<Integer> yTrainSet = new HashSet<Integer>();
            yTrainSet.addAll(generateTrainingData.YTrain);
            if (yTrainSet.size() <= 1)
                logger.info("Training data must have more than one semantic type!");
        }

        logger.info("Creating arff file");

        try {
            FileWriter myWriter = new FileWriter("train_data_set.arff");

            String line = "% 1. Title: Semantic Typing Database\n % 2. Sources:\n %   (a) Creator: Rutuja Rane\n %   (B) Date: June, 2020\n";
            myWriter.write(line);
            myWriter.write("@RELATION semantic_label\n");
            myWriter.write("@ATTRIBUTE col_jaccard  NUMERIC\n@ATTRIBUTE col_jaccard2  NUMERIC\n@ATTRIBUTE num_ks  NUMERIC\n@ATTRIBUTE num_mann  NUMERIC\n@ATTRIBUTE num_jaccard   NUMERIC\n@ATTRIBUTE text_jaccard  NUMERIC\n@ATTRIBUTE text_cosine   NUMERIC\n@ATTRIBUTE class        {0,1}\n");
            myWriter.write("@DATA\n");

            for (int i = 0; i < generateTrainingData.XTrain.size(); i++) {
                line = "";
                if (i > 0)
                    line += "\n";
                for (int j = 0; j < generateTrainingData.XTrain.get(i).size(); j++) {
                    line += generateTrainingData.XTrain.get(i).get(j) + ",";
                }
                line += generateTrainingData.YTrain.get(i);
                myWriter.write(line);
            }
            myWriter.close();
            logger.info("Successfully wrote to the training file.");
        } catch (IOException e) {
            logger.info("An error occurred.");
            e.printStackTrace();
        }
        logger.info("Starting to fit the model");
        RandomForestAlgorithm_ rfa = new RandomForestAlgorithm_();
        this.model = rfa.RandomForestAlgorithm_create();
        System.out.println("Trained the model... Writing to file.");
        String modelFilePath = Paths.get("karma-semanticlabeling/src/main/resources/train/random_forest_model").toAbsolutePath().toString();
        this.modelFile = new File(modelFilePath);
        FileOutputStream f = new FileOutputStream(this.modelFile);
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(this.model);
        o.close();
        f.close();
        System.out.println("Saved model...");
        logger.info("Got this.model");

    }

    public List<SemTypePrediction> predictSemanticType(Column col, int topN) throws Exception {

        List<List<Double>> X = this.featureExtractorObject.computeFeatureVectors(col);
        GenerateTrainingData generateTrainingData = new GenerateTrainingData();
        generateTrainingData.generateTrainingDataForTest(this.featureExtractorObject, X);
        Instances test_data_instance = createInstance(generateTrainingData);
        test_data_instance.setClassIndex(test_data_instance.numAttributes() - 1);
        List<List<Double>> res = new ArrayList<List<Double>>();
        Enumeration<Instance> test_instances = test_data_instance.enumerateInstances();
        while (test_instances.hasMoreElements()) {
            Instance test_ins = test_instances.nextElement();
            double result[] = this.model.distributionForInstance(test_ins);
            res.add(Arrays.asList(ArrayUtils.toObject(result)));
        }
        logger.info("Found result");
        if (res.get(0).size() == 1)
            //have only one class, which mean that it is always false
            return new ArrayList<SemTypePrediction>();

        List<Double> result_col = new ArrayList<Double>();
        for (List<Double> result : res)
            result_col.add(result.get(1));

        double result_enum[][] = new double[result_col.size()][2];
        for (int i = 0; i < result_col.size(); i++) {
            result_enum[i][0] = (double) i;
            result_enum[i][1] = result_col.get(i);
        }


        // sort the array on item id(first column)
        Arrays.sort(result_enum, new Comparator<double[]>() {
            @Override
            //arguments to this method represent the arrays to be sorted
            public int compare(double[] o1, double[] o2) {
                //get the item ids which are at index 0 of the array
                Double itemIdOne = o1[1];
                Double itemIdTwo = o2[1];
                // sort on item id
                return itemIdOne.compareTo(itemIdTwo);
            }
        });


        List<SemTypePrediction> predictions = new ArrayList<SemTypePrediction>();
        List<String> existing_stypes = new ArrayList<String>(); // make it dictionary
        int i = result_enum.length - 1;
        while (predictions.size() < topN && i > -1) {
            int col_i = (int) result_enum[i][0];
            double prob = result_enum[i][1];
            i--;
            //this column is in training data, so we have to remove it out
            if (this.featureExtractorObject.column2idx.containsKey(col.id) && this.featureExtractorObject.column2idx.get(col.id) == col_i)
                continue;

            SemType pred_stype = this.featureExtractorObject.trainColumns.get(col_i).semantic_type;
            if (existing_stypes.contains(pred_stype.classID + " " + pred_stype.predicate))
                continue;

            existing_stypes.add(pred_stype.classID + " " + pred_stype.predicate);
            predictions.add(new SemTypePrediction(pred_stype, prob));
        }

        return predictions;
    }

    private Instances createInstance(GenerateTrainingData generateTrainingData) {
        String colNames[] = {"col_jaccard", "col_jaccard2", "num_ks", "num_mann", "num_jaccard", "text_jaccard", "text_cosine"};
        ArrayList<Attribute> attributeArrayList = new ArrayList<>();
        for (int i = 0; i < colNames.length; i++) {
            attributeArrayList.add(new Attribute(colNames[i]));
        }
        List classValues = new ArrayList(2);
        classValues.add("0");
        classValues.add("1");
        attributeArrayList.add(new Attribute("class", classValues));
        Instances mainInstance = new Instances("semantic_label", attributeArrayList, 0);
        for (int i = 0; i < generateTrainingData.XTest.size(); i++) {
            Instance inst = new DenseInstance(generateTrainingData.XTest.get(i).size() + 1);
            for (int j = 0; j < generateTrainingData.XTest.get(i).size(); j++) {
                inst.setValue(attributeArrayList.get(j), generateTrainingData.XTest.get(i).get(j));
            }
            Attribute classAtt = attributeArrayList.get(attributeArrayList.size() - 1);
            inst.setValue(classAtt, generateTrainingData.YTest.get(i));
            mainInstance.add(inst);
        }
        mainInstance.setClassIndex(mainInstance.numAttributes() - 1);
        return mainInstance;
    }
}