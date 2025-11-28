import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

public class BatchInsert {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mydatabase";
        String user = "john";
        String password = "1";

        String sql = "INSERT INTO hit_results (x, y, r, hit, created_at) VALUES (?, ?, ?, ?, ?)";

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            conn.setAutoCommit(false);
            Random rand = new Random();

            for (int i = 0; i < 1000000; i++) {
                double x = rand.nextDouble() * 3  - 1; 
                double y = rand.nextDouble() * 5 - 2.9;
                double r = 2 + rand.nextDouble() * 3;  
                boolean hit = (x >= 0 && y <= 0 && x + y <= r) ||
                              (x <= 0 && y >= 0 && x * x + y * y <= r * r / 4) ||
                              (x <= 0 && y <= 0 && y >= 2 * x + r / 2);
                Timestamp now = Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.ofHours(3)));

                ps.setDouble(1, x);
                ps.setDouble(2, y);
                ps.setDouble(3, r);
                ps.setBoolean(4, hit);
                ps.setTimestamp(5, now);

                ps.addBatch();

                if ((i + 1) % 500 == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
            conn.commit();
            System.out.println("1000 строк успешно добавлено");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
