package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleConnection {
    private static OracleConnection instance ;
    private final Connection conn;

    private OracleConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load the driver");
        }
        String url = "jdbc:oracle:thin:@localhost:1523:xe";
        String user = "zelda";
        String password = "password";
        conn = DriverManager.getConnection(url, user, password);
    }

    public static OracleConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new OracleConnection();
        } else if (instance.getConnection().isClosed()) {
            instance = new OracleConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }

    public void closeConnection() throws SQLException {
        conn.close();
    }
}
