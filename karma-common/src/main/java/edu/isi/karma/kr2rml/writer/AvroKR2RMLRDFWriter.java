package edu.isi.karma.kr2rml.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.RecordBuilder;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.RefObjectMap;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.TriplesMapGraph;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.RepFactory;

public class AvroKR2RMLRDFWriter extends SFKR2RMLRDFWriter<GenericRecord> {

	private static Logger LOG = LoggerFactory.getLogger(AvroKR2RMLRDFWriter.class);
	protected Map<String, Schema> triplesMapIdToSchema = new HashMap<>();
	protected RepFactory rep;
	protected Schema rootSchema;
	private OutputStream output; 
	private DatumWriter<GenericRecord> datumWriter;
	private DataFileWriter<GenericRecord> dfw;	
	//TODO come up with a good naming convention for records
	private int id = 1;
	public AvroKR2RMLRDFWriter(OutputStream output)
	{
		super(new PrintWriter(output));
		this.output = output;
	}
	
	public void setRepFactory(RepFactory rep)
	{
		this.rep = rep;
	}
	public void setProcessingOrder(Map<TriplesMapGraph, List<String>> triplesMapProcessingOrder) throws IOException
	{

		for(Entry<TriplesMapGraph, List<String>> entry : triplesMapProcessingOrder.entrySet())
		{
			for(String triplesMapId : entry.getValue())
			{
				triplesMapIdToSchema.put(triplesMapId, getSchemaForTriplesMap(entry.getKey(), triplesMapId));
			}
		}
		String rootTriplesMapId = this.rootTriplesMapIds.iterator().next();
		rootSchema = triplesMapIdToSchema.get(rootTriplesMapId);

		datumWriter = new GenericDatumWriter<>(rootSchema);
		dfw = new DataFileWriter<>(datumWriter);
		dfw.create(rootSchema, output);
		
		
	}

	protected Schema getSchemaForTriplesMap(TriplesMapGraph graph, String triplesMapId)
	{
		TriplesMap map = graph.getTriplesMap(triplesMapId);
		RecordBuilder<Schema> rb = SchemaBuilder.record("subjr"+(id++));
		
		Set<String> currentPredicates = new HashSet<>(); 
		FieldAssembler<Schema> fieldAssembler = rb.fields();
		for(PredicateObjectMap pom : map.getPredicateObjectMaps())
		{
		
		
				boolean isMap = false;
				Schema targetSchema = null;
				String predicateShortHand = null;
				if(pom.getPredicate().getTemplate().getAllColumnNameTermElements().isEmpty())
				{
					String predicate = pom.getPredicate().getTemplate().getR2rmlTemplateString(rep);
					predicateShortHand = shortHandURIGenerator.getShortHand(predicate).toString().replaceAll("[^\\w]", "_");
				}
				else
				{
					isMap = true;
				}
				
				if(pom.getObject() != null && pom.getObject().hasRefObjectMap())
				{
					RefObjectMap refObjectMap = pom.getObject().getRefObjectMap();
					
					if(!refObjectMap.getParentTriplesMap().getId().equalsIgnoreCase(triplesMapId))
					{
							targetSchema = triplesMapIdToSchema.get(refObjectMap.getParentTriplesMap().getId());
					
					}
					
				}
				
				if(currentPredicates.add(predicateShortHand))
				{
					fieldAssembler = addField(fieldAssembler, pom, isMap,
						targetSchema, predicateShortHand);
				}
				else
				{
					//TODO handle conflicting types
					LOG.warn("Duplicate predicate detected in schema");
				}
		}
		
		fieldAssembler = fieldAssembler.name("id").type().unionOf().array().items().stringType().and().stringType().and().nullType().endUnion().noDefault();
		fieldAssembler = fieldAssembler.name("rdf_type").type().unionOf().array().items().stringType().and().stringType().and().nullType().endUnion().noDefault();
	
		
		return fieldAssembler.endRecord();
	}

	private FieldAssembler<Schema> addField(
			FieldAssembler<Schema> fieldAssembler, PredicateObjectMap pom,
			boolean isMap, Schema targetSchema, String predicateShortHand) {
		try{
		if(isMap)
		{
			if(targetSchema == null)
			{
				fieldAssembler = fieldAssembler.name(pom.getPredicate().getId().replaceAll("[^\\w]", "_")).type().unionOf().map().values().unionOf().map().values().stringType().and().stringType().and().nullType().endUnion().and().nullType().endUnion().noDefault();
			}
			else
			{
				fieldAssembler = fieldAssembler.name(pom.getPredicate().getId().replaceAll("[^\\w]", "_")).type().unionOf().map().values().unionOf().map().values(targetSchema).and().type(targetSchema).and().nullType().endUnion().and().nullType().endUnion().noDefault();
			}
			
		}
		else {
			if(targetSchema == null)
			{
				fieldAssembler = fieldAssembler.name(predicateShortHand).type().unionOf().array().items().stringType().and().stringType().and().nullType().endUnion().noDefault();
			}
			else
			{
				fieldAssembler = fieldAssembler.name(predicateShortHand).type().unionOf().array().items(targetSchema).and().type(targetSchema).and().nullType().endUnion().noDefault();
			}
		}
		}
		catch(Exception e)
		{
			LOG.error("Unable to add field: " + predicateShortHand + " for " + pom.getTriplesMap().getSubject().getTemplate().toString(), e);
		}
		
		return fieldAssembler;
	}

	@Override
	protected void initializeOutput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addValue(PredicateObjectMap pom, GenericRecord subject, String predicateUri,
			Object object) {
		String shortHandPredicateURI = shortHandURIGenerator.getShortHand(predicateUri).toString().replaceAll("[^\\w]", "_");
		Schema schema = subject.getSchema();
		Field field = schema.getField(shortHandPredicateURI);
		Field mapField = schema.getField(pom.getPredicate().getId().replaceAll("[^\\w]", "_"));
		if (subject.get(shortHandPredicateURI) != null || predicateUri.contains(Uris.RDF_TYPE_URI)) {
			
			if(field != null)
			{
				addValueToArray(pom, subject, object,
					shortHandPredicateURI);
			}
			else if(mapField != null && mapField.schema().getType() == Schema.Type.MAP)
			{
				addValueToMap(pom, subject,object, shortHandPredicateURI);
			}
		}
		else
		{
			if(field != null)
			{
				subject.put(shortHandPredicateURI, object);
			}
			else if(mapField != null && mapField.schema().getTypes().get(0).getType() == Schema.Type.MAP)
			{
				addValueToMap(pom, subject,object, shortHandPredicateURI);
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	protected void addValueToMap(PredicateObjectMap pom, GenericRecord subject, Object object,
			String shortHandPredicateURI)
	{

		String mapPredicateName = pom.getPredicate().getId().replaceAll("[^\\w]", "_");
		if(object instanceof String)
		{
			Map<String, String> values;
			if(subject.get(mapPredicateName)== null)
			{
				subject.put(mapPredicateName, new ConcurrentHashMap<String, String>());
			}
			values = (Map<String, String>)subject.get(mapPredicateName);
			values.put(shortHandPredicateURI, (String)object);
		}
		else if(object instanceof GenericRecord)
		{
			Map<String, GenericRecord> values;
			if(subject.get(mapPredicateName)== null)
			{
				subject.put(mapPredicateName, new ConcurrentHashMap<String, GenericRecord>());
			}
			values = (Map<String, GenericRecord>)subject.get(mapPredicateName);
			values.put(shortHandPredicateURI, (GenericRecord)object);
		}
	
		
	}
	@SuppressWarnings("unchecked")
	@Override
	protected void addValueToArray(PredicateObjectMap pom, GenericRecord subject, Object object,
			String shortHandPredicateURI) {
		Object currentObj = subject.get(shortHandPredicateURI);
		GenericArray<GenericRecord> array = null;
		GenericArray<String> strings = null;

		if(object instanceof GenericRecord)
		{
			if(currentObj != null)
			{
				if(currentObj instanceof GenericArray)
				{
					array = (GenericArray<GenericRecord>) currentObj;
					array.add((GenericRecord) object);
				}
				else if(currentObj instanceof GenericRecord)
				{
					array = new GenericData.Array<>(subject.getSchema().getField(shortHandPredicateURI).schema().getTypes().get(0), new LinkedList<GenericRecord>());
					array.add((GenericRecord)object);
					array.add((GenericRecord)currentObj);
				}
			}
			else
			{
					GenericRecord objectToAdd = (GenericRecord)object;
					array = new GenericData.Array<>(objectToAdd.getSchema(), new LinkedList<GenericRecord>());
					array.add(objectToAdd);
				
			}
			subject.put(shortHandPredicateURI, array);
		}
		else if(object instanceof String)
		{
			if(currentObj != null)
			{
				if(currentObj instanceof GenericArray)
				{
					strings = (GenericArray<String>) currentObj;
					strings.add((String) object);
				}
				else if(currentObj instanceof String)
				{
					strings = new GenericData.Array<>(SchemaBuilder.array().items().stringType(), new LinkedList<String>());
					strings.add((String)object);
					strings.add((String)currentObj);
				}
			}
			else
			{
					String objectToAdd = (String)object;
					strings = new GenericData.Array<>(SchemaBuilder.array().items().stringType(), new LinkedList<String>());
					strings.add(objectToAdd);
				
			}
			subject.put(shortHandPredicateURI, strings);
		}
		
	}
	
	@Override
	protected Object generateLanguageLiteral(Object literal, String language) {
		return literal;
	}
	
	@Override
	public void finishRow() {
		for(Map<String, GenericRecord> records : this.rootObjectsByTriplesMapId.values())
		{
			
			for(GenericRecord record : records.values()){ 
				try {
					collapseSameType(record);
					dfw.append(record);
					
				} catch (Exception e) {
					LOG.error("Unable to append Avro record to writer!", e);
				}
			}
			
		}
		for(Entry<String, ConcurrentHashMap<String, GenericRecord>> entry : this.rootObjectsByTriplesMapId.entrySet())
		{
			entry.getValue().clear();
		}
		for(Entry<String, ConcurrentHashMap<String, GenericRecord>> entry : this.generatedObjectsByTriplesMapId.entrySet())
		{
			entry.getValue().clear();
		}
		this.generatedObjectsWithoutTriplesMap.clear();
		
	};
	

	@Override
	public void close() {
		
		try {
			dfw.flush();
			output.flush();
			output.close();
		} catch (IOException e) {
			LOG.error("Unable to flush and close output!", e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void collapseSameType(GenericRecord obj) {
		
		for (Field f : obj.getSchema().getFields()) {
			Object value = obj.get(f.name());
			if(value == null)
			{
				continue;
			}
			if (value instanceof GenericRecord)
				collapseSameType((GenericRecord)value);
			if (value instanceof GenericArray) {
				GenericArray array = (GenericArray)value;
				Set<Object> valuesHash = new HashSet<>();
				boolean unmodified = true;
				for (int i = 0; i < array.size(); i++) {
					Object o = array.get(i);
					if (o instanceof GenericRecord)
						collapseSameType((GenericRecord) o);
					
					unmodified &= valuesHash.add(o);
					
					
				}
				if(!unmodified)
				{
					GenericArray<Object> newValues = new GenericData.Array<>(array.getSchema(), valuesHash);
					obj.put(f.name(), newValues);
				}
			}
		}
		
	}

	@Override
	public GenericRecord getNewObject(String triplesMapId, String subjUri) {
		GenericRecord record =new GenericData.Record(this.triplesMapIdToSchema.get(triplesMapId));
		record.put("id", subjUri);
		return record;
	}
	
	@Override
	protected Object convertLiteral(String value, String literalType, String language) {
		return value;
	}

	@Override
	public void setR2RMLMappingIdentifier(
			R2RMLMappingIdentifier mappingIdentifer) {
		
	}
}
