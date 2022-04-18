package edu.isi.karma.semanticlabeling.dsl.featureextraction.columnbase;

import java.util.*;

import edu.isi.karma.semanticlabeling.dsl.algorithm.StringCol;

/**
 * This class is responsible for measuring similarity between columns by their names
 * @author rutujarane
 */

public class ColumnName {

    public double jaccard_sim_test(String col1_name, String col2_name, Boolean lower){ //lower=False

        //System.out.println("IN columnname jaccard:"+col1_name+" "+col2_name);
        StringCol stringCol = new StringCol();
        List<String> tok1 = stringCol.tokenize_label(col1_name);
        List<String> tok2 = stringCol.tokenize_label(col2_name);
        //System.out.println("tok:"+tok1+" "+tok2);

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
            // Set<String> lbl1_ = tok1.stream().collect(Collectors.toSet());
            // Set<String> lbl2_ = tok2.stream().collect(Collectors.toSet());
            
            lbl1.addAll(lbl1_);
            lbl2.addAll(lbl2_);
            
        }
    
        // System.out.println("lbl:"+lbl1+" "+lbl2);
        Set<String> temp = new HashSet<String>();
        temp.addAll(lbl1);
        lbl1.retainAll(lbl2);
        Set<String> intersect = lbl1;
        // System.out.println("intersect:"+intersect);
        double intersect_size = intersect.size();
        lbl1.clear();
        lbl1.addAll(temp);
        lbl1.addAll(lbl2);
        // System.out.println("temp:"+temp+" "+lbl1+" "+lbl1);
        Set<String> union = lbl1;
        double union_size = union.size();
        // System.out.println("union:"+union);
        lbl1 = temp;
        // System.out.println("intersect union sizes:"+intersect_size+" "+union_size);
        // List<String> intersect = lbl1.stream().filter(lbl2::contains).collect(Collectors.toList());
        // List<String> union = Stream.concat(lbl1.stream(), lbl2.stream()).distinct().collect(Collectors.toList());

        // logger.info("returning Colname jaccard");
        if (union_size == 0)
            return 0.0;
        return (intersect_size) / (union_size);
    }

}