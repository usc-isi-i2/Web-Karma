package edu.isi.karma.semanticlabeling.dsl;

import java.io.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class creates an object for every predicted semantic type.
 * @author rutujarane
 */

public class SemTypePrediction implements Serializable{

    static Logger logger = LogManager.getLogger(SemTypePrediction.class.getName());
    public final SemType sem_type;
    public final double prob;
    public SemTypePrediction(SemType sem_type, double prob){
        this.sem_type = sem_type;
        this.prob = prob;
        logger.info("Semantic Type Prediction");
    }
}