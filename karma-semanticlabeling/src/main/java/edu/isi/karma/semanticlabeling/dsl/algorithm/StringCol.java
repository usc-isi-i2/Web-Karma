package edu.isi.karma.semanticlabeling.dsl.algorithm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author rutujarane
 */

public class StringCol {

    public List<String> tokenize_label(String lbl){

        Pattern camel_reg = Pattern.compile(".+?(?:(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z0-9])|$)");
        Pattern split_reg = Pattern.compile("_| |:|\\.");
    
        List<String> result = new ArrayList<String>();
        CharSequence cs = lbl;
        String split_array[] = split_reg.split(cs);
        for(String name : split_array){
            Matcher myMatcher = camel_reg.matcher(name);
            while(myMatcher.find()){
                result.add(myMatcher.group(0));
            }
        }
        return result;
    }
}