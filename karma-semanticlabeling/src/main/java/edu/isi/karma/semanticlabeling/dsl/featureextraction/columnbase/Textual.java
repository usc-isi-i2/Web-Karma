package edu.isi.karma.semanticlabeling.dsl.featureextraction.columnbase;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
// import java.util.*;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import edu.isi.karma.semanticlabeling.dsl.Column;

/**
 * This class is responsible for measuring similarity between column data which is textual.
 * @author rutujarane
 */

public class Textual{

    static Logger logger = LogManager.getLogger(Textual.class.getName());

    public double jaccard_sim_test(Column col1, Column col2){

        Set<String> col1data = new HashSet<String>();
        col1data.addAll(col1.get_textual_data());
        Set<String> col2data = new HashSet<String>();
        col2data.addAll(col2.get_textual_data());
        
        if(col1data.size() == 0 || col2data.size() == 0)
            return 0;
        Set<String> temp = new HashSet<String>();
        temp.addAll(col1data);
        col1data.retainAll(col2data);
        Set<String> intersect = col1data;
        double intersect_size = intersect.size();
        col1data.clear();
        col1data.addAll(temp);
        col1data.addAll(col2data);
        Set<String> union = col1data;
        double union_size = union.size();

        col1data = temp;
        if (union_size == 0)
            return 0.0;
        return (intersect_size) / (union_size);
    }

    // vec1: numpy.ndarray, vec2: numpy.ndarray
    public double cosine_similarity(List<Double> vec1, List<Double> vec2){
        
       
        double sum = 0;
        for(double d: vec1){
            sum += Math.pow(d,2);
        }
        double norm1 = Math.sqrt(sum);

        double sum1 = 0;
        for(double d: vec2){
            sum1 += Math.pow(d,2);
        }
        double norm2 = Math.sqrt(sum1);

        // double norm1 = vec1.getNorm();
        // double norm2 = vec2.getNorm();

        // System.out.println("COS:" + norm1 + " " + norm2);
        if(norm1 == 0.0 || norm2 == 0.0)
            return 0.0;

        double product = 0.0;
        for(int i=0; i<vec1.size(); i++){
            product += vec1.get(i) * vec2.get(i);
            // System.out.println("vec:"+ vec1.get(i) + " " + vec2.get(i));
        }
        // System.out.println("COS1:"+ product);
        if (product>0)
            //System.out.println("\n\n\nyayy!");
        if (norm1*norm2 == 0)
            return 0.0;
        return product / (norm1 * norm2);

        
    }
        
}