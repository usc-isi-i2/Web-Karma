package edu.isi.karma.imp.avro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonFactory;
import org.json.JSONException;

import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class AvroImport extends Import {

	private int maxNumLines;
	//TODO writing to a file each time is a hack, but avro seems to like it.
	private File file;
	private String encoding;
	private String worksheetName;
	
	public AvroImport (InputStream stream, String worksheetName, Workspace workspace,
			String encoding, int maxNumLines) throws IOException
	{
		super(worksheetName, workspace, encoding);
		this.maxNumLines = maxNumLines;
		this.encoding = encoding;
		this.worksheetName = worksheetName;
		this.file = File.createTempFile("karma-avro"+System.currentTimeMillis(), "avro");
		FileOutputStream fw = new FileOutputStream(file);
		fw.write(IOUtils.toByteArray(stream));
		fw.flush();
		fw.close();
		this.file.deleteOnExit();
	}
	
	public AvroImport duplicate() {
		return new AvroImport(this.file, this.worksheetName, this.workspace, this.encoding, this.maxNumLines);
	}
	
	public AvroImport (String string, String worksheetName, Workspace workspace,
			String encoding, int maxNumLines) throws IOException
	{
		super(worksheetName, workspace, encoding);
		this.maxNumLines = maxNumLines;
		this.worksheetName = worksheetName;
		this.encoding = encoding;
		this.file = File.createTempFile("karma-avro"+System.currentTimeMillis(), "avro");
		FileWriter fw = new FileWriter(file);
		fw.write(string);
		fw.flush();
		fw.close();
		this.file.deleteOnExit();
	}
	
	public AvroImport (File file, String worksheetName, Workspace workspace,
			String encoding, int maxNumLines) 
	{
		super(worksheetName, workspace, encoding);
		this.maxNumLines = maxNumLines;
		this.worksheetName = worksheetName;
		this.encoding = encoding;
		this.file = file;
	}

	@Override
	public Worksheet generateWorksheet() throws JSONException, IOException,
			KarmaException {
		DataFileReader<Void> schemareader = new DataFileReader<>(file, new GenericDatumReader<Void>());
		Schema schema = schemareader.getSchema();
		schemareader.close();
		DataFileReader<GenericRecord> reader = new DataFileReader<>(file, new GenericDatumReader<GenericRecord>(schema));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write('[');
		baos.write('\n');
		GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(reader.getSchema());
		while(reader.hasNext())
		{
			
			GenericRecord record = reader.next();
				JsonEncoder encoder = EncoderFactory.get().jsonEncoder(reader.getSchema(), new JsonFactory().createJsonGenerator(baos)).configure(baos);
				writer.write(record, encoder);
				encoder.flush();
				if(reader.hasNext())
				{
					baos.write(',');
				}
				
			
		}
		reader.close();
		baos.write('\n');
		baos.write(']');
		baos.flush();
		baos.close();
		String json = new String(baos.toByteArray());
		JsonImport jsonImport = new JsonImport(json, this.getFactory(), this.getWorksheet(), workspace, maxNumLines);
		return jsonImport.generateWorksheet();
	}
}
