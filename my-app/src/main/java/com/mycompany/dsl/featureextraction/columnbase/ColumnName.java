package com.mycompany.dsl.featureextraction.columnbase;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.mycompany.dsl.algorithm.StringCol;

/**
 * This class is responsible for measuring similarity between columns by their names
 * @author rutujarane
 */

public class ColumnName {

    public double jaccard_sim_test(String col1_name, String col2_name, Boolean lower){ //lower=False

        // logger.info("IN columnname jaccard");
        StringCol stringCol = new StringCol();
        List<String> tok1 = stringCol.tokenize_label(col1_name);
        List<String> tok2 = stringCol. tokenize_label(col1_name);

        List<String> lbl1 = new ArrayList<String>();
        List<String> lbl2 = new ArrayList<String>();

        if(lower){
            for(int i=0; i<tok1.size(); i++){
                tok1.set(i,tok1.get(i).toLowerCase());
            }
            lbl1 = tok1;
            for(int i=0; i<tok2.size(); i++){
                tok2.set(i,tok2.get(i).toLowerCase());
            }
            lbl2 = tok2;
        }
        else{
            Set<String> lbl1_ = new HashSet<String>();
            lbl1_.addAll(tok1);
            Set<String> lbl2_ = new HashSet<String>();
            lbl2_.addAll(tok2);
            // Set<String> lbl1_ = tok1.stream().collect(Collectors.toSet());
            // Set<String> lbl2_ = tok2.stream().collect(Collectors.toSet());
            
            lbl1.addAll(lbl1_);
            lbl2.addAll(lbl2_);
            
        }
    
        List<String> temp = lbl1;
        lbl1.retainAll(lbl2);
        List<String> intersect = lbl1;
        lbl1 = temp;
        lbl1.addAll(lbl2);
        List<String> union = lbl1;
        lbl1 = temp;
        // List<String> intersect = lbl1.stream().filter(lbl2::contains).collect(Collectors.toList());
        // List<String> union = Stream.concat(lbl1.stream(), lbl2.stream()).distinct().collect(Collectors.toList());

        // logger.info("returning Colname jaccard");
        if (union.size() == 0)
            return 0.0;
        return (intersect.size()) / (union.size());
    }

}