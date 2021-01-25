package edu.isi.karma.semanticlabeling.dsl;

import java.util.*;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.io.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;

/**
 * This class is responsible for tokenizing all the columns in the datasets.
 * String tokenizer and Stanford Core NLP tokenizers were tried. Stanford Core NLP tokenizer works best with our data.
 * @author rutujarane
 */

public class TfidfDatabase implements Serializable {

    static Logger logger = LogManager.getLogger(TfidfDatabase.class.getName());

    HashMap<String, Integer> vocab = new HashMap<String, Integer>();
    HashMap<String, Integer> invert_token_idx = new HashMap<String, Integer>();
    HashMap<String, List<Double>> col2tfidf = new HashMap<String, List<Double>>();
    int n_docs;
    HashMap<String, List<Double>> cache_col2tfidf = new HashMap<String, List<Double>>();
    // String pipeline;
    StanfordCoreNLP pipeline;

    // public TfidfDatabase(String pipeline, HashMap<String, Integer> vocab, HashMap<String, Integer> invert_token_idx, HashMap<String, List<Double>> col2tfidf){
    public TfidfDatabase(StanfordCoreNLP pipeline, HashMap<String, Integer> vocab, HashMap<String, Integer> invert_token_idx, HashMap<String, List<Double>> col2tfidf){    
        this.vocab = vocab;
        this.invert_token_idx = invert_token_idx;
        this.pipeline = pipeline;
        this.n_docs = col2tfidf.size();
        this.cache_col2tfidf = col2tfidf;
    }
        


    // public static TfidfDatabase create(String pipeline, List<Column> columns){
    public static TfidfDatabase create(StanfordCoreNLP pipeline, List<Column> columns) throws IOException{
        logger.info("Creating TfidfDatabase");
        HashMap<String, Integer> vocab = new HashMap<String, Integer>();
        HashMap<String, Integer> invert_token_idx = new HashMap<String, Integer>();
        HashMap<String, List<Double>> col2tfidf = new HashMap<String, List<Double>>();
        HashMap<String, Integer> token_count = new HashMap<String, Integer>();
        int n_docs = columns.size();

        List<HashMap<String, Double>> tf_cols = new ArrayList<HashMap<String, Double>>(); //Counter
        HashMap<String, Double> tf = new HashMap<String, Double>();
        // Tokenize data in each column.
        for(int i=0; i<columns.size(); i++){
            logger.info("Column:"+columns.get(i).name);
            if(columns.get(i).value!=null){
                tf = TfidfDatabase._compute_tf(pipeline, columns.get(i));
                tf_cols.add(tf);
            }
        }

        // Compute vocabulary & preparing for idf
        for(HashMap<String, Double> tf_col: tf_cols){
            Iterator tf_col_Iterator = tf_col.entrySet().iterator(); 
            while (tf_col_Iterator.hasNext()) { 
                Map.Entry mapElement = (Map.Entry)tf_col_Iterator.next(); 

                if(!invert_token_idx.containsKey(mapElement.getKey())){
                    invert_token_idx.put(mapElement.getKey().toString(),1);
                }
                else{
                    invert_token_idx.put(mapElement.getKey().toString(), invert_token_idx.get(mapElement.getKey())+1);
                }
                    
                if(!token_count.containsKey(mapElement.getKey())){
                    token_count.put(mapElement.getKey().toString(),1);
                }
                else
                    token_count.put(mapElement.getKey().toString(), token_count.get(mapElement.getKey())+1);
            }
            
        }
        
        // logger.info("Computed vocab invertoken_"+ invert_token_idx);

        // Reduce vocab size
        Iterator token_count_Iterator = token_count.entrySet().iterator();
        while (token_count_Iterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)token_count_Iterator.next(); 
            String w = mapElement.getKey().toString();
            if(token_count.get(w) < 2 && isNumeric(w)){
                // # delete this word
                if(invert_token_idx.containsKey(w))
                    invert_token_idx.remove(w);
                // del invert_token_idx[w]
            }
            else
                vocab.put(w, vocab.size());
        }
            
        
        // Revisit it and make tfidf
        for(int i=0; i<tf_cols.size(); i++){
            List<Double> tfidf = new ArrayList<Double>(); //A double list of size number of vocab words
            for(int iter=0; iter<vocab.size(); iter++)
                tfidf.add(0.0);
            Iterator tf_col_Iterator = tf_cols.get(i).entrySet().iterator(); 
            while (tf_col_Iterator.hasNext()) { 
                Map.Entry mapElement = (Map.Entry)tf_col_Iterator.next(); 
                if(vocab.containsKey(mapElement.getKey())){
                    double val = (double)mapElement.getValue() * Math.log((double)((double)n_docs / (double)(1 + invert_token_idx.get(mapElement.getKey()))));
                    tfidf.set(vocab.get(mapElement.getKey()), val);
                }
            }
            col2tfidf.put(columns.get(i).id, tfidf);

        }

        return new TfidfDatabase(pipeline, vocab, invert_token_idx, col2tfidf);

    }

    // Check if column data is numeric.
    public static boolean isNumeric(String strNum) {
        if (strNum == null && strNum.trim().isEmpty()) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // Check if string is punctuation.
    public static boolean notPunctuation(String nextToken){
        // nextToken = nextToken.trim().replaceAll("\\p{Punct};",""); 
        nextToken = nextToken.trim().replaceAll("[^a-zA-Z0-9 ]", "");
        if (nextToken.equals("")){
            // logger.info("Returning false"+nextToken);
            return false;
        }
        // logger.info("Returning true"+nextToken);
        return true;
    }
    // @staticmethod
    // public static HashMap<String, Double> _compute_tf(String pipeline, Column col){
    public static HashMap<String, Double> _compute_tf(StanfordCoreNLP pipeline, Column col) throws IOException{
        // counter = Counter();
        logger.info("In compute_tf");
        List<String> sents = new ArrayList<String>();
        List<String> sentences_full = new ArrayList<String>();
        StringTokenizer st1;
        List<StringTokenizer> sent_tokens = new ArrayList<StringTokenizer>();
        for(String sent: col.get_textual_data()){
            // st1 = new StringTokenizer(sent);
            // sent_tokens.add(st1);
            String sente[] = sent.split(" ");
            for(String subsent: sente){
                sents.add(subsent);
            }
            sentences_full.add(sent);
        }
        HashMap<String, Double> counter = new HashMap<String, Double>();
        int number_of_token = 0;
        if(sents.size()==0){
            return null;
        }
        // for(String sent: sents){
        for(String sent: sentences_full){
            // create a document object
            if(sent.length()==0){
                continue;
            }
            CoreDocument document = new CoreDocument(sent);
            //annnotate the document
            pipeline.annotate(document);
            int i=0;
            for (CoreLabel tok : document.tokens()) {
                i++;
                String nextToken = tok.word();
                if(!notPunctuation(nextToken)){
                    continue;
                }
                
                if(!counter.containsKey(nextToken)){
                    counter.put(nextToken,1.0);
                }
                else
                    counter.put(nextToken,counter.get(nextToken)+1);
            }
            
            number_of_token += i;
            if(number_of_token%100==0)
                logger.info("Number of tokens:"+number_of_token);
        }
       
        logger.info("Done counter");
        Iterator CIterator = counter.entrySet().iterator(); 
        HashMap<String, Double> ccounter = new HashMap<String, Double>(); //for average
        while (CIterator.hasNext()) { 
            Map.Entry mapElement = (Map.Entry)CIterator.next(); 
            double val = (double)mapElement.getValue() / number_of_token;
            ccounter.put(mapElement.getKey().toString(), val);
        } 
        logger.info("RETURNING CCOUNTER");
        return ccounter;
    }

    // featureExtractor.java (compute_feature_vectors)
    public List<Double> compute_tfidf(Column col) throws IOException{
        if(this.cache_col2tfidf.containsKey(col.id))
            return this.cache_col2tfidf.get(col.id);

        List<Double> tfidf = new ArrayList<Double>();
        for(int i=0; i<this.vocab.size(); i++)
            tfidf.add(0.0);
        
        HashMap<String, Double> compute_counter = this._compute_tf(this.pipeline, col);
        Iterator compute_counter_Iterator = compute_counter.entrySet().iterator(); 
        while (compute_counter_Iterator.hasNext()) { 
            Map.Entry mapElement = (Map.Entry)compute_counter_Iterator.next(); 
            if(this.vocab.containsKey(mapElement.getKey())){
                // logger.info(mapElement + " " + (double)mapElement.getValue() + " " + invert_token_idx.get(mapElement.getKey()) + " " + Math.log((double)((double)n_docs / (double)(1 + invert_token_idx.get(mapElement.getKey())))));
                double val = (double)mapElement.getValue() * Math.log((double)((double)n_docs / (double)(1 + invert_token_idx.get(mapElement.getKey()))));
                tfidf.set(this.vocab.get(mapElement.getKey()), val);
            }
        }

        return tfidf;
    }
        
      
}