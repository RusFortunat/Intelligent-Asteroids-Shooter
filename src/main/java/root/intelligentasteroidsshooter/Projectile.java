package root.intelligentasteroidsshooter;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Projectile extends Hitbox {

    public Projectile(int x, int y) {
        super(new Polygon(2, -2, 2, 2, -2, 2, -2, -2), Color.RED, x, y);
    }

    @Override
    public void move() {
        super.getPolygon().setTranslateX(super.getPolygon().getTranslateX() + super.getMovement().getX());
        super.getPolygon().setTranslateY(super.getPolygon().getTranslateY() + super.getMovement().getY());

        if (super.getPolygon().getTranslateX() < -0.1*SinglePlayerView.WIDTH) {
            super.setAlive(false);
        }

        if (super.getPolygon().getTranslateX() > 1.5*SinglePlayerView.WIDTH ) {
            super.setAlive(false);
        }

        if (super.getPolygon().getTranslateX() < - 0.1*SinglePlayerView.HEIGHT) {
            super.setAlive(false);
        }

        if (super.getPolygon().getTranslateX() > 1.2*SinglePlayerView.HEIGHT) {
            super.setAlive(false);
        }
    }
}