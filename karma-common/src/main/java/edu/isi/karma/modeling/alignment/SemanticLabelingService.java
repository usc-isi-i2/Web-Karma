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
    private static String ID_DIVIDER = "-";  // The divider that is used to separate the different parts of ID's, like domain and type

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
        connection.setRequestProperty("Content-Type","application/json");
        OutputStream os = connection.getOutputStream();
        os.write(body.getBytes("UTF-8"));
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
        return POST(SEMANTIC_TYPE_PART + "/" + id + query, body);
    }
    public String get(String query, String id, String column_id){
        return "";
    }
    public static String getSemanticTypeId(String domain, String type) throws MalformedURLException {
        return Base64.encodeBase64String(domain.getBytes()) + ID_DIVIDER + Base64.encodeBase64String(type.getBytes());
    }
}
