package root.intelligentasteroidsshooter;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SinglePlayerView {

    public static int WIDTH = 500;
    public static int HEIGHT = 400;

    public void start(Stage singlePlayer) throws IOException {
        Pane pane = new Pane();
        Image backgroundFile = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter\\src\\main\\resources\\root\\intelligentasteroidsshooter\\stars.jpg");
        BackgroundImage myBI= new BackgroundImage(backgroundFile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background paneBackgr = new Background(myBI);
        Text text = new Text(10, 20, "Points: 0");
        text.setFill(Color.WHITE);
        pane.getChildren().add(text);
        pane.setPrefSize(WIDTH, HEIGHT);
        //pane.setBackground(paneBackgr);

        Image imageForShip = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter\\src\\main\\resources\\root\\intelligentasteroidsshooter\\pepeShip_nobackgr.png");
        ImageView shipImage = new ImageView(imageForShip);
        double scale = 0.1;
        shipImage.setScaleX(scale);
        shipImage.setScaleY(scale);
        //Ship ship = new Ship(shipImage,WIDTH / 2, HEIGHT / 2);
        Ship ship = new Ship(shipImage, scale,0, 0);
        pane.getChildren().add(ship.getImage());
        //pane.getChildren().add(ship.getHitbox().getPolygon());
        //System.out.println("Ship added");

        List<Projectile> projectiles = new ArrayList<>();

        Image imageForAsteroid = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter\\src\\main\\resources\\root\\intelligentasteroidsshooter\\asteroid_nobackgr.png");
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
        System.out.println("Asteroids and ship are created");

        asteroids.forEach(asteroid -> {
            pane.getChildren().add(asteroid.getImage());
            pane.getChildren().add(asteroid.getHitbox().getPolygon());
        });
        System.out.println("Ship and asteroids added to pane");

        AtomicInteger points = new AtomicInteger();

        Scene scene = new Scene(pane);

        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();

        scene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });

        System.out.println("We got to the AnimationTimer");

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
                    double changeX = 25*Math.cos(Math.toRadians(ship.getImage().getRotate()));
                    double changeY = 25*Math.sin(Math.toRadians(ship.getImage().getRotate()));
                    int x = (int )(ship.getImage().getLayoutX() + WIDTH/2 + changeX);
                    int y = (int) (ship.getImage().getLayoutY() + 1.25*HEIGHT/2 + changeY); // this has to scale with images size and Pane sizes
                    Projectile projectile = new Projectile(x, y);
                    projectile.getPolygon().setRotate(ship.getImage().getRotate());
                    projectiles.add(projectile);

                    projectile.accelerate();
                    projectile.setMovement(projectile.getMovement().normalize().multiply(3));

                    pane.getChildren().add(projectile.getPolygon());
                }

                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                // removing projectiles and asteroids
                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if(projectile.collide(asteroid.getHitbox())) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                            score += points.addAndGet(1000);
                            text.setText("Points: " + score);
                        }
                    });
                });
                projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .forEach(projectile -> pane.getChildren().remove(projectile.getPolygon()));
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .collect(Collectors.toList()));

                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid ->{
                            pane.getChildren().remove(asteroid.getImage());
                            pane.getChildren().remove(asteroid.getHitbox().getPolygon());
                        } );
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList()));

                // add new asteroids
                if(Math.random() < 0.005) {
                    ImageView asteroidImage = new ImageView(imageForAsteroid);
                    Random rnd = new Random();
                    double rangeMin = 0.1;
                    double rangeMax = 0.2;
                    double size = rangeMin + (rangeMax - rangeMin) * rnd.nextDouble();
                    asteroidImage.setScaleX(size);
                    asteroidImage.setScaleY(size);
                    Asteroid asteroid = new Asteroid(asteroidImage, size,
                            rnd.nextInt(-3*WIDTH/4, -WIDTH/4),
                            rnd.nextInt(-3*HEIGHT/4, -HEIGHT/4));
                    if(!asteroid.collide(ship.getHitbox())) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getImage());
                        pane.getChildren().add(asteroid.getHitbox().getPolygon());
                    }
                }

                // stop the game
                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid.getHitbox())) {
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
