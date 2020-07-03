package com.mycompany.dsl;


import java.io.*;
import java.util.*;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// import com.mycompany.dsl.TfidfDatabase;
import com.mycompany.dsl.featureextraction.columnbase.Textual;

import com.mycompany.dsl.featureextraction.columnbase.ColumnName;
import com.mycompany.dsl.featureextraction.columnbase.Numeric;
import com.mycompany.dsl.featureextraction.columnbase.Textual;

/**
 * This class is responsible for creation of an object from all the tables in the train directory.
 * @author rutujarane
 */

public class FeatureExtractor implements Serializable{

    static Logger logger = LogManager.getLogger(FeatureExtractor.class.getName());
    String SIMILARITY_METRICS[] = {"label_jaccard", "stype_jaccard", "num_ks_test", "num_mann_whitney_u_test", "num_jaccard", "text_jaccard", "text_tf-idf"};

    List<ColumnBasedTable> trainTables = new ArrayList<ColumnBasedTable>();
    List<Column> trainColumns = new ArrayList<Column>();
    HashMap<Column, Integer> column2idx = new HashMap<Column, Integer>();
    TfidfDatabase tfidfDB;
    // Demo demo;

    public FeatureExtractor(List<ColumnBasedTable> trainTables){
        this.trainTables = trainTables;
        int kk=0;
        for(ColumnBasedTable tbl: trainTables){
            for(Column col: tbl.columns){
                if(col.value != null)
                    this.trainColumns.add(col);
                // kk++;
                // if(kk>=2)
                //     break;
            }
            // break;
        }
        logger.info("Train_cols"+ this.trainColumns.size());

        int i=0;
        for(Column col: this.trainColumns){
            this.column2idx.put(col, i);
            i++;
        }
        if (this.column2idx.size() != this.trainColumns.size())
            logger.info("ERROR: colID is not unique");

        logger.info("Build tfidf database...");

        Textual textual = new Textual();
        this.tfidfDB = TfidfDatabase.create(textual.get_pipeline(), this.trainColumns);
        logger.info("Done with FeatureExtractor");
        // this.demo = new Demo();
    }
    
    public List<List<Double>> computeFeatureVectors(Column col){
        // """
        // Extract a feature vector of the given column with columns in the training set. Return a matrix N x K,
        // in which N is the number of train columns and K is the number of features that is going to be used to compare
        // similarity of two columns
        // """
        logger.info("In computerFeatureVectors: extracting features");
        List<List<Double>> features = new ArrayList<List<Double>>();
        int i=0;
        Numeric numeric = new Numeric();
        ColumnName columnName = new ColumnName();
        Textual textual = new Textual();
        List<Double> col_tfidf = this.tfidfDB.compute_tfidf(col);
        for(Column refcol: this.trainColumns){
            List<Double> feature_now = new ArrayList<Double>();
            // features.append([
            // logger.info("colName 1");
            feature_now.add(columnName.jaccard_sim_test(refcol.name, col.name, true));
           
            // logger.info("colName 2");
            feature_now.add(columnName.jaccard_sim_test(refcol.semantic_type.predicate, col.name, true));

            // logger.info("numeric 1");
            feature_now.add(numeric.ks_test(refcol, col));

            // logger.info("numeric 2");
            feature_now.add(numeric.mann_whitney_u_test(refcol, col));

            // logger.info("numeric 3");
            feature_now.add(numeric.jaccard_sim_test(refcol, col));

            // logger.info("textual 1");
            feature_now.add(textual.jaccard_sim_test(refcol, col));

            // logger.info("textual 2");
            feature_now.add(textual.cosine_similarity(this.tfidfDB.compute_tfidf(refcol), col_tfidf));
            
            features.add(feature_now);
        }
        logger.info("Returning from computerFeatureVectors"+features);
        return features;
    }
}