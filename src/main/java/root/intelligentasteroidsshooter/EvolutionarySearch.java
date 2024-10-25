package root.intelligentasteroidsshooter;

import javafx.animation.AnimationTimer;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.w3c.dom.ls.LSOutput;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EvolutionarySearch {
    // to work with AnimationTimer();
    private int playingNetworkID;
    private NeuralNetwork playingNetwork;
    private Ship ship;
    private boolean firstEventTriggered;
    private boolean secondEventTriggered;
    private int pointsToDisplay;
    private int action;

    public static int GameWindowWIDTH = 500;
    public static int GameWindowHEIGHT = 400;

    public void start(Stage stage, AnchorPane anchorPane, Pane gamingPane, Pane graphPane){
        // At first, I was hoping to show how neural network plays and train at the same time, but these are two separate
        // processes and I can't do them simultaneously. Therefore, I will be sequentially showing NNs playing and training
        // I will check out multithreading later, starting from here: https://stackoverflow.com/questions/73326895/javafx-animationtimer-and-events
        // maybe here too: https://stackoverflow.com/questions/26478245/starting-multiple-animations-at-the-same-time

        // ==============================================================
        // 1. Setup
        // ==============================================================
        // score and episode
        Text showPoints = new Text(10, 30, "Points: 0");
        showPoints.setViewOrder(-100.0); // make sure it is always on top layer
        showPoints.setFill(Color.CRIMSON);
        showPoints.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Text showEpisodeNumber = new Text(350, 30, "Episode: 0");
        showEpisodeNumber.setViewOrder(-100.0); // make sure it is always on top layer
        showEpisodeNumber.setFill(Color.CRIMSON);
        showEpisodeNumber.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gamingPane.getChildren().add(showPoints);
        gamingPane.getChildren().add(showEpisodeNumber);
        // images for ship and asteroids
        Image imageForAsteroid = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter" +
                "\\src\\main\\resources\\root\\intelligentasteroidsshooter\\asteroid_nobackgr.png");
        Image imageForShip = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter\\src\\main" +
                "\\resources\\root\\intelligentasteroidsshooter\\falcon_no_bgr.png");
        ImageView shipImage = new ImageView(imageForShip);
        double scale = 0.12;
        shipImage.setScaleX(scale);
        shipImage.setScaleY(scale);
        //System.out.println("styles set");

        // simulation parameters
        int networkPopulationSize = 200;
        int totalEpisodes = 20;
        int episodeDurationPerNetwork = 500; // the time every network has per episode, i.e., each NN can do 10000 inputs
        int inputSize = 2*16 + 4; // two channels with 5x5 grid -- one for asteroids and one for ship, plus 8 inputs for velocity vector and ship orientation
        int hiddenSize = 32;
        int outputSize = 4; // accelerate, decelerate, turn left, turn right //, shoot -- try with always shooting
        double mutationRate = 0.005; // aka learning rate
        int gamingTimeMilSec = 10000;

        // create population of neural networks that will be playing the game
        List<NeuralNetwork> ourNNPopulation = new ArrayList<>();
        for(int network = 0; network < networkPopulationSize; network++){
            NeuralNetwork braveNetwork = new NeuralNetwork(mutationRate, inputSize, hiddenSize, outputSize);
            ourNNPopulation.add(braveNetwork);
        }
        // we will track the training progress with this list
        ArrayList<Object> scoresVsEpisodes = new ArrayList<>();

        // graph pane
        NumberAxis xAxis = new NumberAxis(0,totalEpisodes,2);
        NumberAxis yAxis = new NumberAxis(0, 100000,20000);
        xAxis.tickLabelFontProperty().set(Font.font(15));
        yAxis.tickLabelFontProperty().set(Font.font(15));
        xAxis.setLabel("Episode");
        yAxis.setLabel("Points");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setStyle("-fx-font-size: " + 20 + "px;");
        Text graphLabel = new Text(70, -12, "Neural Networks Performance");
        graphLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        //XYChart.Series averScorePerEpisodeGraph = new XYChart.Series();
        XYChart.Series bestScorePerEpisodeGraph = new XYChart.Series();
        //lineChart.getData().add(averScorePerEpisodeGraph);
        lineChart.getData().add(bestScorePerEpisodeGraph);
        lineChart.setLegendVisible(false);
        lineChart.setLayoutX(-15);
        lineChart.setLayoutY(12);
        //averScorePerEpisodeGraph.setName("average score"); // don't know how to manipulate legend and it looks super ugly
        //bestScorePerEdisodeGraph.setName("best performer");

        graphPane.getChildren().add(lineChart);
        graphPane.getChildren().add(graphLabel);
        //System.out.println("Step 1 clear");

        // ===============================================================
        // 2. Begin playing
        // ===============================================================
        playingNetwork = ourNNPopulation.getFirst();
        playingNetworkID = 0;

        ship = new Ship(shipImage, scale,0, 0);
        gamingPane.getChildren().add(ship.getImage());
        //gamingPane.getChildren().add(ship.getHitbox().getPolygon());
        List<Asteroid> asteroids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ImageView asteroidImage = new ImageView(imageForAsteroid);
            Random rnd = new Random();
            double rangeMin = 0.1;
            double rangeMax = 0.2;
            double size = rangeMin + (rangeMax - rangeMin) * rnd.nextDouble();
            asteroidImage.setScaleX(size);
            asteroidImage.setScaleY(size);
            Asteroid asteroid = new Asteroid(asteroidImage,
                    size, rnd.nextInt(-3*GameWindowWIDTH/4, -GameWindowWIDTH/4),
                    rnd.nextInt(-3*GameWindowHEIGHT/4, -GameWindowHEIGHT/4));
            if(!asteroid.collide(ship.getHitbox())) {asteroids.add(asteroid);}else{i--;}
        }
        asteroids.forEach(asteroid -> {
            gamingPane.getChildren().add(asteroid.getImage());
            //gamingPane.getChildren().add(asteroid.getHitbox().getPolygon());
        });

        List<Projectile> projectiles = new ArrayList<>();

        AtomicInteger points = new AtomicInteger(); // to count points
        Timer forActions = new Timer();
        forActions.setStart(Instant.now());

        Timer forFirstGamingRound = new Timer();
        Timer forSecondGamingRound = new Timer();
        forFirstGamingRound.setStart(Instant.now());
        forSecondGamingRound.setStart(Instant.now());
        firstEventTriggered = false;
        secondEventTriggered = false;

        new AnimationTimer() {

            @Override
            public void handle(long now) {
                if(Duration.between(forActions.getStart(), Instant.now()).toMillis() > 50){ // allow to act every 0.1sec
                    forActions.setStart(Instant.now());

                    List<Hitbox> asteroidHitboxes = asteroids.stream()
                            .map(s -> s.getHitbox()).toList();
                    // network decides upon action
                    double[] input = getObservation(inputSize, asteroidHitboxes, ship.getHitbox());
                    action = playingNetwork.forward(input); // pass observation to network and get action
                    if (action == 0) ship.accelerate();
                    else if (action == 1) ship.decelerate();
                    else if (action == 2) ship.turnLeft();
                    else if (action == 3) ship.turnRight();
                    // network always shoots if it can
                    if (projectiles.size() < 10) {// limit number of projectiles present at the time to 10 (aka ammo capacity)
                        // NN shoots
                        double changeX = 1 * Math.cos(Math.toRadians(ship.getImage().getRotate()));
                        double changeY = 1 * Math.sin(Math.toRadians(ship.getImage().getRotate()));
                        int x = (int) (ship.getImage().getLayoutX() + 0.8*GameWindowWIDTH/2 + 0.4*changeX*ship.getImageWidth());
                        int y = (int) (ship.getImage().getLayoutY() + GameWindowHEIGHT/2 + 0.4*changeY*ship.getImageHeight());

                        Projectile projectile = new Projectile(x, y);
                        projectile.getPolygon().setRotate(ship.getImage().getRotate());
                        projectiles.add(projectile);

                        projectile.accelerate();
                        projectile.setMovement(projectile.getMovement().normalize().multiply(3));
                        gamingPane.getChildren().add(projectile.getPolygon());
                    }
                }else{ // repeat actions until neural network doesn't choose a new one
                    if (action == 0) ship.accelerate();
                    else if (action == 1) ship.decelerate();
                    else if (action == 2) ship.turnLeft();
                    else if (action == 3) ship.turnRight();
                }

                // move all objects
                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                // removing colliding projectiles and asteroids
                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if(projectile.collide(asteroid.getHitbox())) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                            setPointsToDisplay(points.addAndGet(1000));
                            showPoints.setText("Points: " + pointsToDisplay);
                        }
                    });
                });
                projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .forEach(projectile -> gamingPane.getChildren().remove(projectile.getPolygon())); // remove from the pane
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .collect(Collectors.toList())); // remove from the projectiles list
                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid ->{
                            gamingPane.getChildren().remove(asteroid.getImage());
                            //gamingPane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
                        } );
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList())); // remove from the asteroids list

                // add new asteroids randomly at the edges of the screen, if they don't collide with the ship
                if(Math.random() < 0.01) {
                    ImageView asteroidImage = new ImageView(imageForAsteroid);
                    Random rnd = new Random();
                    double rangeMin = 0.1;
                    double rangeMax = 0.2;
                    double size = rangeMin + (rangeMax - rangeMin) * rnd.nextDouble(); // restrict asteroids size in this range
                    asteroidImage.setScaleX(size);
                    asteroidImage.setScaleY(size);
                    Asteroid asteroid = new Asteroid(asteroidImage,
                            size, rnd.nextInt(-3*GameWindowWIDTH/4, -GameWindowWIDTH/4),
                            rnd.nextInt(-3*GameWindowHEIGHT/4, -GameWindowHEIGHT/4));
                    if(!asteroid.collide(ship.getHitbox())) {
                        asteroids.add(asteroid);
                        gamingPane.getChildren().add(asteroid.getImage());
                        //gamingPane.getChildren().add(asteroid.getHitbox().getPolygon());
                    }
                }

                // restart the game when ship and asteroids collide, replaced forEach with explicit for loop because i delete asteroids
                for(int i=0; i < asteroids.size();i++){
                    if(ship.collide(asteroids.get(i).getHitbox())){
                        showPoints.setText("Points: 0");
                        System.out.println("How well did it perform: " + points);
                        points.set(0);
                        resetEnvironment(gamingPane, shipImage, imageForAsteroid, asteroids, projectiles, scale);
                        //System.out.println("resetEnvironment successful");
                        if(playingNetworkID < networkPopulationSize - 1){
                            changePlayingNetwork(ourNNPopulation); // can't change playing network here explicitly
                            //System.out.println("changePlayingNetwork successful");
                        }
                        break;
                    }
                }


                // pause game after 30 sec and train
                if(Duration.between(forFirstGamingRound.getStart(), Instant.now()).toMillis() > gamingTimeMilSec
                        && !firstEventTriggered){
                    setFirstEventTriggered(); // to avoid entering this if-statement more than once

                    // start training simulation
                    for(int trainingEpisode = 0; trainingEpisode < totalEpisodes/2; trainingEpisode++){
                        int score = startTrainingEpisode(ourNNPopulation, episodeDurationPerNetwork);
                        int bestPerformerScore = ourNNPopulation.getFirst().getScoreForPrinting();
                        bestScorePerEpisodeGraph.getData().add(new XYChart.Data(trainingEpisode + 1, bestPerformerScore));
                        //averScorePerEpisodeGraph.getData().add(new XYChart.Data(trainingEpisode + 1, score));
                        //System.out.println("averScorePerEpisodeGraph: " + averScorePerEpisodeGraph);
                        //System.out.println("bestScorePerEpisodeGraph: " + bestScorePerEpisodeGraph);

                        System.out.println("Episode: " + (trainingEpisode+1) +
                                "; Average score per network: " + 1.0*score / networkPopulationSize +
                                "; Best score per generation: " + bestPerformerScore);
                    }
                    System.out.println("first training successful");

                    // reset environment after training
                    points.set(0);
                    resetPlayingNetwork(ourNNPopulation);
                    resetEnvironment(gamingPane, shipImage, imageForAsteroid, asteroids, projectiles, scale);

                    forSecondGamingRound.setStart(Instant.now()); // play another 30 sec
                    //System.out.println("ready to play again");
                }

                // play again and pause the game after another 30 sec
                if(Duration.between(forSecondGamingRound.getStart(), Instant.now()).toMillis() > gamingTimeMilSec
                    && !secondEventTriggered){
                    setSecondEventTriggered(); // to avoid entering this if-statement more than once

                    // continue training simulation
                    for(int trainingEpisode = totalEpisodes/2; trainingEpisode < totalEpisodes; trainingEpisode++){
                        int score = startTrainingEpisode(ourNNPopulation, episodeDurationPerNetwork);
                        int bestPerformerScore = ourNNPopulation.getFirst().getScoreForPrinting();
                        bestScorePerEpisodeGraph.getData().add(new XYChart.Data(trainingEpisode + 1, bestPerformerScore));
                        //averScorePerEpisodeGraph.getData().add(new XYChart.Data(trainingEpisode + 1, score));
                        //System.out.println("averScorePerEpisodeGraph: " + averScorePerEpisodeGraph);
                        //System.out.println("bestScorePerEpisodeGraph: " + bestScorePerEpisodeGraph);
                        System.out.println("Episode: " + (trainingEpisode+1) +
                                "; Average score per network: " + 1.0*score / networkPopulationSize +
                                "; Best score per generation: " + bestPerformerScore);
                    }
                    //System.out.println("Second training successful");

                    // reset environment after training
                    points.set(0);
                    resetPlayingNetwork(ourNNPopulation);
                    resetEnvironment(gamingPane, shipImage, imageForAsteroid, asteroids, projectiles, scale);
                    //System.out.println("ready to play again");

                    // training results
                    System.out.println("Done with training!");
                    System.out.println("Individual scores");
                    ourNNPopulation.stream().
                            map(s->s.getScoreForPrinting()).limit(10).forEach(score -> System.out.println(score));
                }
            }
        }.start();
    }

    public int startTrainingEpisode(List<NeuralNetwork> ourNNPopulation,int episodeDurationPerNetwork){
        // don't know if i should just forward this all in method call...
        int populationSize = ourNNPopulation.size();
        int inputSize = ourNNPopulation.getFirst().getInputSize();
        int hiddenSize = ourNNPopulation.getFirst().getHiddenSize();
        int outputSize = ourNNPopulation.getFirst().getOutputSize();
        double mutationRate = ourNNPopulation.getFirst().getMutationRate();
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
            //System.out.println("All ready to begin!");

            // Here we will have a speed-up version of a single player game, where every network will play a game.
            // We won't be displaying training process of every single network in real time, otherwise it would take forever
            for(int timestep = 0; timestep < episodeDurationPerNetwork; timestep++){
                //System.out.println("timestep: " + timestep);
                double[] input = getObservation(inputSize, asteroids, playingNetwork.getShip());
                int action = playingNetwork.forward(input); // pass observation to network and get action
                if (action == 0) {
                    for(int k = 0; k < 10; k++){
                        ship.accelerate();
                    }
                }
                else if (action == 1){
                    for(int k = 0; k < 10; k++){
                        ship.decelerate();
                    }
                }
                else if (action == 2) {
                    for(int k = 0; k < 10; k++){
                        ship.turnLeft();
                    }
                }
                else if (action == 3) {
                    for(int k = 0; k < 10; k++){
                        ship.turnRight();
                    }
                }
                // network always shoots
                if(projectiles.size() < 10 ){// limit number of projectiles present at the time to 10 (aka ammo capacity)
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

                // move objects and do it faster
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
                if(Math.random() < 0.1) {
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
            }
            //System.out.println("NN finished playing the game");
        }

        // compute total score for the episode
        int totalScore = 0;
        for(NeuralNetwork network:ourNNPopulation){
            int networkScore = network.getScore();
            network.setScoreForPrinting(networkScore);
            totalScore += networkScore;
        }

        Collections.sort(ourNNPopulation); // sort network population by their performance
        // discard everything and leave only top 10%
        for(int i = 0; i < (int)(0.9*populationSize); i++){ //
            NeuralNetwork toBeDisposed = ourNNPopulation.get(populationSize - 1 - i);
            ourNNPopulation.remove( populationSize - 1 - i);
            toBeDisposed = null; // remove loser network from memory
        }
        // refill population by duplicating networks that performed well
        for(int i = 0; i < (int)(0.1*populationSize); i++){
            for(int j = 0; j < 9; j++){
                NeuralNetwork newNetwork = new NeuralNetwork(mutationRate, inputSize,hiddenSize,outputSize);
                newNetwork.copyNetworkParameters(ourNNPopulation.get(i));
                newNetwork.mutate();
                ourNNPopulation.add(newNetwork);
            }
            ourNNPopulation.get(i).setScore(0); // reset scores
            ourNNPopulation.get(i).mutate(); // mutate parameters of all agents using Gaussian distribution
        }
        //System.out.println("population updated, new generation is ready; updated ourNNPopulation.size() " + ourNNPopulation.size());
        if(ourNNPopulation.size() != populationSize){
            System.out.println("ourNNPopulation.size() != population size! ourNNPopulation.size() = " + ourNNPopulation.size());
        }

        return totalScore;
    }

    public static double[] getObservation(int inputSize, List<Hitbox> asteroids, Hitbox ship){
        double[] shipObservation = new double[inputSize];
        // split whole screen into 10x10 grid and fill each cell with 1 if it has asteroid and with 0 if it doesn't
        for(Hitbox asteroid:asteroids){
            int x = (int) Math.abs(asteroid.getPolygon().getTranslateX());
            int y = (int) Math.abs(asteroid.getPolygon().getTranslateY());
            int X = (x % GameWindowWIDTH) / (GameWindowWIDTH/4); // to avoid weird behavior of Pane env
            int Y = (y % GameWindowHEIGHT) / (GameWindowHEIGHT/4);
            //System.out.println("x = " + x + ", y = " + y + "; X = " + X + ", Y = " + Y);
            int gridIndex = 4*Y + X;
            //System.out.println("gridIndex " + gridIndex);
            shipObservation[gridIndex] = 1;
        }

        // for the second input channel provide a vector with all zeroes and a single 1 for ship location
        int x = (int) Math.abs(ship.getPolygon().getTranslateX());
        int y = (int) Math.abs(ship.getPolygon().getTranslateY());
        int X = (x % GameWindowWIDTH) / (GameWindowWIDTH/4);
        int Y = (y % GameWindowHEIGHT) / (GameWindowHEIGHT/4);
        //System.out.println("Ship coords: x = " + x + ", y = " + y + "; X = " + X + ", Y = " + Y);
        int gridIndex = 4*Y + X + 16; // we will use the second channel for ship's position
        //System.out.println("gridIndex " + gridIndex);
        shipObservation[gridIndex] = 1;

        // provide orientation of ship
        double changeX = 1*Math.cos(Math.toRadians(ship.getPolygon().getRotate()));
        double changeY = 1*Math.sin(Math.toRadians(ship.getPolygon().getRotate()));
        if(changeX > 0){
            shipObservation[inputSize-4] = changeX;
        }else{
            shipObservation[inputSize-3] = -1*changeX;
        }
        if(changeY > 0){
            shipObservation[inputSize-2] = changeY;
        }else{
            shipObservation[inputSize-1] = -1*changeY;
        }

        // finally, provide ship's velocity; to avoid providing negative values to network we will use 4 inputs
        /*double Vx = ship.getMovement().getX();
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
        }*/

        return shipObservation;
    }

    private void resetEnvironment(Pane gamingPane, ImageView shipImage, Image imageForAsteroid,
                                  List<Asteroid> asteroids, List<Projectile> projectiles, double scale){
        pointsToDisplay = 0;
        // remove old ship
        gamingPane.getChildren().remove(ship.getImage());
        gamingPane.getChildren().remove(ship.getHitbox().getPolygon());
        // and add new
        ship = new Ship(shipImage, scale, 0,0);
        shipImage.setRotate(0);
        gamingPane.getChildren().add(ship.getImage());
        //gamingPane.getChildren().add(ship.getHitbox().getPolygon());
        //System.out.println("ship added");
        // remove old asteroids
        projectiles.forEach(projectile -> gamingPane.getChildren().remove(projectile.getPolygon())); // remove from the pane
        projectiles.clear(); // remove from the projectiles list
        asteroids.forEach(asteroid ->{
            gamingPane.getChildren().remove(asteroid.getImage());
            //gamingPane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
        });
        asteroids.clear();
        //System.out.println("projectiles and asteroids removed");
        // add new ones
        for (int i = 0; i < 5; i++) {
            ImageView asteroidImage = new ImageView(imageForAsteroid);
            Random rnd = new Random();
            double rangeMin = 0.1;
            double rangeMax = 0.2;
            double size = rangeMin + (rangeMax - rangeMin) * rnd.nextDouble();
            asteroidImage.setScaleX(size);
            asteroidImage.setScaleY(size);
            Asteroid asteroid = new Asteroid(asteroidImage,
                    size, rnd.nextInt(-3*GameWindowWIDTH/4, -GameWindowWIDTH/4),
                    rnd.nextInt(-3*GameWindowHEIGHT/4, -GameWindowHEIGHT/4));
            if(!asteroid.collide(ship.getHitbox())) {asteroids.add(asteroid);}else{i--;}
        }
        if(asteroids.isEmpty()) System.out.println("No asteroids added!");
        asteroids.forEach(asteroid -> {
            gamingPane.getChildren().add(asteroid.getImage());
            //gamingPane.getChildren().add(asteroid.getHitbox().getPolygon());
        });
        //System.out.println("Asteroids added");
    }

    private void changePlayingNetwork(List<NeuralNetwork> ourNNPopulation){
        playingNetworkID++;
        playingNetwork = ourNNPopulation.get(playingNetworkID);
        System.out.println("Score of the selected network: " + playingNetwork.getScoreForPrinting());
    }

    private void resetPlayingNetwork(List<NeuralNetwork> ourNNPopulation){
        playingNetworkID = 0;
        pointsToDisplay = 0;
        playingNetwork = ourNNPopulation.getFirst();
        System.out.println("Score of the selected network: " + playingNetwork.getScoreForPrinting());
    }

    public void setFirstEventTriggered(){
        firstEventTriggered = true;
    }

    public void setSecondEventTriggered(){
        secondEventTriggered = true;
    }

    public void setPointsToDisplay(int value) { pointsToDisplay = value;}

}
