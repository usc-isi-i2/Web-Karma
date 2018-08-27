package edu.isi.karma.rdf;

import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by chengyey on 12/6/15.
 */
public class JSONImpl extends BaseRDFImpl {
    private static final long serialVersionUID = -1590543154341174334L;
	protected String atId = "@id";

    public JSONImpl(String propertyPath) {
        super(propertyPath);
    }

    public JSONImpl(Properties properties) {
    	super(properties);
    }
    
    public JSONImpl() {
        super();
    }

    @Override
    protected KR2RMLRDFWriter configureRDFWriter(StringWriter sw) {
        PrintWriter pw = new PrintWriter(sw);
        KR2RMLRDFWriter outWriter = new JSONKR2RMLRDFWriter(pw, karma.getBaseURI(), disableNesting);
        ContextIdentifier contextId = karma.getContextId();
        try {
            atId = getAtId(karma.getGenerator().getContext(contextId).getJSONObject("@context"));
        } catch(Exception e)
        {}
        return outWriter;
    }

    private String getAtId(JSONObject c) {
        @SuppressWarnings("rawtypes")
        Iterator itr = c.keys();
        while (itr.hasNext()) {
            String key = itr.next().toString();
            try {
                if (c.get(key).toString().equals("@id")) {
                    return key;
                }
            }
            catch(Exception e)
            {

            }
        }
        return atId;
    }

    public String getAtId() {
        return atId;
    }
}
