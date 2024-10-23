package root.intelligentasteroidsshooter;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EvolutionarySearch {
    private int populationSize;
    private List<NeuralNetwork> ourNNPopulation; // represents population of neural networks
    private List<Integer> totalScorePerEpisode; // will be used to track the training progress

    public EvolutionarySearch(int populationSize){
        this.populationSize = populationSize;
        this.ourNNPopulation = new ArrayList<>();
        this.totalScorePerEpisode = new ArrayList<>();
    }

    public void startTraining(int totalEpisodes, double mutationRate){
        int episodeDurationPerNetwork = 10000; // the time every network has per episode, i.e., each NN can do 10000 inputs
        // starting NN population
        int inputSize = 2*100 + 4; // two channels with 10x10 grid -- one for asteroids and one for ship, plus 4 inputs for velocity vector
        int hiddenSize = 256;
        int outputSize = 5; // accelerate, decelerate, turn left, turn right, shoot
        for(int i = 0; i < populationSize; i++){
            NeuralNetwork neuralNetwork = new NeuralNetwork(mutationRate, inputSize, hiddenSize, outputSize);
            ourNNPopulation.add(neuralNetwork);
        }
        //System.out.println("population of networks initialized");

        for(int trainingEpisode = 0; trainingEpisode < totalEpisodes; trainingEpisode++){
            //System.out.println("Episode: " + trainingEpisode);
            for(int networkID = 0; networkID < populationSize; networkID++){
                //System.out.println("Network: " + networkID);
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
                //System.out.println("All ready to begin!");

                // Here we will have a speed-up version of a single player game, where every network will play a game.
                // We won't be displaying training process of every single network in real time, otherwise it would take forever
                for(int timestep = 0; timestep < episodeDurationPerNetwork; timestep++){
                    //System.out.println("timestep: " + timestep);
                    double[] input = getObservation(inputSize, asteroids, playingNetwork.getShip());
                    int action = playingNetwork.forward(input); // pass observation to network and get action
                    if(action == 0) playingNetwork.getShip().accelerate();
                    else if(action == 1) playingNetwork.getShip().decelerate();
                    else if(action == 2) playingNetwork.getShip().turnLeft();
                    else if(action == 3) playingNetwork.getShip().turnRight();
                    else if(action == 4 // shoot
                            && projectiles.size() < 10 ){// limit number of projectiles present at the time to 10 (aka ammo capacity)
                        // NN shoots
                        double changeX = 1*Math.cos(Math.toRadians(playingNetwork.getShip().getPolygon().getRotate()));
                        double changeY = 1*Math.sin(Math.toRadians(playingNetwork.getShip().getPolygon().getRotate()));
                        // for projectiles to nicely come out of the ship, their coordinates have to scale with ship and playground sizes
                        int x = (int)(playingNetwork.getShip().getPolygon().getLayoutX()
                                + 0.8*SinglePlayerView.WIDTH/2 + 0.4*changeX*playingNetwork.getShipSize());
                        int y = (int)(playingNetwork.getShip().getPolygon().getLayoutY()
                                + SinglePlayerView.HEIGHT/2 + 0.4*changeY*playingNetwork.getShipSize());
                        Projectile projectile = new Projectile(x, y);
                        projectile.getPolygon().setRotate(playingNetwork.getShip().getPolygon().getRotate());
                        projectiles.add(projectile);

                        projectile.accelerate();
                        projectile.setMovement(projectile.getMovement().normalize().multiply(3));
                    }
                    //System.out.println("input processed and decision is made");

                    // move objects
                    playingNetwork.getShip().move();
                    asteroids.forEach(asteroid -> asteroid.move());
                    projectiles.forEach(projectile -> projectile.move());

                    // removing colliding projectiles and asteroids
                    projectiles.forEach(projectile -> {
                        asteroids.forEach(asteroid -> {
                            if(projectile.collide(asteroid)) {
                                projectile.setAlive(false);
                                asteroid.setAlive(false);
                                playingNetwork.addPoints(1000); //
                            }
                        });
                    });
                    projectiles.removeAll(projectiles.stream()
                            .filter(projectile -> !projectile.isAlive())
                            .collect(Collectors.toList())); // remove from the projectiles list
                    asteroids.removeAll(asteroids.stream()
                            .filter(asteroid -> !asteroid.isAlive())
                            .collect(Collectors.toList())); // remove from the asteroids list

                    // add new asteroids randomly at the edges of the screen, if they don't collide with the ship
                    if(Math.random() < 0.01) {
                        Random rng = new Random();
                        double size = 0.5 + rng.nextDouble(); // let size of asteroids vary a bit
                        Polygon squarePolygon = new Polygon(-20*size, -20*size, 20*size, -20*size,
                                20*size, 20*size, -20*size, 20*size); // asteroid's size
                        Hitbox asteroid = new Hitbox(squarePolygon, Color.BLACK,
                                rng.nextInt(-4*SinglePlayerView.WIDTH/5, -SinglePlayerView.WIDTH/5),
                                rng.nextInt(-4*SinglePlayerView.HEIGHT/5, -SinglePlayerView.HEIGHT/5));
                        if(!asteroid.collide(playingNetwork.getShip())) {
                            asteroids.add(asteroid);
                        }
                    }

                    // stop the game when ship and asteroids collide
                    asteroids.forEach(asteroid -> {
                        if (playingNetwork.getShip().collide(asteroid)) {
                            playingNetwork.getShip().setAlive(false);
                            //System.out.println("Our NN died trying :(");
                        }
                    });
                    if(!playingNetwork.getShip().isAlive()) break;

                    //System.out.println("timestep is over");
                }
                //System.out.println("NN finished playing the game");
            }
            //System.out.println("All NNs played the game");


            // compute total score for the episode
            int totalScore = 0;
            for(NeuralNetwork network:ourNNPopulation){
                totalScore += network.getScore();
            }
            totalScorePerEpisode.add(totalScore);
            //System.out.println("Total score: " + totalScore);

            // Evolutionary Algorithm (the variation of it that i will use here)
            //System.out.println("NN scores before sorting");
            //ourNNPopulation.stream().map(s->s.getScore()).forEach(score -> System.out.println(score));

            Collections.sort(ourNNPopulation); // sort network population by their performance
            //System.out.println("NN scores after sorting");
            //ourNNPopulation.stream().map(s->s.getScore()).forEach(score -> System.out.println(score));
            // discard half of population that performed badly
            if(trainingEpisode != totalEpisodes-1){
                for(int i = 0; i < (int)(4.0*populationSize/5); i++){
                    NeuralNetwork toBeDisposed = ourNNPopulation.get(populationSize - 1 - i);
                    ourNNPopulation.remove( populationSize - 1 - i);
                    toBeDisposed = null;
                }
                //System.out.println("bad networks removed, current population size: " + ourNNPopulation.size());
                // refill population by duplicating networks that performed well
                for(int i = 0; i < (int)(populationSize/5.0); i++){
                    for(int j = 0; j < 4; j++){
                        NeuralNetwork newNetwork = new NeuralNetwork(mutationRate, inputSize,hiddenSize,outputSize);
                        newNetwork.copyNetworkParameters(ourNNPopulation.get(i));
                        //System.out.println("check parameters");
                        //newNetwork.printNetworkParameteres();
                        newNetwork.mutate();
                        ourNNPopulation.add(newNetwork);
                    }
                    ourNNPopulation.get(i).mutate(); // mutate parameters of all agents using Gaussian distribution
                }
            }

            //System.out.println("population updated, new generation is ready; updated ourNNPopulation.size() " + ourNNPopulation.size());
            if(ourNNPopulation.size() != populationSize){
                System.out.println("ourNNPopulation.size() != population size! ourNNPopulation.size() = " + ourNNPopulation.size());
            }

            System.out.println("Episode: " + trainingEpisode + "; Average score per network: " + 1.0*totalScore / populationSize);
        }
        System.out.println("Done!");
        System.out.println("Individual scores");
        ourNNPopulation.stream().map(s->s.getScore()).forEach(score -> System.out.println(score));
    }

    public double[] getObservation(int inputSize, List<Hitbox> asteroids, Hitbox ship){
        double[] shipObservation = new double[inputSize];
        // split whole screen into 10x10 grid and fill each cell with 1 if it has asteroid and with 0 if it doesn't
        for(Hitbox asteroid:asteroids){
            int x = (int) Math.abs(asteroid.getPolygon().getTranslateX());
            int y = (int) Math.abs(asteroid.getPolygon().getTranslateY());
            int X = x / (SinglePlayerView.WIDTH/10);
            int Y = y / (SinglePlayerView.HEIGHT/10);
            //System.out.println("x = " + x + ", y = " + y + "; X = " + X + ", Y = " + Y);
            int gridIndex = 10*Y + X;
            //System.out.println("gridIndex " + gridIndex);
            shipObservation[gridIndex] = 1;
        }

        // for the second input channel provide a vector with all zeroes and a single 1 for ship location
        int x = (int) Math.abs(ship.getPolygon().getTranslateX());
        int y = (int) Math.abs(ship.getPolygon().getTranslateY());
        int X = x / (SinglePlayerView.WIDTH/10);
        int Y = y / (SinglePlayerView.HEIGHT/10);
        //System.out.println("x = " + x + ", y = " + y + "; X = " + X + ", Y = " + Y);
        int gridIndex = 10*Y + X + 100; // we will use the second channel for ship's position
        //System.out.println("gridIndex " + gridIndex);
        shipObservation[gridIndex] = 1;

        // finally, provide ship's velocity; to avoid providing negative values to network we will use 4 inputs
        double Vx = ship.getMovement().getX();
        double Vy = ship.getMovement().getY();
        if(Vx > 0){
            shipObservation[inputSize-4] = Vx;
        }else{
            shipObservation[inputSize-3] = -1*Vx;
        }
        if(Vy > 0){
            shipObservation[inputSize-2] = Vy;
        }else{
            shipObservation[inputSize-1] = -1*Vy;
        }

        return shipObservation;
    }

    public List<NeuralNetwork> getOurNNPopulation(){ return ourNNPopulation; }
}
