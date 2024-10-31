package root.intelligentasteroidsshooter;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.sql.SQLException;
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
    private int pointsToDisplay;
    private int trainingEpisode;
    private int inputSize;
    private int hiddenSize;
    private int outputSize;
    private int inGameAction;
    private AnimationTimer trainer;
    private AnimationTimer player;

    public static int GameWindowWIDTH = 500;
    public static int GameWindowHEIGHT = 400;

    public void start(Pane gamingPane, Pane graphPane, VBox messageWindow,
        int networkPopulationSize, int totalEpisodes, LineChart lineChart){
        // At first, I was hoping to show how neural network plays and train at the same time, but these are two separate
        // processes and I can't do them simultaneously. Therefore, I will be sequentially showing NNs playing and training
        // I will check out multithreading later, starting from here: https://stackoverflow.com/questions/73326895/javafx-animationtimer-and-events
        // maybe here too: https://stackoverflow.com/questions/26478245/starting-multiple-animations-at-the-same-time
        graphPane = graphPane;
        lineChart = lineChart;

        // internal simulation parameters that user don't see
        int episodeDurationPerNetwork = 1000; // the time every network has per episode, i.e., each NN can do 10000 inputs
        inputSize = 9; // 3x3 grid of asteroid positions around the ship
        hiddenSize = 16;
        outputSize = 8; // accelerate, decelerate, turn left, turn right //, shoot -- try with always shooting
        double mutationRate = 0.005; // aka learning rate
        int playTime = 15000; // in milliseconds, e.g.., 10sec
        // create population of neural networks that will be playing the game
        List<NeuralNetwork> ourNNPopulation = new ArrayList<>();
        for(int network = 0; network < networkPopulationSize; network++){
            NeuralNetwork braveNetwork = new NeuralNetwork(mutationRate, inputSize, hiddenSize, outputSize);
            ourNNPopulation.add(braveNetwork);
        }
        trainingEpisode = 0;
        //superNetworksPath = "";

        // gaming pane
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

        Image imageForAsteroid =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/asteroid_nobackgr.png").toString());
        Image imageForShip =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/falcon_no_bgr.png").toString());

        ImageView shipImage = new ImageView(imageForShip);
        double scale = 0.12;
        shipImage.setScaleX(scale);
        shipImage.setScaleY(scale);
        ship = new Ship(shipImage, scale,0, 0);

        // graph pane -- all of this could be avoided if i could modify X-Axis extension
        Text graphLabel = new Text(70, 40, "Neural Networks Performance");
        graphLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        graphPane.getChildren().remove(lineChart);
        NumberAxis xAxis = new NumberAxis(0,totalEpisodes,totalEpisodes/10);
        NumberAxis yAxis = new NumberAxis(-30000, 0,5000);
        xAxis.tickLabelFontProperty().set(Font.font(15));
        yAxis.tickLabelFontProperty().set(Font.font(15));
        xAxis.setLabel("Episode");
        yAxis.setLabel("Average Loss");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setStyle("-fx-font-size: " + 20 + "px;");
        lineChart.setLegendVisible(false);
        lineChart.setLayoutX(-15);
        lineChart.setLayoutY(50);
        XYChart.Series bestScorePerEpisodeGraph = new XYChart.Series();
        graphPane.getChildren().addAll(lineChart, graphLabel);
        //lineChart.getData().add(averScorePerEpisodeGraph);
        lineChart.getData().add(bestScorePerEpisodeGraph);
        //System.out.println("Step 1 clear");

        // show first message
        Timer forFirstMessage = new Timer();
        forFirstMessage.setStart(Instant.now());
        if(Duration.between(forFirstMessage.getStart(), Instant.now()).toMillis() < 6000){ // show message for 5 sec
            messageWindow.setVisible(true);
            messageWindow.getChildren().clear();
            Text welcomeDescription = new Text("We create " + networkPopulationSize + " neural networks\n" +
                    "with randomly chosen parameters\n" +
                    "and let them control ship's motion");
            Text shootMessage = new Text("Ship always shoots between\n        short time intervals");
            welcomeDescription.setLineSpacing(10);
            shootMessage.setLineSpacing(10);
            welcomeDescription.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            shootMessage.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            messageWindow.getChildren().addAll(welcomeDescription, shootMessage);
            messageWindow.setAlignment(Pos.CENTER);
            messageWindow.setSpacing(30);
        }

        // play game and train networks alternatively
        // i need to call train() method from within playGame(), otherwise program begins executing other stuff in this
        // method before playGame() is finished. The stack concept works funny when AnimationTimer() is active
        playGame(gamingPane, graphPane, lineChart, messageWindow, ourNNPopulation, showPoints, showEpisodeNumber,
                imageForAsteroid, shipImage, scale, bestScorePerEpisodeGraph, episodeDurationPerNetwork, totalEpisodes,
                playTime);
    }

    public void playGame(Pane gamingPane, Pane graphPane, LineChart lineChart, VBox messageWindow,
                         List<NeuralNetwork> ourNNPopulation, Text showPoints, Text showEpisodeNumber, Image imageForAsteroid,
                         ImageView shipImage, double scale, XYChart.Series bestScorePerEpisodeGraph,
                         int episodeDurationPerNetwork, int totalEpisodes, int playTime){

        int networkPopulationSize = ourNNPopulation.size();
        playingNetwork = ourNNPopulation.get(0);
        playingNetworkID = 0;
        List<Asteroid> asteroids = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();
        AtomicInteger points = new AtomicInteger(); // to count points
        // set environment
        resetEnvironment(gamingPane, shipImage, imageForAsteroid, asteroids, projectiles, scale);

        Timer forEntireGame = new Timer();
        forEntireGame.setStart(Instant.now());
        Timer forActions = new Timer();
        forActions.setStart(Instant.now());
        Timer messageWindowLingerTime = new Timer();
        messageWindowLingerTime.setStart(Instant.now());

        Random rng = new Random();
        player = new AnimationTimer() {
            @Override
            public void handle(long now) {

                // remove message after 5 seconds
                if(Duration.between(messageWindowLingerTime.getStart(), Instant.now()).toMillis() > 5000){
                    messageWindow.setVisible(false);
                }

                showEpisodeNumber.setText("Episode: " + trainingEpisode);
                if(Duration.between(forEntireGame.getStart(), Instant.now()).toMillis() > playTime){
                    stop();
                    System.out.println("finished playing");
                    int endEpisode = 0;
                    if(trainingEpisode <totalEpisodes/2){
                        endEpisode = totalEpisodes/2;
                        // messages for the first time training
                        messageWindow.setVisible(true);
                        messageWindow.getChildren().clear();
                        Text randomResult = new Text("    To no surprise, networks with\n" +
                                "random parameters perform poorly");
                        Text trainBegins = new Text("Lets train our networks");
                        randomResult.setLineSpacing(10);
                        trainBegins.setLineSpacing(10);
                        randomResult.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                        trainBegins.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                        messageWindow.getChildren().addAll(randomResult, trainBegins);
                        messageWindow.setAlignment(Pos.CENTER);

                        train(gamingPane, graphPane, lineChart, messageWindow, ourNNPopulation, showPoints,
                                showEpisodeNumber, imageForAsteroid, shipImage,
                                scale, bestScorePerEpisodeGraph, totalEpisodes, episodeDurationPerNetwork,
                                endEpisode, playTime);
                    }else if(trainingEpisode >= totalEpisodes/2 && trainingEpisode < totalEpisodes){
                        endEpisode = totalEpisodes;
                        // seecond message
                        messageWindow.setVisible(true);
                        messageWindow.getChildren().clear();
                        Text lessRandomResult = new Text("Ship should be doing better,\n" +
                                "      but not much better");
                        Text trainContinues = new Text("Lets continue with training");
                        lessRandomResult.setLineSpacing(10);
                        trainContinues.setLineSpacing(10);
                        lessRandomResult.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                        trainContinues.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                        messageWindow.getChildren().addAll(lessRandomResult, trainContinues);
                        messageWindow.setAlignment(Pos.CENTER);

                        train(gamingPane, graphPane, lineChart, messageWindow, ourNNPopulation, showPoints,
                                showEpisodeNumber, imageForAsteroid, shipImage,
                                scale, bestScorePerEpisodeGraph, totalEpisodes, episodeDurationPerNetwork,
                                endEpisode, playTime);
                    }
                }

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
                            gamingPane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
                        } );
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList())); // remove from the asteroids list

                // allow to act every 0.2sec +/- 0.1sec
                int variationInActionTime = rng.nextInt(-100,100);
                if(Duration.between(forActions.getStart(), Instant.now()).toMillis() > 200 + variationInActionTime){
                    forActions.setStart(Instant.now());

                    // network decides upon action, and executes it until next time to choose comes
                    List<Hitbox> asteroidHitboxes = asteroids.stream().map(s -> s.getHitbox()).toList();
                    double[] input = getObservation(asteroidHitboxes, ship.getHitbox());
                    inGameAction = playingNetwork.forward(input); // pass observation to network and get action
                    // get speed to zero
                    for(int k = 0; k < 5; k++){
                        ship.decelerate();
                    }
                }else{ // repeat actions until neural network doesn't choose a new one
                    // to make sure the code below works, i choose work only with positive angles
                    double angle = ship.getImage().getRotate() % 360;
                    if(angle < 0){
                        ship.getImage().setRotate(angle + 360);
                    }else{
                        ship.getImage().setRotate(angle);
                    }

                    if (inGameAction == 0) { // turn to 0 degrees (along +X axis)
                        if(angle < 180){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }

                        ship.accelerate();
                    }
                    else if (inGameAction == 1){ // turn to +45 degrees (+X,+Y)
                        if(angle > 45 && angle < 225){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }

                        ship.accelerate();
                    }
                    else if (inGameAction == 2) { // turn to +90 degrees (0,+Y)
                        if(angle > 90 && angle < 270){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }

                        ship.accelerate();
                    }
                    else if (inGameAction == 3) { // turn to +135 degrees (-X,+Y)
                        if(angle > 135 && angle < 315){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }

                        ship.accelerate();
                    }
                    else if (inGameAction == 4) { // turn to +180 degrees (-X,0)
                        if(angle > 180){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }

                        ship.accelerate();
                    }
                    else if (inGameAction == 5) { // turn to +225 degrees (-X,-Y)
                        if(angle > 225 || angle < 45){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }

                        ship.accelerate();
                    }
                    else if (inGameAction == 6) { // turn to +270 degrees (0,-Y)
                        if(angle > 270 || angle < 90){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }

                        ship.accelerate();
                    }
                    else if (inGameAction == 7) { // turn to +315 degrees (+X,-Y)
                        if(angle > 315 || angle < 135){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }

                        ship.accelerate();
                    }
                }

                // network always shoots if it can
                if (projectiles.size() < 5) {// limit number of projectiles present at the time to 10 (aka ammo capacity)
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

                // move all objects
                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

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
                        gamingPane.getChildren().add(asteroid.getHitbox().getPolygon()); // explicitly adding polygons eliminates delay in removal
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
            }
        };
        player.start();
    }

    public void train(Pane gamingPane, Pane graphPane, LineChart lineChart, VBox messageWindow,
                      List<NeuralNetwork> ourNNPopulation, Text showPoints,
                      Text showEpisodeNumber, Image imageForAsteroid, ImageView shipImage, double scale,
                      XYChart.Series averageLossPerEpisodeGraph, int totalEpisodes,
                      int episodeDurationPerNetwork, int endEpisode, int playTime){
        int networkPopulationSize = ourNNPopulation.size();
        //System.out.println("we are inside train() method");

        Timer messageWindowLingerTime = new Timer();
        messageWindowLingerTime.setStart(Instant.now());
        trainer = new AnimationTimer(){
            @Override
            public void handle(long now) {
                gamingPane.getChildren().clear();

                // hide message after 5 seconds
                if(Duration.between(messageWindowLingerTime.getStart(), Instant.now()).toMillis() > 5000){
                    messageWindow.setVisible(false);
                }

                int score = startTrainingEpisode(ourNNPopulation, episodeDurationPerNetwork); // trainingEpisode++ there
                System.out.println("Episode: " + (trainingEpisode) +
                        "; Average score per network: " + 1.0*score / networkPopulationSize +
                        "; Best score per generation: " + ourNNPopulation.get(0).getScoreForPrinting());
                averageLossPerEpisodeGraph.getData().add(new XYChart.Data(trainingEpisode, 1.0*score / networkPopulationSize));

                // worth saving
                if(1.0*score / networkPopulationSize > -17000) { // adjust this parameter if training rewards are changed
                    saveNetworkParameters(ourNNPopulation, ourNNPopulation.get(0).getScoreForPrinting());
                }

                if(trainingEpisode >= endEpisode && trainingEpisode < totalEpisodes) {
                    stop();
                    gamingPane.getChildren().add(showPoints);
                    gamingPane.getChildren().add(showEpisodeNumber);

                    playGame(gamingPane, graphPane, lineChart, messageWindow, ourNNPopulation, showPoints,
                            showEpisodeNumber, imageForAsteroid, shipImage,
                            scale, averageLossPerEpisodeGraph, episodeDurationPerNetwork, totalEpisodes, playTime);
                }

                if(trainingEpisode >= totalEpisodes) {
                    stop();
                    //gamingPane.getChildren().add(showPoints);
                    //gamingPane.getChildren().add(showEpisodeNumber);

                    graphPane.getChildren().remove(lineChart);
                    NNPLaysGameOnly ourBestNetworkPlays = new NNPLaysGameOnly();
                    ourBestNetworkPlays.start(gamingPane, graphPane, messageWindow, ourNNPopulation.get(0), lineChart);
                }
            }
        };
        //System.out.println("AnimationTimer trainer defined");

        trainer.start();
    }

    public int startTrainingEpisode(List<NeuralNetwork> ourNNPopulation,int episodeDurationPerNetwork){
        // don't know if i should just forward this all in method call...
        int populationSize = ourNNPopulation.size();
        int inputSize = ourNNPopulation.get(0).getInputSize(); // getFirst() doesn't work on some systems
        int hiddenSize = ourNNPopulation.get(0).getHiddenSize();
        int outputSize = ourNNPopulation.get(0).getOutputSize();
        double mutationRate = ourNNPopulation.get(0).getMutationRate();

        // networks play
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
                        rng.nextInt(-3*GameWindowWIDTH/4, -GameWindowWIDTH/4),
                        rng.nextInt(-3*GameWindowHEIGHT/4, -GameWindowHEIGHT/4));
                for(int k = 0; k < 3; k++) asteroid.accelerate();
                asteroids.add(asteroid);
            }
            // projectiles will be stored here
            //List<Projectile> projectiles = new ArrayList<>();
            //System.out.println("All ready to begin!");

            // Here we will have a speed-up version of a single player game, where every network will play a game.
            // We won't be displaying training process of every single network in real time, otherwise it would take forever
            int score = 0;
            for(int timestep = 0; timestep < episodeDurationPerNetwork; timestep++){
                //score++;
                //System.out.println("networkID " + networkID + "; timestep: " + timestep);
                double[] input = getObservation(asteroids, playingNetwork.getShip());
                int action = playingNetwork.forward(input); // pass observation to network and get action
                // indices: 0 - X,Y; 1 - nextX,Y; 2 - nextX,nextY; 3 - X,nextY; 4 - prevX,nextY; 5 - prevX,Y; 6 - prevX,prevY, 7 - X,prevY; 8 - nextX,prevY
                int angle = 45*action;
                playingNetwork.getShip().getPolygon().setRotate(angle);
                if(input[action+1] == 1) playingNetwork.addPoints(-100); // punish the network if it moves toward asteroid
                for(int k = 0; k < 5; k++){
                    playingNetwork.getShip().decelerate();
                }
                for(int k = 0; k < 5; k++){
                    playingNetwork.getShip().accelerate();
                }
                // punish ship if it comes too close to boundaries where asteroids spawn
                int shipX = (int) Math.abs(playingNetwork.getShip().getPolygon().getTranslateX());
                int shipY = (int) Math.abs(playingNetwork.getShip().getPolygon().getTranslateY());
                int X = shipX / 50;
                int Y = shipY / 50;
                if(X == 0 || Y == 0 || X == (int)(GameWindowWIDTH/50) - 1
                        || Y == (int)(GameWindowHEIGHT/50) - 1) playingNetwork.addPoints(-10);

                // network always shoots
                /*if(projectiles.size() < 10 ){// limit number of projectiles present at the time to 10 (aka ammo capacity)
                    // NN shoots
                    double changeX = 1*Math.cos(Math.toRadians(playingNetwork.getShip().getPolygon().getRotate()));
                    double changeY = 1*Math.sin(Math.toRadians(playingNetwork.getShip().getPolygon().getRotate()));
                    // for projectiles to nicely come out of the ship, their coordinates have to scale with ship and playground sizes
                    int x = (int)(playingNetwork.getShip().getPolygon().getLayoutX()
                            + 0.8*GameWindowWIDTH/2 + 0.4*changeX*playingNetwork.getShipSize());
                    int y = (int)(playingNetwork.getShip().getPolygon().getLayoutY()
                            + GameWindowHEIGHT/2.0 + 0.4*changeY*playingNetwork.getShipSize());
                    Projectile projectile = new Projectile(x, y);
                    projectile.getPolygon().setRotate(playingNetwork.getShip().getPolygon().getRotate());
                    projectiles.add(projectile);

                    projectile.accelerate();
                    projectile.setMovement(projectile.getMovement().normalize().multiply(3));
                }*/
                //System.out.println("input processed and decision is made");

                // move objects and do it faster
                playingNetwork.getShip().move();
                asteroids.forEach(asteroid -> asteroid.move());
                //projectiles.forEach(projectile -> projectile.move());

                // removing colliding projectiles and asteroids
                /*projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if(projectile.collide(asteroid)) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                            //playingNetwork.addPoints(1000); //
                        }
                    });
                });
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .collect(Collectors.toList())); // remove from the projectiles list
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList())); // remove from the asteroids list
*/
                // add new asteroids randomly at the edges of the screen, if they don't collide with the ship
                if(Math.random() < 0.05) {
                    Random rng = new Random();
                    double size = 0.5 + rng.nextDouble(); // let size of asteroids vary a bit
                    Polygon squarePolygon = new Polygon(-20*size, -20*size, 20*size, -20*size,
                            20*size, 20*size, -20*size, 20*size); // asteroid's size
                    Hitbox asteroid = new Hitbox(squarePolygon, Color.BLACK,
                            rng.nextInt(-3*GameWindowWIDTH/4, -GameWindowWIDTH/4),
                            rng.nextInt(-3*GameWindowHEIGHT/4, -GameWindowHEIGHT/4));
                    if(!asteroid.collide(playingNetwork.getShip())) {
                        asteroids.add(asteroid);
                    }
                }

                // stop the game when ship and asteroids collide
                asteroids.forEach(asteroid -> {
                    if (playingNetwork.getShip().collide(asteroid)) {
                        playingNetwork.getShip().setAlive(false);
                        //System.out.println("Our NN died trying :(");
                        playingNetwork.resetShip();

                        playingNetwork.addPoints(-100);
                    }
                });
                // repopulate asteroids
                if(!playingNetwork.getShip().isAlive()){
                    playingNetwork.getShip().setAlive(true);
                    asteroids.clear();
                    for(int i = 0; i < 10; i++){ // start with 10 asteroids
                        Random rng = new Random();
                        double size = 0.5 + rng.nextDouble(); // let size of asteroids vary a bit
                        Polygon squarePolygon = new Polygon(-20*size, -20*size, 20*size, -20*size,
                                20*size, 20*size, -20*size, 20*size); // asteroid's size
                        Hitbox asteroid = new Hitbox(squarePolygon, Color.BLACK,
                                rng.nextInt(-4*SinglePlayerView.WIDTH/5, -SinglePlayerView.WIDTH/5),
                                rng.nextInt(-4*SinglePlayerView.HEIGHT/5, -SinglePlayerView.HEIGHT/5));
                        for(int k = 0; k < 3; k++) asteroid.accelerate();
                        asteroids.add(asteroid);
                    }
                }
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

        // networks evolve
        Collections.sort(ourNNPopulation); // sort network population by their performance
        ourNNPopulation.get(0).setAveragePopulationScore(1.0*totalScore/populationSize);
        // discard everything and leave only top 10%
        for(int i = 0; i < (int)(0.75*populationSize); i++){ //
            NeuralNetwork toBeDisposed = ourNNPopulation.get(populationSize - 1 - i);
            ourNNPopulation.remove( populationSize - 1 - i);
            toBeDisposed = null; // remove loser network from memory
        }
        // refill population by duplicating networks that performed well
        for(int i = 0; i < (int)(0.25*populationSize); i++){
            for(int j = 0; j < 3; j++){
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

        trainingEpisode++; // putting it inside AnimationTimer leads to trainingEpisode count going faster than actual execution
        return totalScore;
    }

    public double[] getObservation(List<Hitbox> asteroids, Hitbox ship){
        // indices: 0 - X,Y; 1 - nextX,Y; 2 - nextX,nextY; 3 - X,nextY; 4 - prevX,nextY; 5 - prevX,Y; 6 - prevX,prevY, 7 - X,prevY; 8 - nextX,prevY
        double[] shipObservation = new double[inputSize];

        // i will discretize the whole gaming space into 50x50 squares; if asteroid is in one of them, i put 1
        // determine ship's position
        int x = (int) Math.abs(ship.getPolygon().getTranslateX());
        int y = (int) Math.abs(ship.getPolygon().getTranslateY());
        int X = x / 50;
        int Y = y / 50;
        //System.out.println("ships coordinates: x = " + x + ", y = " + y + "; X = " + X + "; Y = " + Y );

        // look at 8 nearest neighbors, I will use reflective boundary conditions; ideally ship should learn to stay away from boundaries
        int prevX = X <= 0 ? (int)(GameWindowWIDTH/50) - 1 : X - 1;
        int prevY = Y <= 0 ? (int)(GameWindowHEIGHT/50) - 1 : Y - 1;
        int nextX = X >= (int)(GameWindowWIDTH/50) - 1 ? 0 : X + 1;
        int nextY = Y >= (int)(GameWindowHEIGHT/50) - 1 ? 0 : Y + 1;

        asteroids.stream().forEach(ast ->{
            int xAst = (int) Math.abs(ast.getPolygon().getTranslateX());
            int yAst = (int) Math.abs(ast.getPolygon().getTranslateY());
            int astX = xAst / 50;
            int astY = yAst / 50;
            //System.out.println("asteroid coordinates: x = " + xAst + ", y = " + yAst + "; X = " + astX + "; Y = " + astY );
            if(astX == X && astY == Y){
                shipObservation[0] = 1; //System.out.println("0");
            }
            if(astX == nextX && astY == Y){
                shipObservation[1] = 1; //System.out.println("1");
            }
            if(astX == nextX && astY == nextY){
                shipObservation[2] = 1; //System.out.println("2");
            }
            if(astX == X && astY == nextY){
                shipObservation[3] = 1; //System.out.println("3");
            }
            if(astX == prevX && astY == nextY){
                shipObservation[4] = 1; //System.out.println("4");
            }
            if(astX == prevX && astY == Y){
                shipObservation[5] = 1; //System.out.println("5");
            }
            if(astX == prevX && astY == prevY){
                shipObservation[6] = 1; //System.out.println("6");
            }
            if(astX == X && astY == prevY){
                shipObservation[7] = 1; //System.out.println("7");
            }
            if(astX == nextX && astY == prevY){
                shipObservation[8] = 1; //System.out.println("8");
            }
        });

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
        gamingPane.getChildren().add(ship.getHitbox().getPolygon());
        //System.out.println("ship added");
        // remove old asteroids
        projectiles.forEach(projectile -> gamingPane.getChildren().remove(projectile.getPolygon())); // remove from the pane
        projectiles.clear(); // remove from the projectiles list
        asteroids.forEach(asteroid ->{
            gamingPane.getChildren().remove(asteroid.getImage());
            gamingPane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
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
            gamingPane.getChildren().add(asteroid.getHitbox().getPolygon());
        });
        //System.out.println("Asteroids added");
    }

    private void changePlayingNetwork(List<NeuralNetwork> ourNNPopulation){
        playingNetworkID++;
        playingNetwork = ourNNPopulation.get(playingNetworkID);
        System.out.println("Score of the selected network: " + playingNetwork.getScoreForPrinting());
    }

    public void setPointsToDisplay(int value) { pointsToDisplay = value;}

    public void saveNetworkParameters(List<NeuralNetwork> ourNNPopulation, int bestScore){
        StoreTrainedNNsDB recordNNParameters = new StoreTrainedNNsDB("jdbc:h2:./trained-NNs-database");
        List<String> NNParameters = ourNNPopulation.get(0).NNParametersToList();
        // NN data stored in format: int score, Str firstLWeights, Str firstLBayeses, Str secondLWeights, Str secondLBiases
        try{
            recordNNParameters.addNetworkToDB(ourNNPopulation.get(0).getScoreForPrinting(),
                    NNParameters.get(0), NNParameters.get(1),NNParameters.get(2),NNParameters.get(3));
        }catch(SQLException e){ System.out.printf(e.getMessage());}
    }

    public AnimationTimer getPlayer(){ return player;}
    public AnimationTimer getTrainer(){ return trainer;}

    public void stopAll(){
        player.stop();
        trainer.stop();
    }
}
