package dev.marker.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionUtil {

    private static String host = "";
    private static String protocol = "";
    private static String username = "";
    private static String password = "";

    public static Connection createConnection(){
        try {
            Connection connection = DriverManager.getConnection(String.format("jdbc:%s://%s?user=%s&password=%s", protocol, host, username, password));
            return connection;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    public static void setConnectionProperties(Properties properties){
        ConnectionUtil.protocol = properties.getProperty("protocol");
        ConnectionUtil.host = properties.getProperty("host");
        ConnectionUtil.username = properties.getProperty("username");
        ConnectionUtil.password = properties.getProperty("password");
    }
}