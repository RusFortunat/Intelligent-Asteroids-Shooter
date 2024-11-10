package root.intelligentasteroidsshooter.trainAI;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import root.intelligentasteroidsshooter.model.Asteroid;
import root.intelligentasteroidsshooter.model.Hitbox;
import root.intelligentasteroidsshooter.model.Projectile;
import root.intelligentasteroidsshooter.model.Ship;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EvolutionarySearch {
    // to work with AnimationTimer(); using local variables causes error in lambda expression
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

    // At first, I was hoping to show how neural networks play and train at the same time, but these are two separate
    // processes and I can't run them simultaneously via Application node. Therefore, I will be sequentially showing
    // how NNs are playing and training. I will check out multithreading options later
    public void start(Pane gamingPane, Pane graphPane, VBox messageWindow,
        int networkPopulationSize, int totalEpisodes, LineChart lineChart){
        graphPane = graphPane;
        lineChart = lineChart;

        // internal simulation parameters that user don't see
        int episodeDurationPerNetwork = 1000; // the time every network has per episode, don't set too high cause networks don't shoot in training!
        inputSize = 9 + 4; // 3x3 grid of asteroid positions around the ship, and screen edges
        hiddenSize = 16;
        outputSize = 8; // directions of motion
        double mutationRate = 0.01; // aka learning rate in machine learning
        int playTime = 10000; // in milliseconds, e.g., 15sec

        // create population of neural networks that will be playing the game
        List<NeuralNetwork> ourNNPopulation = new ArrayList<>();
        for(int network = 0; network < networkPopulationSize; network++){
            NeuralNetwork braveNetwork = new NeuralNetwork(mutationRate, inputSize, hiddenSize, outputSize);
            ourNNPopulation.add(braveNetwork);
        }
        trainingEpisode = 0;

        // gaming pane design
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
        Image imageForAsteroid = new Image(getClass().getResource(
                "/root/intelligentasteroidsshooter/images/asteroid_nobackgr.png").toString());
        Image imageForShip = new Image(getClass().getResource(
                "/root/intelligentasteroidsshooter/images/falcon_no_bgr.png").toString());
        ImageView shipImage = new ImageView(imageForShip);
        double scale = 0.12; // scale down the ship image
        shipImage.setScaleX(scale);
        shipImage.setScaleY(scale);
        ship = new Ship(shipImage, scale,0, 0);

        // graph pane design
        graphPane.getChildren().remove(lineChart); // remove previous graph; I specify this to make sure reset buttons clear prev results
        Text graphLabel = new Text(70, 40, "Neural Networks Performance");
        graphLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        NumberAxis xAxis = new NumberAxis(0,totalEpisodes,totalEpisodes/10);
        NumberAxis yAxis = new NumberAxis(-3500, 1000,500);
        xAxis.tickLabelFontProperty().set(Font.font(15));
        yAxis.tickLabelFontProperty().set(Font.font(15));
        xAxis.setLabel("Episode");
        yAxis.setLabel("Average Loss");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setStyle("-fx-font-size: " + 20 + "px;");
        lineChart.setLegendVisible(false);
        lineChart.setLayoutX(-15);
        lineChart.setLayoutY(50);
        XYChart.Series averageScorePerEpisode = new XYChart.Series();
        graphPane.getChildren().addAll(lineChart, graphLabel);
        lineChart.getData().add(averageScorePerEpisode);

        // show the first message to user after the training has begun
        Timer forFirstMessage = new Timer(); // to set message linger time
        forFirstMessage.setStart(Instant.now());
        if(Duration.between(forFirstMessage.getStart(), Instant.now()).toMillis() < 5000){ // show message for 5 sec
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
        // i need to call train() method from within playGame(), otherwise the program tries executing them together
        // and fails. The concept of stack works funny for Javafx and AnimationTimer()
        playGame(gamingPane, messageWindow, ourNNPopulation, showPoints, showEpisodeNumber,
                imageForAsteroid, shipImage, scale, averageScorePerEpisode, episodeDurationPerNetwork, totalEpisodes,
                playTime);
    }

    // the method launches game on the left gaming window to demonstrate how the neural network controls tre ship
    public void playGame(Pane gamingPane, VBox messageWindow, List<NeuralNetwork> ourNNPopulation, Text showPoints,
                         Text showEpisodeNumber, Image imageForAsteroid, ImageView shipImage, double scale,
                         XYChart.Series averageScorePerEpisodeGraph, int episodeDurationPerNetwork, int totalEpisodes,
                         int playTime){

        // (re-)set the environment
        int networkPopulationSize = ourNNPopulation.size();
        playingNetwork = ourNNPopulation.get(0);
        playingNetworkID = 0;
        List<Asteroid> asteroids = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();
        AtomicInteger points = new AtomicInteger(); // to count points
        resetEnvironment(gamingPane, shipImage, imageForAsteroid, asteroids, projectiles, scale);

        // timers
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

                // remove the message window after 5 seconds
                if(Duration.between(messageWindowLingerTime.getStart(), Instant.now()).toMillis() > 5000){
                    messageWindow.setVisible(false);
                }

                showEpisodeNumber.setText("Episode: " + trainingEpisode);

                // Network decides upon the action, and executes the chosen action until time comes to pick new one.
                // This is done because AnimationTimer() is called 60 times per second. Without this limitation
                // the network action frequency would be too high for action to have any impact. Here we
                // allow network to act every 0.2 +/- 0.1 sec
                int variationInActionTime = rng.nextInt(-100,100);
                if(Duration.between(forActions.getStart(), Instant.now()).toMillis() > 200 + variationInActionTime){
                    forActions.setStart(Instant.now()); // reset action timer

                    // we provide asteroids locations as an input vector
                    List<Hitbox> asteroidHitboxes = asteroids.stream().map(s -> s.getHitbox()).toList();
                    double[] input = getObservation(asteroidHitboxes, ship.getHitbox());
                    inGameAction = playingNetwork.forward(input); // pass observation to get new direction for motion
                    // get speed to zero to prepare the ship to make a rotation
                    for(int k = 0; k < 5; k++){
                        ship.decelerate();
                    }
                }else{ // repeat actions until neural network doesn't choose a new one
                    // TODO: fix compact rotation code block below by removing ship's trembling / vibrating motion;
                    int delta = 2;
                    // restrict angle values between 0 and 360 for the code below to work
                    /*double angle = ship.getImage().getRotate() % 360 >= 0 ?
                            ship.getImage().getRotate() % 360 : ship.getImage().getRotate() % 360 + 360;
                    ship.getImage().setRotate(angle);
                    int chosenDirection = inGameAction*45;
                    int oppositeEnd = inGameAction*45 + 180;
                    // chooses shortest rotation path

                    if(Math.abs(chosenDirection - angle) > delta) {
                        if (angle < 180) {
                            if (angle > chosenDirection && angle < oppositeEnd) {
                                ship.turnLeft(); // += -3 grad
                            } else {
                                ship.turnRight(); // += 3 grad
                            }
                        } else {
                            if (angle > chosenDirection || angle < oppositeEnd) {
                                ship.turnLeft(); // += -3 grad
                            } else {
                                ship.turnRight(); // += 3 grad
                            }
                        }
                    }*/
                    double angle = ship.getImage().getRotate() % 360;
                    if(angle < 0){
                        ship.getImage().setRotate(angle + 360);
                    }else{
                        ship.getImage().setRotate(angle);
                    }

                    if (inGameAction == 0) { // turn to 0 degrees (along +X axis)
                        if(angle < 180 && angle > delta){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }
                    }
                    else if (inGameAction == 1){ // turn to +45 degrees (+X,+Y)
                        if(angle > 45+delta && angle < 225){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }
                    }
                    else if (inGameAction == 2) { // turn to +90 degrees (0,+Y)
                        if(angle > 90+delta && angle < 270){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }
                    }
                    else if (inGameAction == 3) { // turn to +135 degrees (-X,+Y)
                        if(angle > 135 + delta && angle < 315){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }
                    }
                    else if (inGameAction == 4) { // turn to +180 degrees (-X,0)
                        if(angle > 180 + delta){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }
                    }
                    else if (inGameAction == 5) { // turn to +225 degrees (-X,-Y)
                        if(angle > 225 + delta || angle < 45 - delta){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }
                    }
                    else if (inGameAction == 6) { // turn to +270 degrees (0,-Y)
                        if(angle > 270 + delta || angle < 90 - delta){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }
                    }
                    else if (inGameAction == 7) { // turn to +315 degrees (+X,-Y)
                        if(angle > 315 + delta || angle < 135 - delta){
                            ship.turnRight();
                        }else{
                            ship.turnLeft();
                        }
                    }

                    ship.accelerate();
                }

                // ship always shoots if it can
                if (projectiles.size() < 5) {// limit number of projectiles present at the time to 10 (aka ammo capacity)
                    // set the projectile's direction to be the same as the ship's orientation
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
                    double rangeMin = 0.1;
                    double rangeMax = 0.2;
                    double size = ThreadLocalRandom.current().nextDouble(rangeMin,rangeMax); // restrict asteroids size in this range
                    asteroidImage.setScaleX(size);
                    asteroidImage.setScaleY(size);
                    Asteroid asteroid = new Asteroid(asteroidImage,
                            size, ThreadLocalRandom.current().nextInt(-3*GameWindowWIDTH/4, -GameWindowWIDTH/4),
                            ThreadLocalRandom.current().nextInt(-3*GameWindowHEIGHT/4, -GameWindowHEIGHT/4));
                    if(!asteroid.collide(ship.getHitbox())) {
                        asteroids.add(asteroid);
                        gamingPane.getChildren().add(asteroid.getImage());
                        // explicitly adding transparent polygons that act as hitboxes eliminates delay in asteroid removal
                        gamingPane.getChildren().add(asteroid.getHitbox().getPolygon());
                    }
                }

                // removing colliding projectiles and asteroids -- taken from MOOC Ch.14.3 Exercise
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

                // restart the game when ship and asteroids collide, replaced forEach with explicit for loop because i delete asteroids
                for(int i=0; i < asteroids.size();i++){
                    if(ship.collide(asteroids.get(i).getHitbox())){
                        showPoints.setText("Points: 0");
                        System.out.println("How well did it perform: " + points);
                        points.set(0);
                        resetEnvironment(gamingPane, shipImage, imageForAsteroid, asteroids, projectiles, scale);
                        playingNetworkID++;
                        if(playingNetworkID < networkPopulationSize - 1) playingNetworkID++; // let the next network play
                        break;
                    }
                }

                // stop playing after a certain time
                if(Duration.between(forEntireGame.getStart(), Instant.now()).toMillis() > playTime){
                    stop(); // stop -> train for a certain number of episodes

                    int endEpisode = 0;
                    if(trainingEpisode <totalEpisodes) {
                        endEpisode = totalEpisodes;

                        // display message for the first time training
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

                        // begin training
                        train(gamingPane, messageWindow, ourNNPopulation, averageScorePerEpisodeGraph,
                                totalEpisodes, episodeDurationPerNetwork);
                    }
                }
            }
        };
        player.start();
    }

    public void train(Pane gamingPane, VBox messageWindow, List<NeuralNetwork> ourNNPopulation,
                      XYChart.Series averageLossPerEpisodeGraph, int totalEpisodes, int episodeDurationPerNetwork){

        int networkPopulationSize = ourNNPopulation.size();

        Timer messageWindowLingerTime = new Timer(); // hide message window after ~ 5 sec
        messageWindowLingerTime.setStart(Instant.now());

        trainer = new AnimationTimer(){
            @Override
            public void handle(long now) {
                gamingPane.getChildren().clear();

                // hide message after 5 seconds
                if(Duration.between(messageWindowLingerTime.getStart(), Instant.now()).toMillis() > 5000){
                    messageWindow.setVisible(false);
                }

                // start evolutionary training
                int score = startTrainingEpisode(ourNNPopulation, episodeDurationPerNetwork); // returns total score
                System.out.println("Episode: " + (trainingEpisode) +
                        "; Average score per network: " + 1.0*score / networkPopulationSize +
                        "; Best score per generation: " + ourNNPopulation.get(0).getScoreForPrinting()); // one of the few sout's left
                averageLossPerEpisodeGraph.getData().add(new XYChart.Data(trainingEpisode, 1.0*score / networkPopulationSize));

                // save network if it is worth saving; adjust this parameter if training rewards are changed
                if(1.0*score / networkPopulationSize > 500 || ourNNPopulation.get(0).getScoreForPrinting() > 500) {
                    saveNetworkParameters(ourNNPopulation, ourNNPopulation.get(0).getScoreForPrinting());
                }

                //
                if(trainingEpisode >= totalEpisodes) {
                    stop();
                    player.stop();

                    messageWindow.setVisible(true);
                    messageWindow.getChildren().clear();
                    Text finalResult1 =new Text("The training is finished");
                    Text finalResult2 =new Text("To test the performance, load the models");
                    finalResult1.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                    finalResult2.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                    messageWindow.getChildren().addAll(finalResult1, finalResult2);
                    messageWindow.setAlignment(Pos.CENTER);
                    messageWindow.setPadding(new Insets(20));
                }
            }
        };
        trainer.start();
    }

    // I won't be playing game here, but rather cycling through all possible entries on the input vector and
    // punishing wrong outputs. Reproducing an actual game is extremely time-inefficient
    public int startTrainingEpisode(List<NeuralNetwork> ourNNPopulation,int episodeDurationPerNetwork){

        int populationSize = ourNNPopulation.size();

        // networks play the game
        for(int networkID = 0; networkID < populationSize; networkID++){
            NeuralNetwork playingNetwork = ourNNPopulation.get(networkID); // wer ist dran?

            for(int iteration = 0; iteration < episodeDurationPerNetwork; iteration++){
                // prepare pseudo-input vector that would reflect the real game situation
                double[] input = new double[inputSize];

                // possible asteroid positions
                // indices: 0 - X,Y; 1 - nextX,Y; 2 - nextX,nextY; 3 - X,nextY; 4 - prevX,nextY; 5 - prevX,Y;
                // 6 - prevX,prevY, 7 - X,prevY; 8 - nextX,prevY
                for(int k = 0; k < 9; k++) {
                    int moreEmpty = ThreadLocalRandom.current().nextInt(0,2);
                    if(moreEmpty == 1) input[k] = ThreadLocalRandom.current().nextInt(0,2); // fill only in ~ 25% cases
                }

                // screen bounds, we punish approaching those because asteroids spawn there
                // 9 - left edge, 10 - bottom edge, 11 - right edge, 12 - top edge
                int xBound = ThreadLocalRandom.current().nextInt(0,2);
                if(xBound == 0){
                    input[9] = 1;
                }else{
                    input[11] = 1;
                }
                int yBound = ThreadLocalRandom.current().nextInt(0,2);
                if(yBound == 0){
                    input[10] = 1;
                }else{
                    input[12] = 1;
                }

                // pass the input to the network and get the action from it
                int action = playingNetwork.forward(input);

                // now punish the network for wrong moves and reward for good ones -- in essence, here happens the supervised learning
                int punishValue = -5;
                int rewardValue = 1;
                if(input[action + 1] == 1){
                    playingNetwork.addPoints(punishValue); // punish for moving towards the asteroid
                }else{
                    playingNetwork.addPoints(rewardValue); // reward for moving away from the asteroid
                }

                // punish the network for moving towards the edge
                if((input[9] == 1 && (action == 3 || action == 4 || action == 5)) ||
                        (input[10] == 1 && (action == 5 || action == 6 || action == 7)) ||
                        (input[11] == 1 && (action == 0 || action == 1 || action == 7)) ||
                        (input[12] == 1 && (action == 1 || action == 2 || action == 3))){
                    playingNetwork.addPoints(punishValue);
                }else{
                    playingNetwork.addPoints(rewardValue); // reward the network for moving away from the edge
                }
            }
        }

        // compute total score for the training episode;
        int totalScore = 0;
        for(NeuralNetwork network:ourNNPopulation){
            int networkScore = network.getScore();
            network.setScoreForPrinting(networkScore);
            totalScore += networkScore;
        }

        // evolve the networks
        Collections.sort(ourNNPopulation); // sort network population by their performance
        ourNNPopulation.get(0).setAveragePopulationScore(1.0*totalScore/populationSize);
        // discard 3/4 of population and leave only top 25%
        for(int i = 0; i < (int)(0.75*populationSize); i++){ // chao losers
            NeuralNetwork toBeDisposed = ourNNPopulation.get(populationSize - 1 - i);
            ourNNPopulation.remove( populationSize - 1 - i);
            toBeDisposed = null; // remove loser network from memory, is it necessary?
        }

        // refill population by duplicating networks that performed well
        for(int i = 0; i < (int)(0.25*populationSize); i++){
            for(int j = 0; j < 3; j++){
                int parent1 = ThreadLocalRandom.current().nextInt(0,(int)(0.1*populationSize));
                int parent2 = ThreadLocalRandom.current().nextInt(0,(int)(0.1*populationSize));

                // create a new neural network via crossover
                NeuralNetwork newNetwork = new NeuralNetwork(ourNNPopulation.get(parent1), ourNNPopulation.get(parent2));

                ourNNPopulation.add(newNetwork);
            }
            ourNNPopulation.get(i).setScore(0); // reset scores
            ourNNPopulation.get(i).mutate(); // mutate parameters of all agents using Gaussian distribution
        }
        if(ourNNPopulation.size() != populationSize){
            System.out.println("ourNNPopulation.size() != population size! ourNNPopulation.size() = " + ourNNPopulation.size());
        }

        // executing trainingEpisode++ inside AnimationTimer leads to trainingEpisode count going faster than actual execution
        trainingEpisode++;

        return totalScore;
    }

    // I will discretize the whole gaming space into 100x100 squares; if asteroid is in one of them, i put 1
    // indices: 0 - X,Y; 1 - nextX,Y; 2 - nextX,nextY; 3 - X,nextY; 4 - prevX,nextY; 5 - prevX,Y; 6 - prevX,prevY, 7 - X,prevY; 8 - nextX,prevY
    public double[] getObservation(List<Hitbox> asteroids, Hitbox ship){
        // determine ship's position
        int x = (int) ship.getPolygon().getTranslateX() < 0 ? (int)
                ship.getPolygon().getTranslateX() + GameWindowWIDTH : (int) ship.getPolygon().getTranslateX();
        int y = (int) ship.getPolygon().getTranslateY() < 0 ? (int)
                ship.getPolygon().getTranslateY() + GameWindowHEIGHT : (int) ship.getPolygon().getTranslateY();
        int X = x / 100;
        int Y = y / 100;
        // look at 8 nearest neighbors, I will use periodic boundary conditions;
        int prevX = X <= 0 ? GameWindowWIDTH/100 - 1 : X - 1;
        int prevY = Y <= 0 ? GameWindowHEIGHT/100 - 1 : Y - 1;
        int nextX = X >= GameWindowWIDTH/100 - 1 ? 0 : X + 1;
        int nextY = Y >= GameWindowHEIGHT/100 - 1 ? 0 : Y + 1;

        double[] shipObservation = new double[inputSize];

        asteroids.stream().forEach(ast ->{
            int xAst = (int) ast.getPolygon().getTranslateX() < 0 ? (int)
                    ast.getPolygon().getTranslateX() + GameWindowWIDTH : (int) ast.getPolygon().getTranslateX();
            int yAst = (int) ast.getPolygon().getTranslateY() < 0 ? (int)
                    ast.getPolygon().getTranslateY() + GameWindowHEIGHT : (int) ast.getPolygon().getTranslateY();
            int astX = (int) Math.round(xAst / 100.0);
            int astY = (int) Math.round(yAst / 100.0);

            if(astX == X && astY == Y){
                shipObservation[0] = 1;
            }
            if(astX == nextX && astY == Y){
                shipObservation[1] = 1;
            }
            if(astX == nextX && astY == nextY){
                shipObservation[2] = 1;
            }
            if(astX == X && astY == nextY){
                shipObservation[3] = 1;
            }
            if(astX == prevX && astY == nextY){
                shipObservation[4] = 1;
            }
            if(astX == prevX && astY == Y){
                shipObservation[5] = 1;
            }
            if(astX == prevX && astY == prevY){
                shipObservation[6] = 1;
            }
            if(astX == X && astY == prevY){
                shipObservation[7] = 1;
            }
            if(astX == nextX && astY == prevY){
                shipObservation[8] = 1;
            }
        });

        // ship should learn to stay away from edges
        if(X == 0 || prevX == 0) shipObservation[9] = 1;
        if(Y == 0 || prevY == 0) shipObservation[10] = 1;
        if(X == GameWindowWIDTH/100 - 1 || nextX == GameWindowWIDTH/100 - 1) shipObservation[11] = 1;
        if(Y == GameWindowHEIGHT/100 - 1 || nextY == GameWindowHEIGHT/100 - 1) shipObservation[12] = 1;

        return shipObservation;
    }

    private void resetEnvironment(Pane gamingPane, ImageView shipImage, Image imageForAsteroid,
                                  List<Asteroid> asteroids, List<Projectile> projectiles, double scale){
        pointsToDisplay = 0;

        // remove old ship and add a new one
        gamingPane.getChildren().remove(ship.getImage());
        gamingPane.getChildren().remove(ship.getHitbox().getPolygon());
        ship = new Ship(shipImage, scale, 0,0);
        shipImage.setRotate(0);
        gamingPane.getChildren().add(ship.getImage());
        gamingPane.getChildren().add(ship.getHitbox().getPolygon());

        // remove old asteroids and add new ones
        projectiles.forEach(projectile -> gamingPane.getChildren().remove(projectile.getPolygon())); // remove from the pane
        projectiles.clear(); // remove from the projectiles list
        asteroids.forEach(asteroid ->{
            gamingPane.getChildren().remove(asteroid.getImage());
            gamingPane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
        });
        asteroids.clear();
        for (int i = 0; i < 5; i++) {
            ImageView asteroidImage = new ImageView(imageForAsteroid);
            //Random rnd = new Random();
            double rangeMin = 0.1;
            double rangeMax = 0.2;
            double size = ThreadLocalRandom.current().nextDouble(rangeMin,rangeMax); //rangeMin + (rangeMax - rangeMin) * rnd.nextDouble();
            asteroidImage.setScaleX(size);
            asteroidImage.setScaleY(size);
            Asteroid asteroid = new Asteroid(asteroidImage,
                    size, ThreadLocalRandom.current().nextInt(-3*GameWindowWIDTH/4, -GameWindowWIDTH/4),
                    ThreadLocalRandom.current().nextInt(-3*GameWindowHEIGHT/4, -GameWindowHEIGHT/4));
            if(!asteroid.collide(ship.getHitbox())) {asteroids.add(asteroid);}else{i--;}
        }
        if(asteroids.isEmpty()) System.out.println("No asteroids added!");
        asteroids.forEach(asteroid -> {
            gamingPane.getChildren().add(asteroid.getImage());
            gamingPane.getChildren().add(asteroid.getHitbox().getPolygon());
        });
    }

    public void saveNetworkParameters(List<NeuralNetwork> ourNNPopulation, int bestScore){
        StoreTrainedNNsDB recordNNParameters =
                new StoreTrainedNNsDB("jdbc:h2:./src/main/resources/root/intelligentasteroidsshooter/trained-NNs-database");
        List<String> NNParameters = ourNNPopulation.get(0).NNParametersToList();

        // NN data stored in format: int score, Str firstLWeights, Str firstLBayeses, Str secondLWeights, Str secondLBiases
        try{
            recordNNParameters.addNetworkToDB(ourNNPopulation.get(0).getScoreForPrinting(),
                    NNParameters.get(0), NNParameters.get(1),NNParameters.get(2),NNParameters.get(3));
        }catch(SQLException e){ System.out.printf(e.getMessage());}
    }

    // setters, getters
    public void setPointsToDisplay(int value) { pointsToDisplay = value;}

    public AnimationTimer getPlayer(){ return player;}
    public AnimationTimer getTrainer(){ return trainer;}
}
