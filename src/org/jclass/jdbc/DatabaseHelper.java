package org.jclass.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {
	
	private static String DRIVER;
	private static String URL;
	private static String USERNAME;
	private static String PASSWORD;
	
	static {
		DRIVER = "com.mysql.jdbc.Driver";
		URL = "jdbc:mysql://127.0.0.1:3306/demo";
		USERNAME = "root";
		PASSWORD = "root";
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println("can not find the sql driver");
			throw new RuntimeException(e);
		}
	}
	
	public static Connection getConnect(){
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		} catch (SQLException e) {
			System.out.println("can not get sql connection!");
			throw new RuntimeException(e);
		}
		return conn;
	}
	
	public static void closeConnect(Connection conn){
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println("can not close the connection");
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, Object> queryMap(String sql){
		Connection conn = getConnect();
		Map<String, Object> map = new HashMap<>();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();
				for (int i=1; i<=count; i++){
					String key = rsmd.getColumnName(i);
					map.put(key, rs.getObject(key));
				}
			}
		} catch (SQLException e) {
			System.out.println("can not query for map");
			throw new RuntimeException(e);
		} finally {
			closeConnect(conn);
		}
		return map;
	}
	
	public static <T> T queryEntity(Class<T> clazz, String sql, Object... params){
		T entity = null;
		Field[] fields = clazz.getDeclaredFields();
		try {
			for (int i=0; i<fields.length; i++){
				entity = clazz.newInstance();
				Method[] ms = clazz.getMethods();
				
				System.out.println(fields[i].getName());
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return entity;
	}
}
