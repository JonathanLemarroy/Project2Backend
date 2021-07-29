package dev.marker;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import dev.marker.daos.*;
import dev.marker.endpoints.AccountEndpoints;
import dev.marker.endpoints.ExerciseEndpoints;
import dev.marker.endpoints.RoutineEndpoints;
import dev.marker.endpoints.RoutineExerciseEndpoints;
import dev.marker.services.*;
import dev.marker.utils.ConnectionUtil;
import dev.marker.utils.Setup;
import io.javalin.Javalin;

public class App {

    public static void main(String[] args){

        String USER_TABLE;
        String EXERCISE_TABLE;
        String ROUTINE_TABLE;
        String ROUTINE_EXERCISE_TABLE;

        if(args.length == 1 && args[0].equalsIgnoreCase("selenium")){
            USER_TABLE = "test_users";
            EXERCISE_TABLE = "test_exercises";
            ROUTINE_TABLE = "test_routines";
            ROUTINE_EXERCISE_TABLE = "test_routine_exercises";
        }
        else{
            USER_TABLE = "users";
            EXERCISE_TABLE = "exercises";
            ROUTINE_TABLE = "routines";
            ROUTINE_EXERCISE_TABLE = "routine_exercises";
        }
        
        final long SESSION_TIMEOUT_SECONDS = 86400; // 86400 seconds = 24 hours

        try {
            Properties connectionProperties = new Properties();
            connectionProperties.load(App.class.getResourceAsStream("/database.properties"));
            ConnectionUtil.setConnectionProperties(connectionProperties);
            Connection connection = ConnectionUtil.createConnection();
            if(args.length == 1 && args[0].equalsIgnoreCase("selenium")){
                Setup.runSQLScript(App.class.getResourceAsStream("/test_database_setup.sql"));
                String sql = String.format("DELETE FROM %s", ROUTINE_EXERCISE_TABLE);
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.execute();
                sql = String.format("DELETE FROM %s", ROUTINE_TABLE);
                ps = connection.prepareStatement(sql);
                ps.execute();
                sql = String.format("DELETE FROM %s", EXERCISE_TABLE);
                ps = connection.prepareStatement(sql);
                ps.execute();
                sql = String.format("DELETE FROM %s", USER_TABLE);
                ps = connection.prepareStatement(sql);
                ps.execute();
            }
            else
                Setup.runSQLScript(App.class.getResourceAsStream("/database_setup.sql"));
            String sql = String.format("DELETE FROM %s WHERE \"admin\" = true", USER_TABLE);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
            Properties adminProperties = new Properties();
            adminProperties.load(App.class.getResourceAsStream("/admin.properties"));
            Setup.addAdmin(adminProperties.getProperty("username"), adminProperties.getProperty("password"), USER_TABLE);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        RoutineDao routineDao = new RoutineDaoPostgres(ROUTINE_TABLE);

        RoutineService routineService = new RoutineServiceImpl(routineDao);
        ExerciseService exerciseService = new ExerciseServiceImpl(new ExerciseDaoPostgres(EXERCISE_TABLE));
        AccountService accountService = new AccountServiceImpl(new UserDaoPostgres(USER_TABLE), SESSION_TIMEOUT_SECONDS);
        RoutineExerciseService routineExerciseService = new RoutineExerciseServiceImpl(new RoutineExerciseDaoPostgres(ROUTINE_EXERCISE_TABLE), routineDao);

        AccountEndpoints accountEndpoints = new AccountEndpoints(accountService);
        RoutineEndpoints routineEndpoints = new RoutineEndpoints(routineService, accountService);
        ExerciseEndpoints exerciseEndpoints = new ExerciseEndpoints(exerciseService, accountService);
        RoutineExerciseEndpoints routineExerciseEndpoints = new RoutineExerciseEndpoints(routineExerciseService, accountService);

        Javalin app = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
            config.enableDevLogging();
        });

        app.post("/createAccount", accountEndpoints.createAccount);
        app.post("/loginAccount", accountEndpoints.loginAccount);
        app.post("/logoutAccount", accountEndpoints.logoutAccount);
        app.patch("/updateAccount", accountEndpoints.updateAccount);
        app.post("/getAccount", accountEndpoints.getAccount);
        
        app.post("/createRoutine", routineEndpoints.createRoutine);
        app.post("/getRoutineById", routineEndpoints.getRoutineById);
        app.post("/getRoutinesForUser", routineEndpoints.getRoutinesForUser);
        app.patch("/updateRoutine", routineEndpoints.updateRoutine);
        app.delete("/deleteRoutine", routineEndpoints.deleteRoutine);

        app.post("/createExercise", exerciseEndpoints.createExercise);
        app.post("/getExercise", exerciseEndpoints.getExercise);
        app.post("/getAllExercises", exerciseEndpoints.getAllExercises);
        app.patch("/updateExercise", exerciseEndpoints.updateExercise);
        app.delete("/deleteExercise", exerciseEndpoints.deleteExercise);

        app.post("/createRoutineExercise", routineExerciseEndpoints.createRoutineExercise);
        app.post("/getRoutineExercise", routineExerciseEndpoints.getRoutineExercise);
        app.post("/getAllExercisesInRoutine", routineExerciseEndpoints.getAllExercisesInRoutine);
        app.patch("/updateRoutineExercise", routineExerciseEndpoints.updateRoutineExercise);
        app.delete("/deleteRoutineExercise", routineExerciseEndpoints.deleteRoutineExercise);

        app.start();
    }
}
