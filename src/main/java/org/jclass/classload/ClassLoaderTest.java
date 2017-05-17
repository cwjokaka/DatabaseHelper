package org.jclass.classload;

import org.jclass.jdbc.DatabaseHelper;
import org.jclass.jdbc.LsAnnotation;
import org.jclass.jdbc.LsBaseJDBC;
import org.jclass.model.Customer;

import java.util.Map;


public class ClassLoaderTest {

	public static void main(String[] args) {
		Ls ls = new Ls();
		Customer cus = ls.queryEntityById(1);
		System.out.println(cus);
		Map<String, Object> rsmap = ls.queryMapById(1);
		System.out.println(rsmap);
		//int count = ls.deleteEntityById(4);
//		TranDao td = new TranDao();
//		Map<String, Object> rsmap = td.queryMapById("001");
//		System.out.println(rsmap);
	}

}

class Db extends DatabaseHelper{
	public static <T> T queryEntityById(Object id, Class<T> clazz){
		return null;
	}
}

class Ls extends LsBaseJDBC<Customer> {
	
}

@LsAnnotation(tableName = "tb_transaction_pay", id="OR_ID")
class TranDao extends LsBaseJDBC{
	
}