package root.intelligentasteroidsshooter;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SinglePlayerView {

    public static int WIDTH = 500;
    public static int HEIGHT = 400;

    public void start(Stage singlePlayer) throws IOException {
        Pane pane = new Pane();
        Text text = new Text(10, 20, "Points: 0");
        pane.getChildren().add(text);
        pane.setPrefSize(WIDTH, HEIGHT);

        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        pane.getChildren().add(ship.getCharacter());
        //System.out.println("Ship added");

        List<Projectile> projectiles = new ArrayList<>();

        List<Asteroid> asteroids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH / 3), rnd.nextInt(HEIGHT));
            asteroids.add(asteroid);
        }
        //System.out.println("Asteroids and ship are created");

        asteroids.forEach(asteroid -> pane.getChildren().add(asteroid.getCharacter()));
        //System.out.println("Ship and asteroids added to pane");

        AtomicInteger points = new AtomicInteger();

        Scene scene = new Scene(pane);

        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();

        scene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });

        //System.out.println("We got to the AnimationTimer");

        new AnimationTimer() {
            int score = 0;

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

                if (pressedKeys.getOrDefault(KeyCode.SPACE, false) && projectiles.size() < 10) {
                    // we shoot
                    Projectile projectile = new Projectile((int) ship.getCharacter().getTranslateX(), (int) ship.getCharacter().getTranslateY());
                    projectile.getCharacter().setRotate(ship.getCharacter().getRotate());
                    projectiles.add(projectile);

                    projectile.accelerate();
                    projectile.setMovement(projectile.getMovement().normalize().multiply(3));

                    pane.getChildren().add(projectile.getCharacter());
                }

                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                // removing projectiles and asteroids
                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if(projectile.collide(asteroid)) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                        }
                    });
                    if(!projectile.isAlive()) {
                        score += points.addAndGet(1000);
                        text.setText("Points: " + score);
                    }
                });
                projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .forEach(projectile -> pane.getChildren().remove(projectile.getCharacter()));
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .collect(Collectors.toList()));

                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid -> pane.getChildren().remove(asteroid.getCharacter()));
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList()));

                // add new asteroids
                if(Math.random() < 0.005) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
                    if(!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getCharacter());
                    }
                }

                // stop the game
                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid)) {
                        stop();
                        singlePlayer.close();
                        try{
                            System.out.println("Did we get here?");
                            showScoreAndAskToPlayAgain(score);
                            System.out.println("Restart window executed");
                        }catch(Exception e){e.getMessage();}
                    }
                });
            }
        }.start();

        projectiles.forEach(projectile -> projectile.move());

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
        System.out.println("inside showScoreAndAskToPlayAgain");
        FXMLLoader restartView = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("restart-view.fxml"));
        Scene restartScene = new Scene(restartView.load());
        System.out.println("Scene loaded");
        RestartViewController restartController = restartView.getController();
        restartController.setLabels(score);
        System.out.println("Labels set");

        Stage restartStage = new Stage();
        restartStage.setScene(restartScene);
        System.out.println("Stage set and scene forwarded to it");
        restartStage.show();
    }
}
