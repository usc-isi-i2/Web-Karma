package edu.isi.karma.semanticlabeling.dsl;

import java.util.*;
import java.io.*;

/**
 * This class is responsible for creating an object for each table.
 * Each ColumnBasedTable object will have multiple Column objects in a list.
 * @author rutujarane
 */

public class ColumnBasedTable implements Serializable{

    public String id;
    public List<Column> columns = new ArrayList<Column>();
    public HashMap<String, Integer> name2colidx = new HashMap<String, Integer>();

    public ColumnBasedTable(String id, List<Column> columns){
        this.id = id;
        this.columns = columns;
        // self.name2colidx: Dict[str, int] = {cname.name: idx for idx, cname in enumerate(columns)}
        int i=0;
        for(Column col_name: columns){
            this.name2colidx.put(col_name.name.toString(), i);
            i++;
        }
    }
}