package root.intelligentasteroidsshooter;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public abstract class Character {

    private Polygon character;
    private Point2D movement;
    private boolean alive;

    public Character(Polygon polygon, int x, int y) {
        this.character = polygon;
        this.character.setTranslateX(x);
        this.character.setTranslateY(y);
        this.alive = true;

        this.movement = new Point2D(0, 0);
    }

    public Polygon getCharacter() {
        return character;
    }

    public void turnLeft() {
        this.character.setRotate(this.character.getRotate() - 5);
    }

    public void turnRight() {
        this.character.setRotate(this.character.getRotate() + 5);
    }

    public void move() {
        this.character.setTranslateX(this.character.getTranslateX() + this.movement.getX());
        this.character.setTranslateY(this.character.getTranslateY() + this.movement.getY());

        if (this.character.getTranslateX() < 0) {
            this.character.setTranslateX(this.character.getTranslateX() + SinglePlayerView.WIDTH);
        }

        if (this.character.getTranslateX() > SinglePlayerView.WIDTH) {
            this.character.setTranslateX(this.character.getTranslateX() % SinglePlayerView.WIDTH);
        }

        if (this.character.getTranslateY() < 0) {
            this.character.setTranslateY(this.character.getTranslateY() + SinglePlayerView.HEIGHT);
        }

        if (this.character.getTranslateY() > SinglePlayerView.HEIGHT) {
            this.character.setTranslateY(this.character.getTranslateY() % SinglePlayerView.HEIGHT);
        }
    }

    public void accelerate() {
        double changeX = Math.cos(Math.toRadians(this.character.getRotate()));
        double changeY = Math.sin(Math.toRadians(this.character.getRotate()));

        changeX *= 0.05;
        changeY *= 0.05;

        this.movement = this.movement.add(changeX, changeY);
    }

    public void decelerate() {
        double newX = -0.1*this.movement.getX();
        double newY = -0.1*this.movement.getY();
        this.movement = this.movement.add(newX,newY);
    }

    public boolean collide(Character other) {
        Shape collisionArea = Shape.intersect(this.character, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }

    public Point2D getMovement(){
        return this.movement;
    }

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