package dev.marker.servicetests;

import dev.marker.entities.Exercise;
import dev.marker.entities.User;
import dev.marker.exceptions.DuplicationException;
import dev.marker.exceptions.IncorrectArguments;
import dev.marker.exceptions.PermissionException;
import dev.marker.exceptions.ResourceNotFound;
import dev.marker.services.ExerciseService;
import dev.marker.daos.ExerciseDaoPostgres;
import dev.marker.services.ExerciseServiceImpl;
import dev.marker.utils.ConnectionUtil;
import dev.marker.utils.Setup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ExerciseServiceTests {

    private static String tableName = "test_exercises";
    private static Connection connection;
    private static ExerciseService exerciseService = new ExerciseServiceImpl(new ExerciseDaoPostgres(tableName));
    private static User admin = new User("newAdmin", "pass", "system", "admin", "male", 0, 0, 0, true);
    private static User user = new User("newUser", "pass", "new", "user", "male", 0, 0, 0, false);
    private static Exercise exercise;

    @BeforeClass
    void setup() {
        try {
            Properties connectionProperties = new Properties();
            connectionProperties.load(ExerciseServiceTests.class.getResourceAsStream("/database.properties"));
            ConnectionUtil.setConnectionProperties(connectionProperties);
            connection = ConnectionUtil.createConnection();
            Setup.runSQLScript(ExerciseServiceTests.class.getResourceAsStream("/test_database_setup.sql"));
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
    void createValidExercise() {
        try {
            exercise = exerciseService.createExercise(admin, "Jumping Jacks", "Jumping", "Cardio", "Youtube.com");
        } catch (IncorrectArguments | PermissionException | DuplicationException e) {
            Assert.assertFalse(true);
        }
    }

    @Test(dependsOnMethods = { "createValidExercise" })
    void createInvalidExercise() {
        try {
            exerciseService.createExercise(admin, exercise.getExerciseName(), "Abdominal workout", "Strength",
                    "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertFalse(true);
        } catch (PermissionException e) {
            Assert.assertFalse(true);
        } catch (DuplicationException e) {
            Assert.assertTrue(true);
        }
        try {
            exerciseService.createExercise(user, "Situps", "Abdominal workout", "Strength", "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertFalse(true);
        } catch (PermissionException e) {
            Assert.assertTrue(true);
        } catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            exerciseService.createExercise(admin, null, "Abdominal workout", "Strength", "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        } catch (PermissionException e) {
            Assert.assertFalse(true);
        } catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            exerciseService.createExercise(admin, "Situps", null, "Strength", "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        } catch (PermissionException e) {
            Assert.assertFalse(true);
        } catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
        try {
            exerciseService.createExercise(admin, "Situps", "Abdominal workout", null, "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        } catch (PermissionException e) {
            Assert.assertFalse(true);
        } catch (DuplicationException e) {
            Assert.assertFalse(true);
        }
    }

    @Test(dependsOnMethods = { "createValidExercise" })
    void getValidExercise() {
        try {
            Exercise returnedExercise = exerciseService.getExercise(exercise.getExerciseName());
            Assert.assertEquals(returnedExercise.getDescription(), exercise.getDescription());
        } catch (ResourceNotFound e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    void getInvalidExercise() {
        try {
            exerciseService.getExercise("nonExistentExercise");
            Assert.assertFalse(true);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = { "createValidExercise" })
    void getAllExercises() {
        List<Exercise> exercises = exerciseService.getAllExercises();
        Assert.assertEquals(exercises.size(), 1);
    }

    @Test(dependsOnMethods = { "createValidExercise" })
    void updateValidExercise() {
        try {
            exercise = exerciseService.updateExercise(admin, exercise.getExerciseName(), "New Description", "New Type",
                    "New Link");
            Assert.assertEquals(exercise.getDescription(), "New Description");
            Assert.assertEquals(exercise.getType(), "New Type");
            Assert.assertEquals(exercise.getVideoLink(), "New Link");
        } catch (ResourceNotFound | IncorrectArguments | PermissionException e) {
            Assert.assertFalse(true);
        }
    }

    @Test(dependsOnMethods = { "createValidExercise" })
    void updateInvalidExercise() {
        try {
            exerciseService.updateExercise(admin, exercise.getExerciseName(), null, "Strength", "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        } catch (PermissionException e) {
            Assert.assertFalse(true);
        } catch (ResourceNotFound e) {
            Assert.assertFalse(true);
        }
        try {
            exerciseService.updateExercise(admin, exercise.getExerciseName(), "Abdominal workout", null, "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertTrue(true);
        } catch (PermissionException e) {
            Assert.assertFalse(true);
        } catch (ResourceNotFound e) {
            Assert.assertFalse(true);
        }
        try {
            exerciseService.updateExercise(user, exercise.getExerciseName(), "Abdominal workout", "Strength",
                    "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertFalse(true);
        } catch (PermissionException e) {
            Assert.assertTrue(true);
        } catch (ResourceNotFound e) {
            Assert.assertFalse(true);
        }
        try {
            exerciseService.updateExercise(admin, "nonExistingExercise", "Abdominal workout", "Strength",
                    "Youtube.com");
            Assert.assertFalse(true);
        } catch (IncorrectArguments e) {
            Assert.assertFalse(true);
        } catch (PermissionException e) {
            Assert.assertFalse(true);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(true);
        }
    }

    @Test(dependsOnMethods = { "createValidExercise" })
    void deleteInvalidExercise() {
        try {
            exerciseService.deleteExercise(user, exercise.getExerciseName());
            Assert.assertFalse(true);
        } catch (ResourceNotFound e) {
            Assert.assertFalse(true);
        } catch (PermissionException e) {
            Assert.assertTrue(true);
        }
        try {
            exerciseService.deleteExercise(admin, "nonExistingExercise");
            Assert.assertFalse(true);
        } catch (ResourceNotFound e) {
            Assert.assertTrue(true);
        } catch (PermissionException e) {
            Assert.assertFalse(true);
        }
    }

    @Test(dependsOnMethods = { "createValidExercise", "deleteInvalidExercise" })
    void deleteValidExercise() {
        try {
            String exerciseName = exerciseService.deleteExercise(admin, exercise.getExerciseName());
            Assert.assertEquals(exerciseName, exercise.getExerciseName());
        } catch (ResourceNotFound | PermissionException e) {
            Assert.assertFalse(true);
        }
    }
}
