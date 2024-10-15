package root.mac_version_of_api;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.animation.AnimationTimer;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;

public class IntelligentAsteroidsShooter extends Application {

    public static int WIDTH = 500;
    public static int HEIGHT = 400;

    @Override
    public void start(Stage window) throws IOException {
        //FXMLLoader fxmlLoader = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("hello-view.fxml"));
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
                        GridPane myPane = new GridPane();
                        myPane.setHgap(20);
                        myPane.setVgap(20);
                        myPane.setAlignment(Pos.CENTER);
                        myPane.setPadding(new Insets (20,20,20,20));
                        myPane.setPrefSize(WIDTH/3, HEIGHT/6);
                        Text finalScore = new Text("Your final score: " + score);
                        myPane.add(finalScore,1,0);
                        Text question = new Text("Repeat Game?");
                        HBox buttons = new HBox();
                        buttons.setSpacing(50);
                        Button repeatGame = new Button("Yes");
                        Button closeGame = new Button("No");
                        buttons.getChildren().addAll(repeatGame, closeGame);
                        myPane.add(question,1,1);
                        myPane.add(buttons,1,2);

                        Scene askForNewGame = new Scene(myPane,320,240);
                        Stage newStage = new Stage();

                        repeatGame.setOnAction((event)->{
                            newStage.close();
                            window.close();
                        });

                        closeGame.setOnAction((event)->{
                            newStage.close();
                            window.close();
                        });

                        newStage.setScene(askForNewGame);
                        newStage.show();
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



        window.setTitle("Our asteroids shooter!");
        window.setScene(scene);
        window.show();
    }

    public static void main(String[] args) {
        launch();
    }
}