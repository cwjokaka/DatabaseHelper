package org.jclass.classload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
//		Customer cus = DatabaseHelper.queryEntity(Customer.class, "select * from customer where id=1",1);
//		System.out.println(cus.toString());
		System.out.println(DatabaseHelper.toJavaFieldName("im_agent_id"));
		
	}

}
