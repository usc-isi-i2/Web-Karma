package edu.isi.karma.semanticlabeling.dsl;

import java.io.*;


/**
 * This class is responsible for creating an object of the data type of each column.
 * @author rutujarane
 */

public class ColumnType implements Serializable{

    String NUMBER = "number";
    String STRING = "string";
    String DATETIME = "datetime";
    String NULL = "null";

    /*public boolean is_comparable(ColumnType self){
        return (self.toString() == self.NUMBER || self.toString() == self.DATETIME);
    }*/
}

