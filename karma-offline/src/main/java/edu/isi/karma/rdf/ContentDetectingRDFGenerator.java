package edu.isi.karma.rdf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class ContentDetectingRDFGenerator extends GenericRDFGenerator {

	private static Logger logger = LoggerFactory.getLogger(ContentDetectingRDFGenerator.class);
	
	
	@Override
	protected Worksheet generateWorksheet(String sourceName, InputStream data,
			Workspace workspace, int maxNumLines) throws IOException, KarmaException {
		Worksheet worksheet = null;
		try{

    	   worksheet = generateWorksheetFromUnknownStream(new BufferedInputStream(data), sourceName,
					workspace,  maxNumLines, worksheet);
		}
		catch (Exception e )
		{
			throw new KarmaException("Unable to generate worksheet: " + e.getMessage());
		}
	     if(worksheet == null)
	     {
	     	throw new KarmaException("Content type unrecognized");
	     }
	     return worksheet;
	}

	private Worksheet generateWorksheetFromUnknownStream(BufferedInputStream is, String sourceName,
			Workspace workspace, int maxNumLines,
			Worksheet worksheet) throws IOException,
			KarmaException, ClassNotFoundException {
		
		   Metadata metadata = new Metadata();
		   metadata.set(Metadata.RESOURCE_NAME_KEY, sourceName);
		   DefaultDetector detector = new DefaultDetector();
		   MediaType type = detector.detect(is, metadata);
		   
		   ContentHandler contenthandler = new BodyContentHandler();  
		   AutoDetectParser parser = new AutoDetectParser();
		   try {
			parser.parse(is, contenthandler,  metadata);
			} catch (SAXException | TikaException e) {
				logger.error("Unable to parse stream: " + e.getMessage());
				throw new KarmaException("Unable to parse stream: " + e.getMessage());
			}
		   MediaTypeRegistry registry =MimeTypes.getDefaultMimeTypes().getMediaTypeRegistry();
		   registry.addSuperType(new MediaType("text","csv"), new MediaType("text","plain"));
		   MediaType parsedType = MediaType.parse(metadata.get(Metadata.CONTENT_TYPE));
		   
		   if(registry.isSpecializationOf(registry.normalize(type), registry.normalize(parsedType).getBaseType()))
		   {
			   metadata.set(Metadata.CONTENT_TYPE, type.toString());
		   }
		   logger.info("Detected " + metadata.get(Metadata.CONTENT_TYPE));
		   String[] contentType = metadata.get(Metadata.CONTENT_TYPE).split(";");
		   String encoding = metadata.get(Metadata.CONTENT_ENCODING);
		   is.reset();
		   switch(contentType[0])
		   {
		   	case "application/json":{
		   		
		   	  worksheet = generateWorksheetFromJSONStream(sourceName, is, workspace,
					encoding, maxNumLines);
		   	  break;
		   	}
		   	case "application/xml":{
		        worksheet = generateWorksheetFromXMLStream(sourceName, is, workspace,
						encoding, maxNumLines);
		     	  break;
		     	}
		   	case "text/csv":{
		        worksheet = generateWorksheetFromDelimitedStream(sourceName, is, workspace,
						encoding);
		   	  break;
		   	}
		   }
		return worksheet;
	}
	
	private Worksheet generateWorksheetFromDelimitedStream(String sourceName, InputStream is,
			Workspace workspace, String encoding) throws IOException,
			KarmaException, ClassNotFoundException {
		Worksheet worksheet;
		Import fileImport = new CSVImport(1, 2, ',', '\"', encoding, -1, sourceName, is, workspace);

		worksheet = fileImport.generateWorksheet();
		return worksheet;
	}

	private Worksheet generateWorksheetFromXMLStream(String sourceName, InputStream is,
			Workspace workspace, String encoding, int maxNumLines)
			throws IOException {
		Worksheet worksheet;
		String contents = IOUtils.toString(is, encoding);
		JSONObject json = XML.toJSONObject(contents);
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}

	private Worksheet generateWorksheetFromJSONStream(String sourceName, InputStream is,
			Workspace workspace, String encoding, int maxNumLines)
			throws IOException {
		Worksheet worksheet;
		Reader reader = EncodingDetector.getInputStreamReader(is, encoding);
		Object json = JSONUtil.createJson(reader);
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}

}
