package root.intelligentasteroidsshooter.singlePlayer;

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
import root.intelligentasteroidsshooter.*;
import root.intelligentasteroidsshooter.trainAI.Timer;
import root.intelligentasteroidsshooter.model.Asteroid;
import root.intelligentasteroidsshooter.model.Projectile;
import root.intelligentasteroidsshooter.model.Ship;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
        if(chosenDifficulty.equals("no sweat")){
            respawnRate = 0.01;
            asteroidValue = 1000; // not sure if I should change it for different difficulty levels
            asteroidSpeed = 1;
        }
        else if(chosenDifficulty.equals("survival")){
            respawnRate = 0.05;
            asteroidValue = 1000;
            asteroidSpeed = 2;
        }
        else if(chosenDifficulty.equals("extreme")) {
            respawnRate = 0.1;
            asteroidValue = 1000;
            asteroidSpeed = 3;
        }

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
        // ship
        Image imageForShip = new Image(getClass().getResource(chosenImage).toString());
        ImageView shipImage = new ImageView(imageForShip);
        double scale = 0.12;
        shipImage.setScaleX(scale);
        shipImage.setScaleY(scale);
        Ship ship = new Ship(shipImage, scale,0, 0);
        pane.getChildren().add(ship.getImage());
        // asteroids
        Image imageForAsteroid =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/asteroid_nobackgr.png").toString());
        List<Asteroid> asteroids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ImageView asteroidImage = new ImageView(imageForAsteroid);
            double rangeMin = 0.1;
            double rangeMax = 0.2;
            double size = ThreadLocalRandom.current().nextDouble(rangeMin,rangeMax);;
            asteroidImage.setScaleX(size);
            asteroidImage.setScaleY(size);
            Asteroid asteroid = new Asteroid(asteroidImage,
                    size, ThreadLocalRandom.current().nextInt(-3*WIDTH/4, -WIDTH/4),
                    ThreadLocalRandom.current().nextInt(-3*HEIGHT/4, -HEIGHT/4));
            if(!asteroid.collide(ship.getHitbox())) {asteroids.add(asteroid);}else{i--;} // don't add asteroids on top of the ship
        }
        asteroids.forEach(asteroid -> {
            if(asteroidSpeed > 1) {
                for(int i =1; i < asteroidSpeed; i++) asteroid.accelerate();
            }
            pane.getChildren().add(asteroid.getImage());
        });
        // projectiles
        List<Projectile> projectiles = new ArrayList<>();

        Scene scene = new Scene(pane);

        // to enable App to trace user input
        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
        scene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });

        // to count points
        AtomicInteger points = new AtomicInteger();
        RecordHolders newPlayer = new RecordHolders("futureRecordHolder?", "0");
        // to restrict projectile firing frequency, for now it is 1 shot per 0.5sec
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

                    forProjectiles.setStart(Instant.now());
                    // to orient the projectile along the ship's direction
                    double changeX = 1*Math.cos(Math.toRadians(ship.getImage().getRotate()));
                    double changeY = 1*Math.sin(Math.toRadians(ship.getImage().getRotate()));
                    int x = (int)(ship.getImage().getLayoutX() + 0.8*WIDTH/2
                            + 0.4*changeX*scale*shipImage.getImage().getWidth());
                    int y = (int)(ship.getImage().getLayoutY() + HEIGHT/2
                            + 0.4*changeY*scale*shipImage.getImage().getHeight()); // this has to scale with images size and Pane sizes
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
                        .toList()); // remove from the projectiles list
                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid ->{
                            pane.getChildren().remove(asteroid.getImage());
                            pane.getChildren().remove(asteroid.getHitbox().getPolygon()); // remove from the pane
                        } );
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .toList()); // remove from the asteroids list

                // add new asteroids randomly at the edges of the screen, if they don't collide with the ship
                if(Math.random() < respawnRate) {
                    ImageView asteroidImage = new ImageView(imageForAsteroid);
                    double rangeMin = 0.1;
                    double rangeMax = 0.2;
                    double size = ThreadLocalRandom.current().nextDouble(rangeMin,rangeMax);;
                    asteroidImage.setScaleX(size);
                    asteroidImage.setScaleY(size);
                    Asteroid asteroid = new Asteroid(asteroidImage,
                            size, ThreadLocalRandom.current().nextInt(-3*WIDTH/4, -WIDTH/4),
                            ThreadLocalRandom.current().nextInt(-3*HEIGHT/4, -HEIGHT/4));
                    if(!asteroid.collide(ship.getHitbox())) {
                        if(asteroidSpeed > 1) {
                            for(int i =1; i < asteroidSpeed; i++) asteroid.accelerate();
                        }
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getImage());
                    }
                }

                // stop the game when ship and asteroids collide
                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid.getHitbox())) {
                        stop();
                        singlePlayer.close();
                        try{
                            showScoreAndAskToPlayAgain(newPlayer.getPoints());
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                });
            }
        }.start();

        singlePlayer.setScene(scene);
        singlePlayer.show();
    }

    public void showScoreAndAskToPlayAgain(int score) throws IOException{
        // call restart window
        FXMLLoader restartView = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("restart-view.fxml"));
        Scene restartScene = new Scene(restartView.load());
        restartScene.getStylesheets().add("root/intelligentasteroidsshooter/static/customStyles.css");
        RestartViewController restartController = restartView.getController();
        restartController.setBackground();
        restartController.setLabels(score);

        // get records database
        RecordTableDB recordTableDB =
                new RecordTableDB("jdbc:h2:./src/main/resources/root/intelligentasteroidsshooter/record-table-database");
        try{

            // go through all DB entries and create sorted lists of names and records
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
                    .toList();

            // show the current table state to the user
            restartController.fillTable(records, "");

            // if player's score is bigger than DB records entries, then it's a new record
            if(compareScores(records, score)){
                restartController.showRecordField(true); // let user enter they name
                // add listener
                restartController.getName().textProperty().addListener((observable, oldValue, newValue) -> {
                    restartController.getName().setOnKeyReleased(event -> {
                        // accept on Enter; don't accept existing names
                        if (event.getCode() == KeyCode.ENTER && !names.contains(newValue)){
                            restartController.getName().setEditable(false);
                            try{
                                // generate SQL ID and add a new record holder to the database
                                Random rnd = new Random();
                                int id = rnd.nextInt(100000);
                                recordTableDB.add(id,newValue,score);
                                // add new entry to the list and sort it again
                                records.add(id + "," + newValue + "," + score);
                                List<String> sorted = records.stream()
                                        .map(row->row.split(","))
                                        .sorted((entry1, entry2) -> Integer.valueOf(entry2[2]) - Integer.valueOf(entry1[2]))
                                        .limit(10)
                                        .map(s->{
                                            return s[0] + "," + s[1] + "," + s[2];
                                        })
                                        .collect(Collectors.toList());
                                // sort & delete bottom entry, i tried writing SQL queries myself, but I clearly need more knowledge on this
                                updateDB(recordTableDB, sorted);
                                // update the record table
                                restartController.fillTable(sorted, newValue);
                            }catch(SQLException e){
                                System.out.println(e.getMessage());
                            }
                        }
                    });
                });
            }else{
                restartController.showRecordField(false); // if score is below all the records
            }
        }catch(SQLException e){
            System.out.printf(e.getMessage());
        }

        Stage restartStage = new Stage();
        restartStage.setScene(restartScene);
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

    private void updateDB(RecordTableDB recordTableDB, List<String> newRecordHolders) throws SQLException{
        // get all IDs from the provided record holder list
        List<Integer> IDs = newRecordHolders.stream().map(s->{
            String[] parts = s.split(",");
            return Integer.valueOf(parts[0]);
        })
        .toList();

        // remove all entries from database that are not in the new record holder list
        List<String> readDB = recordTableDB.toList();
        readDB.forEach(s->{
            String[] parts = s.split(","); // [0] is ID
            try{
                if(!IDs.contains(Integer.valueOf(parts[0]))) {
                    recordTableDB.remove(Integer.valueOf(parts[0]));
                }
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
        });
    }
}
