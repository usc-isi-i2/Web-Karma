package edu.isi.karma.rdf;

import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.N3KR2RMLRDFWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by chengyey on 12/6/15.
 */
public class N3Impl extends BaseRDFImpl{
    @Override
    protected KR2RMLRDFWriter configureRDFWriter(StringWriter sw) {
        PrintWriter pw = new PrintWriter(sw);
        URIFormatter uriFormatter = new URIFormatter();
        N3KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
        outWriter.setBaseURI(karma.getBaseURI());
        return outWriter;
    }
}
