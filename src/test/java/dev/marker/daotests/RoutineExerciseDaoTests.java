package dev.marker.daotests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dev.marker.daos.ExerciseDao;
import dev.marker.daos.ExerciseDaoPostgres;
import dev.marker.daos.RoutineDao;
import dev.marker.daos.RoutineDaoPostgres;
import dev.marker.daos.RoutineExerciseDao;
import dev.marker.daos.RoutineExerciseDaoPostgres;
import dev.marker.daos.UserDao;
import dev.marker.daos.UserDaoPostgres;
import dev.marker.entities.Exercise;
import dev.marker.entities.Routine;
import dev.marker.entities.RoutineExercise;
import dev.marker.entities.User;
import dev.marker.utils.ConnectionUtil;
import dev.marker.utils.Setup;

public class RoutineExerciseDaoTests {

    private static User user = new User("TestUser", "password", "Test", "User", "Male", 20, 70, 150, false);
    private static Routine routine = new Routine(0, user.getUsername(), "Legs Day", 0, 0);
    private static Exercise ex = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");

    private static String userTableName = "test_users";
    private static String routineTableName = "test_routines";
    private static String exerciseTableName = "test_exercises";
    private static String routineExerciseTableName = "test_routine_exercises";
    private static UserDao userDao = new UserDaoPostgres(userTableName);
    private static RoutineDao routineDao = new RoutineDaoPostgres(routineTableName);
    private static ExerciseDao exerciseDao = new ExerciseDaoPostgres(exerciseTableName);
    private static RoutineExerciseDao routineExerciseDao = new RoutineExerciseDaoPostgres(routineExerciseTableName);
    private static Connection connection;

    @BeforeClass
    void testInit(){
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
            sql = String.format("DELETE FROM %s", routineTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", exerciseTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", userTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        userDao.createUser(user);
        routineDao.createRoutine(routine);
        exerciseDao.createExercise(ex);
    }

    @AfterMethod
    void emptyTables(){
        try{
            String sql = String.format("DELETE FROM %s", routineExerciseTableName);
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.execute();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @AfterClass
    void closeConnection(){
        try{
            String sql = String.format("DELETE FROM %s", routineTableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", exerciseTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            sql = String.format("DELETE FROM %s", userTableName);
            ps = connection.prepareStatement(sql);
            ps.execute();
            connection.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    @Test
    void createNewExercise() {
        RoutineExercise exercise = new RoutineExercise(1, ex.getExerciseName(), routine.getRoutineId(), 1, 1, 1);
        RoutineExercise returnedExercise = routineExerciseDao.createExercise(exercise);
        Assert.assertEquals(exercise.getExerciseName(), returnedExercise.getExerciseName());
        Assert.assertEquals(exercise.getReps(), returnedExercise.getReps());
        Assert.assertEquals(exercise.getWeight(), returnedExercise.getWeight());
        Assert.assertEquals(exercise.getDuration(), returnedExercise.getDuration());
        Assert.assertNotEquals(exercise.getRoutineExerciseId(), 0);
    }

    @Test
    void getExistingExercise() {
        RoutineExercise exercise = new RoutineExercise(1, ex.getExerciseName(), routine.getRoutineId(), 1, 1, 1);
        exercise = routineExerciseDao.createExercise(exercise);
        RoutineExercise returnedExercise = routineExerciseDao.getExercise(exercise.getRoutineExerciseId());
        Assert.assertNotNull(returnedExercise);
        Assert.assertEquals(exercise.getRoutineExerciseId(), returnedExercise.getRoutineExerciseId());
        Assert.assertEquals(exercise.getExerciseName(), returnedExercise.getExerciseName());
        Assert.assertEquals(exercise.getDuration(), returnedExercise.getDuration());
        Assert.assertEquals(exercise.getReps(), returnedExercise.getReps());
        Assert.assertEquals(exercise.getRoutineId(), returnedExercise.getRoutineId());
        Assert.assertEquals(exercise.getWeight(), returnedExercise.getWeight());
    }

    @Test
    void getNonExistingExercise() {
        RoutineExercise returnedExercise = routineExerciseDao.getExercise(1);
        Assert.assertNull(returnedExercise);
    }

    @Test
    void getAllExercises() {
        RoutineExercise exercise1 = new RoutineExercise(1, ex.getExerciseName(), routine.getRoutineId(), 1, 1, 1);
        exercise1 = routineExerciseDao.createExercise(exercise1);
        RoutineExercise exercise2 = new RoutineExercise(1, ex.getExerciseName(), routine.getRoutineId(), 1, 1, 1);
        exercise2 = routineExerciseDao.createExercise(exercise2);
        RoutineExercise exercise3 = new RoutineExercise(1, ex.getExerciseName(), routine.getRoutineId(), 1, 1, 1);
        exercise3 = routineExerciseDao.createExercise(exercise3);
        List<RoutineExercise> returnedExercises = routineExerciseDao.getAllExercisesInRoutine(exercise1.getRoutineId());
        Assert.assertNotNull(returnedExercises);
        Assert.assertEquals(returnedExercises.size(), 3);
        Assert.assertEquals(returnedExercises.get(0).getExerciseName(), ex.getExerciseName());
        Assert.assertEquals(returnedExercises.get(1).getExerciseName(), ex.getExerciseName());
        Assert.assertEquals(returnedExercises.get(2).getExerciseName(), ex.getExerciseName());
    }

    @Test
    void updateExistingExercise() {
        RoutineExercise exercise1 = new RoutineExercise(1, ex.getExerciseName(), routine.getRoutineId(), 1, 1, 1);
        RoutineExercise exercise2 = routineExerciseDao.createExercise(exercise1);
        exercise2.setWeight(200);
        exercise2.setReps(100);
        RoutineExercise returnedExercise = routineExerciseDao.updateExercise(exercise2);
        Assert.assertNotNull(returnedExercise);
        Assert.assertEquals(exercise1.getExerciseName(), returnedExercise.getExerciseName());
        Assert.assertEquals(exercise2.getWeight(), returnedExercise.getWeight());
        Assert.assertEquals(exercise2.getReps(), returnedExercise.getReps());
    }

    @Test
    void updateNonExistingExercise() {
        RoutineExercise exercise1 = new RoutineExercise(1, ex.getExerciseName(), routine.getRoutineId(), 1, 1, 1);
        RoutineExercise returnedExercise = routineExerciseDao.updateExercise(exercise1);
        Assert.assertNull(returnedExercise);
    }

    @Test
    void deleteExistingExercise() {
        RoutineExercise exercise1 = new RoutineExercise(1, ex.getExerciseName(), routine.getRoutineId(), 1, 1, 1);
        RoutineExercise exercise2 = routineExerciseDao.createExercise(exercise1);
        boolean deleted = routineExerciseDao.deleteExercise(exercise2.getRoutineExerciseId());
        Assert.assertTrue(deleted);
    }

    @Test
    void deleteNonExistingExercise() {
        boolean deleted = routineExerciseDao.deleteExercise(1);
        Assert.assertFalse(deleted);
    }
}
