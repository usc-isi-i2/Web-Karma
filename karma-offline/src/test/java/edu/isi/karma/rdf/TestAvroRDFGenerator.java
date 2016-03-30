package edu.isi.karma.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.writer.AvroKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.webserver.ContextParametersRegistry;

public class TestAvroRDFGenerator extends TestJSONRDFGenerator {
	private static Logger logger = LoggerFactory.getLogger(TestBasicJSONRDFGenerator.class);
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		// Add the models in
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"people-model", getTestResource(
						 "people-model.ttl"));
		rdfGen.addModel(modelIdentifier);
		
		modelIdentifier = new R2RMLMappingIdentifier(
				"people-avro-model", getTestResource(
						 "people-avro-model.ttl"));
		rdfGen.addModel(modelIdentifier);
		
		modelIdentifier = new R2RMLMappingIdentifier(
				"people-array.avro-model", getTestResource(
						 "people-array.avro-model.ttl"));
		rdfGen.addModel(modelIdentifier);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	@Test
	public void testGenerateAvro1() {
		try {
			String filename = "people.json";
			logger.info("Loading json file: " + filename);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			AvroKR2RMLRDFWriter arvowriter = new AvroKR2RMLRDFWriter(baos);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			JSONKR2RMLRDFWriter jsonwriter = new JSONKR2RMLRDFWriter(pw);
			List<KR2RMLRDFWriter> writers = new LinkedList<>();
			writers.add(arvowriter);
			writers.add(jsonwriter);
			RDFGeneratorRequest request = new RDFGeneratorRequest("people-model", filename);
			request.setInputFile(new File(getTestResource(filename).toURI()));
			request.setAddProvenance(false);
			request.setDataType(InputType.JSON);
			request.addWriters(writers);
			request.setContextParameters(ContextParametersRegistry.getInstance().getDefault());
			rdfGen.generateRDF(request);
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split("(\r\n|\n)");
			int count = lines.length;
			assertEquals(148, count);
		} catch (Exception e) {
			logger.error("testGenerateAvro1 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}
	
	@Test
	public void testGenerateAvro2() {
		try {
			String filename = "people.avro";
			logger.info("Loading avro file: " + filename);
			File tempAvroOutput = File.createTempFile("testgenerateavro2", "avro");
			tempAvroOutput.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempAvroOutput );
			AvroKR2RMLRDFWriter arvowriter = new AvroKR2RMLRDFWriter(fos);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			JSONKR2RMLRDFWriter jsonwriter = new JSONKR2RMLRDFWriter(pw);
			List<KR2RMLRDFWriter> writers = new LinkedList<>();
			writers.add(arvowriter);
			writers.add(jsonwriter);
			RDFGeneratorRequest request = new RDFGeneratorRequest("people-avro-model", filename);
			request.setInputFile(new File(getTestResource(filename).toURI()));
			request.setAddProvenance(false);
			request.setDataType(InputType.AVRO);
			request.addWriters(writers);
			request.setContextParameters(ContextParametersRegistry.getInstance().getDefault());
			rdfGen.generateRDF(request);
			fos.flush();
			fos.close();
			DataFileReader<Void> schemareader = new DataFileReader<>(tempAvroOutput, new GenericDatumReader<Void>());
			Schema schema = schemareader.getSchema();
			
			DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
			DataFileReader<GenericRecord> reader = new DataFileReader<>(tempAvroOutput, datumReader);
			
			int count = 0;
			while(reader.hasNext())
			{
				reader.next();
				count ++;
			}
			reader.close();
			schemareader.close();
			assertEquals(7, count);
		} catch (Exception e) {
			logger.error("testGenerateAvro2 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}
	
	@Test
	public void testGenerateAvro3() {
		try {
			Parser parser = new Schema.Parser();
			Schema peopleSchema = parser.parse(new File(getTestResource("people.avsc").toURI()));
			GenericDatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(peopleSchema);
			DataFileWriter<GenericRecord> dfw = new DataFileWriter<>(datumWriter);
			File tempfile = File.createTempFile("karma-people", "avro");
			
			tempfile.deleteOnExit();
			dfw.create(peopleSchema, new FileOutputStream(tempfile));
			JSONArray array = new JSONArray(IOUtils.toString(new FileInputStream(new File(getTestResource("people.json").toURI()))));
			for(int i = 0; i < array.length(); i++)
			{
				dfw.append(generatePersonRecord(peopleSchema, array.getJSONObject(i)));
			}
			dfw.flush();
			dfw.close();
		} catch (Exception e) {
			logger.error("testGenerateAvro3 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}

	@Test
	public void testGenerateAvro4() {
		try {
			String filename = "people-array.avro";
			logger.info("Loading json file: " + filename);
			File tempAvroOutput = File.createTempFile("testgenerateavro4", "avro");
			tempAvroOutput.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempAvroOutput );
			AvroKR2RMLRDFWriter arvowriter = new AvroKR2RMLRDFWriter(fos);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			JSONKR2RMLRDFWriter jsonwriter = new JSONKR2RMLRDFWriter(pw);
			List<KR2RMLRDFWriter> writers = new LinkedList<>();
			writers.add(arvowriter);
			writers.add(jsonwriter);
			RDFGeneratorRequest request = new RDFGeneratorRequest("people-array.avro-model", filename);
			request.setInputFile(new File(getTestResource(filename).toURI()));
			request.setAddProvenance(false);
			request.setDataType(InputType.AVRO);
			request.addWriters(writers);
			request.setContextParameters(ContextParametersRegistry.getInstance().getDefault());
			rdfGen.generateRDF(request);
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			
		} catch (Exception e) {
			logger.error("testGenerateAvro4 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}
	
	private GenericRecord generatePersonRecord(Schema peopleSchema, JSONObject object)
	{
		return generatePersonRecord(peopleSchema, object.getString("name"), object.getString("email"), object.getString("title"), object.has("homepage")? object.getString("homepage") : null, object.getString("depiction"), object.has("twitter")? object.getString("twitter") : null);
	}
	
	private static String[] idNames = {"foaf:yahooChatId", "foaf:msnChatId", "foaf:skypeId" };
	private GenericRecord generatePersonRecord(Schema peopleSchema, String name, String email,String title, String homepage, String depiction,  String twitter)
	{
		GenericRecord record = new GenericData.Record(peopleSchema);
		record.put("name", name);
		record.put("email", email);
		record.put("title", title);
		record.put("homepage", homepage);
		record.put("depiction", depiction);
		record.put("twitter", twitter);
		Schema userIdArraySchema = peopleSchema.getField("userids").schema();
		Schema userIdSchema = userIdArraySchema.getElementType();
		GenericArray<GenericRecord> useridrecords = new GenericData.Array<>(userIdArraySchema, new LinkedList<GenericRecord>());
		
		Random r = new Random();
		for(int j = 0; j < 2; j++)
		{
			StringBuilder idBuilder = new StringBuilder();
			for(int i = 0; i < 8; i++)
			{
				idBuilder.append((char)(r.nextInt(26) + 'a'));
			}
			GenericRecord userId = new GenericData.Record(userIdSchema);
			userId.put("type", idNames[r.nextInt(idNames.length)]);
			userId.put("id", idBuilder.toString());
			useridrecords.add(userId);
		}
		record.put("userids", useridrecords);
		return record;
	}
}
