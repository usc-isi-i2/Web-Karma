import java.io.IOException;
import java.util.LinkedList;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.RecordBuilder;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;



public class TestKR2RMLAvroWriter {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws IOException {
		RecordBuilder<Schema> rb = SchemaBuilder.record("derp");
		Schema schemaHerp = SchemaBuilder.record("herp").fields().requiredString("name").endRecord();
		Schema schemaDerp = rb.fields().name("a").type().unionOf().array().items(schemaHerp).and().type(schemaHerp).and().nullType().endUnion().noDefault().endRecord();
		
		
		GenericRecord datumHerp1 = new GenericData.Record(schemaHerp);
		datumHerp1.put("name", "Jason");
		GenericRecord datumHerp2 = new GenericData.Record(schemaHerp);
		datumHerp2.put("name", "James");
		GenericRecord datumDerp = new GenericData.Record(schemaDerp);
		datumDerp.put("a", datumHerp1);
		datumDerp.put("a", datumHerp2);
		GenericArray<GenericRecord> herpRecords = new GenericData.Array<GenericRecord>(schemaDerp.getField("a").schema().getTypes().get(0), new LinkedList<GenericRecord>());
		herpRecords.add(datumHerp1);
		herpRecords.add(datumHerp2);
		datumHerp2.put("name", "Anthony");
		datumDerp.put("a", herpRecords);
		datumHerp2.put("name", "Anthony");
		
		DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schemaDerp);
		DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
		
		ByteArrayOutputStream bos =new ByteArrayOutputStream(10000);
		
		dataFileWriter.create(schemaDerp,bos);
		dataFileWriter.append(datumDerp);
		dataFileWriter.close();
		byte[] serializedDerp = bos.toByteArray();
		DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schemaDerp);
		DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(new SeekableByteArrayInput(serializedDerp), datumReader);
		GenericRecord derp = null;
		while (dataFileReader.hasNext()) {
		// Reuse user object by passing it to next(). This saves us from
		// allocating and garbage collecting many objects for files with
		// many items.
		derp = dataFileReader.next(derp);
		System.out.println(derp);
		}
		
	}

}
