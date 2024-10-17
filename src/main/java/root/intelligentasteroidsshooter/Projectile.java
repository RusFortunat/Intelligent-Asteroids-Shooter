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

        if (super.getPolygon().getTranslateX() < 0) {
            super.getPolygon().setTranslateX(super.getPolygon().getTranslateX() + SinglePlayerView.WIDTH);
            super.setAlive(false);
        }

        if (super.getPolygon().getTranslateX() > SinglePlayerView.WIDTH) {
            super.getPolygon().setTranslateX(super.getPolygon().getTranslateX() - SinglePlayerView.WIDTH);
            super.setAlive(false);
        }

        if (super.getPolygon().getTranslateY()< 0) {
            super.getPolygon().setTranslateY(super.getPolygon().getTranslateY() + SinglePlayerView.HEIGHT);
            super.setAlive(false);
        }

        if (super.getPolygon().getTranslateY() > SinglePlayerView.HEIGHT) {
            super.getPolygon().setTranslateY(super.getPolygon().getTranslateY() - SinglePlayerView.HEIGHT);
            super.setAlive(false);
        }
    }

}