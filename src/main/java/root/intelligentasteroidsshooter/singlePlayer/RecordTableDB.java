package root.intelligentasteroidsshooter.singlePlayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordTableDB {

    private String databasePath;

    public RecordTableDB(String databasePath) {
        this.databasePath = databasePath;
    }

    public List<String> toList() throws SQLException {
        List<String> recordScores = new ArrayList<>();
        try (Connection connection = createConnectionAndEnsureDatabase();
             ResultSet results = connection.prepareStatement("SELECT * FROM RecordTable").executeQuery()) {
            while (results.next()) {
                String entry = results.getInt("id") + ","
                        + results.getString("name") + "," + results.getInt("score");
                recordScores.add(entry);
            }
        }

        return recordScores;
    }

    public void add(int id, String name, int score) throws SQLException {
        try (Connection connection = createConnectionAndEnsureDatabase()) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO RecordTable (id, name, score) VALUES (?, ?, ?)");
            stmt.setInt(1, id); // seriously? SQL indexing starts from 1?
            stmt.setString(2, name);
            stmt.setInt(3, score);
            stmt.executeUpdate();
        }
    }

    public void remove(int id) throws SQLException {
        try (Connection connection = createConnectionAndEnsureDatabase()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM RecordTable WHERE id = ?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Connection createConnectionAndEnsureDatabase() throws SQLException {
        Connection conn = DriverManager.getConnection(this.databasePath, "sa", "");
        try {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS RecordTable (id int primary key, name varchar(255), score int)").execute();
        } catch (SQLException t) {
            System.out.println(t.getMessage());
        }

        return conn;
    }
}
