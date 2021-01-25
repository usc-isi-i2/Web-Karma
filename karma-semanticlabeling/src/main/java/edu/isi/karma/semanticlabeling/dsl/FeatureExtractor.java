package edu.isi.karma.semanticlabeling.dsl;


import java.io.*;
import java.util.*;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// import com.mycompany.dsl.TfidfDatabase;
import edu.isi.karma.semanticlabeling.dsl.featureextraction.columnbase.Textual;

import edu.isi.karma.semanticlabeling.dsl.featureextraction.columnbase.ColumnName;
import edu.isi.karma.semanticlabeling.dsl.featureextraction.columnbase.Numeric;
import edu.isi.karma.semanticlabeling.dsl.featureextraction.columnbase.Textual;

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

    public FeatureExtractor(List<ColumnBasedTable> trainTables) throws IOException{
        this.trainTables = trainTables;
        // Each column in each table:
        for(ColumnBasedTable tbl: trainTables){
            for(Column col: tbl.columns){
                if(col.value != null)
                    this.trainColumns.add(col);
            }
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
    }
    
    public List<List<Double>> computeFeatureVectors(Column col) throws IOException{
        // Extract a feature vector of the given column with columns in the training set. Return a matrix N x K,
        // in which N is the number of train columns and K is the number of features that is going to be used to compare
        // similarity of two columns
        logger.info("In computerFeatureVectors: extracting features");
        List<List<Double>> features = new ArrayList<List<Double>>();
        Numeric numeric = new Numeric();
        ColumnName columnName = new ColumnName();
        Textual textual = new Textual();
        List<Double> col_tfidf = this.tfidfDB.compute_tfidf(col);
        for(Column refcol: this.trainColumns){
            List<Double> feature_now = new ArrayList<Double>();
            
            feature_now.add(columnName.jaccard_sim_test(refcol.name, col.name, true));
           
            feature_now.add(columnName.jaccard_sim_test(refcol.semantic_type.predicate, col.name, true));

            feature_now.add(numeric.ks_test(refcol, col));

            feature_now.add(numeric.mann_whitney_u_test(refcol, col));

            feature_now.add(numeric.jaccard_sim_test(refcol, col));

            feature_now.add(textual.jaccard_sim_test(refcol, col));

            feature_now.add(textual.cosine_similarity(this.tfidfDB.compute_tfidf(refcol), col_tfidf));
            
            features.add(feature_now);
        }
        return features;
    }
}