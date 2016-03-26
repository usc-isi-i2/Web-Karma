package edu.isi.karma.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodingDetector {
    
	private static Logger logger = LoggerFactory.getLogger(EncodingDetector.class);
    public final static String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    private EncodingDetector() {
    }

    public static String detect(InputStream is) throws IOException {

        byte[] buf = new byte[4096];
        // (1)
        UniversalDetector detector = new UniversalDetector(null);

        // (2)
        int nread;
        while ((nread = is.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        // (3)
        detector.dataEnd();

        // (4)
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            logger.debug("Detected encoding = " + encoding);
        } else {
            logger.debug("No encoding detected, using default: " + DEFAULT_ENCODING);
            encoding = DEFAULT_ENCODING;
        }
		encoding = validateEncoding(encoding);	
        // (5)
        detector.reset();
        
        return encoding;
    }
    
    public static String detect(File file) {
        try {
	        FileInputStream fis = new FileInputStream(file);
	        
	        String encoding = EncodingDetector.detect(fis);
	        
	        logger.debug("Detected encoding for file: " + file.getName() + ": " + encoding);
	        if (encoding == null) {
	            encoding = DEFAULT_ENCODING;
	        }
			encoding = validateEncoding(encoding);	
	        return encoding;
        } catch(Exception e) {
        	logger.debug("Exception detecting encoding, using default: " + DEFAULT_ENCODING);
        }
        return DEFAULT_ENCODING;
    }

    public static InputStreamReader getInputStreamReader(InputStream is, String encoding) throws IOException {
        
        logger.debug("Reading stream: using encoding: " + encoding);
		encoding = validateEncoding(encoding);	
        BOMInputStream bis = new BOMInputStream(is); //So that we can remove the BOM
        return new InputStreamReader(bis, encoding);
    }

	private static String validateEncoding(String encoding) {
		if(!isCharsetSupported(encoding))
		{
			logger.error("Encoding: " + encoding + " not supported. Falling back to UTF-8");
			encoding = "UTF-8";
		}
		return encoding;
	}
    
    public static boolean isCharsetSupported(String name) {
	    return Charset.availableCharsets().keySet().contains(name);
	}
    
    public static InputStreamReader getInputStreamReader(File file, String encoding) throws IOException {
        
        FileInputStream fis = new FileInputStream(file);
        logger.debug("Reading file: " + file + " using encoding: " + encoding);
        BOMInputStream bis = new BOMInputStream(fis); //So that we can remove the BOM
        return new InputStreamReader(bis, encoding);
    }
    
    public static String getString(File file, String encoding) throws IOException {
        StringWriter sw = new StringWriter();
        
        FileInputStream fis = new FileInputStream(file);
        logger.debug("Reading file: " + file + " using encoding: " + encoding);
		encoding = validateEncoding(encoding);	
        IOUtils.copy(fis, sw, encoding);

        return sw.toString();
    }
}