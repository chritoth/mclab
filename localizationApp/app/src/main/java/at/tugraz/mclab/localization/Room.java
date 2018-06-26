package at.tugraz.mclab.localization;

public class Room {

    public final Position lowerLeftCorner;
    public final Position upperRightCorner;
    public final double area;

    public Room(Position lowerLeftCorner, Position upperRightCorner) {
        this.lowerLeftCorner = new Position(lowerLeftCorner);
        this.upperRightCorner = new Position(upperRightCorner);

        // compute the area of the room -> we assume rectangular rooms
        area = (upperRightCorner.getX() - lowerLeftCorner.getX()) * (upperRightCorner.getY() - lowerLeftCorner.getY());
    }

    public double getXLength() {
        return upperRightCorner.getX() - lowerLeftCorner.getX();
    }

    public double getYLength() {
        return upperRightCorner.getY() - lowerLeftCorner.getY();
    }

    public Position getRoomCenter() {
        double x = (lowerLeftCorner.getX() + upperRightCorner.getX()) / 2.0;
        double y = (lowerLeftCorner.getY() + upperRightCorner.getY()) / 2.0;
        return new Position(x, y);
    }
}
