package org.example;
import java.sql.*;
import java.util.Properties;

public class PGConnection {
    private static PGConnection instance ;
    private final Connection conn;

    private PGConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load the driver");
        }
        String url = "jdbc:postgresql://localhost:5432/enlace";
        Properties props = new java.util.Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "password");
        props.setProperty("prepareThreshold", "0");
        conn = DriverManager.getConnection(url, props);
    }
    public static PGConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new PGConnection();
        } else if (instance.getConnection().isClosed()) {
            instance = new PGConnection();
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
