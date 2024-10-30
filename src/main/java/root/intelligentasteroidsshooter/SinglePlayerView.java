package root.intelligentasteroidsshooter;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SinglePlayerView {
    private double respawnRate;
    private int asteroidValue;
    private double asteroidSpeed;

    public static int WIDTH = 500;
    public static int HEIGHT = 400;

    public void start(Stage singlePlayer, String chosenImage, String chosenDifficulty) throws IOException {
        // difficulty
        int diffTextPosX = 0;
        if(chosenDifficulty.equals("no sweat")){
            respawnRate = 0.01;
            asteroidValue = 1000;
            asteroidSpeed = 1;
        }
        else if(chosenDifficulty.equals("survival")){
            respawnRate = 0.05;
            asteroidValue = 1000;
            asteroidSpeed = 2;
        }
        else if(chosenDifficulty.equals("extreme")) {
            respawnRate = 0.1;
            asteroidValue = 1000; // not sure if I should change it
            asteroidSpeed = 3;
        }
        System.out.println("respawnRate: " + respawnRate + ", asteroidValue: " + asteroidValue + ", asteroidSpeed: " + asteroidSpeed);

        // scene setup
        Pane pane = new Pane();
        Image backgroundFile =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/space2.gif").toString());
        BackgroundImage myBI= new BackgroundImage(backgroundFile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background paneBackgr = new Background(myBI);
        Text text = new Text(10, 20, "Points: 0");
        text.setViewOrder(-100.0); // make sure it is always on top
        text.setFill(Color.CRIMSON);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Text difficulty = new Text(270, 20, "Difficulty: " + chosenDifficulty);
        difficulty.setViewOrder(-100.0); // make sure it is always on top
        difficulty.setFill(Color.CRIMSON);
        difficulty.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pane.getChildren().addAll(text, difficulty);
        pane.setPrefSize(WIDTH, HEIGHT);
        pane.setBackground(paneBackgr);

        Image imageForShip = new Image(getClass().getResource(chosenImage).toString());
        ImageView shipImage = new ImageView(imageForShip);
        double scale = 0.12;
        shipImage.setScaleX(scale);
        shipImage.setScaleY(scale);
        Ship ship = new Ship(shipImage, scale,0, 0);
        pane.getChildren().add(ship.getImage());
        //pane.getChildren().add(ship.getHitbox().getPolygon());
        //System.out.println("Ship added");

        List<Projectile> projectiles = new ArrayList<>();

        Image imageForAsteroid =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/asteroid_nobackgr.png").toString());
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
                    size, rnd.nextInt(-3*WIDTH/4, -WIDTH/4), rnd.nextInt(-3*HEIGHT/4, -HEIGHT/4));
            if(!asteroid.collide(ship.getHitbox())) {asteroids.add(asteroid);}else{i--;}
        }
        //System.out.println("Asteroids and ship are created");

        asteroids.forEach(asteroid -> {
            if(asteroidSpeed > 1) {
                for(int i =1; i < asteroidSpeed; i++) asteroid.accelerate();
            }
            pane.getChildren().add(asteroid.getImage());
            //pane.getChildren().add(asteroid.getHitbox().getPolygon());
        });

        Scene scene = new Scene(pane);

        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
        scene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });

        AtomicInteger points = new AtomicInteger(); // to count points
        RecordHolders newPlayer = new RecordHolders("futureRecordHolder?", "0");
        Timer forProjectiles = new Timer();
        forProjectiles.setStart(Instant.now());

        new AnimationTimer() {

            @Override
            public void handle(long now) {
                if(pressedKeys.getOrDefault(KeyCode.LEFT, false)) {
                    ship.turnLeft();
                }

                if(pressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
                    ship.turnRight();
                }

                if(pressedKeys.getOrDefault(KeyCode.UP, false)) {
                    ship.accelerate();
                }

                if(pressedKeys.getOrDefault(KeyCode.DOWN, false)) {
                    ship.decelerate();
                }

                // shoot!
                if (pressedKeys.getOrDefault(KeyCode.SPACE, false) && projectiles.size() < 10
                        && Duration.between(forProjectiles.getStart(), Instant.now()).toMillis() > 100 ) { // 1 shot per 0.5sec
                    // we shoot
                    forProjectiles.setStart(Instant.now());
                    double changeX = 1*Math.cos(Math.toRadians(ship.getImage().getRotate()));
                    double changeY = 1*Math.sin(Math.toRadians(ship.getImage().getRotate()));
                    int x = (int)(ship.getImage().getLayoutX() + 0.8*WIDTH/2 + 0.4*changeX*scale*shipImage.getImage().getWidth());
                    int y = (int)(ship.getImage().getLayoutY() + HEIGHT/2 + 0.4*changeY*scale*shipImage.getImage().getHeight()); // this has to scale with images size and Pane sizes
                    Projectile projectile = new Projectile(x, y);
                    projectile.getPolygon().setRotate(ship.getImage().getRotate());
                    projectiles.add(projectile);

                    projectile.accelerate();
                    projectile.setMovement(projectile.getMovement().normalize().multiply(3));

                    pane.getChildren().add(projectile.getPolygon());
                }

                // move objects
                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                // removing colliding projectiles and asteroids
                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if(projectile.collide(asteroid.getHitbox())) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                            newPlayer.add(points.addAndGet(asteroidValue));
                            text.setText("Points: " + newPlayer.getPoints());
                        }
                    });
                });
                projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .forEach(projectile -> pane.getChildren().remove(projectile.getPolygon())); // remove from the pane
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .collect(Collectors.toList())); // remove from the projectiles list
                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid ->{
                            pane.getChildren().remove(asteroid.getImage());
                            pane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
                        } );
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList())); // remove from the asteroids list

                // add new asteroids randomly at the edges of the screen, if they don't collide with the ship
                if(Math.random() < respawnRate) {
                    ImageView asteroidImage = new ImageView(imageForAsteroid);
                    Random rnd = new Random();
                    double rangeMin = 0.1;
                    double rangeMax = 0.2;
                    double size = rangeMin + (rangeMax - rangeMin) * rnd.nextDouble(); // restrict asteroids size in this range
                    asteroidImage.setScaleX(size);
                    asteroidImage.setScaleY(size);
                    Asteroid asteroid = new Asteroid(asteroidImage, size,
                            rnd.nextInt(-4*WIDTH/5, -WIDTH/5),
                            rnd.nextInt(-4*HEIGHT/5, -HEIGHT/5));
                    if(!asteroid.collide(ship.getHitbox())) {
                        if(asteroidSpeed > 1) {
                            for(int i =1; i < asteroidSpeed; i++) asteroid.accelerate();
                        }
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getImage());
                        //pane.getChildren().add(asteroid.getHitbox().getPolygon());
                    }
                }

                // stop the game when ship and asteroids collide
                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid.getHitbox())) {
                        stop();
                        singlePlayer.close();
                        try{
                            showScoreAndAskToPlayAgain(newPlayer.getPoints());
                        }catch(Exception e){e.getMessage();}
                    }
                });
            }
        }.start();

        //projectiles.forEach(projectile -> projectile.move());

        new AnimationTimer() {

            @Override
            public void handle(long now) {
                //text.setText("Points: " + points.incrementAndGet());
            }
        }.start();

        singlePlayer.setScene(scene);
        singlePlayer.show();
    }

    public void showScoreAndAskToPlayAgain(int score) throws IOException{
        //System.out.println("inside showScoreAndAskToPlayAgain");
        FXMLLoader restartView = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("restart-view.fxml"));
        Scene restartScene = new Scene(restartView.load());
        restartScene.getStylesheets().add("customStyles.css");
        //System.out.println("Scene loaded");
        RestartViewController restartController = restartView.getController();
        restartController.setBackground();
        restartController.setLabels(score);

        RecordTableDB recordTableDB = new RecordTableDB("jdbc:h2:./record-table-database");
        try{
            List<String> records = recordTableDB.toList().stream()
                    .map(row->row.split(","))
                    .sorted((entry1, entry2) -> Integer.valueOf(entry2[2]) - Integer.valueOf(entry1[2]))
                    .limit(10)
                    .map(s->{
                        return s[0] + "," + s[1] + "," + s[2];
                    })
                    .collect(Collectors.toList());
            List<String> names = records.stream()
                    .map(row->row.split(","))
                    .map(s->{
                        return  s[1];
                    })
                    .collect(Collectors.toList());
            //System.out.println("Record list:");
            //System.out.println(records);
            restartController.fillTable(records, "");
            if(compareScores(records, score)){ // if current score is bigger than DB entries, then it's a record
                //System.out.println("add record");
                restartController.showRecordField(true);
                restartController.getName().textProperty().addListener((observable, oldValue, newValue) -> {
                    //System.out.println("textfield changed from " + oldValue + " to " + newValue);
                    restartController.getName().setOnKeyReleased(event -> {
                        if (event.getCode() == KeyCode.ENTER && !names.contains(newValue)){ // don't accept existing names
                            restartController.getName().setEditable(false);
                            //restartController.setName(newValue);
                            //System.out.println("recordName: " + newValue);
                            try{
                                //System.out.println("Inside try after name is recorded");
                                Random rnd = new Random();
                                int id = rnd.nextInt(100000);
                                recordTableDB.add(id,newValue,score);
                                records.add(id + "," + newValue + "," + score);
                                //System.out.println("before sorting");
                                //System.out.println(records);
                                List<String> sorted = records.stream()
                                        .map(row->row.split(","))
                                        .sorted((entry1, entry2) -> Integer.valueOf(entry2[2]) - Integer.valueOf(entry1[2]))
                                        .limit(10)
                                        .map(s->{
                                            return s[0] + "," + s[1] + "," + s[2];
                                        })
                                        .collect(Collectors.toList());
                                //System.out.println("After sorting");
                                //System.out.println(sorted);
                                updateDB(recordTableDB, sorted); // sort & delete bottom entry, i tried writing SQL queries myself, but I clearly need more knowledge on this
                                //System.out.println("updated DB");
                                //System.out.println(recordTableDB.toList());
                                restartController.fillTable(sorted, newValue);
                                //System.out.println("Table refilled");
                            }catch(SQLException e){
                                System.out.println(e.getMessage());}
                        }
                    });
                });
            }else{
                //System.out.println("don't record");
                restartController.showRecordField(false);
            }
        }catch(SQLException e){ System.out.printf(e.getMessage());}

        // !!! update the window immediately after the name was entered and display the new entry with a different color

        Stage restartStage = new Stage();
        restartStage.setScene(restartScene);
        //System.out.println("Stage set and scene forwarded to it");
        restartStage.show();
    }

    private boolean compareScores(List<String> list, int score){
        if(list.isEmpty() || list.size() < 10) return true;

        for(String entry:list){
            String[] parts = entry.split(",");
            if(score > Integer.valueOf(parts[2])) return true;
        }

        return false;
    }

    private void updateDB(RecordTableDB recordTableDB, List<String> list) throws SQLException{
        List<Integer> IDs = list.stream().map(s->{
            String[] parts = s.split(",");
            return Integer.valueOf(parts[0]);
        })
        .collect(Collectors.toList());
        //System.out.println("IDs:");
        //System.out.println(IDs);

        List<String> readDB = recordTableDB.toList();
        //System.out.println("Our BD before updating: ");
        //System.out.println(readDB);
        readDB.stream().forEach(s->{
            String[] parts = s.split(","); // [0] is ID
            //System.out.println("parts: " + parts);
            try{
                if(!IDs.contains(Integer.valueOf(parts[0]))) {
                    recordTableDB.remove(Integer.valueOf(parts[0]));
                    //System.out.println("ID: " + Integer.valueOf(parts[0]) + " is removed from BD");
                }else{
                    //System.out.println("ID: " + Integer.valueOf(parts[0]) + " is in the record list");
                }
            }catch(SQLException e){
                System.out.println(e);
            }
        });
    }
}
