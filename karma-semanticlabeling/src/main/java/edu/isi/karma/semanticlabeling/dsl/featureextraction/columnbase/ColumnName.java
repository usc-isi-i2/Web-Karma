package edu.isi.karma.semanticlabeling.dsl.featureextraction.columnbase;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.isi.karma.semanticlabeling.dsl.algorithm.StringCol;

/**
 * This class is responsible for measuring similarity between columns by their names
 * @author rutujarane
 */

public class ColumnName {

    public double jaccard_sim_test(String col1_name, String col2_name, Boolean lower){ //lower=False

        StringCol stringCol = new StringCol();
        List<String> tok1 = stringCol.tokenize_label(col1_name);
        List<String> tok2 = stringCol. tokenize_label(col2_name);

        Set<String> lbl1 = new HashSet<String>();
        Set<String> lbl2 = new HashSet<String>();

        if(lower){
            for(int i=0; i<tok1.size(); i++){
                tok1.set(i,tok1.get(i).toLowerCase());
            }
            lbl1.addAll(tok1);
            for(int i=0; i<tok2.size(); i++){
                tok2.set(i,tok2.get(i).toLowerCase());
            }
            lbl2.addAll(tok2);
        }
        else{
            Set<String> lbl1_ = new HashSet<String>();
            lbl1_.addAll(tok1);
            Set<String> lbl2_ = new HashSet<String>();
            lbl2_.addAll(tok2);
            
            lbl1.addAll(lbl1_);
            lbl2.addAll(lbl2_);
            
        }
    
        Set<String> temp = new HashSet<String>();
        temp.addAll(lbl1);
        lbl1.retainAll(lbl2);
        Set<String> intersect = lbl1;
        int intersect_size = intersect.size();
        lbl1.clear();
        lbl1.addAll(temp);
        lbl1.addAll(lbl2);
        Set<String> union = lbl1;
        int union_size = union.size();
        lbl1 = temp;
       
        if (union_size == 0)
            return 0.0;
        return (intersect_size) / (union_size);
    }

}