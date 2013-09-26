/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 *****************************************************************************
 */
package edu.isi.karma.imp.rdf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class RDFImport {

    private final RepFactory factory;
    private final Worksheet worksheet;
    PrintWriter pw = null;
    private JSONArray confArr = new JSONArray();
    public static int jj = 0;

    public RDFImport(JSONArray inputSpecs, Workspace workspace,
            Worksheet worksheet) {
        this.confArr = inputSpecs;
        this.factory = workspace.getFactory();
        this.worksheet = worksheet;

    }

    private HTable addNestedHTable(HNode hNode, String key) {
        HTable ht = hNode.getNestedTable();
        if (ht == null) {
            ht = hNode.addNestedTable(createNestedTableName(key), worksheet,
                    factory);
        }
        return ht;
    }

    private Map<String, String> sub(HTable headers,
            ArrayList<String> headersList, JSONArray propertyArr,
            String propertyName, String properties, Map<String, String> map) {
        HNode hNode = null;
        for (int j = 0; j < propertyArr.length(); j++) {
            JSONObject obj = propertyArr.optJSONObject(j);
            if (obj.has(propertyName)) {

                String pName = obj.optString(propertyName).toString();
                hNode = headers.addHNode(pName, worksheet, factory);

                headersList.add(hNode.getId());
                map.put(pName, hNode.getId());
            }

            if (obj.has(properties)) {
                JSONArray subArr = obj.optJSONArray(properties);
                HTable hTable = addNestedHTable(hNode, "nestedTable");
                System.out.println("nestedTable is"
                        + hNode.getNestedTable().getId());
                sub(hTable, headersList, subArr, propertyName, properties, map);
            }
        }
        return map;
    }

    public ArrayList<String> generateWorksheetFromGeneralJSON(
            Map<String, String> map) throws JSONException {

        /**
         * Add the headers *
         */
        HTable headers = worksheet.getHeaders();
        ArrayList<String> headersList = new ArrayList<String>();

        String properties = "properties";
        String propertyName = "propertyName";

        for (int i = 0; i < this.confArr.length(); i++) {
            try {
                JSONArray propertyArr = this.confArr.getJSONObject(i)
                        .getJSONArray(properties);
                sub(headers, headersList, propertyArr, propertyName,
                        properties, map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        this.pw = new PrintWriter(System.out, true);
        System.out.println("PRINTING THE HTABLE SCHEMA:");
        worksheet.getHeaders().prettyPrint("", pw, factory);// show the HTable
        // schema;
        return headersList;
    }

    private String createNestedTableName(String key) {
        return "Table for " + key;
    }
}
