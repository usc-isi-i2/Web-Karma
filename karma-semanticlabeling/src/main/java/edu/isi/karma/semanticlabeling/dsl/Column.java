package edu.isi.karma.semanticlabeling.dsl;

import java.util.*;
import java.io.*;

/**
 * This class is responsible for creating a column object for each column.
 * @author rutujarane
 */

public class Column implements Serializable{
    public String id;
    public String table_name;
    public String name;
    public SemType semantic_type;
    public int sizee;
    public Hashtable<String, Float> type_stats;
    public String typee;
    public ColumnData value;

    public Column(String table_name, String name, SemType semantic_type, String typee, int sizee, Hashtable<String, Float> type_stats){
        this.id = table_name.concat(name);
        // f"{table_name}:{name}"
        this.table_name = table_name;
        this.name = name;
        this.semantic_type = semantic_type;
        this.sizee = sizee;
        this.type_stats = type_stats;
        this.typee = typee;
        // this.value = Optional[ColumnData] = null;
        this.value = null;
    }

    public List<String> get_textual_data(){
        if(this.value.string_data()) {
            return this.value.string_array;
        }
//        else
//            return this.value.number_array; // Removing this after comparing with the python implementation
        return new ArrayList<String>();
    }

    public List<String> get_numeric_data(){
        return this.value.number_array;
    }
        
     
}
