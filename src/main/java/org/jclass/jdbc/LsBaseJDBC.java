package org.jclass.jdbc;

import org.jclass.util.PropsUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 * Created by hasee on 2017/5/14.
 */
public class LsBaseJDBC<T> {

    private final static String DRIVER;
    private final static String URL;
    private final static String USERNAME;
    private final static String PASSWORD;

    private final static ThreadLocal<Connection> CONNECTION_POOL = new ThreadLocal<>();

    private Class<T> clazz;
    
    private String tableName;
    private String id;
    
    public LsBaseJDBC(){
        //得到带泛型的父类org.jclass.jdbc.LsBaseJDBC<org.jclass.model.Customer>
        Type superclass = this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = null;
        //检测父类是否被泛型化
        if (superclass instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) superclass;
            //得到绑定的类型数组
            Type[] typeArray = parameterizedType.getActualTypeArguments();
            if (typeArray != null && typeArray.length > 0) {
                clazz=(Class)typeArray[0];
            }
        }
        //初始化配置
        init();
    }

    static {
        Properties props = PropsUtil.loadProps("jdbc.properties");
        DRIVER = props.getProperty("jdbc.driver");
        URL = props.getProperty("jdbc.url");
        USERNAME = props.getProperty("jdbc.username");
        PASSWORD = props.getProperty("jdbc.password");
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("can not find the sql driver");
            throw new RuntimeException(e);
        }
    }

    public T queryEntity(String sql, Object... params){
        T entity = null;
        try {
            ResultSet rs = query(sql, params);
            while (rs.next()){
                entity = rsToEntity(rs, this.clazz);
            }
        } catch (SQLException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeConnect();
        }
        return entity;
    }

    public T queryEntityById(Object id){
        T entity = null;
        String sql = "SELECT * FROM " + this.tableName + " WHERE " + this.id + "="+id;
        try {
            ResultSet rs = query(sql);
            while (rs.next()){
                entity = rsToEntity(rs, this.clazz);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnect();
        }

        return entity;
    }

    public List<T> queryEntityList(String sql, Object... params){
        List<T> list = new ArrayList<>();
        T entity;
        try {
            ResultSet rs = query(sql,params);
            while (rs.next()){
                entity = rsToEntity(rs, this.clazz);
                list.add(entity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnect();
        }
        return list;
    }

    public static Map<String, Object> queryMap(String sql, Object... params){
        Map<String, Object> map = null;
        try {
            ResultSet rs = query(sql, params);
            if (rs.next()) {
                map = rsToMap(rs);
            }
        } catch (SQLException e) {
                System.out.println("can not query for map");
                throw new RuntimeException(e);
        } finally {
            closeConnect();
        }
        return map;
    }

    public Map<String, Object> queryMapById(Object id){
        Connection conn = getConnect();
        Map<String, Object> map = null;
        String sql = "SELECT * FROM " + this.tableName + " WHERE "+this.id+"="+id;
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
            closeConnect();
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
            closeConnect();
        }
        return resultMap;
    }

    public static ResultSet query(String sql, Object... params) throws SQLException{
        Connection conn = getConnect();
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i=0; i<params.length; i++){
            ps.setObject(i+1, params[i]);
        }
        return ps.executeQuery();
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

    public int insertEntity(T entity){
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(getTableName(this.getClass()));
        sb.append(" ( ");

        return 0;
    }

    public int insertEntity(Map<String, Object> params){
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(getTableName(this.getClass()));
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        Set<String> set = params.keySet();
        for (String columnName : set){
            columns.append(columnName).append(",");
            values.append("?, ");
        }
        columns.replace(columns.lastIndexOf(","), columns.length(), ")");
        values.replace(values.lastIndexOf(","), values.length(), ")");
        Object[] vals = params.values().toArray();
        return update(sql.append(columns).append(" VALUES ").append(values).toString(), vals);
    }

    public int deleteEntityById(Object id){
        String sql = "DELETE FROM " + this.tableName + " WHERE " + this.id + "=?";
        return update(sql,id);
    }

    public static int excuteSqlFile(String filePath){
        int rows = 0;
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String lineContent = null;
        try {
            while ((lineContent = br.readLine()) != null) {
                rows += update(lineContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnect();
        }
        return rows;
    }

    private static Map<String, Object> rsToMap(ResultSet rs) throws SQLException{
        Map<String, Object> map = new HashMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();
        for (int i=1; i<=count; i++){
            String key = rsmd.getColumnName(i);
            map.put(key, rs.getObject(key));
        }
        return map;
    }

    private static List<Map<String, Object>> rsToMapList(ResultSet rs) throws SQLException{
        if (null != rs){
            Map<String, Object> map;
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()){
                map = rsToMap(rs);
                list.add(map);
            }
            return list;
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

        } catch (NoSuchFieldException e) {

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

    private static String fieldToSetMethodName(String field){
        char[] chars = field.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return "set" + new String(chars);
    }

    private static String toJavaGetMethodName(String columnName){
        char[] chars = toJavaFieldName(columnName).toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return "get" + new String(chars);
    }
    
    private void init(){
    	this.id = getId(this.clazz);
    	this.tableName = getTableName(this.clazz);
    }

    private static String getTableName(Class clazz){
        String tableName = clazz.getSimpleName();
        Annotation anno = clazz.getAnnotation(LsAnnotation.class);
        if (null != anno){
            try {
                Method method = anno.annotationType().getDeclaredMethod("tableName");
                tableName = (String) method.invoke(anno);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } 
        return tableName;
    }
    
    private static String getId(Class clazz){
    	String id = "id";
    	Annotation anno = clazz.getAnnotation(LsAnnotation.class);
        if (null != anno){
            try {
                Method method = anno.annotationType().getDeclaredMethod("id");
                id = (String) method.invoke(anno);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } 
        return id;
    }

    private static Connection getConnect(){
        Connection conn = CONNECTION_POOL.get();
        if (null == conn){
            try {
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("打开连接:"+conn);
            } catch (SQLException e) {
                System.out.println("can not get sql connection!");
                throw new RuntimeException(e);
            } finally {
                CONNECTION_POOL.set(conn);
            }
        } else {
            System.out.println("得到连接:"+conn);
        }

        return conn;
    }

    private static void closeConnect(){
        Connection conn = CONNECTION_POOL.get();
        if (null != conn){
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("can not close the connection");
                throw new RuntimeException(e);
            } finally {
                System.out.println("关闭连接:"+conn);
                CONNECTION_POOL.remove();
            }
        }
    }

    private static int getResultSetRows(ResultSet rs) throws SQLException{
        rs.last();
        int rowCount = rs.getRow();
        rs.beforeFirst();
        return rowCount;
    }
}
