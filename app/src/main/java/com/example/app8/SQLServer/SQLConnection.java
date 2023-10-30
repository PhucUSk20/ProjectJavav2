package com.example.app8.SQLServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {
    private static String ip = "192.168.1.6";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "PROJECT";
    private static String username = "phuc";
    private static String password = "123";
    private static String url = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + database;
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName(Classes);
                connection = DriverManager.getConnection(url, username, password);
                return connection;
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return connection;
        }
    }
}
