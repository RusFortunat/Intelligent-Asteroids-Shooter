package root.intelligentasteroidsshooter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordTable {

    private String databasePath;

    public RecordTable(String databasePath) {
        this.databasePath = databasePath;
    }

    public List<String> list() throws SQLException {
        List<String> recordScores = new ArrayList<>();
        try (Connection connection = createConnectionAndEnsureDatabase();
             ResultSet results = connection.prepareStatement("SELECT * FROM RecordTable").executeQuery()) {
            while (results.next()) {
                String entry = results.getInt("id")
                        + "," + results.getString("name") + "," + results.getInt("score");
                recordScores.add(entry);//, results.getString("name"), ""+results.getInt("score"));
            }
        }
        return recordScores;
    }

    public void add(String name, int score) throws SQLException {
        try (Connection connection = createConnectionAndEnsureDatabase()) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO RecordTable (name, score) VALUES (?, ?)");
            stmt.setString(1, name);
            stmt.setInt(2, score);
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
            conn.prepareStatement("CREATE TABLE RecordTable (id int auto_increment primary key, name varchar(255), score int)").execute();
        } catch (SQLException t) {
        }

        return conn;
    }
}
