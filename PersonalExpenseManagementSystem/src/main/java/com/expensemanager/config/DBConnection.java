
package com.expensemanager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/expence_tracker";

    private static final String USER = "root";

    private static final String PASSWORD = "lol";

    public static Connection getConnection() {

        try {
            Connection connection =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Database connected successfully!");

            return connection;

        } catch (SQLException e) {
                   System.out.println("Database connection failed!");
                   System.out.println(e.getMessage());
                   return null;
      }
    }
}
