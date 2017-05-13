package org.jclass.classload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jclass.jdbc.DatabaseHelper;
import org.jclass.model.Customer;

public class ClassLoaderTest {

	public static void main(String[] args) {
//		Map<String, Object> map = DatabaseHelper.queryMap("select * from customer");
//		Customer cus = DatabaseHelper.queryEntity(Customer.class, "select * from customer where id=?",1);
//		System.out.println(cus.toString());
//		System.out.println(DatabaseHelper.toJavaFieldName("im_agent_id"));
//		List<Map<String, Object>> mapList = DatabaseHelper.queryMapList("select * from customer");
//		List<Customer> customerList = DatabaseHelper.queryEntityList(Customer.class, "select * from customer");
//		int count = DatabaseHelper.update("insert into customer (name,contact,phone,email,remark) values(?,?,?,?,?)",
//				"王尼玛","本人","13702532706", "funny135@126com", "getout");
//		System.out.println(count);
		Customer customer = Db.queryEntityById(1, Customer.class);
	}

}

class Db extends DatabaseHelper{
	public static <T> T queryEntityById(Object id, Class<T> clazz){
		return null;
	}
}
