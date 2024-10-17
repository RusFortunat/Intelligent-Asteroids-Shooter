package root.intelligentasteroidsshooter;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.Random;

public class Asteroid extends ImageMotion{
    private double rotationalMovement;

    public Asteroid(ImageView imageFile, double scale, int x, int y) {
        super(imageFile,scale,x,y);

        Random rnd = new Random();

        int rotation = rnd.nextInt(360);
        this.getHitbox().getPolygon().setRotate(rotation);
        this.getImage().setRotate(rotation);

        int accelerationAmount = 1 + rnd.nextInt(10);
        for (int i = 0; i < accelerationAmount; i++) {
            super.accelerate();
        }

        this.rotationalMovement = 0.5 - rnd.nextDouble();
    }

    @Override
    public void move() {
        super.move();
        super.getImage().setRotate(super.getImage().getRotate() + rotationalMovement);
        super.getHitbox().getPolygon().setRotate(super.getHitbox().getPolygon().getRotate() + rotationalMovement);
    }
}