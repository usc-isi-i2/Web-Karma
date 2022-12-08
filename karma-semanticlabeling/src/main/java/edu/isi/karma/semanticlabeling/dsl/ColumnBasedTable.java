package edu.isi.karma.semanticlabeling.dsl;

import java.util.*;
import java.io.*;

/**
 * This class is responsible for creating an object for each table.
 * @author rutujarane
 */

public class ColumnBasedTable implements Serializable{

    public String id;
    public List<Column> columns = new ArrayList<Column>();
    public HashMap<String, Integer> name2colidx = new HashMap<String, Integer>();

    public ColumnBasedTable(String id, List<Column> columns){
        this.id = id;
        this.columns = columns;
        int i=0;
        for(Column col_name: columns){
            this.name2colidx.put(col_name.name.toString(), i);
            i++;
        }
    }
}