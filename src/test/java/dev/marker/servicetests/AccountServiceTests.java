package dev.marker.servicetests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dev.marker.daos.UserDaoPostgres;
import dev.marker.entities.User;
import dev.marker.exceptions.DuplicationException;
import dev.marker.exceptions.IncorrectArguments;
import dev.marker.exceptions.InvalidSession;
import dev.marker.exceptions.ResourceNotFound;
import dev.marker.services.AccountService;
import dev.marker.services.AccountServiceImpl;
import dev.marker.utils.ConnectionUtil;
import dev.marker.utils.Setup;

public class AccountServiceTests {

    private static String tableName = "test_users";
    private static Connection connection;
    private static AccountService accountService = new AccountServiceImpl(new UserDaoPostgres(tableName), 86400);

    @BeforeClass
    void setup() {
        try {
            Properties connectionProperties = new Properties();
            connectionProperties.load(AccountServiceTests.class.getResourceAsStream("/database.properties"));
            ConnectionUtil.setConnectionProperties(connectionProperties);
            Setup.runSQLScript(AccountServiceTests.class.getResourceAsStream("/test_database_setup.sql"));
            connection = ConnectionUtil.createConnection();
            String sql = String.format("DELETE FROM %s", tableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    void closeConnection() {
        try {
            String sql = String.format("DELETE FROM %s", tableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void createAccountAndLogOutSuccess() {
        String session = null;
        try {
            session = accountService.createAccount("testUser", "testPass", "John", "Doe", "Male", 10, 10, 10, false);
            Assert.assertNotNull(session);
            Assert.assertTrue(session.length() > 0);
            User user = accountService.getUser(session);
            Assert.assertEquals(user.getFirstName(), "John");
            Assert.assertEquals(user.getLastName(), "Doe");
        } catch (InvalidSession | DuplicationException | IncorrectArguments e) {
            Assert.assertFalse(true);
        }
        accountService.logOut(session);
        try {
            accountService.getUser(session);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    void createAccountMissingParamsFailure() {
        try {
            accountService.createAccount(null, "testPass", "John", "Doe", "Male", 10, 10, 10, false);
            Assert.assertFalse(true);
        } 
        catch(IncorrectArguments e){
            Assert.assertTrue(true);
        }
        catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            accountService.createAccount("testUser", null, "John", "Doe", "Male", 10, 10, 10, false);
            Assert.assertFalse(true);
        } 
        catch(IncorrectArguments e){
            Assert.assertTrue(true);
        }
        catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            accountService.createAccount("testUser", "testPass", null, "Doe", "Male", 10, 10, 10, false);
            Assert.assertFalse(true);
        } 
        catch(IncorrectArguments e){
            Assert.assertTrue(true);
        }
        catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            accountService.createAccount("testUser", "testPass", "John", null, "Male", 10, 10, 10, false);
            Assert.assertFalse(true);
        } 
        catch(IncorrectArguments e){
            Assert.assertTrue(true);
        }
        catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            accountService.createAccount("testUser", "testPass", "John", "Doe", "Male", -1, 10, 10, false);
            Assert.assertFalse(true);
        } 
        catch(IncorrectArguments e){
            Assert.assertTrue(true);
        }
        catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            accountService.createAccount("testUser", "testPass", "John", "Doe", "Male", 10, -1, 10, false);
            Assert.assertFalse(true);
        } 
        catch(IncorrectArguments e){
            Assert.assertTrue(true);
        }
        catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            accountService.createAccount("testUser", "testPass", "John", "Doe", "Male", 10, 10, -1, false);
            Assert.assertFalse(true);
        } 
        catch(IncorrectArguments e){
            Assert.assertTrue(true);
        }
        catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            accountService.createAccount("testUser", "testPass", "John", "Doe", "Male", 10, 10, 10, false);
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertFalse(true);
        }
        catch(DuplicationException e){
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = { "createAccountAndLogOutSuccess" })
    void logInAndOutOfAccountSuccess() {
        String session = null;
        try {
            session = accountService.logIn("testUser", "testPass");
            Assert.assertNotNull(session);
            Assert.assertTrue(session.length() > 0);
            User user = accountService.getUser(session);
            Assert.assertEquals(user.getFirstName(), "John");
            Assert.assertEquals(user.getLastName(), "Doe");
        } catch (InvalidSession | ResourceNotFound e) {
            Assert.assertFalse(true);
        }
        accountService.logOut(session);
        try {
            accountService.getUser(session);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = { "createAccountAndLogOutSuccess" })
    void logInAccountMissingParamsFailure() {
        try {
            accountService.logIn(null, "testPass");
            Assert.assertFalse(true);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(true);
        }
        try {
            accountService.logIn("testUser", null);
            Assert.assertFalse(true);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = { "createAccountAndLogOutSuccess" , "logInAndOutOfAccountSuccess"})
    void logInAccountAndUpdateSuccess() {
        String session = null;
        try {
            session = accountService.logIn("testUser", "testPass");
        } catch (ResourceNotFound e) {
            Assert.assertFalse(true);
        }
        try {
            User user = accountService.updateUser(session, "newPass", "newFirst", "newLast", "newGen", 1000, 1000, 1000);
            Assert.assertEquals(user.getPassword(), "newPass");
            Assert.assertEquals(user.getFirstName(), "newFirst");
            Assert.assertEquals(user.getLastName(), "newLast");
            Assert.assertEquals(user.getGender(), "newGen");
            Assert.assertEquals(user.getAge(), 1000);
            Assert.assertEquals(user.getHeight(), 1000);
            Assert.assertEquals(user.getWeight(), 1000);
        } catch (InvalidSession | IncorrectArguments e) {
            Assert.assertFalse(true);
        }
    }

    @Test(dependsOnMethods = { "createAccountAndLogOutSuccess" })
    void logInAccountAndUpdateFailure() {
        String session = null;
        try {
            session = accountService.logIn("testUser", "testPass");
        } catch (ResourceNotFound e) {
            Assert.assertFalse(true);
        }
        try {
            accountService.updateUser(session, null, "newFirst", "newLast", "newGen", 1000, 1000, 1000);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        }
        try {
            accountService.updateUser(session, "newPass", null, "newLast", "newGen", 1000, 1000, 1000);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        }
        try {
            accountService.updateUser(session, "newPass", "newFirst", null, "newGen", 1000, 1000, 1000);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        }
        try {
            accountService.updateUser(session, "newPass", "newFirst", "newLast", "newGen", -1, 1000, 1000);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        }
        try {
            accountService.updateUser(session, "newPass", "newFirst", "newLast", "newGen", 1000, -1, 1000);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        }
        try {
            accountService.updateUser(session, "newPass", "newFirst", "newLast", "newGen", 1000, 1000, -1);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        }
        try {
            accountService.updateUser(null, "newPass", "newFirst", "newLast", "newGen", 1000, 1000, 1000);
            Assert.assertFalse(true);
        } catch (InvalidSession e) {
            Assert.assertTrue(true);
        } catch (IncorrectArguments e) {
            Assert.assertFalse(true);
        }
    }
}
