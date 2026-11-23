package com.expensemanager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/expense_tracker";
    private static final String USER = System.getenv().getOrDefault("EXPENSE_DB_USER", "root");
    // Set EXPENSE_DB_PASSWORD in your operating-system environment; do not commit passwords to GitHub.
    private static final String PASSWORD = System.getenv().getOrDefault("EXPENSE_DB_PASSWORD", "");

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }
}
