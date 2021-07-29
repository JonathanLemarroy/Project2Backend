package dev.marker.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.testng.reporters.Files;

import dev.marker.daos.UserDao;
import dev.marker.daos.UserDaoPostgres;
import dev.marker.entities.User;

public abstract class Setup {

    public static void runSQLScript(InputStream file) throws IOException {
        String script = Files.readFile(file);
        String[] statments = script.split(";");
        for (int i = 0; i < statments.length; i++) {
            try (Connection connection = ConnectionUtil.createConnection()) {
                PreparedStatement ps = connection.prepareStatement(statments[i] + ";");
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addAdmin(String username, String password, String userTable) {
        User user = new User(username, password, "System", "Admin", "Admin", 0, 0, 0, true);
        UserDao userDao = new UserDaoPostgres(userTable);
        userDao.createUser(user);
    }
}
