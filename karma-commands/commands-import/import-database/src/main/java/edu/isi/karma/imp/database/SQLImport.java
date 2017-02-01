package edu.isi.karma.imp.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.SourceInformation;
import edu.isi.karma.rep.metadata.SourceInformation.InfoAttribute;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.DBType;
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.webserver.KarmaException;

public class SQLImport extends Import {

    private DBType dbType;
    private String hostname;
    private int portnumber;
    private String username;
    private String password;
    private String dBorSIDName;
    private String query;
    private Workspace workspace;
    private String encoding;
    
    //private static Logger logger = LoggerFactory.getLogger(DatabaseTableImport.class);
    public SQLImport(DBType dbType, String hostname, int portnumber,
            String username, String password, String dBorSIDName,
            String query, Workspace workspace, String encoding) {
        super(query, workspace, encoding);
        this.dbType = dbType;
        this.hostname = hostname;
        this.portnumber = portnumber;
        this.username = username;
        this.password = password;
        this.dBorSIDName = dBorSIDName;
        this.query = query;
        this.workspace = workspace;
        this.encoding = encoding;
    }

    public SQLImport duplicate() {
    	return new SQLImport(dbType, hostname, portnumber, username, password, dBorSIDName, query, workspace, encoding);
    }
    
    @Override
    public Worksheet generateWorksheet() throws KarmaException {
        /**
         * Get the data from the database table *
         */
        AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(dbType);
        // TODO Limiting the number of rows to 1000 for now to avoid all data in memory
        ArrayList<ArrayList<String>> data;
		try {
			data = dbUtil.getSQLQueryDataForLimitedRows(dbType, hostname,
			        portnumber, username, password, query, dBorSIDName, 1000);
			 return generateWorksheet(dbUtil, data);
		} catch (SQLException | ClassNotFoundException e) {
			//If data could not be imported, delete the empty worksheet
			Worksheet ws = getWorksheet();
			if(ws != null)
				workspace.removeWorksheet(ws.getId());
			
			throw new KarmaException("Unable to get data for the SQL Query: " + e.getMessage());
		}
    }

    public Worksheet generateWorksheetForAllRows() throws SQLException, ClassNotFoundException {
        /**
         * Get the data from the database table *
         */
        AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(dbType);

        ArrayList<ArrayList<String>> data = dbUtil.getDataForQuery(dbType, hostname,
                portnumber, username, password, query, dBorSIDName);
        return generateWorksheet(dbUtil, data);
    }

    private Worksheet generateWorksheet(AbstractJDBCUtil dbUtil, ArrayList<ArrayList<String>> data)
    {
	    /**
	     * Add the headers *
	     */
	    HTable headers = getWorksheet().getHeaders();
	    List<String> headersList = new ArrayList<>();
	    for (int i = 0; i < data.get(0).size(); i++)
	    {
		    HNode hNode;
		    hNode = headers.addHNode(data.get(0).get(i), HNodeType.Regular, getWorksheet(), getFactory());
		    headersList.add(hNode.getId());
	    }

	    /**
	     * Add the data *
	     */
	    Table dataTable = getWorksheet().getDataTable();
	    for (int i = 1; i < data.size(); i++)
	    {
		    Row row = dataTable.addRow(getFactory());
		    ArrayList<String> rowData = data.get(i);
		    for (int j = 0; j < rowData.size(); j++)
		    {
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
        srcInfo.setAttributeValue(InfoAttribute.query, query);
        getWorksheet().getMetadataContainer().setSourceInformation(srcInfo);
        getWorksheet().getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType, SourceTypes.DB.toString());
        return getWorksheet();
    }

}
