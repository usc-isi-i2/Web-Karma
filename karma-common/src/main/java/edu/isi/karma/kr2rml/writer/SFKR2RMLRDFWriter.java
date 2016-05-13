package edu.isi.karma.kr2rml.writer;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.Prefix;
import edu.isi.karma.kr2rml.ShortHandURIGenerator;

public abstract class SFKR2RMLRDFWriter<E> extends KR2RMLRDFWriter {
	protected boolean disableNesting = false;
	protected boolean firstObject = true;
	protected ConcurrentHashMap<String, ConcurrentHashMap<String, E>> generatedObjectsByTriplesMapId;
	protected ConcurrentHashMap<String, E> generatedObjectsWithoutTriplesMap;
	protected ConcurrentHashMap<String, ConcurrentHashMap<String, E>> rootObjectsByTriplesMapId = new ConcurrentHashMap<>();
	protected ShortHandURIGenerator shortHandURIGenerator = new ShortHandURIGenerator();
	protected String rootTriplesMapId; 
	protected Set<String> rootTriplesMapIds;
	protected String baseURI = "";
	private static Set<String> numericLiteralTypes = new HashSet<>();
	static {
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#decimal");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#integer");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#nonPositiveInteger");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#negativeInteger");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#long");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#int");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#short");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#byte");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#unsignedLong");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#unsignedInt");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#unsignedShort");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#unsignedInt");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#unsignedByte");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#positiveInteger");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#float");
		numericLiteralTypes.add("http://www.w3.org/2001/XMLSchema#double");
	}
	public SFKR2RMLRDFWriter (PrintWriter outWriter) {
		this.outWriter = outWriter;
		generatedObjectsWithoutTriplesMap = new ConcurrentHashMap<>();
		generatedObjectsByTriplesMapId = new ConcurrentHashMap<>();
		rootTriplesMapIds = new HashSet<>();
		this.rootObjectsByTriplesMapId.put("", new ConcurrentHashMap<String, E>());
		initializeOutput();
	}
	
	public SFKR2RMLRDFWriter (PrintWriter outWriter, String baseURI) {
		this(outWriter);
		if (baseURI != null)
			this.baseURI = baseURI;
	}
	
	public SFKR2RMLRDFWriter (PrintWriter outWriter, String baseURI, boolean disableNesting) {
		this(outWriter, baseURI);
		this.disableNesting = disableNesting;
	}
	
	protected abstract void initializeOutput();

	@Override
	public void outputTripleWithURIObject(String subjUri, String predicateUri,
			String objectUri) {
		E subject = checkAndAddsubjUri(null, generatedObjectsWithoutTriplesMap, subjUri);
		E object = getGeneratedObject(generatedObjectsWithoutTriplesMap, objectUri);
		addValue(null, subject, predicateUri, object !=null? object : objectUri);
		if(!disableNesting)
		{
			rootObjectsByTriplesMapId.get("").remove(objectUri);
		}
	}

	@Override
	public void outputTripleWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType, String language) {
		E subject = checkAndAddsubjUri(null, generatedObjectsWithoutTriplesMap, subjUri);
		addValue(null, subject, predicateUri, convertLiteral(value, literalType, language));
	}

	@Override
	public void outputQuadWithLiteralObject(String subjUri,
			String predicateUri, String value, String literalType, String language, String graph) {
		outputTripleWithLiteralObject(subjUri, predicateUri, value, literalType, language);
	}

	protected E checkAndAddSubjUri(String triplesMapId, String subjUri)
	{
		ConcurrentHashMap<String, E> generatedObjects = generatedObjectsByTriplesMapId.get(triplesMapId);
		if(null == generatedObjects)
		{
			generatedObjectsByTriplesMapId.putIfAbsent(triplesMapId, new ConcurrentHashMap<String, E>());
			generatedObjects = generatedObjectsByTriplesMapId.get(triplesMapId);
		}
		return checkAndAddsubjUri(triplesMapId, generatedObjects, subjUri);
	}
	protected E checkAndAddsubjUri(String triplesMapId, ConcurrentHashMap<String, E> generatedObjects, String subjUri) {
		if (!generatedObjects.containsKey(subjUri)) {
			E object = getNewObject(triplesMapId, subjUri);
			
			generatedObjects.putIfAbsent(subjUri, object);
			object = generatedObjects.get(subjUri);
			if(triplesMapId == null || rootTriplesMapIds.isEmpty() || rootTriplesMapIds.contains(triplesMapId))
			{
				rootObjectsByTriplesMapId.get(triplesMapId).put(subjUri, object);
			}
			else if (disableNesting)
			{
				rootObjectsByTriplesMapId.get("").put(subjUri, object);
			}
			return object;
		}
		return generatedObjects.get(subjUri);
	}

	private void addURIObject(PredicateObjectMap pom, String subjUri,  String predicateUri, String objectUri)
	{
		E subject = checkAndAddSubjUri(pom.getTriplesMap().getId(), subjUri);
		if(pom.getObject().getRefObjectMap() == null)
		{
			addValue(pom, subject, predicateUri, objectUri);
			return;
		}
		String parentTriplesMapId = pom.getObject().getRefObjectMap().getParentTriplesMap().getId();		
		E object = getGeneratedObject(parentTriplesMapId, objectUri);
		String refParentObjectTriplesMapId = pom.getObject().getRefObjectMap().getParentTriplesMap().getId();
		if(object == null || rootTriplesMapIds.isEmpty() || rootTriplesMapIds.contains(refParentObjectTriplesMapId))
		{
			addValue(pom, subject, predicateUri, objectUri);
			return;
		}

		addValue(pom, subject, predicateUri, object);

	}

	protected abstract void addValue(PredicateObjectMap pom, E subject, String predicateUri, Object object);

	protected abstract void addValueToArray(PredicateObjectMap pom, E subject, Object object,
			String shortHandPredicateURI);

	@Override
	public void finishRow() {

	}

	@Override
	public void flush() {
		outWriter.flush();
	}

	@Override
	public abstract void close();

	@Override
	public void outputTripleWithURIObject(PredicateObjectMap predicateObjectMap,
			String subjUri, String predicateUri,
			String objectUri) {

		addURIObject(predicateObjectMap, subjUri, predicateUri, objectUri);
	}


	@Override
	public void outputTripleWithLiteralObject( PredicateObjectMap predicateObjectMap, 
			String subjUri, String predicateUri, String value,
			String literalType, String language) {
		E subject = checkAndAddSubjUri(predicateObjectMap.getTriplesMap().getId(), subjUri);
		//TODO should literal type be ignored?
		addValue(predicateObjectMap, subject, predicateUri, convertLiteral(value, literalType, language));
	}

	@Override
	public void outputQuadWithLiteralObject( PredicateObjectMap predicateObjectMap, 
			String subjUri, String predicateUri, String value,
			String literalType, String language, String graph) {

		E subject = checkAndAddSubjUri(predicateObjectMap.getTriplesMap().getId(), subjUri);
		//TODO should literal type be ignored?
		//TODO should graph be ignored?
		addValue(predicateObjectMap, subject, predicateUri, convertLiteral(value, literalType, language));

	}

	public void addPrefixes(Collection<Prefix> prefixes) {
		shortHandURIGenerator.addPrefixes(prefixes);
	}

	public E getGeneratedObject(String triplesMapId, String generatedObjectUri)
	{
		ConcurrentHashMap<String, E> generatedObjects = this.generatedObjectsByTriplesMapId.get(triplesMapId);
		return getGeneratedObject(generatedObjects, generatedObjectUri);
	}

	protected E getGeneratedObject(
			ConcurrentHashMap<String, E> generatedObjects, String generatedObjectUri) {
		if(null == generatedObjects)
		{
			return null;
		}
		return generatedObjects.get(generatedObjectUri);
	}

	public void addRootTriplesMapId(String rootTriplesMapId) {
		rootTriplesMapIds.add(rootTriplesMapId);
		this.rootObjectsByTriplesMapId.putIfAbsent(rootTriplesMapId, new ConcurrentHashMap<String, E>());
	}
	public void addRootTriplesMapIds(Collection<String> rootTriplesMapIds) {
		
		for(String rootTriplesMapId : rootTriplesMapIds)
		{
			this.rootTriplesMapIds.add(rootTriplesMapId);
			this.rootObjectsByTriplesMapId.putIfAbsent(rootTriplesMapId, new ConcurrentHashMap<String, E>());
		}
	}

	protected abstract void collapseSameType(E obj);
	protected abstract Object generateLanguageLiteral(Object literal, String language);
	
	
	protected Object convertLiteral(String value, String literalType, String language) {
		Object literalObj = generateLiteralWithType(value, literalType);
		if(language != null && !language.equals("")) {
			literalObj = generateLanguageLiteral(literalObj, language);
		}
		return literalObj;
	}
	
	protected Object generateLiteralWithType(String value, String literalType) {
		
		if (numericLiteralTypes.contains(literalType)) {
			ParsePosition parsePosition = new ParsePosition(0);
			Number n = NumberFormat.getNumberInstance(Locale.US).parse(value, parsePosition);
			if (parsePosition.getErrorIndex() != -1 || parsePosition.getIndex() < value.length())
				return value;
			else
				return n;
		}

		if (literalType != null && literalType.equals("http://www.w3.org/2001/XMLSchema#boolean")) {
			if (value.trim().equalsIgnoreCase("false"))
				return false;
			else if (value.trim().equalsIgnoreCase("true"))
				return true;
		}
		
		return value;
	}
	
	
	public abstract E getNewObject(String triplesMapId, String subjUri);


}
