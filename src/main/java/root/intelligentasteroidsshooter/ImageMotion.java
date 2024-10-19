package root.intelligentasteroidsshooter;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public abstract class ImageMotion {
    private ImageView image;
    private double scale;
    private Hitbox hitbox;
    private Point2D movement;
    private boolean alive;

    public ImageMotion(ImageView imageFile, double scale, int x, int y) {
        this.image = imageFile;
        this.scale = scale;
        double imageWidth = 0.5*scale*imageFile.getImage().getWidth();
        double imageHeight = 0.5*scale*imageFile.getImage().getHeight();
        //System.out.println("imageWidth: " + imageWidth);
        //System.out.println("imageWidth: " + imageHeight);
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
        // i mage hitboxes a bit smaller due to irregular shape of asteroids
        Polygon squarePolygon = new Polygon(-0.75*imageWidth, -0.75*imageHeight,
                0.75*imageWidth, -0.75*imageHeight,
                0.75*imageWidth, 0.75*imageHeight,
                -0.75*imageWidth, 0.75*imageHeight);
        this.hitbox = new Hitbox(squarePolygon, Color.BLACK,
                (int)(x + SinglePlayerView.WIDTH/2.0),
                (int)(y + SinglePlayerView.HEIGHT/2.0 )); // square


        this.alive = true;

        this.movement = new Point2D(0, 0);
    }

    public void turnLeft() {
        this.hitbox.turnLeft();
        this.image.setRotate(this.image.getRotate() - 5);
    }

    public void turnRight() {
        this.hitbox.turnRight();
        this.image.setRotate(this.image.getRotate() + 5);
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

        if (this.image.getLayoutY() < - SinglePlayerView.HEIGHT/2.0 - scale*this.image.getImage().getHeight()) {
            this.image.setLayoutY(this.image.getLayoutY() + SinglePlayerView.HEIGHT);
        }

        if (this.image.getLayoutY() > SinglePlayerView.HEIGHT/2) {
            this.image.setLayoutY(this.image.getLayoutY() - SinglePlayerView.HEIGHT);
        }
    }

    public void accelerate() {
        this.hitbox.accelerate();
        double changeX = Math.cos(Math.toRadians(this.image.getRotate()));
        double changeY = Math.sin(Math.toRadians(this.image.getRotate()));

        changeX *= 0.05;
        changeY *= 0.05;

        this.movement = this.movement.add(changeX, changeY);
    }

    public void decelerate() {
        //System.out.println("Ship Image coords: X " + this.image.getLayoutX() + "; Y " + this.image.getLayoutX());
        //System.out.println("Ship Polygon coords: X " + hitbox.getPolygon().getTranslateX() + "; Y " + hitbox.getPolygon().getTranslateY());
        this.hitbox.decelerate();
        double newX = -0.1*this.movement.getX();
        double newY = -0.1*this.movement.getY();
        this.movement = this.movement.add(newX,newY);
    }

    public boolean collide(Hitbox other) {
        return this.hitbox.collide(other);
    }

    public Point2D getMovement(){
        return this.movement;
    }

    public void setMovement(Point2D point){
        this.hitbox.setMovement(point);
        this.movement = this.movement.add(point.getX(),point.getY());
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
}
