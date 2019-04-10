package edu.isi.karma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class IngresUtil extends AbstractJDBCUtil {

    private static Logger logger = LoggerFactory.getLogger(MySQLUtil.class);

    static final String DRIVER = "com.ingres.jdbc.IngresDriver";

    private static final String CONNECT_STRING_TEMPLATE = "jdbc:ingres://host:port/dbname;UID=username;PWD=pwd";

    @Override
    protected String getDriver() {
        return DRIVER;
    }

    @Override
    protected String getConnectStringTemplate() {
        return CONNECT_STRING_TEMPLATE;
    }

    @Override
    public String escapeTablename(String name) {
        return name;
    }

    /**
     * Enclose input string between escape chars specific for each type of DB.
     *
     * @param name
     * @return
     */
    @Override
    public String prepareName(String name) {
        String s = name;
        s = name.replace('-', '_');
        s = "`" + s + "`";
        return s;
    }

    @Override
    public ArrayList<ArrayList<String>> getDataForLimitedRows(DBType dbType, String hostname, int portnumber, String username, String password, String tableName, String dBorSIDName, int rowCount) throws SQLException, ClassNotFoundException {
        logger.error("INGRES: getDataForLimitedRows");
        String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
        Connection conn = getConnection(DRIVER, connectString);
        String query = "Select * from " + escapeTablename(tableName) + " FETCH FIRST " + rowCount + " ROWS ONLY";

        logger.info("Execute:" + query);
        Statement s = conn.createStatement();
        ResultSet r = s.executeQuery(query);

        if (r == null) {
            s.close();
            return null;
        }

        ArrayList<ArrayList<String>> vals = parseResultSetIntoArrayListOfRows(r);

        r.close();
        s.close();
        return vals;
    }

    @Override
    public ArrayList<ArrayList<String>> getSQLQueryDataForLimitedRows(DBType dbType, String hostname, int portnumber, String username, String password, String query, String dBorSIDName, int rowCount) throws SQLException, ClassNotFoundException {
        logger.error("INGRES: getSQLQueryDataForLimitedRows");
        String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
        Connection conn = getConnection(DRIVER, connectString);

        //if (query.toLowerCase().indexOf(" limit ") == -1) //Add limit only if it doesnt exist, else sql will be invalid
        //  query = query + " limit " + rowCount;

        logger.info("Execute:" + query);
        Statement s = conn.createStatement();
        ResultSet r = s.executeQuery(query);

        if (r == null) {
            s.close();
            return null;
        }

        ArrayList<ArrayList<String>> vals = parseResultSetIntoArrayListOfRows(r);

        r.close();
        s.close();
        return vals;
    }

    @Override
    public ArrayList<String> getListOfTables(Connection conn) throws SQLException, ClassNotFoundException {
        logger.error("INGRES: getListOfTables");
        ArrayList<String> tableNames = new ArrayList<>();
        DatabaseMetaData dmd = conn.getMetaData();
        ResultSet rs = dmd.getTables(null, null, null, new String[]{"TABLE", "VIEW"});
        while (rs.next())
            tableNames.add(rs.getString(3));
        Collections.sort(tableNames);
        return tableNames;
    }
}
