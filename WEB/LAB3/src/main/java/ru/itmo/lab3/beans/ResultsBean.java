package ru.itmo.lab3.beans;
import ru. itmo. lab3.beans. PlotBean.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import ru.itmo.lab3.model.HitResult;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static ru.itmo.lab3.beans.PlotBean.isPointInArea;


@Named
@ApplicationScoped
public class ResultsBean {
    private static final Logger logger = Logger.getLogger(ResultsBean.class.getName());
    //private List<HitResult> results = new ArrayList<>();
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/mydatabase";
    private static final String DB_USER = "john";
    private static final String DB_PASSWORD = "1";
    
    @PostConstruct
    public void init() {
        testDatabaseConnection();
    }
    
    private void testDatabaseConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            DatabaseMetaData meta = conn.getMetaData();
            logger.info("подключено к бд");
        } catch (SQLException e) {
            logger.severe("ошибка подключения к бд");
            e.printStackTrace();
        }
    }

    public synchronized void saveResult(HitResult result) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            
            ps = conn.prepareStatement(
                "INSERT INTO hit_results (x, y, r, hit, created_at) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            System.out.println(result.getY() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            ps.setBigDecimal(1, result.getX());
            ps.setBigDecimal(2, result.getY());
            ps.setBigDecimal(3, result.getR());
            ps.setBoolean(4, result.isHit());
            ps.setTimestamp(5, result.getTimestamp());
            
            if (ps.executeUpdate() == 0) {
                throw new SQLException("ошибка, нет обновленных строк");
            }
            
            generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.severe("ошибка отката транзакции " + ex.getMessage());
                }
            }
            logger.severe("ошибка сохранения в бд " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("ошибка сохранения данных в базу", e);
        } finally {
            closeResources(ps, generatedKeys, conn);
        }
    }


//    private synchronized void loadResultsFromDb() {
//        Connection conn = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//
//        try {
//            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//            stmt = conn.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM hit_results ORDER BY created_at DESC");
//
//            List<HitResult> loadedResults = new ArrayList<>();
//            int count = 0;
//
//            while (rs.next()) {
//                BigDecimal x = rs.getBigDecimal("x");
//                BigDecimal y = rs.getBigDecimal("y");
//                BigDecimal r = rs.getBigDecimal("r");
//                boolean hit = rs.getBoolean("hit");
//                Timestamp createdAt = rs.getTimestamp("created_at");
//
//                HitResult result = new HitResult(x, y, r, hit);
//
//                java.lang.reflect.Field timestampField = HitResult.class.getDeclaredField("timestamp");
//                timestampField.setAccessible(true);
//                timestampField.set(result, createdAt);
//
//                loadedResults.add(result);
//                count++;
//
//            }
//
//            results = loadedResults;
//            logger.info("Успешно загружено записей из БД: " + count);
//
//        }catch (Exception e) {
//            logger.severe("ошибка при загрузке данных: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            closeResources(stmt, rs, conn);
//        }
//    }

    private void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    logger.warning("ошибка закрытия ресурса: " + e.getMessage());
                }
            }
        }
    }


    public List<HitResult> getResultsPage(int first, int pageSize) {
        List<HitResult> pageResults = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM hit_results ORDER BY created_at DESC LIMIT ? OFFSET ?")) {
            
            stmt.setInt(1, pageSize);
            stmt.setInt(2, first);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HitResult result = extractResult(rs);
                    pageResults.add(result);
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка загрузки страницы: " + e.getMessage());
            e.printStackTrace();
        }
        return pageResults;
    }
    
    public int getTotalResultsCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM hit_results")) {
            
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            logger.severe("Ошибка подсчёта записей: " + e.getMessage());
        }
        return 0;
    }
    
    private HitResult extractResult(ResultSet rs) throws SQLException {
        HitResult result = new HitResult(
            rs.getBigDecimal("x"),
            rs.getBigDecimal("y"),
            rs.getBigDecimal("r"),
            rs.getBoolean("hit")
        );
        result.setTimestamp(rs.getTimestamp("created_at"));
        return result;
    }


    public PlotData getAllResultsAsPlotData(BigDecimal r) {
        PlotData data = new PlotData();
        data.r = r;
        data.points = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT x, y, r FROM hit_results ORDER BY created_at DESC LIMIT 100000")) {

            while (rs.next()) {
                BigDecimal x = rs.getBigDecimal("x");
                BigDecimal y = rs.getBigDecimal("y");

                boolean hit = isPointInArea(x, y, r);
                PointData point = new PointData();
                point.x = x;
                point.y = y;
                point.hit = hit;
                data.points.add(point);
            }
        }catch (Exception e) {
            logger.severe("Ошибка получения данных из БД: " + e.getMessage());
        }

        return data;
    }

}