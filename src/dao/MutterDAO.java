package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Mutter;

public class MutterDAO {
    private final String JDBC_URL = "jdbc:h2:file:C:/first/2019/Servlet/DB/jugyo;AUTO_SERVER=TRUE";
    private final String DB_USER = "sa";
    private final String DB_PASS = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
    }

    public List<Mutter> findAll() {
        List<Mutter> mutterList = new ArrayList<Mutter>();
        String sql = "SELECT id, name, text FROM mutter ORDER BY id DESC";
        try (Connection conn = getConnection();
                PreparedStatement pStmt = conn.prepareStatement(sql);
                ResultSet rs = pStmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String userName = rs.getString("name");
                String text = rs.getString("text");
                Mutter mutter = new Mutter(id, userName, text);
                mutterList.add(mutter);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mutterList;
    }

    public boolean create(Mutter mutter) {
        String sql = "INSERT INTO mutter(name, text) VALUES(?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setString(1, mutter.getUserName());
            pStmt.setString(2, mutter.getText());

            int result = pStmt.executeUpdate();

            if (result != 1) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
