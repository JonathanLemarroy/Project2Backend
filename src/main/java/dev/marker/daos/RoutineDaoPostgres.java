package dev.marker.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import dev.marker.entities.Routine;
import dev.marker.utils.ConnectionUtil;

public class RoutineDaoPostgres implements RoutineDao {

    private String tableName;

    public RoutineDaoPostgres(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Routine createRoutine(Routine routine) {
        String sql = String.format(
                "INSERT INTO %s (username, routine_name, date_scheduled, date_completed) VALUES (?,?,?,?)",
                this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, routine.getUsername());
            ps.setString(2, routine.getRoutineName());
            ps.setInt(3, routine.getDateScheduled());
            ps.setInt(4, routine.getDateCompleted());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                routine.setRoutineId(rs.getInt("routine_id"));
                return routine;
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Routine getRoutine(int routineId) {
        String sql = String.format("SELECT * FROM %s WHERE routine_id = ?", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, routineId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Routine routine = new Routine();
                routine.setRoutineId(rs.getInt("routine_id"));
                routine.setUsername(rs.getString("username"));
                routine.setRoutineName(rs.getString("routine_name"));
                routine.setDateScheduled(rs.getInt("date_scheduled"));
                routine.setDateCompleted(rs.getInt("date_completed"));
                rs.close();
                return routine;
            } else {
                rs.close();
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<Routine> getAllRoutines() {
        String sql = String.format("SELECT * FROM %s", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            ArrayList<Routine> routines = new ArrayList<Routine>();
            while (rs.next()) {
                Routine routine = new Routine();
                routine.setRoutineId(rs.getInt("routine_id"));
                routine.setUsername(rs.getString("username"));
                routine.setRoutineName(rs.getString("routine_name"));
                routine.setDateScheduled(rs.getInt("date_scheduled"));
                routine.setDateCompleted(rs.getInt("date_completed"));
                routines.add(routine);
            }
            return routines;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<Routine> getAllRoutinesForUser(String username) {
        String sql = String.format("SELECT * FROM %s WHERE username = ?", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            ArrayList<Routine> routines = new ArrayList<Routine>();
            while (rs.next()) {
                Routine routine = new Routine();
                routine.setRoutineId(rs.getInt("routine_id"));
                routine.setUsername(rs.getString("username"));
                routine.setRoutineName(rs.getString("routine_name"));
                routine.setDateScheduled(rs.getInt("date_scheduled"));
                routine.setDateCompleted(rs.getInt("date_completed"));
                routines.add(routine);
            }
            rs.close();
            return routines;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Routine updateRoutine(Routine routine) {
        String sql = String.format(
                "UPDATE %s SET username=?, routine_name=?, date_scheduled=?, date_completed=? WHERE routine_id=?",
                this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, routine.getUsername());
            ps.setString(2, routine.getRoutineName());
            ps.setInt(3, routine.getDateScheduled());
            ps.setInt(4, routine.getDateCompleted());
            ps.setInt(5, routine.getRoutineId());
            int rtnCount = ps.executeUpdate();
            if (rtnCount == 0)
                return null;
            else
                return routine;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public boolean deleteRoutine(int routineId) {
        String sql = String.format("DELETE FROM %s WHERE routine_id = ?", this.tableName);
        try (Connection connection = ConnectionUtil.createConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, routineId);
            int rowsChanged = ps.executeUpdate();
            if (rowsChanged == 0)
                return false;
            else
                return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
