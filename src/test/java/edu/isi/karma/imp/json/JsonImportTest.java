package edu.isi.karma.imp.json;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;

public class JsonImportTest extends TestCase {

	protected Workspace workspace;
	protected RepFactory factory;
	protected StringWriter sw;
	protected PrintWriter pw;

	public JsonImportTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.factory = new RepFactory();
		this.workspace = factory.createWorkspace();
		sw = new StringWriter();
		pw = new PrintWriter(sw);
	}

	@SuppressWarnings("unused")
	public void testGenerateWorksheet01() throws JSONException {
		printAB(pw, "1");
		JSONObject o = new JSONObject(sw.toString());
		// System.err.println("JSON:" + o.toString());
		JsonImport ji = new JsonImport(o, "Test01", workspace);
		Worksheet w = ji.generateWorksheet();
		// prettyPrintWorksheet(w);
	}

	@SuppressWarnings("unused")
	public void testGenerateWorksheet02() throws JSONException {
		pw.println("[");
		printAB(pw, "1");
		pw.println(",");
		printABC(pw, "2");
		pw.println("]");

		JSONArray o = new JSONArray(sw.toString());
		// System.err.println("JSON:" + o.toString());

		JsonImport ji = new JsonImport(o, "Test02", workspace);
		Worksheet w = ji.generateWorksheet();

		// prettyPrintWorksheet(w);
	}

	@SuppressWarnings("unused")
	public void testGenerateWorksheet03() throws JSONException {
		pw.println("{");
		pw.println(JSONUtil.doubleQuote("l") + ":");
		pw.println("[");
		printAB(pw, "1");
		pw.println(",");
		printABC(pw, "2");
		pw.println("]");
		pw.println("}");

		JSONObject o = new JSONObject(sw.toString());
		// System.err.println("JSON:" + o.toString());
		JsonImport ji = new JsonImport(o, "Test03", workspace);
		Worksheet w = ji.generateWorksheet();
		// prettyPrintWorksheet(w);
	}

	public void testGenerateWorksheet04() throws JSONException,
			FileNotFoundException {
		//FileReader r = new FileReader("samplejson.txt");

		// The contents of the file are now in this string, constructed using
		// the following statement:
		// System.err.println(JSONObject.quote(o.toString()));
		String jsonString = "{\"c\":[{\"d\":[{\"f\":\"_0_\"},{\"f\":\"_0_\"}],\"h\":\"_h0_\"},{\"d\":[{\"f\":\"_1_\"},{\"f\":\"_1_\"}],\"h\":\"_h1_\"}],\"a\":\"_a_\"}";

		Object o = JSONUtil.createJson(jsonString);
		// Object o = Util.createJson(new JSONTokener(r));

		// System.err.println("JSON:" + o.toString());
		JsonImport ji = new JsonImport(o, "Test04", workspace);
		Worksheet w = ji.generateWorksheet();
		prettyPrintWorksheet(w);
	}

	private void printAB(PrintWriter pw, String value) {
		pw.println("{");
		pw.println(JSONUtil.doubleQuote("a") + ":" + JSONUtil.doubleQuote(value) + " ,");
		pw.println(JSONUtil.doubleQuote("b") + ":" + JSONUtil.doubleQuote(value));
		pw.println("}");
	}

	private void printABC(PrintWriter pw, String value) {
		pw.println("{");
		pw.println(JSONUtil.doubleQuote("a") + ":" + JSONUtil.doubleQuote(value) + " ,");
		pw.println(JSONUtil.doubleQuote("b") + ":" + JSONUtil.doubleQuote(value) + " ,");
		pw.println(JSONUtil.doubleQuote("c") + ":" + JSONUtil.doubleQuote(value));
		pw.println("}");
	}

	private void prettyPrintWorksheet(Worksheet w) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		w.prettyPrint("", pw, factory);
		System.err.println(sw.toString());

	}
}
