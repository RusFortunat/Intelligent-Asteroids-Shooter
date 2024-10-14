package root.intelligentasteroidsshooter;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

public class Ship extends Character {

    public Ship(int x, int y) {
        super(new Polygon(-5, -5, 10, 0, -5, 5),x,y);
        //this.character = new Polygon(-5, -5, 10, 0, -5, 5);
        //this.character.setTranslateX(x);
        //this.character.setTranslateY(y);
    }
}