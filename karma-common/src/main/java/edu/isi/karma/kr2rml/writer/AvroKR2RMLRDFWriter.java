package edu.isi.karma.kr2rml.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.RecordBuilder;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.codehaus.jackson.JsonFactory;

import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.RefObjectMap;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.TriplesMapGraph;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.RepFactory;

public class AvroKR2RMLRDFWriter extends SFKR2RMLRDFWriter<GenericRecord> {

	protected Map<String, Schema> triplesMapIdToSchema = new HashMap<String, Schema>();
	protected RepFactory rep;
	protected Schema rootSchema;
	protected JsonEncoder jsonEncoder;
	private OutputStream output; 
	DatumWriter<GenericRecord> datumWriter;
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

		datumWriter = new GenericDatumWriter<GenericRecord>(rootSchema);
		jsonEncoder = EncoderFactory.get().jsonEncoder(rootSchema,  new JsonFactory().createJsonGenerator(output).useDefaultPrettyPrinter());
		
		
		
	}
	static int id = 1;
	protected Schema getSchemaForTriplesMap(TriplesMapGraph graph, String triplesMapId)
	{
		TriplesMap map = graph.getTriplesMap(triplesMapId);
		RecordBuilder<Schema> rb = SchemaBuilder.record("subjr"+(id++));
		
		
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
				
				fieldAssembler = addField(fieldAssembler, pom, isMap,
						targetSchema, predicateShortHand);
				
			
		}
		
		fieldAssembler = fieldAssembler.name("id").type().unionOf().array().items().stringType().and().stringType().and().nullType().endUnion().noDefault();
		fieldAssembler = fieldAssembler.name("rdf_type").type().unionOf().array().items().stringType().and().stringType().and().nullType().endUnion().noDefault();
	
		
		return fieldAssembler.endRecord();
	}

	private FieldAssembler<Schema> addField(
			FieldAssembler<Schema> fieldAssembler, PredicateObjectMap pom,
			boolean isMap, Schema targetSchema, String predicateShortHand) {
		if(isMap)
		{
			if(targetSchema == null)
			{
				fieldAssembler = fieldAssembler.name(pom.getPredicate().getId()).type().map().values().unionOf().array().items().stringType().and().stringType().and().nullType().endUnion().noDefault();
			}
			else
			{
				fieldAssembler = fieldAssembler.name(pom.getPredicate().getId()).type().map().values().unionOf().array().items(targetSchema).and().type(targetSchema).and().nullType().endUnion().noDefault();
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
		return fieldAssembler;
	}

	@Override
	protected void initializeOutput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addValue(PredicateObjectMap pom, GenericRecord subject, String predicateUri,
			Object object) {
		//TODO maps for dynamic predicates
		String shortHandPredicateURI = shortHandURIGenerator.getShortHand(predicateUri).toString().replaceAll("[^\\w]", "_");
		if (subject.get(shortHandPredicateURI) != null || predicateUri.contains(Uris.RDF_TYPE_URI)) {
			
			addValueToArray(pom, subject, object,
					shortHandPredicateURI);
		}
		else
		{
			Schema schema = subject.getSchema();
			Field field = schema.getField(shortHandPredicateURI);
			Field mapField = schema.getField(pom.getPredicate().getId());
			if(field != null)
			{
				subject.put(shortHandPredicateURI, object);
			}
			else if(mapField != null && mapField.schema().getType() == Schema.Type.MAP)
			{
				addValueToMap(pom, subject,object, shortHandPredicateURI);
			}
		}
		
	}

	protected void addValueToMap(PredicateObjectMap pom, GenericRecord subject, Object object,
			String shortHandPredicateURI)
	{
		//TODO
		
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
				GenericRecord record = (GenericRecord)currentObj;
				array = new GenericData.Array<GenericRecord>(record.getSchema(), new LinkedList<GenericRecord>());
				array.add((GenericRecord)object);
				array.add((GenericRecord)currentObj);
			}
		}
		else
		{
				GenericRecord objectToAdd = (GenericRecord)object;
				array = new GenericData.Array<GenericRecord>(objectToAdd.getSchema(), new LinkedList<GenericRecord>());
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
					strings = new GenericData.Array<String>(SchemaBuilder.nullable().stringBuilder().endString(), new LinkedList<String>());
					strings.add((String)object);
					strings.add((String)currentObj);
				}
			}
			else
			{
					String objectToAdd = (String)object;
					strings = new GenericData.Array<String>(SchemaBuilder.array().items().stringType(), new LinkedList<String>());
					strings.add(objectToAdd);
				
			}
			subject.put(shortHandPredicateURI, strings);
		}
		
	}

	@Override
	public void close() {
		for(GenericRecord record : this.rootObjects.values())
		{
			try {
				datumWriter.write(record, this.jsonEncoder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			jsonEncoder.flush();
			output.flush();
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void collapseSameType(GenericRecord obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GenericRecord getNewObject(String triplesMapId, String subjUri) {
		GenericRecord record =new GenericData.Record(this.triplesMapIdToSchema.get(triplesMapId));
		record.put("id", subjUri);
		return record;
	}
	
	@Override
	protected Object convertValueWithLiteralType(String literalType, String value) {
		return value;
	}
}
