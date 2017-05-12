package org.jclass.jdbc;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if (null != conn){
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("can not close the connection");
                throw new RuntimeException(e);
            }
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
        String columnName;
        Method curMethod;
        Connection conn = getConnect();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            while (rs.next()){
                entity = clazz.newInstance();
                for (int i=1; i<=count; i++){
                    columnName = rsmd.getColumnName(i);
                 
                    curMethod = clazz.getDeclaredMethod(toJavaSetMethodName(columnName),int.class);
                    curMethod.invoke(entity,rs.getInt(columnName));
                }

            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e){
            e.printStackTrace();
        } catch (NoSuchMethodException e){
            e.printStackTrace();
        } catch (InvocationTargetException e){
            e.printStackTrace();
        } finally {
            closeConnect(conn);
        }
        return entity;
    }

    private static String toJavaSetMethodName(String columnName){
        char[] chars = columnName.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return "set" + new String(chars);
    }

    private static String toJavaGetMethodName(String columnName){
        char[] chars = columnName.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return "get" + new String(chars);
    }
    
    public static String toJavaFieldName(String columnName){
    	char[] chars = columnName.toCharArray();
    	for (int i=0,len=chars.length ; i<len; i++){
    		
    		//chars[i] = Character.toLowerCase(chars[i]);
    		
    		if(chars[i] == '_'){
    			chars[i+1] = Character.toUpperCase(chars[i+1]);
    		}
    
    	}
    	
    	return new String(chars).replace("_", "");
    }

    //调用存储过程查询ListMap
    public static List<Map<String, Object>> callProcsForListMap(String procsName, Object... params){
        Connection conn = getConnect();
        ResultSet rs = null;
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
            resultMap = rsToListMap(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
			closeConnect(conn);
		}
        return resultMap;
    }

    //resultSet转成ListMap(要求resultSet没关闭)
    private static List<Map<String, Object>> rsToListMap(ResultSet rs){

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
}

