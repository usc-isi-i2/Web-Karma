package edu.isi.karma.rdf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.ObjectImport;
import edu.isi.karma.imp.ObjectImportEntity;
import edu.isi.karma.imp.avro.AvroImport;
import edu.isi.karma.imp.csv.CSVImport;
import edu.isi.karma.imp.excel.ToCSV;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.rdf.InputProperties.InputProperty;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class WorksheetGenerator {

	private static Logger logger = LoggerFactory.getLogger(WorksheetGenerator.class);

	public static class ObjectEntityIterator implements Iterator<ObjectImportEntity> {

		private List<String> headers;
		private Iterator<List<String>> valuesItr;

		public ObjectEntityIterator(List<String> headers, List<List<String>> values) {
			this.headers = headers;
			this.valuesItr = values.iterator();
		}

		@Override
		public boolean hasNext() {
			return valuesItr.hasNext();
		}

		@Override
		public ObjectImportEntity next() {
			return new ObjectImportEntity(headers, valuesItr.next(), Collections.<String>emptyList());
		}

		@Override
		public void remove() {
			valuesItr.remove();

		}

	}

	protected static Worksheet generateWorksheet(String sourceName, List<String> headers, List<List<String>> data,
			Workspace workspace) throws IOException, KarmaException {
		ObjectImport oi = new ObjectImport(sourceName, workspace, null, new ObjectEntityIterator(headers, data));
		Worksheet worksheet = oi.generateWorksheet();
		return worksheet;
	}

	protected static Worksheet generateWorksheet(String sourceName, BufferedInputStream is, InputType inputType,
			InputProperties inputParameters, Workspace workspace) throws IOException, KarmaException {
		Worksheet worksheet = null;
		try {
			is.mark(Integer.MAX_VALUE);
			String encoding = null;
			if (inputType == null) {
				Metadata metadata = new Metadata();
				metadata.set(Metadata.RESOURCE_NAME_KEY, sourceName);
				DefaultDetector detector = new DefaultDetector();
				MediaType type = detector.detect(is, metadata);

				ContentHandler contenthandler = new BodyContentHandler();
				AutoDetectParser parser = new AutoDetectParser();
				try {
					parser.parse(is, contenthandler, metadata);
				} catch (SAXException | TikaException e) {
					logger.error("Unable to parse stream: " + e.getMessage());
					throw new KarmaException("Unable to parse stream: " + e.getMessage());
				}
				MediaTypeRegistry registry = MimeTypes.getDefaultMimeTypes().getMediaTypeRegistry();
				registry.addSuperType(new MediaType("text", "csv"), new MediaType("text", "plain"));
				MediaType parsedType = MediaType.parse(metadata.get(Metadata.CONTENT_TYPE));

				if (registry.isSpecializationOf(registry.normalize(type),
						registry.normalize(parsedType).getBaseType())) {
					metadata.set(Metadata.CONTENT_TYPE, type.toString());
				}
				logger.info("Detected " + metadata.get(Metadata.CONTENT_TYPE));
				inputType = getInputType(metadata);
				encoding = metadata.get(Metadata.CONTENT_ENCODING);
			} else if (inputParameters.get(InputProperty.ENCODING) != null) {
				encoding = (String) inputParameters.get(InputProperty.ENCODING);
			} else {
				encoding = EncodingDetector.detect(is);
			}
			is.reset();

			if (inputType == null) {
				throw new KarmaException("Content type unrecognized");
			}

			inputParameters.set(InputProperty.ENCODING, encoding);

			switch (inputType) {
			case JSON: {

				worksheet = generateWorksheetFromJSONStream(sourceName, is, inputParameters, workspace);
				break;
			}
			case XML: {
				worksheet = generateWorksheetFromXMLStream(sourceName, is, inputParameters, workspace);
				break;
			}
			case CSV: {
				worksheet = generateWorksheetFromDelimitedStream(sourceName, is, inputParameters, workspace);
				break;
			}
			case EXCEL: {
				worksheet = generateWorksheetFromExcelStream(sourceName, is, inputParameters, workspace);
				break;
			}
			case AVRO: {
				worksheet = generateWorksheetFromAvroStream(sourceName, is, inputParameters, workspace);
				break;
			}
			case JL: {
				worksheet = generateWorksheetFromJLStream(sourceName, is, inputParameters, workspace);
			}

			}
		} catch (Exception e) {
			logger.error("Error generating worksheet", e);
			throw new KarmaException("Unable to generate worksheet: " + e.getMessage());
		}
		if (worksheet == null) {
			throw new KarmaException("Content type unrecognized");
		}
		return worksheet;
	}

	private static InputType getInputType(Metadata metadata) {
		String[] contentType = metadata.get(Metadata.CONTENT_TYPE).split(";");
		switch (contentType[0]) {
		case "application/json": {
			return InputType.JSON;
		}
		case "application/xml": {
			return InputType.XML;
		}
		case "text/csv": {
			return InputType.CSV;
		}
		case "text/excel": {
			return InputType.EXCEL;
		}
		case "text/x-excel": {
			return InputType.EXCEL;
		}
		}
		return null;
	}

	private static Worksheet generateWorksheetFromExcelStream(String sourceName, InputStream is,
			InputProperties inputTypeParams, Workspace workspace)
			throws IOException, KarmaException, ClassNotFoundException, InvalidFormatException {

		int worksheetIndex = (inputTypeParams.get(InputProperty.WORKSHEET_INDEX) != null)
				? (int) inputTypeParams.get(InputProperty.WORKSHEET_INDEX)
				: 1;

		// Convert the Excel file to a CSV file.
		ToCSV csvConverter = new ToCSV();
		StringWriter writer = new StringWriter();
		csvConverter.convertWorksheetToCSV(is, worksheetIndex - 1, writer);
		String csv = writer.toString();
		InputStream sheet = IOUtils.toInputStream(csv);
		return generateWorksheetFromDelimitedStream(sourceName, sheet, inputTypeParams, workspace);
	}

	private static Worksheet generateWorksheetFromDelimitedStream(String sourceName, InputStream is,
			InputProperties inputTypeParams, Workspace workspace)
			throws IOException, KarmaException, ClassNotFoundException {
		Worksheet worksheet;
		int headerStartIndex = (inputTypeParams.get(InputProperty.HEADER_START_INDEX) != null)
				? (int) inputTypeParams.get(InputProperty.HEADER_START_INDEX)
				: 1;
		int dataStartIndex = (inputTypeParams.get(InputProperty.DATA_START_INDEX) != null)
				? (int) inputTypeParams.get(InputProperty.DATA_START_INDEX)
				: 2;
		char delimiter = (inputTypeParams.get(InputProperty.DELIMITER) != null)
				? ((String) inputTypeParams.get(InputProperty.DELIMITER)).charAt(0)
				: ',';

		char qualifier = (inputTypeParams.get(InputProperty.TEXT_QUALIFIER) != null)
				? ((String) inputTypeParams.get(InputProperty.TEXT_QUALIFIER)).charAt(0)
				: '\"';

		String encoding = (String) inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)
				? (int) inputTypeParams.get(InputProperty.MAX_NUM_LINES)
				: -1;

		Import fileImport = new CSVImport(headerStartIndex, dataStartIndex, delimiter, qualifier, encoding, maxNumLines,
				sourceName, is, workspace, null);

		worksheet = fileImport.generateWorksheet();
		return worksheet;
	}

	private static Worksheet generateWorksheetFromXMLStream(String sourceName, InputStream is, InputProperties inputTypeParams,
			Workspace workspace) throws IOException {
		Worksheet worksheet;
		String encoding = (String) inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)
				? (int) inputTypeParams.get(InputProperty.MAX_NUM_LINES)
				: -1;

		String contents = IOUtils.toString(is, encoding);
		JSONObject json = XML.toJSONObject(contents);
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}

	private static Worksheet generateWorksheetFromJSONStream(String sourceName, InputStream is,
			InputProperties inputTypeParams, Workspace workspace) throws IOException {
		Worksheet worksheet;
		String encoding = (String) inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)
				? (int) inputTypeParams.get(InputProperty.MAX_NUM_LINES)
				: -1;
		Reader reader = EncodingDetector.getInputStreamReader(is, encoding);
		Object json = JSONUtil.createJson(reader);
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}

	private static Worksheet generateWorksheetFromAvroStream(String sourceName, InputStream is,
			InputProperties inputTypeParams, Workspace workspace) throws IOException, JSONException, KarmaException {
		Worksheet worksheet;
		String encoding = (String) inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)
				? (int) inputTypeParams.get(InputProperty.MAX_NUM_LINES)
				: -1;
		AvroImport imp = new AvroImport(is, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}

	private static Worksheet generateWorksheetFromJLStream(String sourceName, InputStream is, InputProperties inputTypeParams,
			Workspace workspace) throws Exception {
		Worksheet worksheet;
		String encoding = (String) inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)
				? (int) inputTypeParams.get(InputProperty.MAX_NUM_LINES)
				: -1;
		Object json = JSONUtil.convertJSONLinesToJSONArray(is, encoding);

		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;

	}
}
