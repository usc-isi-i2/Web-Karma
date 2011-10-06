package edu.isi.karma.util;

public class JDBCUtilFactory {
	public static AbstractJDBCUtil getInstance(AbstractJDBCUtil.DBType dbType) {
		if(dbType == AbstractJDBCUtil.DBType.MySQL)
			return new MySQLUtil();
		else if(dbType == AbstractJDBCUtil.DBType.Oracle)
			return new OracleUtil();
		else return null;
	}

}
