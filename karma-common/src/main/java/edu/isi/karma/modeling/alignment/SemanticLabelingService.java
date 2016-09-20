package edu.isi.karma.modeling.alignment;

import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Encoder;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class SemanticLabelingService {
    private static String SEMANTIC_TYPE_PART = "/semantic_types";
    private static String COLUMN_PART = "/columns";
    private static String BASE_URL = "http://52.38.65.60:80";

    public SemanticLabelingService() {

    }

    // GET
    private String GET(String urlPart) throws IOException {
        URL url = new URL( BASE_URL + urlPart);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        InputStream response = new BufferedInputStream(connection.getInputStream());
        BufferedReader reader =  new BufferedReader(new InputStreamReader(response));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
    // POST
    private String POST(String urlPart, String body) throws IOException{
        URL url = new URL( BASE_URL + urlPart);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        Writer w = new OutputStreamWriter(os, "UTF-8");
        w.write(body);
        w.close();
        os.close();
        InputStream response = new BufferedInputStream(connection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(response));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
    public String get(String query) throws IOException {
        if (!query.equals(""))
            query = "?" + query;
        return GET(SEMANTIC_TYPE_PART + query);
    }
    public String post(String query) throws IOException {
        if (!query.equals(""))
            query = "?" + query;
        return POST(SEMANTIC_TYPE_PART + query, "");
    }
    public String get(String query, String id){
        return "";
    }
    public String post(String query, String id, String body) throws IOException{
        if (!query.equals(""))
            query = "?" + query;
        return POST(SEMANTIC_TYPE_PART + "/" + id + COLUMN_PART + query, body);
    }
    public String get(String query, String id, String column_id){
        return "";
    }
    public static String getSemanticTypeId(String domain, String type) throws MalformedURLException {
        URL url = new URL(domain);
        String namespace = Base64.encodeBase64String((url.getProtocol() + "://" + url.getHost()).getBytes()).replace("/","-");
        String part = Base64.encodeBase64String((domain + "\n" + type).getBytes()).replace("/", "-");
        return Base64.encodeBase64String((namespace + "\n" + part).getBytes()).replace("/", "-");
    }
}
