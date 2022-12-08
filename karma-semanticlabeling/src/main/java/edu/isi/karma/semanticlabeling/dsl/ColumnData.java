package edu.isi.karma.semanticlabeling.dsl;

import java.util.*;
import java.io.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class is responsible for creating an object of the data in every column.
 * @author rutujarane, Bidisha Das Baksi (bidisha.bksh@gmail.com)
 */

public class ColumnData implements Serializable{

    List<String> array;
    List<String> number_array = new ArrayList<String>();
    List<Integer> number_idx_array = new ArrayList<Integer>();
    List<String> string_array = new ArrayList<String>();
    List<Integer> string_idx_array = new ArrayList<Integer>();
    
    public ColumnData(List<String> array){
        this.array = array;
        int i=0;
        for(Object arr: array){
            if(arr != null){
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
            // checking valid integer using parseDouble() method
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