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
        System.out.println("imageWidth " + imageWidth + "; imageHeight" + imageHeight);
        this.image.setLayoutX(x + 0.25*imageWidth); // Polygon and ImageView coordinates diverge, have to search for these numbers to ensure overlap
        this.image.setLayoutY(y - 1.75*imageHeight);
        Polygon squarePolygon = new Polygon(-imageWidth, -imageHeight,
                imageWidth, -imageHeight,
                imageWidth, imageHeight,
                -imageWidth, imageHeight);
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
            this.image.setLayoutX(this.image.getLayoutX() % SinglePlayerView.WIDTH);
        }

        if (this.image.getLayoutY() < - SinglePlayerView.HEIGHT/2.0 - scale*this.image.getImage().getHeight()) {
            this.image.setLayoutY(this.image.getLayoutY() + SinglePlayerView.HEIGHT);
        }

        if (this.image.getLayoutY() > SinglePlayerView.HEIGHT/2) {
            this.image.setLayoutY(this.image.getLayoutY() % SinglePlayerView.HEIGHT);
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
