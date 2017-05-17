package org.jclass;

import org.jclass.dao.CustomerDao;
import org.jclass.model.Customer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;


/**
 * Created by hasee on 2017/5/15.
 */
public class JDBCTest {

    private CustomerDao customerDao;

    @Before
    public void init(){
        customerDao = new CustomerDao();
        customerDao.excuteSqlFile("sql/customer_init.sql");
    }

    @Test
    public void queryEntityTest(){
        Customer customer = customerDao.queryEntity("SELECT * FROM customer WHERE id=?",1);
        System.out.println(customer);
    }

    @Test
    public void queryEntityByIdTest(){
        Customer customer = customerDao.queryEntityById(2);
        System.out.println(customer);
    }

    @Test
    public void queryEntityListTest(){
        List<Customer> customerList = customerDao.queryEntityList("SELECT * FROM customer");
        System.out.println(customerList);
    }

    @Test
    public void queryMapTest(){
        Map<String, Object> customer = customerDao.queryMap("SELECT * FROM customer");
        System.out.println(customer);
    }
}
