package root.intelligentasteroidsshooter;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

//public class Ship extends Character {
public class Ship {
    private ImageView shipImage;
    private Hitbox hitBox;
    private Point2D movement;
    private boolean alive;

    //public Ship(int x, int y) {
        //super(new Polygon(-5, -5, 10, 0, -5, 5),x,y);
        //this.character = new Polygon(-5, -5, 10, 0, -5, 5);
        //this.character.setTranslateX(x);
        //this.character.setTranslateY(y);
    //}

    public Ship(ImageView imageFile, int x, int y) {
        this.shipImage = imageFile;
        this.shipImage.setLayoutX(0);
        this.shipImage.setLayoutY(-50);
        //System.out.println("getLayoutBounds"+ shipImage.getLayoutBounds());

        this.alive = true;

        this.movement = new Point2D(0, 0);
    }

    public void turnLeft() {
        this.shipImage.setRotate(this.shipImage.getRotate() - 5);
    }

    public void turnRight() {
        this.shipImage.setRotate(this.shipImage.getRotate() + 5);
    }

    public void move() {
        this.shipImage.setLayoutX(this.shipImage.getLayoutX() + this.movement.getX());
        this.shipImage.setLayoutY(this.shipImage.getLayoutY() + this.movement.getY());
        //this.shipImage.setLayoutX(this.shipImage.getLayoutX() + 1);
        //this.shipImage.setLayoutY(this.shipImage.getLayoutY() + 1);

        if (this.shipImage.getLayoutX() < -SinglePlayerView.WIDTH/2.0) {
            this.shipImage.setLayoutX(this.shipImage.getLayoutX() + SinglePlayerView.WIDTH);
        }

        if (this.shipImage.getLayoutX() > SinglePlayerView.WIDTH/2.0) {
            this.shipImage.setLayoutX(this.shipImage.getLayoutX() - SinglePlayerView.WIDTH);
        }

        if (this.shipImage.getLayoutY() < -1.05*SinglePlayerView.HEIGHT/2) {
            this.shipImage.setLayoutY(this.shipImage.getLayoutY() + SinglePlayerView.HEIGHT);
        }

        if (this.shipImage.getLayoutY() > 0.95*SinglePlayerView.HEIGHT/2) {
            this.shipImage.setLayoutY(this.shipImage.getLayoutY() - SinglePlayerView.HEIGHT);
        }
    }

    public void accelerate() {
        double changeX = Math.cos(Math.toRadians(this.shipImage.getRotate()));
        double changeY = Math.sin(Math.toRadians(this.shipImage.getRotate()));

        changeX *= 0.05;
        changeY *= 0.05;
        //double newX = 0.1;//*this.movement.getX();
        //double newY = 0.1;//*this.movement.getY();

        this.movement = this.movement.add(changeX, changeY);
        //this.movement = this.movement.add(newX, newY);
    }

    public void decelerate() {
        double newX = -0.1*this.movement.getX();
        double newY = -0.1*this.movement.getY();
        this.movement = this.movement.add(newX,newY);
    }

    public boolean collide(Character other) {
        //Shape collisionArea = Shape.intersect(this.shipImage, other.getCharacter());
        //return collisionArea.getBoundsInLocal().getWidth() != -1;
        return false;
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


    public ImageView getShip() {
        return shipImage;
    }

}