package com.idos.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.typesafe.config.ConfigFactory;
 
public class SingletonDBConnection
{
  private static SingletonDBConnection singleInstance;
  private static MysqlDataSource dataSource;
  private static Connection dbConnect;
  private static final  String dbClassName = ConfigFactory.load().getString("db.default.driver");
  private static final  String connection =  ConfigFactory.load().getString("db.default.url");
  private static final  String user =  ConfigFactory.load().getString("db.default.user");
  private static final  String pwd =  ConfigFactory.load().getString("db.default.password");
   
  private SingletonDBConnection()
  {
    try
    {
      System.out.println("SK >>>>>>>>>>>>>>>>> Inside Single DB");
      //Context initContext = new InitialContext();
      //Context envContext  = (Context) initContext.lookup("java:/comp/env");
      //dataSource       = (DataSource) envContext.lookup("jdbc/testdb");
      dataSource = new MysqlDataSource();
     
      dataSource.setUrl(connection);
      dataSource.setUser(user);
      dataSource.setPassword(pwd);
      //dataSource.setDatabaseName("manavrachna");
       
      try
      {
        dbConnect  = dataSource.getConnection();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }  
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
   
  public static SingletonDBConnection getInstance()
  {
    if(singleInstance == null)
    {
      synchronized (SingletonDBConnection.class)
      {
        if(singleInstance == null)
        {
          singleInstance = new SingletonDBConnection();
        }
      }
    }
 
    return singleInstance;
  }
   
  public static Connection getConnInst()
  {
    try
    {
      dbConnect = dataSource.getConnection();
    }
    catch (SQLException e1)
    {
      e1.printStackTrace();
    }
     
    if(dbConnect == null)
    {
      try
      {
        //Context initContext = new InitialContext();
        //Context envContext  = (Context) initContext.lookup("java:/comp/env");
        //dataSource        = (DataSource) envContext.lookup("jdbc/testdb");
        dataSource = new MysqlDataSource();        
        dataSource.setUrl(connection);
         
        try
        {
          dbConnect  = dataSource.getConnection();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }  
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
     
    return dbConnect;   
  }
}
