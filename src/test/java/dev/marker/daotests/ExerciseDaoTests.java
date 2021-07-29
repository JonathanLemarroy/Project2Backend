package dev.marker.daotests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dev.marker.daos.ExerciseDao;
import dev.marker.daos.ExerciseDaoPostgres;
import dev.marker.entities.Exercise;
import dev.marker.utils.ConnectionUtil;
import dev.marker.utils.Setup;

public class ExerciseDaoTests {

    private static String tableName = "test_exercises";
    private static ExerciseDao exerciseDao = new ExerciseDaoPostgres(tableName);
    private static Connection connection;

    @BeforeClass
    void setupConnection(){
        try {
            Properties connectionProperties = new Properties();
            connectionProperties.load(ExerciseDaoTests.class.getResourceAsStream("/database.properties"));
            ConnectionUtil.setConnectionProperties(connectionProperties);
            connection = ConnectionUtil.createConnection();
            Setup.runSQLScript(ExerciseDaoTests.class.getResourceAsStream("/test_database_setup.sql"));
            String sql = String.format("DELETE FROM %s", tableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterMethod
    void emptyTables(){
        try{
            String sql = String.format("DELETE FROM %s", tableName);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
        }
        catch(Exception e){
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
    void createNewExercise() {
        Exercise exercise = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise returnedExercise = exerciseDao.createExercise(exercise);
        Assert.assertNotNull(returnedExercise);
        Assert.assertEquals(exercise.getExerciseName(), returnedExercise.getExerciseName());
        Assert.assertEquals(exercise.getDescription(), returnedExercise.getDescription());
        Assert.assertEquals(exercise.getType(), returnedExercise.getType());
        Assert.assertEquals(exercise.getVideoLink(), returnedExercise.getVideoLink());
    }

    @Test
    void createNullExercise1() {
        Exercise exercise = new Exercise(null, "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise returnedExercise = exerciseDao.createExercise(exercise);
        Assert.assertNull(returnedExercise);
    }

    @Test
    void createNullExercise2() {
        Exercise exercise = new Exercise("Jogging", null, "Cardio", "www.youtube.com");
        Exercise returnedExercise = exerciseDao.createExercise(exercise);
        Assert.assertNull(returnedExercise);
    }

    @Test
    void createNullExercise3() {
        Exercise exercise = new Exercise("Jogging", "Run for a duration of time.", null, "www.youtube.com");
        Exercise returnedExercise = exerciseDao.createExercise(exercise);
        Assert.assertNull(returnedExercise);
    }

    @Test
    void createExistingExercise() {
        Exercise exercise1 = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise returnedExercise1 = exerciseDao.createExercise(exercise1);
        Exercise exercise2 = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise returnedExercise2 = exerciseDao.createExercise(exercise2);
        Assert.assertNotNull(returnedExercise1);
        Assert.assertNull(returnedExercise2);
    }

    @Test
    void getExistingExercise() {
        Exercise exercise1 = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise exercise2 = new Exercise("Sprinting", "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise exercise3 = new Exercise("Lifting", "Lifting weights for a specified number of reps.", "Strenth", "www.youtube.com");
        exerciseDao.createExercise(exercise1);
        exerciseDao.createExercise(exercise2);
        exerciseDao.createExercise(exercise3);
        Exercise returnedExercise = exerciseDao.getExercise("Sprinting");
        Assert.assertNotNull(returnedExercise);
    }

    @Test
    void getNonExistingExercise() {
        Exercise returnedExercise = exerciseDao.getExercise("Sprinting");
        Assert.assertNull(returnedExercise);
    }

    @Test
    void getAllExercises1() {
        Exercise exercise1 = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise exercise2 = new Exercise("Sprinting", "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise exercise3 = new Exercise("Lifting", "Lifting weights for a specified number of reps.", "Strenth", "www.youtube.com");
        exerciseDao.createExercise(exercise1);
        exerciseDao.createExercise(exercise2);
        exerciseDao.createExercise(exercise3);
        List<Exercise> exercises = exerciseDao.getAllExercises();
        Assert.assertEquals(exercises.size(), 3);
    }

    @Test
    void getAllExercises2() {
        List<Exercise> exercises = exerciseDao.getAllExercises();
        Assert.assertEquals(exercises.size(), 0);
    }

    @Test
    void updateExistingExercise(){
        Exercise exercise1 = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");
        exerciseDao.createExercise(exercise1);
        Exercise exercise2 = new Exercise("Jogging", "Updated description.", "Updated", "www.youtube.com/updated");
        Exercise returnedExercise = exerciseDao.updateExercise(exercise2);
        Assert.assertEquals(exercise2.getExerciseName(), returnedExercise.getExerciseName());
        Assert.assertEquals(exercise2.getDescription(), returnedExercise.getDescription());
        Assert.assertEquals(exercise2.getType(), returnedExercise.getType());
        Assert.assertEquals(exercise2.getVideoLink(), returnedExercise.getVideoLink());
    }

    @Test
    void updateNonExistingExercise(){
        Exercise exercise = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");
        Exercise returnedExercise = exerciseDao.updateExercise(exercise);
        Assert.assertNull(returnedExercise);
    }

    @Test
    void deleteExistingExercise(){
        Exercise exercise = new Exercise("Jogging", "Run for a duration of time.", "Cardio", "www.youtube.com");
        exerciseDao.createExercise(exercise);
        String exerciseName = exerciseDao.deleteExercise(exercise.getExerciseName());
        Assert.assertEquals(exerciseName, exercise.getExerciseName());
    }

    @Test
    void deleteNonExistingExercise(){
        String exerciseName = exerciseDao.deleteExercise("Jogging");
        Assert.assertNull(exerciseName);
    }
}
