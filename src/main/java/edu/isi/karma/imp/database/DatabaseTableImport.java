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
 * ****************************************************************************
 */
package edu.isi.karma.imp.database;

import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.SourceInformation;
import edu.isi.karma.rep.metadata.SourceInformation.InfoAttribute;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.util.JDBCUtilFactory;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseTableImport extends Import {

    private AbstractJDBCUtil.DBType dbType;
    private String hostname;
    private int portnumber;
    private String username;
    private String password;
    private String dBorSIDName;
    private String tableName;

    //private static Logger logger = LoggerFactory.getLogger(DatabaseTableImport.class);
    public DatabaseTableImport(DBType dbType, String hostname, int portnumber,
            String username, String password, String dBorSIDName,
            String tableName, Workspace workspace) {
        super(tableName, workspace);
        this.dbType = dbType;
        this.hostname = hostname;
        this.portnumber = portnumber;
        this.username = username;
        this.password = password;
        this.dBorSIDName = dBorSIDName;
        this.tableName = tableName;
    }


    @Override
    public Worksheet generateWorksheet() throws SQLException, ClassNotFoundException {
        /**
         * Get the data from the database table *
         */
        AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(dbType);
        // TODO Limiting the number of rows to 1000 for now to avoid all data in memory
        ArrayList<ArrayList<String>> data = dbUtil.getDataForLimitedRows(dbType, hostname,
                portnumber, username, password, tableName, dBorSIDName, 100);
        return generateWorksheet(dbUtil, data);
    }

    public Worksheet generateWorksheetForAllRows() throws SQLException, ClassNotFoundException {
        /**
         * Get the data from the database table *
         */
        AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(dbType);

        ArrayList<ArrayList<String>> data = dbUtil.getDataForTable(dbType, hostname,
                portnumber, username, password, tableName, dBorSIDName);
        return generateWorksheet(dbUtil, data);
    }

    private Worksheet generateWorksheet(AbstractJDBCUtil dbUtil, ArrayList<ArrayList<String>> data) {
        /**
         * Add the headers *
         */
        HTable headers = getWorksheet().getHeaders();
        ArrayList<String> headersList = new ArrayList<String>();
        for (int i = 0; i < data.get(0).size(); i++) {
            HNode hNode = null;
            hNode = headers.addHNode(data.get(0).get(i), getWorksheet(), getFactory());
            headersList.add(hNode.getId());
        }

        /**
         * Add the data *
         */
        Table dataTable = getWorksheet().getDataTable();
        for (int i = 1; i < data.size(); i++) {
            Row row = dataTable.addRow(getFactory());
            ArrayList<String> rowData = data.get(i);
            for (int j = 0; j < rowData.size(); j++) {
                row.setValue(headersList.get(j), rowData.get(j), getFactory());
            }
        }

        /**
         * Save the db info in the source information part of worksheet's
         * metadata *
         */
        SourceInformation srcInfo = new SourceInformation();
        srcInfo.setAttributeValue(InfoAttribute.dbType, dbType.name());
        srcInfo.setAttributeValue(InfoAttribute.hostname, hostname);
        srcInfo.setAttributeValue(InfoAttribute.portnumber, String.valueOf(portnumber));
        srcInfo.setAttributeValue(InfoAttribute.username, username);
        srcInfo.setAttributeValue(InfoAttribute.dBorSIDName, dBorSIDName);
        srcInfo.setAttributeValue(InfoAttribute.tableName, tableName);
        getWorksheet().getMetadataContainer().setSourceInformation(srcInfo);
        return getWorksheet();
    }

}
