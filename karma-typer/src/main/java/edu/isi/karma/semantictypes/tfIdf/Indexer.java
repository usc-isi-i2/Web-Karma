package edu.isi.karma.semantictypes.tfIdf;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.util.Version;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This class is responsible for creation of an index, adding documents to the index,
 * committing changes
 * 
 * @author ramnandan
 *
 */

public class Indexer {
	public static String INDEX_LOC = "index";
	public static String CONTENT_FIELD_NAME = "content";
	public static String LABEL_FIELD_NAME = "label";
	
  private IndexWriter indexWriter = null;
  
  public void openIndexWriter() throws IOException
  {
  	Directory dir = FSDirectory.open(new File(INDEX_LOC));
  	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48); 
  	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
  	
  	// Creates a new index if one does not exist, 
  	// otherwise opens the index and documents will be appended
  	config.setOpenMode(OpenMode.CREATE_OR_APPEND);
  	
  	indexWriter = new IndexWriter(dir, config);
  }   
  
  public void closeIndexWriter() throws IOException
  {
  	indexWriter.close();
  }
  
  public void addDocument(String content, String label) throws IOException 
  {
  	Document doc = new Document();
  	doc.add(new TextField(CONTENT_FIELD_NAME, content, Field.Store.YES));
  	doc.add(new StringField(LABEL_FIELD_NAME, label, Field.Store.YES));
  	indexWriter.addDocument(doc);
  }
  
  public void deleteDocuments() throws IOException
  {
  	indexWriter.deleteAll();
  }
  
  public int getNoOfDocs()
  {
  	return indexWriter.numDocs();
  }
  
  public void commit() throws IOException
  {
  	indexWriter.commit();
  }
  
}
