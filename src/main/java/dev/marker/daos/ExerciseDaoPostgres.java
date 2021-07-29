package dev.marker.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dev.marker.entities.Exercise;
import dev.marker.utils.ConnectionUtil;

public class ExerciseDaoPostgres implements ExerciseDao {

    private String tableName;

    public ExerciseDaoPostgres(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Exercise createExercise(Exercise exercise) {
        String sql = String.format("INSERT INTO %s (exercise_name, description, type, video_link) VALUES (?,?,?,?)",
                this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, exercise.getExerciseName());
            ps.setString(2, exercise.getDescription());
            ps.setString(3, exercise.getType());
            ps.setString(4, exercise.getVideoLink());
            ps.execute();
            return exercise;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Exercise getExercise(String exerciseName) {
        String sql = String.format("SELECT * FROM %s WHERE exercise_name = ?", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, exerciseName);
            Exercise exercise = new Exercise();
            ResultSet rs = ps.executeQuery();
            rs.next();
            exercise.setExerciseName(rs.getString("exercise_name"));
            exercise.setDescription(rs.getString("description"));
            exercise.setType(rs.getString("type"));
            exercise.setVideoLink(rs.getString("video_link"));
            return exercise;
        } catch (SQLException e) {
            return null;
        }

    }

    @Override
    public List<Exercise> getAllExercises() {
        String sql = String.format("SELECT * FROM %s", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            ArrayList<Exercise> exercises = new ArrayList<Exercise>();
            while (rs.next()) {
                Exercise exercise = new Exercise();
                exercise.setExerciseName(rs.getString("exercise_name"));
                exercise.setDescription(rs.getString("description"));
                exercise.setType(rs.getString("type"));
                exercise.setVideoLink(rs.getString("video_link"));
                exercises.add(exercise);
            }
            rs.close();
            return exercises;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Exercise updateExercise(Exercise exercise) {
        String sql = String.format("UPDATE %s SET description=?, type=?, video_link=? WHERE exercise_name=?",
                this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, exercise.getDescription());
            ps.setString(2, exercise.getType());
            ps.setString(3, exercise.getVideoLink());
            ps.setString(4, exercise.getExerciseName());
            int rtnCount = ps.executeUpdate();
            if (rtnCount == 0)
                return null;
            else
                return exercise;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public String deleteExercise(String exerciseName) {
        String sql = String.format("DELETE FROM %s WHERE exercise_name = ?", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, exerciseName);
            int rowsChanged = ps.executeUpdate();
            if (rowsChanged == 0)
                return null;
            else
                return exerciseName;
        } catch (SQLException e) {
            return null;
        }
    }
}
