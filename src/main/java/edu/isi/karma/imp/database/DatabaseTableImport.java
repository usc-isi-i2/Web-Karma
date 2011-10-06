package edu.isi.karma.imp.database;

import java.sql.SQLException;
import java.util.ArrayList;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.util.JDBCUtilFactory;

public class DatabaseTableImport {
	
	private AbstractJDBCUtil.DBType dbType;
	private String 	hostname;
	private int 	portnumber;
	private String 	username;
	private String 	password;
	private String 	dBorSIDName;
	private String tableName;
	private final RepFactory factory;
	private final Worksheet worksheet;
	
	//private static Logger logger = LoggerFactory.getLogger(DatabaseTableImport.class);

	public DatabaseTableImport(DBType dbType, String hostname, int portnumber,
			String username, String password, String dBorSIDName,
			String tableName, Workspace workspace) {
		super();
		this.dbType = dbType;
		this.hostname = hostname;
		this.portnumber = portnumber;
		this.username = username;
		this.password = password;
		this.dBorSIDName = dBorSIDName;
		this.tableName = tableName;
		this.factory = workspace.getFactory();
		this.worksheet = factory.createWorksheet(tableName, workspace);
	}

	public Worksheet generateWorksheet() throws SQLException, ClassNotFoundException {
		/** Get the data from the database table **/
		AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(dbType);
		ArrayList<ArrayList<String>> data = dbUtil.getDataForTable(dbType, hostname, 
				portnumber, username, password, tableName, dBorSIDName);
		
		/** Add the headers **/
		HTable headers = worksheet.getHeaders();
		ArrayList<String> headersList = new ArrayList<String>();
        for(int i=0; i<data.get(0).size(); i++){
        	HNode hNode = null;
        	hNode = headers.addHNode(data.get(0).get(i), worksheet, factory);
        	headersList.add(hNode.getId());
        }
        
        /** Add the data **/
        Table dataTable = worksheet.getDataTable();
        for(int i=1; i<data.size(); i++) {
        	Row row = dataTable.addRow(factory);
        	ArrayList<String> rowData = data.get(i);
			for (int j = 0; j < rowData.size(); j++) {
				row.setValue(headersList.get(j), rowData.get(j));
			}
        }
		
		return worksheet;
	}

	

}
