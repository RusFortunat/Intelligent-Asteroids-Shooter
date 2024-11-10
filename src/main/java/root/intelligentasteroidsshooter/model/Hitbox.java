package root.intelligentasteroidsshooter.model;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import root.intelligentasteroidsshooter.singlePlayer.SinglePlayerView;

public class Hitbox {
    private Polygon polygon;
    private Point2D movement;
    private boolean alive;

    public Hitbox(Polygon polygon, Color color, double x, double y) {
        this.polygon = polygon;
        this.polygon.setTranslateX(x);
        this.polygon.setTranslateY(y);
        this.polygon.setFill(color);
        this.alive = true;

        this.movement = new Point2D(0, 0);
    }

    public void turnLeft() {
        this.polygon.setRotate(this.polygon.getRotate() - 3);
    }

    public void turnRight() {
        this.polygon.setRotate(this.polygon.getRotate() + 3);
    }

    public void move() {
        this.polygon.setTranslateX(this.polygon.getTranslateX() + this.movement.getX());
        this.polygon.setTranslateY(this.polygon.getTranslateY() + this.movement.getY());

        if (this.polygon.getTranslateX() < 0) {
            this.polygon.setTranslateX(this.polygon.getTranslateX() + SinglePlayerView.WIDTH);
        }

        if (this.polygon.getTranslateX() > SinglePlayerView.WIDTH) {
            this.polygon.setTranslateX(this.polygon.getTranslateX() - SinglePlayerView.WIDTH);
        }

        if (this.polygon.getTranslateY() < 0) {
            this.polygon.setTranslateY(this.polygon.getTranslateY() + SinglePlayerView.HEIGHT);
        }

        if (this.polygon.getTranslateY() > SinglePlayerView.HEIGHT) {
            this.polygon.setTranslateY(this.polygon.getTranslateY() - SinglePlayerView.HEIGHT);
        }
    }

    public void accelerate() {
        double changeX = Math.cos(Math.toRadians(this.polygon.getRotate()));
        double changeY = Math.sin(Math.toRadians(this.polygon.getRotate()));

        changeX *= 0.05;
        changeY *= 0.05;

        this.movement = this.movement.add(changeX, changeY);
    }

    public void decelerate() {
        double newX = -0.1*this.movement.getX();
        double newY = -0.1*this.movement.getY();
        this.movement = this.movement.add(newX,newY);
    }

    public boolean collide(Hitbox other) {
        Shape collisionArea = Shape.intersect(this.polygon, other.getPolygon());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }

    public Point2D getMovement(){
        return this.movement;
    }

    public Polygon getPolygon() { return polygon; }

    public void setMovement(Point2D point){
        this.movement = this.movement.add(point.getX(),point.getY());
    }

    public boolean isAlive(){
        return alive;
    }

    public void setAlive(boolean value){
        alive = value;
    }
}