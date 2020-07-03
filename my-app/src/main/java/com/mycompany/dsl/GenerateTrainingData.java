package com.mycompany.dsl;

import java.util.*;
import java.io.*;

import org.apache.commons.lang3.tuple.Pair;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class is responsible for generating the training data given the columns - by similarity.
 * @author rutujarane
 */

public class GenerateTrainingData implements Serializable{

    static Logger logger = LogManager.getLogger(GenerateTrainingData.class.getName());
    List<List<Double>> XTrain = new ArrayList<List<Double>>();
    List<Integer> YTrain = new ArrayList<Integer>();

    List<List<Double>> XTest = new ArrayList<List<Double>>();
    List<Integer> YTest = new ArrayList<Integer>();

    public void generateTrainingDataForMain(FeatureExtractor featureExtractorObject){
        logger.info("In generateTrainingData");
        
        int i=0;
        for(Column  col: featureExtractorObject.trainColumns){
            List<List<Double>> sim_ref_cols = featureExtractorObject.computeFeatureVectors(col);
            for(int j=0; j<featureExtractorObject.trainColumns.size(); j++){
                if(i==j)
                    continue;

                Column refCol = featureExtractorObject.trainColumns.get(j);

                this.XTrain.add(sim_ref_cols.get(j));
                if(col.semantic_type == refCol.semantic_type)
                    this.YTrain.add(1);
                else
                    this.YTrain.add(0);
            }
        }
        logger.info("Returning from generateTrainingData");
        logger.info("Train:"+this.XTrain+" y: "+this.YTrain);
    }

    public void generateTrainingDataForTest(FeatureExtractor featureExtractorObject, List<List<Double>> sim_ref_cols){
        logger.info("In generateTrainingDataForTest");
        
        int i=0;
        // for(Column  col: featureExtractorObject.trainColumns){
        //     List<List<Double>> sim_ref_cols = featureExtractorObject.computeFeatureVectors(col);
            for(int j=0; j<featureExtractorObject.trainColumns.size(); j++){
                logger.info("In generateTest data"+j);
                // if(i==j)
                //     continue;

                Column refCol = featureExtractorObject.trainColumns.get(j);

                this.XTest.add(sim_ref_cols.get(j));
                // if(col.semantic_type == refCol.semantic_type)
                this.YTest.add(1);
                // else
                    // this.YTest.add(0);
            }
        // }
        logger.info("Returning from generateTrainingDataForTest");
        logger.info("Test:"+this.XTest+" y: "+this.YTest);
    }
}