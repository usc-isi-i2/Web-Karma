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
import org.mozilla.universalchardet.UniversalDetector;

public class EncodingDetector {
    
    private final static String DEFAULT = StandardCharsets.UTF_8.name();

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
            System.out.println("Detected encoding = " + encoding);
        } else {
            System.out.println("No encoding detected, using default: " + DEFAULT);
        }

        // (5)
        detector.reset();
        
        return encoding;
    }

    public static InputStreamReader getInputStreamReader(File file) throws IOException {
        
        FileInputStream fis = new FileInputStream(file);
        
        String encoding = EncodingDetector.detect(fis);
        
        fis = new FileInputStream(file);
        
        if (encoding == null) {
            encoding = DEFAULT;
        }

        return new InputStreamReader(fis, encoding);
    }
    
    public static String getString(File file) throws IOException {
        StringWriter sw = new StringWriter();
        
        FileInputStream fis = new FileInputStream(file);
        
        String encoding = EncodingDetector.detect(fis);
        
        fis = new FileInputStream(file);
        
        if (encoding == null) {
            encoding = DEFAULT;
        }
        
        IOUtils.copy(fis, sw, encoding);

        return sw.toString();
    }
}