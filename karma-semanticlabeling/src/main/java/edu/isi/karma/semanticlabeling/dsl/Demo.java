package edu.isi.karma.semanticlabeling.dsl;

import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;

public class Demo {

    static Logger logger = LogManager.getLogger(Demo.class.getName());
    public static String text = "Joe Smith was born in California. "
            + "In 2017, he went to Paris, France in the summer. " + "His flight left at 3:00pm on July 10th, 2017. "
            + "After eating some escargot for the first time, Joe said, \"That was delicious!\" "
            + "He sent a postcard to his sister Jane Smith. "
            + "After hearing about Joe's trip, Jane decided she might go to France one day.";

    public Demo() {
        logger.info("IN  DEMO");

        // set up pipeline properties
    Properties props = new Properties();
    // set the list of annotators to run
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
    // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
    props.setProperty("coref.algorithm", "neural");
    // build pipeline
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    // #DONE
    // create a document object
    CoreDocument document = new CoreDocument(text);
    // annnotate the document
    pipeline.annotate(document);
    // examples

    // 10th token of the document
    // for(int i=0; i<document.tokens().size(); i++){
    //   logger.info(document.tokens().get(i));
    // }
    CoreLabel token = document.tokens().get(10);
    logger.info("Example: token");
    logger.info(token);
    }
  }
