package com.mycompany.dsl;
// package edu.isi.karma.semanticlabeling.dsl;

import java.io.*;
import java.util.*;
import java.lang.*;

import org.apache.commons.lang3.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.Instance;
import weka.classifiers.evaluation.Prediction;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class is responsible for loading and training of the model as well as prediction of semantic labels for new columns.
 * @author rutujarane
 */


public class DSL_main implements Serializable{


    static Logger logger = LogManager.getLogger(DSL_main.class.getName());
    File modelFile;
    FeatureExtractor featureExtractorObject;
    boolean loadModel;
    boolean autoTrain;
    boolean allowOneClass;
    RandomForest model;
    // GenerateTrainingData generateTrainingData;

    public DSL_main(String modelFile, FeatureExtractor featureExtractorObject, boolean loadTheModel, boolean autoTrain, boolean allowOneClass) throws Exception{
        this.featureExtractorObject = featureExtractorObject;
        this.modelFile = new File(modelFile);
        if(!this.modelFile.exists())
            this.modelFile = null;
        else
        {
            FileInputStream fi = new FileInputStream(this.modelFile);
            ObjectInputStream oi = new ObjectInputStream(fi);
            RandomForest rf_model = (RandomForest) oi.readObject();
            this.model = rf_model;
            oi.close();
            fi.close();

        }
        // this.model = new RandomForest();

        if(loadTheModel)
            this.loadModel(autoTrain, allowOneClass, modelFile);

    }

    public void loadModel(boolean autoTrain, boolean allowOneClass, String modelFile) throws Exception{
        logger.info("in load Model");
        if(this.model != null)
            return;

        if(this.modelFile!=null){
            System.out.println("Load previous trained model...");
            FileInputStream fi = new FileInputStream(this.modelFile);
            ObjectInputStream oi = new ObjectInputStream(fi);
            RandomForest rf_model = (RandomForest) oi.readObject();
            oi.close();
            fi.close();

            // with open(str(self.model_file), "rb") as f:
            //     self.model = pickle.load(f)
        }  
        else{
            System.out.println("Cannot load model... Training.");
            if(autoTrain){
                this.trainModel(allowOneClass, modelFile);
            }
            else{
                System.out.println("Exception: Model doesn't exist... ");
            }
        }

    }

    public void trainModel(boolean allowOneClass, String modelFile) throws Exception{
        System.out.println("Train model...");
        // clf = RandomForestClassifier(n_estimators=200, max_depth=10, class_weight="balanced", random_state=120)

        logger.info("Calling generate training data");
        GenerateTrainingData generateTrainingData = new GenerateTrainingData();
        generateTrainingData.generateTrainingDataForMain(this.featureExtractorObject);
        logger.info("Returned from generate training data:"+generateTrainingData.XTrain+"  "+generateTrainingData.YTrain);
        //print features
        FileWriter writer = new FileWriter("/Users/bidishadasbaksi/Docs_no_icloud/GitHub/feature_java.txt");
        for(int i=0;i<generateTrainingData.XTrain.size();i++)
        {
            writer.write(generateTrainingData.XTrain.get(i)+" : "+generateTrainingData.YTrain.get(i)+System.lineSeparator());
        }
        writer.close();
        //Delete the code below
        ArrayList<Integer> checklist = new ArrayList<>();
        for(int i =0;i<generateTrainingData.YTrain.size();i++)
        {
            if(generateTrainingData.YTrain.get(i) == 1)
            {
                checklist.add(i);
            }
        }
        //Delete till here
        if(!allowOneClass){
            Set<Integer> yTrainSet = new HashSet<Integer>();
            yTrainSet.addAll(generateTrainingData.YTrain);
            if(yTrainSet.size() <= 1)
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

            for(int i=0; i<generateTrainingData.XTrain.size(); i++){
                line = "";
                if(i>0)
                    line += "\n";
                for(int j=0; j<generateTrainingData.XTrain.get(i).size(); j++){
                    line += generateTrainingData.XTrain.get(i).get(j)+",";
                }
                line += generateTrainingData.YTrain.get(i);
                // logger.info("L:"+line);
                myWriter.write(line);
            }
            // myWriter.write("End!");
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
        this.modelFile = new File(modelFile);
        FileOutputStream f = new FileOutputStream(this.modelFile);
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(this.model);
        o.close();
        f.close();

        System.out.println("Saved model...");
        
        // this.model = rfa;
        // rfa.testModel("libsvm_test.arff", "randomForestModel");
        logger.info("Got this.model");

    }

    public List<SemTypePrediction> predictSemanticType(Column col, int topN) throws Exception{

        // logger.info("In predictSemanticType");
        List<List<Double>> X = new ArrayList<List<Double>>();
        X = this.featureExtractorObject.computeFeatureVectors(col);
        HashMap<Integer,Integer> dict_c = new HashMap<Integer,Integer>();
        dict_c.put(0,0);
        dict_c.put(1,0);
        dict_c.put(2,0);
        dict_c.put(3,0);
        dict_c.put(4,0);
        dict_c.put(5,0);
        dict_c.put(6,0);
        for(int i=0;i<X.size();i++)
        {
            for(int j=0;j<=6;j++)
            {
                if(X.get(i).get(j)>0)
                {
                    dict_c.put(j,dict_c.get(j)+1);
                }
            }
        }
        //System.out.println("Computed X:"+X.size()+" X: "+X);
        GenerateTrainingData generateTrainingData = new GenerateTrainingData();
        generateTrainingData.generateTrainingDataForTest(this.featureExtractorObject, X);
        writeToFilePredict(generateTrainingData);
        // logger.info("Wrote to file");

        
        RandomForestAlgorithm_ rfa = new RandomForestAlgorithm_();
        Instances test_data_instance = rfa.getDataSet("test_data.arff");
        rfa.eval.evaluateModel(this.model, test_data_instance);

        ArrayList<Prediction> predictionsf = rfa.eval.predictions();
//        for (int i = 0; i < predictionsf.size(); i++) {
//	        //Determine fraud class probability
//            // double classProbability = predictionsf.get(i).distribution()[(int) predictionsf.get(i).predicted()];
//            double classProbability = predictionsf.get(i).weight();
//            System.out.println("Class prob:"+classProbability);
//        }
        test_data_instance.setClassIndex(test_data_instance.numAttributes() - 1); 
        // logger.info("Finding result");
        List<List<Double>> res = new ArrayList<List<Double>>();
        Enumeration<Instance> test_instances = test_data_instance.enumerateInstances();
        while(test_instances.hasMoreElements()){
            Instance test_ins = test_instances.nextElement();
            //System.out.println("test_instance:"+test_ins);
            double result[] = this.model.distributionForInstance(test_ins);
            res.add(Arrays.asList(ArrayUtils.toObject(result)));
        }
        logger.info("Found result");
//        for(int i=0; i<res.size(); i++)
//            System.out.println("r="+res.get(i));

        if(res.get(0).size()==1)
            //have only one class, which mean that it is always false
            return new ArrayList<SemTypePrediction>();
        
        List<Double> result_col = new ArrayList<Double>();
        for(List<Double> result:res)
            result_col.add(result.get(1));
        //System.out.println("Result col:"+result_col);

        double result_enum[][] = new double[result_col.size()][2];
        for(int i=0; i<result_col.size(); i++){
            result_enum[i][0] = (double)i;
            result_enum[i][1] = result_col.get(i);
        }
//        System.out.println("Result enum:"+result_enum);
//        for(int i=0; i<result_enum.length; i++){
//            System.out.println(result_enum[i][0]+" "+result_enum[i][1]);
//        }
        
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
//        System.out.println("Result sorted:"+result_enum);
//        for(int i=0; i<result_enum.length; i++){
//            System.out.println(result_enum[i][0]+" "+result_enum[i][1]);
//        }
        

        List<SemTypePrediction> predictions = new ArrayList<SemTypePrediction>();
        List<String> existing_stypes = new ArrayList<String>(); // make it dictionary
        int i = result_enum.length-1;
        while(predictions.size() < topN && i > -1){
            int col_i = (int)result_enum[i][0];
            double prob = result_enum[i][1];
            i--;
            //System.out.println("COL,PROB:"+col_i+" "+prob);

            //this column is in training data, so we have to remove it out
            if(this.featureExtractorObject.column2idx.containsKey(col.id) && this.featureExtractorObject.column2idx.get(col.id) == col_i)
                continue;

            SemType pred_stype = this.featureExtractorObject.trainColumns.get(col_i).semantic_type;
            if(existing_stypes.contains(pred_stype.classID + " " + pred_stype.predicate))
                continue;
            
            existing_stypes.add(pred_stype.classID + " " + pred_stype.predicate);
            predictions.add(new SemTypePrediction(pred_stype, prob));
            //System.out.println("My predications:"+pred_stype.classID+" "+pred_stype.predicate+" "+prob);
        }

        return predictions;
    }

    public void writeToFilePredict(GenerateTrainingData generateTrainingData){
        System.out.println("Creating test data file");

        try {
            FileWriter myWriter = new FileWriter("test_data.arff");
            String line = "% 1. Title: Semantic Typing Database\n % 2. Sources:\n %   (a) Creator: Rutuja Rane\n %   (B) Date: June, 2020\n";
            myWriter.write(line);
            myWriter.write("@RELATION semantic_label\n");
            myWriter.write("@ATTRIBUTE col_jaccard  NUMERIC\n@ATTRIBUTE col_jaccard2  NUMERIC\n@ATTRIBUTE num_ks  NUMERIC\n@ATTRIBUTE num_mann  NUMERIC\n@ATTRIBUTE num_jaccard   NUMERIC\n@ATTRIBUTE text_jaccard  NUMERIC\n@ATTRIBUTE text_cosine   NUMERIC\n@ATTRIBUTE class        {0,1}\n");
            myWriter.write("@DATA\n");

            line = "";
            for(int i=0; i<generateTrainingData.XTest.size(); i++){
                line = "";
                if(i>0)
                    line += "\n";
                for(int j=0; j<generateTrainingData.XTest.get(i).size(); j++){
                    line += generateTrainingData.XTest.get(i).get(j)+",";
                }
                line += generateTrainingData.YTest.get(i);
                // logger.info("L:"+line);
                myWriter.write(line);
            }
            // myWriter.write("End!");
            myWriter.close();
            System.out.println("Successfully wrote to the test file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return;
    }
}