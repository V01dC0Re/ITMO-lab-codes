package ru.itmo.lab3.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.itmo.lab3.model.HitResult;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Named
@ApplicationScoped
public class ResultsBean {
    private static final Logger logger = Logger.getLogger(ResultsBean.class.getName());
    private List<HitResult> results = new ArrayList<>();
    private static final String DB_URL = "jdbc:postgresql://192.168.10.80:5432/studs";
    private static final String DB_USER = "s468030";
    private static final String DB_PASSWORD = "osgp44WPnRR9MSSK";
    
    @PostConstruct
    public void init() {
        testDatabaseConnection();
        loadResultsFromDb();
    }
    
    private void testDatabaseConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            DatabaseMetaData meta = conn.getMetaData();
            logger.info(String.format("подключено к бд"));
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
                loadResultsFromDb();
                logger.info(String.format("сохранена точка X=%.2f, Y=%.2f, R=%.1f, Hit=%b",
                    result.getX(), result.getY(), result.getR(), result.isHit()));
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

    public List<HitResult> getAllResults() {
        synchronized (this) {
            return new ArrayList<>(results);
        }
    }

    private synchronized void loadResultsFromDb() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM hit_results ORDER BY created_at DESC");
            
            List<HitResult> loadedResults = new ArrayList<>();
            int count = 0;
            
            while (rs.next()) {
                BigDecimal x = rs.getBigDecimal("x");
                BigDecimal y = rs.getBigDecimal("y");
                BigDecimal r = rs.getBigDecimal("r");
                boolean hit = rs.getBoolean("hit");
                Timestamp createdAt = rs.getTimestamp("created_at");

                HitResult result = new HitResult(x, y, r, hit);

                java.lang.reflect.Field timestampField = HitResult.class.getDeclaredField("timestamp");
                timestampField.setAccessible(true);
                timestampField.set(result, createdAt);
                
                loadedResults.add(result);
                count++;

            }
            
            results = loadedResults;
            logger.info("Успешно загружено записей из БД: " + count);
            
        }catch (Exception e) {
            logger.severe("ошибка при загрузке данных: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(stmt, rs, conn);
        }
    }

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
}