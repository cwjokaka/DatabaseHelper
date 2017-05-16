package org.jclass;

import org.jclass.dao.CustomerDao;
import org.jclass.model.Customer;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by hasee on 2017/5/15.
 */
public class JDBCTest {

    private CustomerDao customerDao;

    @Before
    public void init(){
        customerDao = new CustomerDao();
        customerDao.excuteSqlFile("sql/init.sql");
    }

    @Test
    public void queryEntityTest(){
        Customer customer = customerDao.queryEntity("SELECT * FROM customer WHERE id=?",1);

    }
}
