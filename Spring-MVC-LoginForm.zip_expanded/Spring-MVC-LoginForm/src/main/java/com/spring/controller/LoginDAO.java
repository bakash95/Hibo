package com.spring.controller;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class LoginDAO {

  DataSource dataSource;
  JdbcTemplate jdbcTemplateObject;

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    this.jdbcTemplateObject = new JdbcTemplate(dataSource);
  }

  public boolean getPassword(String userName, String password) {
    String query = "select password from login where username='"+ userName+"'";
    System.out.println("query"+query);
    String passwordInDB = (String) jdbcTemplateObject.queryForObject(query, String.class);
    if (passwordInDB.equals(password)) {
      return true;
    } else
      return false;
  }

}
