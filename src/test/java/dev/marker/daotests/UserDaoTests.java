package dev.marker.daotests;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dev.marker.daos.UserDao;
import dev.marker.daos.UserDaoPostgres;
import dev.marker.entities.User;
import dev.marker.utils.ConnectionUtil;
import dev.marker.utils.Setup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.testng.Assert;

public class UserDaoTests {

    private static String tableName = "test_users";
    private static UserDao userDao = new UserDaoPostgres(tableName);
    private static Connection connection;

    @BeforeClass
    void setupConnection(){
        try {
            Properties connectionProperties = new Properties();
            connectionProperties.load(ExerciseDaoTests.class.getResourceAsStream("/database.properties"));
            ConnectionUtil.setConnectionProperties(connectionProperties);
            Setup.runSQLScript(UserDaoTests.class.getResourceAsStream("/test_database_setup.sql"));
            connection = ConnectionUtil.createConnection();
            String sql = String.format("DELETE FROM %s", tableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterMethod
    void clearUsers(){
        try {
            String sql = String.format("DELETE FROM %s", tableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    void closeConnection(){
        try{
            connection.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    void createNewUser() {
        User user = new User("exUsername", "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        User returnedUser = userDao.createUser(user);
        Assert.assertNotNull(returnedUser);
        Assert.assertEquals(user.getUsername(), returnedUser.getUsername());
        Assert.assertEquals(user.getPassword(), returnedUser.getPassword());
        Assert.assertEquals(user.getFirstName(), returnedUser.getFirstName());
        Assert.assertEquals(user.getLastName(), returnedUser.getLastName());
        Assert.assertEquals(user.getGender(), returnedUser.getGender());
        Assert.assertEquals(user.getWeight(), returnedUser.getWeight());
        Assert.assertEquals(user.getHeight(), returnedUser.getHeight());
        Assert.assertEquals(user.getAge(), returnedUser.getAge());
    }

    @Test
    void createNullUser1() {
        User user = new User(null, "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        User returnedUser = userDao.createUser(user);
        Assert.assertNull(returnedUser);
    }

    @Test
    void createNullUser2() {
        User user = new User("exUsername", null, "John", "Doe", "Male", 21, 60, 160, false);
        User returnedUser = userDao.createUser(user);
        Assert.assertNull(returnedUser);
    }

    @Test
    void createDuplicateUser() {
        User user1 = new User("exUsername", "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        User returnedUser1 = userDao.createUser(user1);
        User user2 = new User("exUsername", "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        User returnedUser2 = userDao.createUser(user2);
        Assert.assertNotNull(returnedUser1);
        Assert.assertNull(returnedUser2);
    }

    @Test
    void getExistingUser() {
        User user = new User("exUsername", "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        userDao.createUser(user);
        User returnedUser = userDao.getUser(user.getUsername());
        Assert.assertNotNull(returnedUser);
        Assert.assertEquals(user.getUsername(), returnedUser.getUsername());
        Assert.assertEquals(user.getFirstName(), returnedUser.getFirstName());
        Assert.assertEquals(user.getLastName(), returnedUser.getLastName());
        Assert.assertEquals(user.getGender(), returnedUser.getGender());
        Assert.assertEquals(user.getWeight(), returnedUser.getWeight());
        Assert.assertEquals(user.getHeight(), returnedUser.getHeight());
        Assert.assertEquals(user.getAge(), returnedUser.getAge());
    }

    @Test
    void getNonExistingUser() {
        User returnedUser = userDao.getUser("nonExistant");
        Assert.assertNull(returnedUser);
    }

    @Test
    void updateExistingUser() {
        User user1 = new User("exUsername", "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        userDao.createUser(user1);
        User user2 = new User("exUsername", "exPassword2", "Karen", "Donzo", "Female", 25, 69, 200, false);
        User returnedUser = userDao.updateUser(user2);
        Assert.assertNotNull(returnedUser);
        Assert.assertEquals(user1.getUsername(), returnedUser.getUsername());
        Assert.assertNotEquals(user1.getPassword(), returnedUser.getPassword());
        Assert.assertNotEquals(user1.getFirstName(), returnedUser.getFirstName());
        Assert.assertNotEquals(user1.getLastName(), returnedUser.getLastName());
        Assert.assertNotEquals(user1.getGender(), returnedUser.getGender());
        Assert.assertNotEquals(user1.getWeight(), returnedUser.getWeight());
        Assert.assertNotEquals(user1.getHeight(), returnedUser.getHeight());
        Assert.assertNotEquals(user1.getAge(), returnedUser.getAge());
    }

    @Test
    void updateUserPasswordToNothing() {
        User user1 = new User("exUsername", "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        userDao.createUser(user1);
        User user2 = new User("exUsername", null, "Karen", "Donzo", "Female", 25, 69, 200, false);
        User returnedUser = userDao.updateUser(user2);
        Assert.assertNull(returnedUser);
    }

    @Test
    void updateNonExistingUser() {
        User user = new User("exUsername", "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        User returnedUser = userDao.updateUser(user);
        Assert.assertNull(returnedUser);
    }

    @Test
    void deleteExistingUser() {
        User user = new User("exUsername", "exPassword", "John", "Doe", "Male", 21, 60, 160, false);
        userDao.createUser(user);
        String deleted = userDao.deleteUser(user.getUsername());
        Assert.assertNotNull(deleted);
        Assert.assertEquals(deleted, user.getUsername());
    }

    @Test
    void deleteNonExistingUser() {
        String deleted = userDao.deleteUser("nonExistant");
        Assert.assertNull(deleted);
    }
}
