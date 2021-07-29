package dev.marker.servicetests;

import dev.marker.daos.*;
import dev.marker.daotests.ExerciseDaoTests;
import dev.marker.entities.Exercise;
import dev.marker.entities.Routine;
import dev.marker.entities.RoutineExercise;
import dev.marker.entities.User;
import dev.marker.exceptions.PermissionException;
import dev.marker.exceptions.ResourceNotFound;
import dev.marker.services.RoutineExerciseServiceImpl;
import dev.marker.services.RoutineService;
import dev.marker.services.RoutineServiceImpl;
import dev.marker.utils.ConnectionUtil;
import dev.marker.utils.Setup;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import dev.marker.services.RoutineExerciseService;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Properties;

public class RoutineExerciseServiceTests {
    private static String routineTableName = "test_routines";
    private static String routineExerciseTableName = "test_routine_exercises";
    private static String userTableName = "test_users";
    private static String exerciseTableName = "test_exercises";
    private static RoutineDao routineDao = new RoutineDaoPostgres(routineTableName);
    private static Connection connection;
    private static RoutineService routineService = new RoutineServiceImpl(new RoutineDaoPostgres(routineTableName));
    private static RoutineExerciseService routineExerciseService = new RoutineExerciseServiceImpl(new RoutineExerciseDaoPostgres(routineExerciseTableName), routineDao);
    private static User user = new User("RServiceUser", "password", "Test", "User", "Male", 20, 70, 150, false);
    private static User userFail = new User("RServiceUserFail", "password", "Test", "User", "Male", 20, 70, 150, false);
    private Routine routine = new Routine(0, "RServiceUser", "High intensity netflix binge", 15356435, 21312414);
    private RoutineExercise routineExercise;


    @BeforeClass
    void setup() {
        try {
            Properties connectionProperties = new Properties();
            connectionProperties.load(ExerciseDaoTests.class.getResourceAsStream("/database.properties"));
            ConnectionUtil.setConnectionProperties(connectionProperties);
            connection = ConnectionUtil.createConnection();
            Setup.runSQLScript(ExerciseDaoTests.class.getResourceAsStream("/test_database_setup.sql"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            String sql = String.format("DELETE FROM %s", routineExerciseTableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", routineExerciseTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", routineTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", exerciseTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", userTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        UserDao userDao = new UserDaoPostgres(userTableName);
        userDao.createUser(user);
        userDao.createUser(userFail);
        routine = routineDao.createRoutine(routine);
        ExerciseDao exerciseDao = new ExerciseDaoPostgres(exerciseTableName);
        exerciseDao.createExercise(new Exercise("Jumping Jacks", "Jumping", "Cardio", "Youtube.com"));
    }

    @AfterClass
    void closeConnection() {
        try {
            String sql = String.format("DELETE FROM %s", routineExerciseTableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", routineExerciseTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", routineTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", exerciseTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", userTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void createValidRoutineExercise() {
        try {
            RoutineExercise testExercise = new RoutineExercise(0, "Jumping Jacks", routine.getRoutineId(), 1, 1, 1);
            routineExercise = routineExerciseService.createExercise(user, testExercise);
            Assert.assertNotEquals(0, routineExercise.getRoutineExerciseId());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
    }

    @Test
    void createRoutineExerciseInvalidRoutine() {
        try {
            RoutineExercise testExercise = new RoutineExercise(0, "Jumping Jacks", 0, 1, 1, 1);
            RoutineExercise routineExercise1 = routineExerciseService.createExercise(user, testExercise);
            Assert.assertTrue(false);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(true);
        } catch (PermissionException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    void createRoutineExerciseInvalidUser() {
        try {
            RoutineExercise testExercise = new RoutineExercise(0, "Jumping Jacks", routine.getRoutineId(), 1, 1, 1);
            RoutineExercise routineExercise1 = routineExerciseService.createExercise(userFail, testExercise);
            Assert.assertTrue(false);
        } catch (ResourceNotFound e) {
            Assert.assertFalse(true);
        } catch (PermissionException e) {
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void getValidRoutineExercise() {
        try {
            RoutineExercise testExercise = routineExerciseService.getExercise(user, routineExercise.getRoutineExerciseId());
            Assert.assertEquals(testExercise.getExerciseName(), routineExercise.getExerciseName());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void getInValidRoutineExercise() {
        try {
            RoutineExercise testExercise = routineExerciseService.getExercise(user, 0);
            Assert.assertTrue(false);
        } catch (PermissionException e) {
            Assert.assertTrue(false);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(true);
        }

    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void getRoutineExerciseWrongUser() {
        try {
            RoutineExercise testExercise = routineExerciseService.getExercise(userFail, routineExercise.getRoutineExerciseId());
            Assert.assertTrue(false);
        } catch (PermissionException e) {
            Assert.assertTrue(true);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(false);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void getAllRoutineExercises() {
        try {
            List<RoutineExercise> exercises = routineExerciseService.getAllExercisesInRoutine(user, routine.getRoutineId());
            Assert.assertEquals(exercises.get(0).getRoutineExerciseId(), routineExercise.getRoutineExerciseId());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void getAllRoutineExercisesInvalidRoutine() {
        try {
            List<RoutineExercise> exercises = routineExerciseService.getAllExercisesInRoutine(user, 0);
            Assert.assertEquals(exercises.get(0).getRoutineExerciseId(), routineExercise.getRoutineExerciseId());
        } catch (PermissionException e) {
            Assert.assertTrue(false);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void getAllRoutineExercisesInvalidUser() {
        try {
            List<RoutineExercise> exercises = routineExerciseService.getAllExercisesInRoutine(userFail, routine.getRoutineId());
            Assert.assertTrue(false);
        } catch (PermissionException e) {
            Assert.assertTrue(true);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(false);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void updateRoutineExercise() {
        try {
            RoutineExercise testExercise = new RoutineExercise(routineExercise.getRoutineExerciseId(),
                    routineExercise.getExerciseName(), routineExercise.getRoutineId(), routineExercise.getDuration(),
                    routineExercise.getReps(), 225);
            routineExercise = routineExerciseService.updateExercise(user, testExercise);
            Assert.assertEquals(routineExercise.getWeight(), testExercise.getWeight());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void updateRoutineExerciseInvalidExercise() {
        try {
            RoutineExercise testExercise = new RoutineExercise(0,
                    routineExercise.getExerciseName(), routineExercise.getRoutineId(), routineExercise.getDuration(),
                    routineExercise.getReps(), 225);
            RoutineExercise routineExercise1 = routineExerciseService.updateExercise(user, testExercise);
            Assert.assertTrue(false);
        } catch (PermissionException e) {
            Assert.assertTrue(false);
        }
        catch(ResourceNotFound e){
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void updateRoutineExerciseInvalidRoutine() {
        try {
            RoutineExercise testExercise = new RoutineExercise(routineExercise.getRoutineExerciseId(),
                    routineExercise.getExerciseName(), 0, routineExercise.getDuration(),
                    routineExercise.getReps(), 225);
            RoutineExercise routineExercise1 = routineExerciseService.updateExercise(user, testExercise);
            Assert.assertTrue(false);
        } catch (PermissionException e) {
            Assert.assertTrue(false);
        }
        catch(ResourceNotFound e){
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void updateRoutineExerciseInvalidUser() {
        try {
            RoutineExercise testExercise = new RoutineExercise(routineExercise.getRoutineExerciseId(),
                    routineExercise.getExerciseName(), routineExercise.getRoutineId(), routineExercise.getDuration(),
                    routineExercise.getReps(), 225);
            RoutineExercise routineExercise1 = routineExerciseService.updateExercise(userFail, testExercise);
            Assert.assertTrue(false);
        } catch (PermissionException e) {
            Assert.assertTrue(true);
        }
        catch(ResourceNotFound e){
            Assert.assertTrue(false);
        }
    }


    @Test(dependsOnMethods = "createValidRoutineExercise")
    void deleteNonExistingExercise(){
        try{
            Boolean result = routineExerciseService.deleteExercise(user, 0);
            Assert.assertTrue(false);
        }
        catch(PermissionException e){
            Assert.assertTrue(false);
        }
        catch(ResourceNotFound e){
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = "createValidRoutineExercise")
    void deleteExerciseInvalidUser(){
        try{
            Boolean result = routineExerciseService.deleteExercise(userFail, routineExercise.getRoutineExerciseId());
            Assert.assertTrue(false);
        }
        catch(PermissionException e){
            Assert.assertTrue(true);
        }
        catch(ResourceNotFound e){
            Assert.assertTrue(false);
        }
    }



}
