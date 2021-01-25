package edu.isi.karma.semanticlabeling.dsl;

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
    // classID and predicate together make the semtype.
    public SemType(String classID, String predicate){
        this.classID = classID;
        this.predicate = predicate;
        logger.info("Semantic Type:"+classID+" "+predicate);
    }
}