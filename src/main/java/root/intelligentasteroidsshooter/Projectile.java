package root.intelligentasteroidsshooter;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Projectile extends Character {

    public Projectile(int x, int y) {
        super(new Polygon(2, -2, 2, 2, -2, 2, -2, -2), Color.RED, x, y);
    }

}