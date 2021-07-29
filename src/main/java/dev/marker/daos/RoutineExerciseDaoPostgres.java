package dev.marker.daos;

import dev.marker.entities.RoutineExercise;
import dev.marker.utils.ConnectionUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoutineExerciseDaoPostgres implements RoutineExerciseDao {
    private String tableName;

    public RoutineExerciseDaoPostgres(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public RoutineExercise createExercise(RoutineExercise routineExercise) {

        String sql = String.format(
                "INSERT INTO %s (exercise_name, routine_id, duration, reps, weight) VALUES (?,?,?,?,?)",
                this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, routineExercise.getExerciseName());
            ps.setInt(2, routineExercise.getRoutineId());
            ps.setInt(3, routineExercise.getDuration());
            ps.setInt(4, routineExercise.getReps());
            ps.setInt(5, routineExercise.getWeight());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            routineExercise.setRoutineExerciseId(rs.getInt("routine_exercise_id"));
            return routineExercise;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public RoutineExercise getExercise(int routineExerciseId) {
        String sql = String.format("SELECT * FROM %s WHERE routine_exercise_id = ?", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, routineExerciseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                RoutineExercise routineExercise = new RoutineExercise();
                routineExercise.setRoutineExerciseId(rs.getInt("routine_exercise_id"));
                routineExercise.setExerciseName(rs.getString("exercise_name"));
                routineExercise.setRoutineId(rs.getInt("routine_id"));
                routineExercise.setDuration(rs.getInt("duration"));
                routineExercise.setReps(rs.getInt("reps"));
                routineExercise.setWeight(rs.getInt("weight"));
                rs.close();
                return routineExercise;
            } else {
                rs.close();
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<RoutineExercise> getAllExercisesInRoutine(int routineId) {
        List<RoutineExercise> routineExercises = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE routine_id = ?", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, routineId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RoutineExercise routineExercise = new RoutineExercise();
                routineExercise.setRoutineExerciseId(rs.getInt("routine_exercise_id"));
                routineExercise.setExerciseName(rs.getString("exercise_name"));
                routineExercise.setRoutineId(rs.getInt("routine_id"));
                routineExercise.setDuration(rs.getInt("duration"));
                routineExercise.setReps(rs.getInt("reps"));
                routineExercise.setWeight(rs.getInt("weight"));
                routineExercises.add(routineExercise);
            }
            return routineExercises;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public RoutineExercise updateExercise(RoutineExercise routineExercise) {
        String sql = String.format(
                "UPDATE %s SET exercise_name = ?, routine_id = ?, duration = ?, reps = ?, weight = ? WHERE routine_exercise_id = ?",
                this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, routineExercise.getExerciseName());
            ps.setInt(2, routineExercise.getRoutineId());
            ps.setInt(3, routineExercise.getDuration());
            ps.setInt(4, routineExercise.getReps());
            ps.setInt(5, routineExercise.getWeight());
            ps.setInt(6, routineExercise.getRoutineExerciseId());
            int count = ps.executeUpdate();
            if (count == 0)
                return null;
            else
                return routineExercise;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteExercise(int routineExerciseId) {
        String sql = String.format("DELETE FROM %s WHERE routine_exercise_id = ?", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, routineExerciseId);
            int rowsChanged = ps.executeUpdate();
            if (rowsChanged == 0)
                return false;
            else
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
