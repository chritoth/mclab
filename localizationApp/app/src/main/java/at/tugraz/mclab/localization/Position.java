package at.tugraz.mclab.localization;

public class Position {

    public static final int CLOCKWISE = -1;
    public static final int COLINEAR = 0;
    public static final int COUNTERCLOCKWISE = 1;
    private double x; // x position coordinate
    private double y; // y position coordinate

    public Position() {
        x = 0.0;
        y = 0.0;
    }

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position position) {
        this.x = position.x;
        this.y = position.y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean leftOf(Position position) {
        return x < position.x ? true : false;
    }

    public boolean rightOf(Position position) {
        return x > position.x ? true : false;
    }

    public boolean above(Position position) {
        return y > position.y ? true : false;
    }

    public boolean below(Position position) {
        return y < position.y ? true : false;
    }

    public static int orientation(Position a, Position b, Position c) {
        double area = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);

        if (area == 0.0)
            return COLINEAR;

        return area > 0.0 ? COUNTERCLOCKWISE : CLOCKWISE;
    }
}
