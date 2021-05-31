package com.catoocraft.demo.dbunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.dbunit.database.CachedResultSetTableFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.ForwardOnlyResultSetTableFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.stream.StreamingDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.xml.sax.InputSource;

public class DbUnitUtils {
	
	private static int dumpCounter = 0;
	private static File dumpFile = null;
	private static IDatabaseConnection dbconn = null;
	
	public static void dumpTables(String... excelPaths) throws Exception {
		
		if (dumpCounter++ > 0) {
			return;
		}
		
		if (dbconn == null) {
			dbconn = getDatabaseConection();
		}
		
		// 大きいサイズのテーブルをダンプできるようにResultSetファクトリを変更
		DatabaseConfig config = dbconn.getConfig();
		config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new ForwardOnlyResultSetTableFactory());
		
		// Excel で指定されたデータセット内のテーブルを退避対象とする
		Set<String> tableSet = new HashSet<>();
		for (String path : excelPaths) {
			IDataSet dataSet = getDataSet(path);
			for (String tableName : dataSet.getTableNames()) {
				tableSet.add(tableName);
			}
		}
		
		QueryDataSet queryDataSet = new QueryDataSet(dbconn);
		for (String tableName : tableSet) {
			queryDataSet.addTable(tableName);
		}
		
		// ダンプの出力先となるテンポラリファイルを作成
		dumpFile = File.createTempFile("dump", ".xml");
		try (OutputStream fs = new FileOutputStream(dumpFile)) {
			FlatXmlDataSet.write(wrap(queryDataSet), fs);
		}
		
		// 変更したファクトリをデフォルトのファクトリに戻す
		config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new CachedResultSetTableFactory());
	}
	
	public static void restoreTables() throws Exception {
		
		if (--dumpCounter > 0) {
			return;
		}
		
		try (InputStream is = new FileInputStream(dumpFile.getPath())) {
			FlatXmlProducer producer = new FlatXmlProducer(new InputSource(is));
			DatabaseOperation.DELETE_ALL.execute(dbconn, unwrap(new StreamingDataSet(producer)));
		}
		try (InputStream is = new FileInputStream(dumpFile.getPath())) {
			FlatXmlProducer producer = new FlatXmlProducer(new InputSource(is));
			DatabaseOperation.INSERT.execute(dbconn, unwrap(new StreamingDataSet(producer)));
		}
		
		dumpFile.delete();
		dbconn.close();
		dbconn = null;
	}
	
	public static void setUpTables(String excelPath) throws Exception {
		// Excel 用データセット作成
		IDataSet dataSet = getDataSet(excelPath);
		
		// データの全削除＆挿入
		DatabaseOperation.CLEAN_INSERT.execute(dbconn, dataSet);
	}
	
	public static boolean updateTable(String sql) throws Exception {
		Connection con = dbconn.getConnection();
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			return ps.execute();
		}
	}
	
	public static IDataSet getDataSet(String excelPath) throws Exception {
		URL url = ClassLoader.getSystemResource(excelPath);
		if (url == null) {
			throw new FileNotFoundException(excelPath);
		}
		File excelFile = new File(url.getFile());
		return new XlsDataSet(excelFile);
	}
	
	public static ITable getActualTable(String tableName) throws Exception {
		return dbconn.createDataSet(new String[] {tableName}).getTable(tableName);
	}
	
	private static IDatabaseConnection getDatabaseConection() throws Exception {
		Properties conf = new Properties();
		conf.load(DbUnitUtils.class.getClassLoader().getResourceAsStream("DbUnitUtils.properties"));
		String url = conf.getProperty("jdbc.url");
		String user = conf.getProperty("jdbc.user");
		String password = conf.getProperty("jdbc.password");
		String schema = conf.getProperty("jdbc.schema");
		Connection connection = DriverManager.getConnection(url, user, password);
		connection.setAutoCommit(true);
		return new DatabaseConnection(connection, schema);
	}

	private static ReplacementDataSet wrap(AbstractDataSet orignal) {
		ReplacementDataSet replacement = new ReplacementDataSet(orignal);
		replacement.addReplacementObject(null, "<null>");
		return replacement;
	}
	
	private static ReplacementDataSet unwrap(AbstractDataSet orignal) {
		ReplacementDataSet replacement = new ReplacementDataSet(orignal);
		replacement.addReplacementObject("<null>", null);
		return replacement;
	}
	
}
