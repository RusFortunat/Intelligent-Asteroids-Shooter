package root.intelligentasteroidsshooter;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NNPLaysGameOnly {
    private Ship ship;
    private int pointsToDisplay;
    private int inGameAction;
    private int gameNumber;
    private AnimationTimer player;

    public NNPLaysGameOnly(){}

    public void start(Pane gamingPane, Pane graphPane, VBox messageWindow,
                             NeuralNetwork loadedNetwork, LineChart lineChart){

        Timer messageWindowLingerTime = new Timer();
        messageWindowLingerTime.setStart(Instant.now());
        messageWindow.setAlignment(Pos.CENTER);
        // getScoreForPrinting() = 0 means we loaded our network, the message below appears only after training
        if(loadedNetwork.getScoreForPrinting() < 2000 && loadedNetwork.getScoreForPrinting() != 0){
            messageWindow.setVisible(true);
            messageWindow.getChildren().clear();
            Text finalResult = new Text("  Improvement is zero to none.");
            Text loadNetworkMessage = new Text("Try increasing simulation parameters\n" +
                    "     or load already trained models");
            finalResult.setLineSpacing(10);
            loadNetworkMessage.setLineSpacing(10);
            finalResult.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            loadNetworkMessage.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            messageWindow.getChildren().addAll(finalResult, loadNetworkMessage);
        }
        else if(loadedNetwork.getScoreForPrinting() < 4000 && loadedNetwork.getScoreForPrinting() != 0){
            messageWindow.setVisible(true);
            messageWindow.getChildren().clear();
            Text finalResult = new Text("   Improvement is decent, but \n" +
                    "we can still do better");
            Text loadNetworkMessage = new Text("Try increasing simulation parameters\n" +
                    "     or load already trained models");
            finalResult.setLineSpacing(10);
            loadNetworkMessage.setLineSpacing(10);
            finalResult.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            loadNetworkMessage.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            messageWindow.getChildren().addAll(finalResult, loadNetworkMessage);
        }
        else if(loadedNetwork.getScoreForPrinting() < 5000 && loadedNetwork.getScoreForPrinting() != 0){
            messageWindow.setVisible(true);
            messageWindow.getChildren().clear();
            Text finalResult = new Text("I'm impressed that you actually run" +
                    "simulation for a few hours to get this" +
                    "far. ");
            finalResult.setLineSpacing(10);
            finalResult.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            messageWindow.getChildren().add(finalResult);
        }

        // graph pane -- all of this could be avoided if i could modify X-Axis extension
        graphPane.getChildren().remove(lineChart);
        NumberAxis xAxis = new NumberAxis(0,100,10);
        NumberAxis yAxis = new NumberAxis(0, 100000,10000);
        xAxis.tickLabelFontProperty().set(Font.font(15));
        yAxis.tickLabelFontProperty().set(Font.font(15));
        xAxis.setLabel("Game Number");
        yAxis.setLabel("Total Points");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setStyle("-fx-font-size: " + 20 + "px;");
        lineChart.setLegendVisible(false);
        lineChart.setLayoutX(-15);
        lineChart.setLayoutY(50);
        XYChart.Series bestScorePerEpisodeGraph = new XYChart.Series();
        graphPane.getChildren().add(lineChart);
        //lineChart.getData().add(averScorePerEpisodeGraph);
        lineChart.getData().add(bestScorePerEpisodeGraph);
        List<Integer> trackPerformance = new ArrayList<>();

        List<Asteroid> asteroids = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();
        AtomicInteger points = new AtomicInteger(); // to count points
        // set environment
        Text showPoints = new Text(10, 30, "Points: 0");
        showPoints.setViewOrder(-100.0); // make sure it is always on top layer
        showPoints.setFill(Color.CRIMSON);
        showPoints.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gamingPane.getChildren().add(showPoints);
        Image imageForAsteroid =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/asteroid_nobackgr.png").toString());
        Image imageForShip =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/falcon_no_bgr.png").toString());
        ImageView shipImage = new ImageView(imageForShip);
        double scale = 0.12;
        shipImage.setScaleX(scale);
        shipImage.setScaleY(scale);
        ship = new Ship(shipImage, scale,0, 0);

        resetEnvironment(gamingPane, shipImage, imageForAsteroid, asteroids, projectiles, scale);

        Timer forActions = new Timer();
        forActions.setStart(Instant.now());

        gameNumber = 1;

        Random rng = new Random();
        player = new AnimationTimer() {
            @Override
            public void handle(long now) {

                // remove message window after 5 sec
                if(Duration.between(messageWindowLingerTime.getStart(), Instant.now()).toMillis() > 5000){
                    messageWindow.setVisible(false);
                }

                // allow to act every 0.2sec +/- 0.1sec
                int variationInActionTime = rng.nextInt(-100,100);
                if(Duration.between(forActions.getStart(), Instant.now()).toMillis() > 200 + variationInActionTime){
                    forActions.setStart(Instant.now());

                    // network decides upon action, and executes it until next time to choose comes
                    List<Hitbox> asteroidHitboxes = asteroids.stream().map(s -> s.getHitbox()).toList();
                    double[] input = getObservation(asteroidHitboxes, ship.getHitbox(), loadedNetwork.getInputSize());
                    inGameAction = loadedNetwork.forward(input); // pass observation to network and get action
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
                    // network always shoots if it can
                    if (projectiles.size() < 10) {// limit number of projectiles present at the time to 10 (aka ammo capacity)
                        // NN shoots
                        double changeX = 1 * Math.cos(Math.toRadians(ship.getImage().getRotate()));
                        double changeY = 1 * Math.sin(Math.toRadians(ship.getImage().getRotate()));
                        int x = (int) (ship.getImage().getLayoutX() + 0.8*EvolutionarySearch.GameWindowWIDTH/2
                                + 0.4*changeX*ship.getImageWidth());
                        int y = (int) (ship.getImage().getLayoutY() + EvolutionarySearch.GameWindowHEIGHT/2
                                + 0.4*changeY*ship.getImageHeight());

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
                                size, rnd.nextDouble(-3*EvolutionarySearch.GameWindowWIDTH/4, -EvolutionarySearch.GameWindowWIDTH/4),
                                rnd.nextDouble(-3.0*EvolutionarySearch.GameWindowHEIGHT/4, -EvolutionarySearch.GameWindowHEIGHT/4));
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
                            //System.out.println("How well did it perform: " + points);
                            bestScorePerEpisodeGraph.getData().add(new XYChart.Data(gameNumber, points.addAndGet(0)));
                            points.set(0);
                            resetEnvironment(gamingPane, shipImage, imageForAsteroid, asteroids, projectiles, scale);
                            gameNumber++;
                            //System.out.println("resetEnvironment successful");
                            //stop();
                            //player.start();
                            break;
                        }
                    }
                }
            }
        };
        player.start();
    }

    private void resetEnvironment(Pane gamingPane, ImageView shipImage, Image imageForAsteroid,
                                  List<Asteroid> asteroids, List<Projectile> projectiles, double scale){
        pointsToDisplay = 0;
        // remove old ship
        gamingPane.getChildren().remove(ship.getImage());
        //gamingPane.getChildren().remove(ship.getHitbox().getPolygon());
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
            // asteroids spawn outside the screen
            Asteroid asteroid = new Asteroid(asteroidImage,
                    size, rnd.nextDouble(-3*EvolutionarySearch.GameWindowWIDTH/4, -EvolutionarySearch.GameWindowWIDTH/4),
                    rnd.nextDouble(-3.0*EvolutionarySearch.GameWindowHEIGHT/4, -EvolutionarySearch.GameWindowHEIGHT/4));
            if(!asteroid.collide(ship.getHitbox())) {asteroids.add(asteroid);}else{i--;}
        }
        if(asteroids.isEmpty()) System.out.println("No asteroids added!");
        asteroids.forEach(asteroid -> {
            gamingPane.getChildren().add(asteroid.getImage());
            //gamingPane.getChildren().add(asteroid.getHitbox().getPolygon());
        });
        //System.out.println("Asteroids added");
    }

    public double[] getObservation(List<Hitbox> asteroids, Hitbox ship, int inputSize){
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
        int prevX = X <= 0 ? (int)(EvolutionarySearch.GameWindowWIDTH/50) - 1 : X - 1;
        int prevY = Y <= 0 ? (int)(EvolutionarySearch.GameWindowHEIGHT/50) - 1 : Y - 1;
        int nextX = X >= (int)(EvolutionarySearch.GameWindowWIDTH/50) - 1 ? 0 : X + 1;
        int nextY = Y >= (int)(EvolutionarySearch.GameWindowHEIGHT/50) - 1 ? 0 : Y + 1;

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

    public void setPointsToDisplay(int value) { pointsToDisplay = value;}

    public AnimationTimer getPlayer(){ return player;}
}
