package edu.isi.karma.semanticlabeling.dsl;

import java.util.*;
import java.util.StringTokenizer;
import java.io.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//import edu.stanford.nlp.pipeline.CoreDocument;
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.ling.*;

/**
 * This class is responsible for tokenizing all the columns in the datasets.
 *
 * @author rutujarane
 */

public class TfidfDatabase implements Serializable {

    private static final long serialVersionUID = 1234567L;
    static Logger logger = LogManager.getLogger(TfidfDatabase.class.getName());

    HashMap<String, Integer> vocab = new HashMap<String, Integer>();
    HashMap<String, Integer> invert_token_idx = new HashMap<String, Integer>();
    HashMap<String, List<Double>> col2tfidf = new HashMap<String, List<Double>>();
    int n_docs;
    HashMap<String, List<Double>> cache_col2tfidf = new HashMap<String, List<Double>>();
    // String pipeline;
    //public transient StanfordCoreNLP pipeline;


    // public TfidfDatabase(String pipeline, HashMap<String, Integer> vocab, HashMap<String, Integer> invert_token_idx, HashMap<String, List<Double>> col2tfidf){
    public TfidfDatabase(HashMap<String, Integer> vocab, HashMap<String, Integer> invert_token_idx, HashMap<String, List<Double>> col2tfidf) {
        this.vocab = vocab;
        this.invert_token_idx = invert_token_idx;
//        this.pipeline = pipeline;
        this.n_docs = col2tfidf.size();
        this.cache_col2tfidf = col2tfidf;
    }


    // public static TfidfDatabase create(Tokenizer tokenizer, List<Column> columns){
    // public static TfidfDatabase create(String pipeline, List<Column> columns){
    public static TfidfDatabase create( List<Column> columns) throws IOException {
        logger.info("Creating TfidfDatabase");
        HashMap<String, Integer> vocab = new HashMap<String, Integer>();
        HashMap<String, Integer> invert_token_idx = new HashMap<String, Integer>();
        HashMap<String, List<Double>> col2tfidf = new HashMap<String, List<Double>>();
        HashMap<String, Integer> token_count = new HashMap<String, Integer>();
        int n_docs = columns.size();

        List<HashMap<String, Double>> tf_cols = new ArrayList<HashMap<String, Double>>(); //Counter
        HashMap<String, Double> tf = new HashMap<String, Double>();
        for (int i = 0; i < columns.size(); i++) {
            //logger.info("Column:" + columns.get(i).name);
            if (columns.get(i).value != null) {
                tf = TfidfDatabase._compute_tf( columns.get(i));
                // if(tf!=null)
                tf_cols.add(tf);
            }
        }

        // logger.info("tf_cols:"+tf_cols);
        // logger.info("Computing vocab");
        // # then compute vocabulary & preparing for idf
        for (HashMap<String, Double> tf_col : tf_cols) {
            Iterator tf_col_Iterator = tf_col.entrySet().iterator();
            while (tf_col_Iterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry) tf_col_Iterator.next();

                if (!invert_token_idx.containsKey(mapElement.getKey())) {
                    invert_token_idx.put(mapElement.getKey().toString(), 1);
                } else {
                    invert_token_idx.put(mapElement.getKey().toString(), invert_token_idx.get(mapElement.getKey()) + 1);
                }

                if (!token_count.containsKey(mapElement.getKey())) {
                    token_count.put(mapElement.getKey().toString(), 1);
                } else
                    token_count.put(mapElement.getKey().toString(), token_count.get(mapElement.getKey()) + 1);
            }

        }

        // logger.info("Computed vocab invertoken_"+ invert_token_idx);

        // # reduce vocab size
        Iterator token_count_Iterator = token_count.entrySet().iterator();
        while (token_count_Iterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) token_count_Iterator.next();
            String w = mapElement.getKey().toString();
            if (token_count.get(w) < 2 && isNumeric(w)) {
                // # delete this word
                if (invert_token_idx.containsKey(w))
                    invert_token_idx.remove(w);
                // del invert_token_idx[w]
            } else
                vocab.put(w, vocab.size());
        }

        // logger.info("Reduced vocab"+vocab);

        // # revisit it and make tfidf
        // logger.info("Comparing size:"+columns.size()+" "+tf_cols.size());
        for (int i = 0; i < tf_cols.size(); i++) {
            List<Double> tfidf = new ArrayList<Double>(); //A double list of size number of vocab words
            for (int iter = 0; iter < vocab.size(); iter++)
                tfidf.add(0.0);
            Iterator tf_col_Iterator = tf_cols.get(i).entrySet().iterator();
            while (tf_col_Iterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry) tf_col_Iterator.next();
                if (vocab.containsKey(mapElement.getKey())) {
                    // logger.info("calc");
                    // logger.info(n_docs+" "+ (1 + invert_token_idx.get(mapElement.getKey())));
                    // logger.info(n_docs / (1 + invert_token_idx.get(mapElement.getKey())));
                    double val = (double) mapElement.getValue() * Math.log((double) ((double) n_docs / (double) (1 + invert_token_idx.get(mapElement.getKey()))));
                    tfidf.set(vocab.get(mapElement.getKey()), val);
                }
            }
            // logger.info("TFIDF:"+tfidf);
            col2tfidf.put(columns.get(i).id, tfidf);

        }
        // logger.info("col2tfidf FINAL:"+col2tfidf);
        // for col, tf_col in zip(columns, tf_cols):
        //     tfidf = numpy.zeros((len(vocab)))
        //     for w, tf in tf_col.items():
        //         if w in vocab:
        //             tfidf[vocab[w]] = tf * numpy.log(n_docs / (1 + invert_token_idx[w]))
        //     col2tfidf[col.id] = tfidf

        return new TfidfDatabase( vocab, invert_token_idx, col2tfidf);

    }

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

    public static boolean notPunctuation(String nextToken) {
        // nextToken = nextToken.trim().replaceAll("\\p{Punct};",""); 
        nextToken = nextToken.trim().replaceAll("[^a-zA-Z0-9 ]", "");
        if (nextToken.equals("")) {
            // System.out.println("Returning false"+nextToken);
            return false;
        }
        // System.out.println("Returning true"+nextToken);
        return true;
    }

    public static HashMap<String, Double> _compute_tf( Column col) throws IOException {
        logger.info("In compute_tf");
        List<String> sents = new ArrayList<String>();
        for (String sent : col.get_textual_data()) {
            String sente[] = sent.split("[-:/ ]");
            for (String subsent : sente) {
                sents.add(subsent);
            }
        }
        logger.info("Done subsent");
        HashMap<String, Double> counter = new HashMap<String, Double>();
        int number_of_token = sents.size();
//        if (sents.size() == 0) {
//            return null;
//        }
        for (String sent : sents) {
            if (sent.length() == 0) {
                continue;
            }
            if (!notPunctuation(sent)) {
                continue;
            }

            if (!counter.containsKey(sent)) {
                counter.put(sent, 1.0);
            } else
                counter.put(sent, counter.get(sent) + 1);
        }
        logger.info("Done counter");
        Iterator CIterator = counter.entrySet().iterator();
        HashMap<String, Double> ccounter = new HashMap<String, Double>(); //for average
        while (CIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) CIterator.next();
            double val = (double) mapElement.getValue() / number_of_token;
            ccounter.put(mapElement.getKey().toString(), val);
        }
        logger.info("RETURNING CCOUNTER");
        return ccounter;
    }



    // #feature_extractor.py (compute_feature_vectors)
    public List<Double> compute_tfidf(Column col) throws IOException {
        if (this.cache_col2tfidf.containsKey(col.id))
            return this.cache_col2tfidf.get(col.id);

        List<Double> tfidf = new ArrayList<Double>();
        for (int i = 0; i < this.vocab.size(); i++)
            tfidf.add(0.0);

        HashMap<String, Double> compute_counter = this._compute_tf( col);
        Iterator compute_counter_Iterator = compute_counter.entrySet().iterator();
        while (compute_counter_Iterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) compute_counter_Iterator.next();
            if (this.vocab.containsKey(mapElement.getKey())) {
                // System.out.println(mapElement + " " + (double)mapElement.getValue() + " " + invert_token_idx.get(mapElement.getKey()) + " " + Math.log((double)((double)n_docs / (double)(1 + invert_token_idx.get(mapElement.getKey())))));
                double val = (double) mapElement.getValue() * Math.log((double) ((double) n_docs / (double) (1 + invert_token_idx.get(mapElement.getKey()))));
                tfidf.set(this.vocab.get(mapElement.getKey()), val);
            }
        }

        return tfidf;
    }


    public HashMap<String, List<Double>> getCache_col2tfidf() {
        return cache_col2tfidf;
    }
}