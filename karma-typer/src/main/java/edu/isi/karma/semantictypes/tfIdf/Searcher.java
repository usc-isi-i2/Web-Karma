package edu.isi.karma.semantictypes.tfIdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;

/**
 * This class is responsible for predicting top-k suggestions for textual data
 * using TF-IDF based cosine similarity approach and checking if a document for
 * a semantic label already exists
 * 
 * @author ramnandan
 * 
 */
public class Searcher {
	private IndexSearcher indexSearcher = null;
	private Analyzer analyzer = null;
	private QueryParser parser = null;

	public Searcher(String filepath, String fieldName) throws IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
				filepath)));
		indexSearcher = new IndexSearcher(reader);
		analyzer = new StandardAnalyzer(Version.LUCENE_48);
		if (fieldName.equalsIgnoreCase(Indexer.LABEL_FIELD_NAME)) {
			parser = new QueryParser(Version.LUCENE_48,
					Indexer.LABEL_FIELD_NAME, analyzer);
		} else {
			parser = new QueryParser(Version.LUCENE_48,
					Indexer.CONTENT_FIELD_NAME, analyzer);
		}

	}

	public List<SemanticTypeLabel> getTopK(int k, String content)
			throws ParseException, IOException {
		List<SemanticTypeLabel> result = new ArrayList<>();
		int spaces = content.length() - content.replace(" ", "").length();
		if (spaces > BooleanQuery.getMaxClauseCount()) {
			BooleanQuery.setMaxClauseCount(spaces);
		}

		Query query = parser.parse(QueryParser.escape(content));

		TopDocs results = indexSearcher.search(query, k);
		ScoreDoc[] hits = results.scoreDocs;

		for (int i = 0; i < hits.length; i++) {
			Document doc = indexSearcher.doc(hits[i].doc);
			String labelString = doc.get(Indexer.LABEL_FIELD_NAME);
			result.add(new SemanticTypeLabel(labelString, hits[i].score));
		}
		return result;
	}

	public boolean existsSemanticLabel(String labelToQuery, Document labelDoc) throws IOException {
		BooleanQuery query = new BooleanQuery();
		query.add(new TermQuery(
				new Term(Indexer.LABEL_FIELD_NAME, labelToQuery)),
				BooleanClause.Occur.MUST);

		TopDocs results = indexSearcher.search(query, 1);
		ScoreDoc[] hits = results.scoreDocs;

		if (hits.length > 0) {
			Document doc = indexSearcher.doc(hits[0].doc);
			String labelString = doc.get(Indexer.LABEL_FIELD_NAME);
			if (labelString.equalsIgnoreCase(labelToQuery)) // document for
															// exact semantic
															// label already
															// exists
			{
				labelDoc = doc; // returning the document corresponding to existing semantic label
				return true;
			} else // inexact match
			{
				return false;
			}
		} else // no match found
		{
			return false;
		}
	}

	public void close() {
		try {
			indexSearcher.getIndexReader().close();
		} catch (IOException e) {
			// Ignore
		}
	}

}
