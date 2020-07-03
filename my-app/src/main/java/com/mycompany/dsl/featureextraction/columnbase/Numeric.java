package com.mycompany.dsl.featureextraction.columnbase;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.mycompany.dsl.Column;

/**
 * This class is responsible for measuring similarity between column data which is numeric.
 * @author rutujarane
 */

public class Numeric {


    //feature_extractor.py (compute_feature_vectors)
    public double ks_test(Column col1, Column col2){
        if(col1.get_numeric_data().size() > 1 && col2.get_numeric_data().size() > 1){
            List<String> column1 = col1.get_numeric_data();
            double col1Data[] = new double[column1.size()];
            for(int i=0; i<column1.size(); i++){
                col1Data[i] = Double.parseDouble(column1.get(i));
            }

            List<String> column2 = col2.get_numeric_data();
            double col2Data[] = new double[column2.size()];
            for(int i=0; i<column2.size(); i++){
                col2Data[i] = Double.parseDouble(column2.get(i));
            }

            KolmogorovSmirnovTest kst = new KolmogorovSmirnovTest();
            return kst.kolmogorovSmirnovTest(col1Data, col2Data);
            // return ks_2samp(col1.get_numeric_data(), col2.get_numeric_data())[1];
        }
           
        return (double) 0.0;

    }

        
    //feature_extractor.py (compute_feature_vectors)
    public double mann_whitney_u_test(Column col1, Column col2){
        if(col1.get_numeric_data().size() > 1 && col2.get_numeric_data().size() > 1){

            List<String> column1 = col1.get_numeric_data();
            double col1Data[] = new double[column1.size()];
            for(int i=0; i<column1.size(); i++){
                col1Data[i] = Double.parseDouble(column1.get(i));
            }

            List<String> column2 = col2.get_numeric_data();
            double col2Data[] = new double[column2.size()];
            for(int i=0; i<column2.size(); i++){
                col2Data[i] = Double.parseDouble(column2.get(i));
            }

            MannWhitneyUTest mwut = new MannWhitneyUTest();
            return mwut.mannWhitneyUTest(col1Data, col2Data);
            // return mannwhitneyu(col1.get_numeric_data(), col2.get_numeric_data())[1];
        }
        return (double) 0.0;
    }


    public double jaccard_sim_test(Column col1, Column col2){
        // logger.info("IN numeric jaccard");
    
        Set<String> col1data = new HashSet<String>();
        col1data.addAll(col1.get_numeric_data());
        Set<String> col2data = new HashSet<String>();
        // for(String s: col2.get_numeric_data())
        col2data.addAll(col2.get_numeric_data());


        if(col2data.size() == 0 || col1data.size() == 0)
            return 0;

        Set<String> temp = col1data;
        col1data.retainAll(col2data);
        Set<String> intersect = col1data;
        col1data = temp;
        col1data.addAll(col2data);
        Set<String> union = col1data;
        col1data = temp;

        // logger.info("Returning numeric jaccard"+(intersect.size()) / (union.size()));
        if (union.size() == 0)
            return 0.0;
        return (intersect.size()) / (union.size());
        // return 0;
    }


}