package com.databend.jdbc;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.*;

public class TestPrepareStatement
{
    private Connection createConnection()
            throws SQLException
    {
        String url = "jdbc:databend://localhost:8000";
        return DriverManager.getConnection(url, "root", "root");
    }
    @BeforeTest
    public void setUp()
            throws SQLException
    {
        // create table
        Connection c = createConnection();

        c.createStatement().execute("drop table if exists test_prepare_statement");
        c.createStatement().execute("drop table if exists test_prepare_time");
        c.createStatement().execute("drop table if exists objects_test1");
        c.createStatement().execute("create table test_prepare_statement (a int, b int)");
        c.createStatement().execute("create table test_prepare_time(a DATE, b TIMESTAMP)");
        // json data
        c.createStatement().execute("CREATE TABLE IF NOT EXISTS objects_test1(id TINYINT, obj VARIANT, var VARIANT) Engine = Fuse");
    }
    @Test(groups = "IT")
    public void TestBatchInsert() throws SQLException {
        Connection c = createConnection();
        c.setAutoCommit(false);

        PreparedStatement ps = c.prepareStatement("insert into test_prepare_statement values");
        ps.setInt(1, 1);
        ps.setInt(2, 2);
        ps.addBatch();
        ps.setInt(1, 3);
        ps.setInt(2, 4);
        ps.addBatch();
        System.out.println("execute batch insert");
        int[] ans = ps.executeBatch();
        Assert.assertEquals(ans.length, 2);
        Assert.assertEquals(ans[0], 1);
        Assert.assertEquals(ans[1], 1);
        Statement statement = c.createStatement();

        System.out.println("execute select");
        statement.execute("SELECT * from test_prepare_statement");
        ResultSet r = statement.getResultSet();

        while (r.next()) {
            System.out.println(r.getInt(1));
            System.out.println(r.getInt(2));
        }
    }

    @Test(groups = "IT")
    public void TestBatchInsertWithTime() throws SQLException {
        Connection c = createConnection();
        c.setAutoCommit(false);
        PreparedStatement ps = c.prepareStatement("insert into test_prepare_time values");
        ps.setDate(1, Date.valueOf("2020-01-10"));
        ps.setTimestamp(2, Timestamp.valueOf("1983-07-12 21:30:55.888"));
        ps.addBatch();
        ps.setDate(1, Date.valueOf("1970-01-01"));
        ps.setTimestamp(2, Timestamp.valueOf("1970-01-01 00:00:01"));
        ps.addBatch();
        ps.setDate(1, Date.valueOf("2021-01-01"));
        ps.setTimestamp(2, Timestamp.valueOf("1970-01-01 00:00:01.234"));
        int[] ans = ps.executeBatch();
        Statement statement = c.createStatement();

        System.out.println("execute select on time");
        statement.execute("SELECT * from test_prepare_time");
        ResultSet r = statement.getResultSet();

        while (r.next()) {
            System.out.println(r.getDate(1).toString());
            System.out.println(r.getTimestamp(2).toString());
        }
    }
}
