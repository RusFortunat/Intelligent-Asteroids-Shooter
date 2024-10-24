package root.intelligentasteroidsshooter;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EvolutionarySearch {
    // to work with AnimationTimer();
    private int playingNetworkID;
    private NeuralNetwork playingNetwork;
    private Ship ship;
    private boolean firstEventTriggered;
    private boolean secondEventTriggered;

    public static int GameWindowWIDTH = 500;
    public static int GameWindowHEIGHT = 400;

    public void start(Stage stage, AnchorPane anchorPane, Pane gamingPane, Pane graphPane){
        // At first, I was hoping to show how neural network plays and train at the same time, but these are two separate
        // processes and I can't do them simultaneously. Therefore, I will be sequentially showing NNs playing and training

        // ==============================================================
        // 1. Setup
        // ==============================================================
        // main background
        Image anchorBackgrFile = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter" +
                "\\src\\main\\resources\\root\\intelligentasteroidsshooter\\trainAIbackground.png"); // doesn't render without full path
        BackgroundImage anchorBI= new BackgroundImage(anchorBackgrFile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background anchorPaneBackgr = new Background(anchorBI);
        anchorPane.setBackground(anchorPaneBackgr);
        // gaming pane background
        Image gamingWindowBackgrFile = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter" +
                "\\src\\main\\resources\\root\\intelligentasteroidsshooter\\space2.gif"); // doesn't render without full path
        BackgroundImage gamingBI= new BackgroundImage(gamingWindowBackgrFile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background gamingPaneBackgr = new Background(gamingBI);
        gamingPane.setBackground(gamingPaneBackgr);
        // score and episode
        Text showPoints = new Text(10, 20, "Points: 0");
        showPoints.setViewOrder(-100.0); // make sure it is always on top layer
        showPoints.setFill(Color.CRIMSON);
        showPoints.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Text showEpisodeNumber = new Text(410, 20, "Episode: 0");
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
        System.out.println("styles set");

        // simulation parameters
        int networkPopulationSize = 50;
        int totalEpisodes = 100;
        int episodeDurationPerNetwork = 10000; // the time every network has per episode, i.e., each NN can do 10000 inputs
        int inputSize = 2*100 + 4; // two channels with 10x10 grid -- one for asteroids and one for ship, plus 4 inputs for velocity vector
        int hiddenSize = 256;
        int outputSize = 5; // accelerate, decelerate, turn left, turn right, shoot
        double mutationRate = 0.01; // aka learning rate
        int gamingPhaseDuration = 20000;

        // create population of neural networks that will be playing the game
        List<NeuralNetwork> ourNNPopulation = new ArrayList<>();
        for(int network = 0; network < networkPopulationSize; network++){
            NeuralNetwork braveNetwork = new NeuralNetwork(mutationRate, inputSize, hiddenSize, outputSize);
            ourNNPopulation.add(braveNetwork);
        }
        // we will track the training progress with this list
        ArrayList<Object> scoresVsEpisodes = new ArrayList<>();

        //Scene scene = new Scene(anchorPane);
        System.out.println("Step 1 clear");

        // ===============================================================
        // 2. Begin playing
        // ===============================================================
        playingNetwork = ourNNPopulation.getFirst();
        playingNetworkID = 0;

        ship = new Ship(shipImage, scale,0, 0);
        gamingPane.getChildren().add(ship.getImage());
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
        });

        List<Projectile> projectiles = new ArrayList<>();

        AtomicInteger points = new AtomicInteger(); // to count points
        Timer forProjectiles = new Timer();
        forProjectiles.setStart(Instant.now());

        Timer forFirstGamingRound = new Timer();
        Timer forSecondGamingRound = new Timer();
        forFirstGamingRound.setStart(Instant.now());
        forSecondGamingRound.setStart(Instant.now());
        firstEventTriggered = false;
        secondEventTriggered = false;
        // int gamingPhaseDuration = 30000; // 30sec

        new AnimationTimer() {

            @Override
            public void handle(long now) {
                List<Hitbox> asteroidHitboxes = asteroids.stream()
                        .map(s -> s.getHitbox()).toList();
                // network decides upon action
                double[] input = getObservation(inputSize, asteroidHitboxes, ship.getHitbox());
                int action = playingNetwork.forward(input); // pass observation to network and get action
                if (action == 0) ship.accelerate();
                else if (action == 1) ship.decelerate();
                else if (action == 2) ship.turnLeft();
                else if (action == 3) ship.turnRight();
                else if (action == 4 // shoot
                        && projectiles.size() < 10) {// limit number of projectiles present at the time to 10 (aka ammo capacity)
                    // NN shoots
                    double changeX = 1 * Math.cos(Math.toRadians(ship.getImage().getRotate()));
                    double changeY = 1 * Math.sin(Math.toRadians(ship.getImage().getRotate()));
                    // for projectiles to nicely come out of the ship, their coordinates have to scale with ship and playground sizes
                    int x = (int) (ship.getImage().getLayoutX() + 0.8*GameWindowWIDTH/2
                            + 0.4*changeX*scale*shipImage.getImage().getWidth());
                    int y = (int) (ship.getImage().getLayoutY() + GameWindowHEIGHT/2
                            + 0.4*changeY*scale*shipImage.getImage().getWidth());
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

                // removing colliding projectiles and asteroids
                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if(projectile.collide(asteroid.getHitbox())) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                            showPoints.setText("Points: " + points.addAndGet(1000));
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
                        //pane.getChildren().add(asteroid.getHitbox().getPolygon());
                    }
                }

                // restart the game when ship and asteroids collide
                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid.getHitbox())) {
                        // remove ship that failed and start with a new one
                        gamingPane.getChildren().remove(ship.getImage());
                        gamingPane.getChildren().remove(ship.getHitbox().getPolygon());
                        if(playingNetworkID < networkPopulationSize){
                            changePlayingNetwork(ourNNPopulation); // can't change playing network here explicitly
                        }
                        resetShip(gamingPane, shipImage, scale);
                    }
                });

                // pause game after 30 sec and train
                if(Duration.between(forFirstGamingRound.getStart(), Instant.now()).toMillis() > 30000
                        && !firstEventTriggered){
                    setFirstEventTriggered();
                    projectiles.forEach(projectile -> gamingPane.getChildren().remove(projectile.getPolygon())); // remove from the pane
                    projectiles.clear(); // remove from the projectiles list
                    asteroids.forEach(asteroid ->{
                                gamingPane.getChildren().remove(asteroid.getImage());
                                gamingPane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
                            });
                    asteroids.clear();
                    gamingPane.getChildren().remove(ship.getImage());
                    gamingPane.getChildren().remove(ship.getHitbox().getPolygon());
                    // start training simulation
                    for(int trainingEpisode = 0; trainingEpisode < totalEpisodes/2; trainingEpisode++){
                        int score = startTrainingEpisode(ourNNPopulation, episodeDurationPerNetwork);
                        scoresVsEpisodes.add(score);
                        System.out.println("Episode: " + trainingEpisode +
                                "; Average score per network: " + 1.0*score / networkPopulationSize);
                    }
                    resetPlayingNetwork(ourNNPopulation);
                    resetShip(gamingPane, shipImage, scale);
                    fillWithAsteroids(gamingPane, imageForAsteroid, asteroids);
                    forSecondGamingRound.setStart(Instant.now()); // play another 30 sec
                }

                // play again and pause the game after another 30 sec
                if(Duration.between(forSecondGamingRound.getStart(), Instant.now()).toMillis() > 30000
                    && !secondEventTriggered){
                    setSecondEventTriggered();
                    projectiles.forEach(projectile -> gamingPane.getChildren().remove(projectile.getPolygon())); // remove from the pane
                    projectiles.clear(); // remove from the projectiles list
                    asteroids.forEach(asteroid ->{
                        gamingPane.getChildren().remove(asteroid.getImage());
                        gamingPane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
                    });
                    asteroids.clear();
                    // continue training simulation
                    for(int trainingEpisode = totalEpisodes/2; trainingEpisode < totalEpisodes; trainingEpisode++){
                        int score = startTrainingEpisode(ourNNPopulation, episodeDurationPerNetwork);
                        scoresVsEpisodes.add(score);
                        System.out.println("Episode: " + trainingEpisode +
                                "; Average score per network: " + 1.0*score / networkPopulationSize);
                    }
                    resetPlayingNetwork(ourNNPopulation);
                    resetShip(gamingPane, shipImage, scale);
                    fillWithAsteroids(gamingPane, imageForAsteroid, asteroids);

                    System.out.println("Done with training!");
                    System.out.println("Individual scores");
                    ourNNPopulation.stream().
                            map(s->s.getScore()).limit(10).forEach(score -> System.out.println(score));
                    //stop();
                }
            }
        }.start();



        // ===============================================================
        // 3. Begin training
        // ===============================================================


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
            }
            //System.out.println("NN finished playing the game");
        }

        Collections.sort(ourNNPopulation); // sort network population by their performance
        // discard half of population that performed badly
        for(int i = 0; i < (int)(4.0*populationSize/5); i++){
            NeuralNetwork toBeDisposed = ourNNPopulation.get(populationSize - 1 - i);
            ourNNPopulation.remove( populationSize - 1 - i);
            toBeDisposed = null; // remove loser network from memory
        }
        // refill population by duplicating networks that performed well
        for(int i = 0; i < (int)(populationSize/5.0); i++){
            for(int j = 0; j < 4; j++){
                NeuralNetwork newNetwork = new NeuralNetwork(mutationRate, inputSize,hiddenSize,outputSize);
                newNetwork.copyNetworkParameters(ourNNPopulation.get(i));
                newNetwork.mutate();
                ourNNPopulation.add(newNetwork);
            }
            ourNNPopulation.get(i).mutate(); // mutate parameters of all agents using Gaussian distribution
        }
        //System.out.println("population updated, new generation is ready; updated ourNNPopulation.size() " + ourNNPopulation.size());
        if(ourNNPopulation.size() != populationSize){
            System.out.println("ourNNPopulation.size() != population size! ourNNPopulation.size() = " + ourNNPopulation.size());
        }

        // compute total score for the episode
        int totalScore = 0;
        for(NeuralNetwork network:ourNNPopulation){
            totalScore += network.getScore();
        }
        return totalScore;
    }

    public static double[] getObservation(int inputSize, List<Hitbox> asteroids, Hitbox ship){
        double[] shipObservation = new double[inputSize];
        // split whole screen into 10x10 grid and fill each cell with 1 if it has asteroid and with 0 if it doesn't
        for(Hitbox asteroid:asteroids){
            int x = (int) Math.abs(asteroid.getPolygon().getTranslateX());
            int y = (int) Math.abs(asteroid.getPolygon().getTranslateY());
            int X = x / (GameWindowWIDTH/10);
            int Y = y / (GameWindowHEIGHT/10);
            //System.out.println("x = " + x + ", y = " + y + "; X = " + X + ", Y = " + Y);
            int gridIndex = 10*Y + X;
            //System.out.println("gridIndex " + gridIndex);
            shipObservation[gridIndex] = 1;
        }

        // for the second input channel provide a vector with all zeroes and a single 1 for ship location
        int x = (int) Math.abs(ship.getPolygon().getTranslateX());
        int y = (int) Math.abs(ship.getPolygon().getTranslateY());
        int X = x / (GameWindowWIDTH/10);
        int Y = y / (GameWindowHEIGHT/10);
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

    private void changePlayingNetwork(List<NeuralNetwork> ourNNPopulation){
        playingNetworkID++;
        playingNetwork = ourNNPopulation.get(playingNetworkID);
    }

    private void resetShip(Pane gamingPane, ImageView shipImage, double scale){
        ship = new Ship(shipImage, scale, 0,0);
        gamingPane.getChildren().add(ship.getImage());
    }

    private void resetPlayingNetwork(List<NeuralNetwork> ourNNPopulation){
        playingNetworkID = 0;
        playingNetwork = ourNNPopulation.getFirst();
    }

    private void fillWithAsteroids(Pane gamingPane, Image imageForAsteroid, List<Asteroid> asteroids){
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
        });
    }

    public void setFirstEventTriggered(){
        firstEventTriggered = true;
    }

    public void setSecondEventTriggered(){
        secondEventTriggered = true;
    }
}
