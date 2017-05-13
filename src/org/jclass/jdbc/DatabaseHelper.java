package org.jclass.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {

    private final static String DRIVER;
    private final static String URL;
    private final static String USERNAME;
    private final static String PASSWORD;

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

    public static <T> T queryEntity(Class<T> clazz, String sql, Object... params){
        T entity = null;
        Connection conn = getConnect();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i=0; i<params.length; i++){
            	ps.setObject(i+1, params[i]);
			}
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
				entity = rsToEntity(rs, clazz);
            }
        } catch (SQLException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeConnect(conn);
        }
        return entity;
    }

    public static <T> List<T> queryEntityList(Class<T> clazz, String sql, Object... params){
    	List<T> list = new ArrayList<>();
    	T entity;
    	Connection conn = getConnect();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()){
				entity = rsToEntity(rs, clazz);
				list.add(entity);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnect(conn);
		}
		return list;
	}

	public static Map<String, Object> queryMap(String sql){
		Connection conn = getConnect();
		Map<String, Object> map = null;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				map = rsToMap(rs);
			}
		} catch (SQLException e) {
			System.out.println("can not query for map");
			throw new RuntimeException(e);
		} finally {
			closeConnect(conn);
		}
		return map;
	}

    public static List<Map<String, Object>> queryMapList(String sql, Object... params){
    	Connection conn = getConnect();
		List<Map<String, Object>> list = null;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			for (int i=0; i<params.length; i++){
				ps.setObject(i+1, params[i]);
			}
			ResultSet rs = ps.executeQuery();
			list = rsToMapList(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnect(conn);
		}

		return list;
	}

    public static List<Map<String, Object>> callProcsForMapList(String procsName, Object... params){
        Connection conn = getConnect();
        ResultSet rs;
        List<Map<String, Object>> resultMap = null;

        try {
            StringBuilder sb = new StringBuilder("{call ");
            sb.append(procsName+"(");
            for (int i=0,len=params.length; i<len; i++){
                if (i < len-1){
                    sb.append("?,");
                } else {
                    sb.append("?");
                }
            }
            sb.append(")}");
            
            CallableStatement cs = conn.prepareCall(sb.toString());
            
            for (int i=0,len=params.length; i<len; i++){
            	cs.setObject(i+1, params[i]);
            }
            
            rs = cs.executeQuery();
            resultMap = rsToMapList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
			closeConnect(conn);
		}
        return resultMap;
    }

	public static int update(String sql, Object... params){
		int rows = 0;
		Connection conn = getConnect();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			for (int i=0; i<params.length; i++){
				ps.setObject(i+1, params[i]);
			}
			rows = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}

	public static <T> Map<String, Object> queryMapById(Object id, Class<T> clazz){
		Connection conn = getConnect();
		Map<String, Object> map = null;
		String sql = "SELECT * FROM " + clazz.getSimpleName() + " WHERE id="+id;
		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()){
				map = rsToMap(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

	public static <T> T queryEntityById(Object id, Class<T> clazz){
		Connection conn = getConnect();
		T entity = null;
		String sql = "SELECT * FROM " + clazz.getSimpleName() + " WHERE id="+id;

		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()){
				entity = rsToEntity(rs, clazz);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return entity;
	}



	private static Map<String, Object> rsToMap(ResultSet rs){
		Map<String, Object> map = new HashMap<>();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			for (int i=1; i<=count; i++){
                String key = rsmd.getColumnName(i);
                map.put(key, rs.getObject(key));
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

    private static List<Map<String, Object>> rsToMapList(ResultSet rs){

        if (null != rs){
            List<Map<String, Object>> list = new ArrayList<>();
            try {
                while (rs.next()){
                    Map<String, Object> map = new HashMap<>();
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int count = rsmd.getColumnCount();
                    for (int i=1; i<=count; i++){
                        String key = rsmd.getColumnName(i);
                        map.put(key, rs.getObject(key));
                    }
                    list.add(map);
                }
                return list;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static <T> T rsToEntity(ResultSet rs, Class<T> clazz){
    	T entity = null;
    	String columnName;
    	Method curMethod;
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			entity = clazz.newInstance();
			for (int i=1; i<=count; i++){
				columnName = rsmd.getColumnName(i);
				String javaFieldName = toJavaFieldName(columnName);
				curMethod = clazz.getDeclaredMethod(toJavaSetMethodName(columnName),clazz.getDeclaredField(javaFieldName).getType());
				curMethod.invoke(entity,rs.getObject(columnName));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return entity;
	}

	private static String toJavaFieldName(String columnName){
		char[] chars = columnName.toCharArray();
		for (int i=0,len=chars.length ; i<len; i++){
			chars[i] = Character.toLowerCase(chars[i]);
		}
		for (int i=0,len=chars.length ; i<len; i++){
			if(chars[i] == '_'){
				chars[i+1] = Character.toUpperCase(chars[i+1]);
			}
		}
		return new String(chars).replace("_", "");
	}

	private static String toJavaSetMethodName(String columnName){
		char[] chars = toJavaFieldName(columnName).toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return "set" + new String(chars);
	}

	private static String toJavaGetMethodName(String columnName){
		char[] chars = toJavaFieldName(columnName).toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return "get" + new String(chars);
	}



	private static Connection getConnect(){
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		} catch (SQLException e) {
			System.out.println("can not get sql connection!");
			throw new RuntimeException(e);
		}
		return conn;
	}

	private static void closeConnect(Connection conn){
		if (null != conn){
			try {
				conn.close();
			} catch (SQLException e) {
				System.out.println("can not close the connection");
				throw new RuntimeException(e);
			}
		}
	}
}

