package root.intelligentasteroidsshooter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StoreTrainedNNsDB {

    private String NNdatabasePath;

    public StoreTrainedNNsDB(String path){
        NNdatabasePath = path;
    }

    // for getting network parameters from DB
    public List<String> toList(int score) throws SQLException {
        List<String> NNparameters = new ArrayList<>();
        NNparameters.add(""+score);

        try (Connection connection = createConnectionAndEnsureDatabase();
             ResultSet getNetwork = connection.prepareStatement("SELECT * FROM TrainedNNs WHERE score = " + (int)score).executeQuery()) {
            if(getNetwork.next()){} // do nothing, deleting .next() call causes an error //System.out.println("getNetwork is not empty: " + getNetwork.toString());

            int NNscore = getNetwork.getInt("score");
            String NNfirstLayerWeights = getNetwork.getString("firstLayerWeights");
            String NNfirstLayerBiases = getNetwork.getString("firstLayerBiases");
            String NNsecondLayerWeights = getNetwork.getString("secondLayerWeights");
            String NNsecondLayerBiases = getNetwork.getString("secondLayerBiases");
            NNparameters.add(NNscore+"");
            NNparameters.add(NNfirstLayerWeights);
            NNparameters.add(NNfirstLayerBiases);
            NNparameters.add(NNsecondLayerWeights);
            NNparameters.add(NNsecondLayerBiases);
        }

        return NNparameters;
    }

    public void addNetworkToDB(int score, String firstLayerWeights, String firstLayerBiases,
                    String secondLayerWeights, String secondLayerBiases) throws SQLException {

        List<String> content = this.getSavedList();
        if(!content.contains(score+"")){
            try (Connection connection = createConnectionAndEnsureDatabase()) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO TrainedNNs " +
                        "(score, firstLayerWeights, firstLayerBiases, secondLayerWeights, secondLayerBiases) VALUES (?, ?, ?, ?, ?)");
                stmt.setInt(1, score); // seriously? SQL indexing starts from 1?
                stmt.setString(2, firstLayerWeights);
                stmt.setString(3, firstLayerBiases);
                stmt.setString(4, secondLayerWeights);
                stmt.setString(5, secondLayerBiases);
                stmt.executeUpdate();
            }
        }
    }

    public void remove(int score) throws SQLException {
        try (Connection connection = createConnectionAndEnsureDatabase()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM TrainedNNs WHERE score = ?");
            stmt.setInt(1, score);
            stmt.executeUpdate();
        }
    }

    // list all different networks by their score in descending order
    public List<String> getSavedList() throws SQLException {
        List<Integer> scoresOfSavedNetworksInts = new ArrayList<>();

        try(Connection connection = createConnectionAndEnsureDatabase();
            ResultSet allNetworks = connection.prepareStatement("SELECT * FROM TrainedNNs").executeQuery()){
            while(allNetworks.next()){
                int NNScore = allNetworks.getInt("score");
                scoresOfSavedNetworksInts.add(NNScore);
            }
        }
        Collections.sort(scoresOfSavedNetworksInts, Collections.reverseOrder());
        List<String> scoresOfSavedNetworks = new ArrayList<>();
        for(Integer score:scoresOfSavedNetworksInts) scoresOfSavedNetworks.add(score + "");

        return scoresOfSavedNetworks;
    }

    private Connection createConnectionAndEnsureDatabase() throws SQLException {
        Connection conn = DriverManager.getConnection(this.NNdatabasePath, "sa", "");
        try {
            // I will save the neural network to the DB in the following way
            // score, firstLayerWeights, firstLayerBiases, secondLayerWeights, secondLayerBiases
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS TrainedNNs (score int primary key, " +
                    "firstLayerWeights varchar(10000), firstLayerBiases varchar(1000), " +
                    "secondLayerWeights varchar(10000), secondLayerBiases varchar(1000))").execute();
        } catch (SQLException t) {
            System.out.println(t.getMessage());
        }

        return conn;
    }
}
