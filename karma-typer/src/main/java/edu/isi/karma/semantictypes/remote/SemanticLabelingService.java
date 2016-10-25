package edu.isi.karma.semantictypes.remote;

import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabelComparator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SemanticLabelingService {
    private static String SEMANTIC_TYPE_PART = "/semantic_types";
    private static String COLUMN_PART = "/type";
    private static String PREDICT = "/predict";
    private static String BASE_URL = "http://52.38.65.60:80";
    private static String ID_DIVIDER = "-";  // The divider that is used to separate the different parts of ID's, like domain and type
    private static String DEFAULT_MODEL = "default";

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
    private String POST(String urlPart, String body) throws IOException {
        URL url = new URL( BASE_URL + urlPart);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type","application/json");
        OutputStream os = connection.getOutputStream();
        os.write(body.getBytes("UTF-8"));
        os.close();
        try {
            InputStream response = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e){
            if (connection.getResponseCode() == java.net.HttpURLConnection.HTTP_CONFLICT) {
                throw new IllegalStateException(connection.getResponseMessage());
            } else {
                throw e;
            }
        }
    }
    // DELETE
    private String DELETE(String urlPart) throws IOException {
        URL url = new URL( BASE_URL + urlPart);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("DELETE");
        InputStream response = new BufferedInputStream(connection.getInputStream());
        BufferedReader reader =  new BufferedReader(new InputStreamReader(response));
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
    // This is to delete column
    public String delete(String id) throws IOException {
        if (!id.equals(""))
            id = "/" + id;
        return DELETE(SEMANTIC_TYPE_PART + COLUMN_PART + id);

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
    public List<SemanticTypeLabel> predict(String values, int numPredictions){
        List<SemanticTypeLabel> predictions = new ArrayList<>();
        try {
            JSONArray response = new JSONArray(POST(PREDICT, values));
            for (int i = 0; i < (response.length() < numPredictions ? response.length() : numPredictions); i++)
            {
                String [] label = response.getJSONObject(i).getString("type_id").split(ID_DIVIDER);
                float score = (float) response.getJSONObject(i).getDouble("score");
                label[0] = StringUtils.newStringUtf8(Base64.decodeBase64(label[0]));
                label[1] = StringUtils.newStringUtf8(Base64.decodeBase64(label[1]));
                predictions.add(new SemanticTypeLabel(label[0] + "|" + label[1], score));
            }
            Collections.sort(predictions, new SemanticTypeLabelComparator());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return predictions;
    }
    public static String getSemanticTypeId(String domain, String type) {
        return Base64.encodeBase64String(domain.getBytes()) + ID_DIVIDER + Base64.encodeBase64String(type.getBytes());
    }
    public static String getColumnId(String type_id, String column_name, String source_name) {
        return type_id + ID_DIVIDER + Base64.encodeBase64String(column_name.getBytes()) + ID_DIVIDER + Base64.encodeBase64String(source_name.getBytes())+ ID_DIVIDER + Base64.encodeBase64String(DEFAULT_MODEL.getBytes());
    }
}
