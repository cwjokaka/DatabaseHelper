package org.jclass.classload;

import java.sql.Connection;
import java.util.Map;

import org.jclass.jdbc.DatabaseHelper;
import org.jclass.model.Customer;

public class ClassLoaderTest {

	public static void main(String[] args) {
//		Map<String, Object> map = DatabaseHelper.queryMap("select * from customer");
		Customer cus = DatabaseHelper.queryEntity(Customer.class, "select * from customer where id=1",1);
		//System.out.println(cus);
	}

}
