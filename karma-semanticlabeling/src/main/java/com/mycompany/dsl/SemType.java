package com.mycompany.dsl;

import java.io.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class creates an object for every semantic type.
 * @author rutujarane
 */

public class SemType implements Serializable{
    
    static Logger logger = LogManager.getLogger(SemType.class.getName());
    public final String classID;
    public final String predicate;
    public SemType(String classID, String predicate){
        this.classID = classID;
        this.predicate = predicate;
        logger.info("Semantic Type:"+classID+" "+predicate);
    }
    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof SemType)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        SemType c = (SemType) o;

        // Compare the data members and return accordingly
        return this.classID.equals(((SemType) o).classID) && this.predicate.equals(((SemType) o).predicate);
    }
}