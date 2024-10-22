package root.intelligentasteroidsshooter;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EvolutionarySearch {
    private int populationSize;
    private List<NeuralNetwork> ourNNPopulation; // represents population of neural networks
    private List<Integer> scores; // will be used to track the training progress

    public EvolutionarySearch(int populationSize){
        this.populationSize = populationSize;
        this.ourNNPopulation = new ArrayList<>();
        this.scores = new ArrayList<>();
    }

    public void startTraining(int totalEpisodes, double mutationRate){
        // starting NN population
        int inputSize = 2*100 +  4; // two channels with 10x10 grid -- one for asteroids and one for ship, plus 4 inputs for velocity vector
        int hiddenSize = 256;
        int outputSize = 4; // accelerate, decelerate, turn left, turn right
        for(int i = 0; i < populationSize; i++){
            NeuralNetwork neuralNetwork = new NeuralNetwork(mutationRate, inputSize, hiddenSize, outputSize);
            ourNNPopulation.add(neuralNetwork);
        }

        for(int trainingEpisode = 0; trainingEpisode < totalEpisodes; trainingEpisode++){
            int totalScorePerEpisode = 0;
            for(int networkID = 0; networkID < populationSize; networkID++){
                NeuralNetwork playingNetwork = ourNNPopulation.get(networkID);
                // generate some asteroids
                List<Hitbox> asteroids = new ArrayList<>();
                for(int i = 0; i < 10; i++){ // start with 10 asteroids
                    Random rng = new Random();
                    double size = 0.5 + rng.nextDouble(); // let size of asteroids vary a bit
                    Polygon squarePolygon = new Polygon(-20*size, -20*size, 20*size, -20*size,
                            20*size, 20*size, -20*size, 20*size); // asteroid's size
                    Hitbox asteroid = new Hitbox(squarePolygon, Color.BLACK,
                            rng.nextInt(-4*SinglePlayerView.WIDTH/5, -SinglePlayerView.WIDTH/5),
                            rng.nextInt(-4*SinglePlayerView.HEIGHT/5, -SinglePlayerView.HEIGHT/5));
                    asteroids.add(asteroid);
                }
                // projectiles will be stored here
                List<Projectile> projectiles = new ArrayList<>();

                // Here we will have a speed-up version of a single player game, where every network will play a game.
                // We won't be displaying training process of every single network in real time, otherwise it would take forever
                double dx = 0.1;
                double dy = 0.1;
                int episodeDurationPerNetwork = 10000; // the time every network has
                for(int timestep = 0; timestep < episodeDurationPerNetwork; timestep++){
                    double x = playingNetwork.getShip().getPolygon().getTranslateX();
                    double y = playingNetwork.getShip().getPolygon().getTranslateY();
                    double[] input = getObservation(inputSize, x, y, asteroids);



                }




            }

            scores.add(totalScorePerEpisode);
            createNextGeneration();
        }



    }

    public void createNextGeneration(){


    }

    public double[] getObservation(int inputSize, double x, double y, List<Hitbox> asteroids){
        double[] shipObservation = new double[inputSize];
        // split whole screen into 10x10 grid and fill each cell with 1 if it has asteroid and with 0 if it doesn't


        return shipObservation;
    }

}
