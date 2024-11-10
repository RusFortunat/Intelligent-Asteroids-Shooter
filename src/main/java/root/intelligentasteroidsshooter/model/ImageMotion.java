package root.intelligentasteroidsshooter.model;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import root.intelligentasteroidsshooter.singlePlayer.SinglePlayerView;

public abstract class ImageMotion {
    private ImageView image;
    private double imageWidth;
    private double imageHeight;
    private Hitbox hitbox;
    private Point2D movement;
    private boolean alive;

    public ImageMotion(ImageView imageFile, double scale, double x, double y) {
        this.image = imageFile;
        imageWidth = 0.5*scale*imageFile.getImage().getWidth();
        imageHeight = 0.5*scale*imageFile.getImage().getHeight();

        // Polygon and ImageView coordinates diverge, have to search for these numbers by hand to ensure overlap
        // as a gamer myself -- I *hate* when hitboxes are wrong!
        if(imageWidth < 25){
            this.image.setLayoutX(x + 2.0*imageWidth);
            this.image.setLayoutY(y - 0*imageHeight);
        }else if(imageWidth < 30){
            this.image.setLayoutX(x + 1.7*imageWidth);
            this.image.setLayoutY(y - 0.05*imageHeight);
        }else if(imageWidth < 35){
            this.image.setLayoutX(x + 1.47*imageWidth);
            this.image.setLayoutY(y - 0*imageHeight);
        }else if(imageWidth < 40){
            this.image.setLayoutX(x + 1.35*imageWidth);
            this.image.setLayoutY(y - 0*imageHeight);
        }else if(imageWidth < 50){
            this.image.setLayoutX(x + 1.25*imageWidth);
            this.image.setLayoutY(y - 0*imageHeight);
        }else{
            this.image.setLayoutX(x + 0.22*imageWidth);
            this.image.setLayoutY(y - 0.9*imageHeight);
        }

        // i make hitboxes to be a bit smaller due to irregular shape of asteroids
        Polygon squarePolygon = new Polygon(-0.75*imageWidth, -0.75*imageHeight,
                0.75*imageWidth, -0.75*imageHeight,
                0.75*imageWidth, 0.75*imageHeight,
                -0.75*imageWidth, 0.75*imageHeight);
        this.hitbox = new Hitbox(squarePolygon, Color.TRANSPARENT,
                (int)(x + SinglePlayerView.WIDTH/2.0),
                (int)(y + SinglePlayerView.HEIGHT/2.0 )); // square


        this.alive = true;

        this.movement = new Point2D(0, 0);
    }

    public void turnLeft() {
        this.hitbox.turnLeft();
        this.image.setRotate(this.hitbox.getPolygon().getRotate() - 3);
    }

    public void turnRight() {
        this.hitbox.turnRight();
        this.image.setRotate(this.hitbox.getPolygon().getRotate() + 3);
    }

    public void move() {
        this.hitbox.move();
        this.image.setLayoutX(this.image.getLayoutX() + this.movement.getX());
        this.image.setLayoutY(this.image.getLayoutY() + this.movement.getY());

        if (this.image.getLayoutX() < -SinglePlayerView.WIDTH/2.0) {
            this.image.setLayoutX(this.image.getLayoutX() + SinglePlayerView.WIDTH);
        }

        if (this.image.getLayoutX() > SinglePlayerView.WIDTH/2.0 ) {
            this.image.setLayoutX(this.image.getLayoutX() - SinglePlayerView.WIDTH);
        }

        if (this.image.getLayoutY() < - SinglePlayerView.HEIGHT/2.0 - imageHeight) {
            this.image.setLayoutY(this.image.getLayoutY() + SinglePlayerView.HEIGHT);
        }

        if (this.image.getLayoutY() > SinglePlayerView.HEIGHT/2) {
            this.image.setLayoutY(this.image.getLayoutY() - SinglePlayerView.HEIGHT);
        }
    }

    public void accelerate() {
        this.hitbox.accelerate();
        double changeX = Math.cos(Math.toRadians(getHitbox().getPolygon().getRotate()));
        double changeY = Math.sin(Math.toRadians(getHitbox().getPolygon().getRotate()));

        changeX *= 0.05;
        changeY *= 0.05;

        this.movement = this.movement.add(changeX, changeY);
    }

    public void decelerate() {
        this.hitbox.decelerate();
        double newX = -0.1*this.movement.getX();
        double newY = -0.1*this.movement.getY();
        this.movement = this.movement.add(newX,newY);
    }

    public boolean collide(Hitbox other) {
        return this.hitbox.collide(other);
    }

    public boolean isAlive(){
        return alive;
    }

    public void setAlive(boolean value){
        hitbox.setAlive(value);
        alive = value;
    }

    public ImageView getImage() {
        return image;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public double getImageWidth(){ return imageWidth;}
    public double getImageHeight(){ return imageHeight;}
}
