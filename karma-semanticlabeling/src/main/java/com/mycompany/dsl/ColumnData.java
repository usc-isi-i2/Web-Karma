package com.mycompany.dsl;

import java.util.*;
import java.io.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// (object)

/**
 * This class is responsible for creating an object of the data in every column.
 * @author rutujarane
 */

public class ColumnData implements Serializable{

    List<String> array;
    List<String> number_array = new ArrayList<String>();
    List<Integer> number_idx_array = new ArrayList<Integer>();
    List<String> string_array = new ArrayList<String>();
    List<Integer> string_idx_array = new ArrayList<Integer>();
    
    public ColumnData(List<String> array){
        // for (Object object : array) {
        //     this.array.add(Objects.toString(object, null));
        // }
        this.array = array;
        // for(int i=0; i<array.size(); i++){
        //     logger.info(" "+array.get(i));
        // }
        // this.number_array = {};
        // this.number_idx_array = {};
        // this.string_array = {};
        // this.string_idx_array = {};
    
        // for i, val in enumerate(array):
        int i=0;
        for(Object arr: array){
            // logger.info(" "+arr);
            if(arr != null){
                // if(isinstance(val, (int, float)){
                
                if(!string_data()){
                    this.number_array.add(arr.toString());
                    this.number_idx_array.add(i);
                }
                else{
                    this.string_array.add(arr.toString());
                    this.string_idx_array.add(i); 
                }
            }
            i++;
        }
            
    }

    public boolean string_data(){
        try 
        { 
            // checking valid integer using parseInt() method 
            for(String arr: this.array)
                Double.parseDouble(arr.toString());
            return false;
        }  
        catch (NumberFormatException e)  
        { 
        } 
        return true;
    }

}