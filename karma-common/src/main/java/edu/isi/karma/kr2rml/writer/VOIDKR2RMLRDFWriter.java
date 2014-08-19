package edu.isi.karma.kr2rml.writer;

import java.io.PrintWriter;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.URIFormatter;

public class VOIDKR2RMLRDFWriter extends N3KR2RMLRDFWriter {

	private static final Logger LOG = LoggerFactory.getLogger(VOIDKR2RMLRDFWriter.class);

	protected String datasetURI;
	protected String subsetURI; 
	protected Properties datasetProperties;
	protected ConcurrentHashMap<String, String> distinctSubjects = new ConcurrentHashMap<String, String>();
	protected ConcurrentHashMap<String, String> distinctPredicates = new ConcurrentHashMap<String, String>();
	protected ConcurrentHashMap<String, String> distinctObjects = new ConcurrentHashMap<String, String>();
	protected ConcurrentHashMap<String, String> distinctClasses = new ConcurrentHashMap<String, String>();
	protected int triplesCount;
	protected boolean generateStatistics;
	
	public VOIDKR2RMLRDFWriter(String datasetURI, String subsetURI, Properties datasetProperties, PrintWriter pw, URIFormatter uriFormatter)
	{
		super(uriFormatter, pw);
		this.datasetProperties = datasetProperties;
		this.datasetURI = datasetURI;
	}
	
	@Override
	protected String buildTriple(String subject, String predicate, String object)
	{
		if(generateStatistics)
		{
			distinctSubjects.put(subject, "");
			distinctPredicates.put(predicate, "");
			
			if(predicate.equalsIgnoreCase("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))
			{
				distinctClasses.put(object, "");
			}
			else
			{
				distinctObjects.put(object, "");
			}
		}
		return super.buildTriple(subject, predicate, object);
 	}
	
	@Override
	public void finishRow()
	{
	}
	@Override
	public void flush() {
	}
	@Override
	public void close() {
		
		generateStatistics = false;
		triplesCount = generatedTriples.size();
		this.generatedTriples = new ConcurrentHashMap<String, String>();
		outputStatistics();
		outputUserDefinedProperties();
		outputVocabularies();
		outWriter.close();

	}

	private void outputVocabularies() {
		
		
	}

	private void outputStatistics() {

		//TODO entities; 
		outputTripleWithLiteralObject(datasetURI, VoIDURIs.VOID_TRIPLES_URI, ""+triplesCount, null);
		outputTripleWithLiteralObject(datasetURI, VoIDURIs.VOID_CLASSES_URI, ""+distinctClasses.size(), null);
		outputTripleWithLiteralObject(datasetURI, VoIDURIs.VOID_PROPERTIES_URI, ""+ distinctPredicates.size(), null);
		outputTripleWithLiteralObject(datasetURI, VoIDURIs.VOID_DISTINCT_SUBJECTS_URI, ""+ distinctSubjects.size(), null);
		outputTripleWithLiteralObject(datasetURI, VoIDURIs.VOID_DISTINCT_OBJECTS_URI, ""+ distinctObjects.size(), null);
		
	}

	private void outputUserDefinedProperties() {
		// Output user defined properties
		for(String propertyName : datasetProperties.stringPropertyNames())
		{
			
		}
	}
}
